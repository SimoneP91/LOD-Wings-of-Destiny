package wingsofdestiny.ngplus;

import legend.game.EngineStates;
import legend.game.modding.events.RenderEvent;
import legend.game.modding.events.battle.EnemyRewardsEvent;
import legend.game.modding.events.battle.MonsterStatsEvent;
import legend.game.submap.SMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import wingsofdestiny.arena.Arena;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static legend.game.Scus94491BpeSegment_800b.submapId_800bd808;

/**
 * New Game+: i mostri diventano forti come i mostri che il gioco mette al livello del tuo party.
 * - Ogni area ha un livello previsto (data/ngplus_areas.csv).
 * - Le curve di HP, attacco, EXP e oro per livello vengono dai mostri comuni del gioco (data/ngplus_curve.csv).
 * - Fattore = curva(livello del party) / curva(livello dell'area), mai sotto 1, +25% per ogni ciclo dopo il primo.
 *   HP, attacco e attacco magico, EXP e oro; la difesa no (nel gioco cresce poco).
 * - Oltre il livello 46 (la Moon, ultimo dato del gioco) le curve continuano a crescere piu' piano.
 * Gira per ultimo (priorita' LOWEST), dopo la tabella Hard; elite, zone maledette e Arena si combinano.
 */
public class NgScaling {
  private static final Logger LOGGER = LogManager.getFormatterLogger(NgScaling.class);

  /** Livello medio di ogni disco: per le aree senza dati e per l'Arena. */
  private static final int[] DISC_LEVEL = {10, 22, 30, 40};
  /** Bonus per ogni ciclo dopo il primo: NG++ +25%, NG+++ +50%... */
  private static final float CYCLE_BONUS = 0.25f;

  static final Map<Integer, Integer> AREA_LEVEL = new HashMap<>();
  static final Map<Integer, List<String>> AREA_ITEMS = new HashMap<>();
  private static final Map<String, Curve> CURVES = new HashMap<>();
  /** Crescita per livello oltre l'ultimo dato. */
  private static final Map<String, Float> EXTRA = Map.of("hp", 0.08f, "at", 0.04f, "xp", 0.06f, "gold", 0.03f);

  /** Ultima area visitata: in battaglia il gioco azzera submapId. */
  private static int lastArea = -1;

  static {
    load();
  }

  private record Curve(int[] levels, float[] logValues, float extra) {
    float at(final int level) {
      final int n = this.levels.length;
      if(level <= this.levels[0]) {
        return (float)Math.exp(this.logValues[0]);
      }
      if(level >= this.levels[n - 1]) {
        return (float)Math.exp(this.logValues[n - 1] + Math.log(1.0f + this.extra) * (level - this.levels[n - 1]));
      }
      for(int i = 0; i < n - 1; i++) {
        if(level <= this.levels[i + 1]) {
          final float t = (float)(level - this.levels[i]) / (this.levels[i + 1] - this.levels[i]);
          return (float)Math.exp(this.logValues[i] + t * (this.logValues[i + 1] - this.logValues[i]));
        }
      }
      return (float)Math.exp(this.logValues[n - 1]);
    }
  }

  private static void load() {
    try {
      for(final String line : lines("/wings_of_destiny/data/ngplus_areas.csv")) {
        final String[] p = line.split(",");
        final int area = Integer.parseInt(p[0].trim());
        AREA_LEVEL.put(area, Integer.parseInt(p[1].trim()));
        final List<String> items = new ArrayList<>();
        if(!"none".equals(p[2].trim())) {
          for(final String id : p[2].split(";")) {
            items.add(id.trim());
          }
        }
        AREA_ITEMS.put(area, items);
      }
      for(final String line : lines("/wings_of_destiny/data/ngplus_curve.csv")) {
        final String[] p = line.split("\\|");
        final String[] lv = p[1].split(";");
        final String[] vs = p[2].split(";");
        final int[] levels = new int[lv.length];
        final float[] values = new float[vs.length];
        for(int i = 0; i < lv.length; i++) {
          levels[i] = Integer.parseInt(lv[i].trim());
          values[i] = Float.parseFloat(vs[i].trim());
        }
        CURVES.put(p[0].trim(), new Curve(levels, values, EXTRA.getOrDefault(p[0].trim(), 0.05f)));
      }
    } catch(final IOException | RuntimeException e) {
      LOGGER.error("Wings of Destiny: dati del New Game+ non leggibili, mostri non scalati", e);
      CURVES.clear();
    }
  }

  private static List<String> lines(final String path) throws IOException {
    final List<String> out = new ArrayList<>();
    try(final InputStream in = NgScaling.class.getResourceAsStream(path)) {
      if(in == null) {
        throw new IOException(path + " mancante");
      }
      try(final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        String line;
        while((line = reader.readLine()) != null) {
          if(!line.isBlank() && !line.startsWith("#")) {
            out.add(line);
          }
        }
      }
    }
    return out;
  }

  private static int discLevel(final int disc) {
    return DISC_LEVEL[Math.clamp(disc - 1, 0, DISC_LEVEL.length - 1)];
  }

  /** Livello previsto per la battaglia in corso: area dove ti trovi, disco del boss in Arena. */
  static int battleAreaLevel() {
    if(Arena.inArenaBattle()) {
      return discLevel(Arena.battleDisc());
    }
    final Integer level = AREA_LEVEL.get(lastArea);
    return level != null ? level : discLevel(Arena.disc());
  }

  private static float cycleBonus() {
    return 1.0f + CYCLE_BONUS * Math.max(0, NewGamePlus.cycle() - 1);
  }

  private static float factor(final String stat, final int partyLevel, final int areaLevel) {
    final Curve curve = CURVES.get(stat);
    if(curve == null) {
      return 1.0f;
    }
    return Math.max(1.0f, curve.at(partyLevel) / curve.at(areaLevel)) * cycleBonus();
  }

  /** Fattore di una statistica ("hp", "at", "xp", "gold") per la battaglia in corso (1 fuori dal New Game+). */
  public static float factor(final String stat) {
    if(!NewGamePlus.active()) {
      return 1.0f;
    }
    return factor(stat, Arena.partyLevel(), battleAreaLevel());
  }

  /** Fattore dell'oro per un boss d'Arena di un certo disco, con il party attuale (1 fuori dal New Game+). */
  public static float factorFor(final int disc) {
    if(!NewGamePlus.active()) {
      return 1.0f;
    }
    return factor("gold", Arena.partyLevel(), discLevel(disc));
  }

  /** L'area con dati piu' alta che non supera il livello dato (per i drop del New Game+). */
  static int areaForLevel(final int level) {
    int best = -1;
    int bestLevel = Integer.MIN_VALUE;
    int lowest = -1;
    int lowestLevel = Integer.MAX_VALUE;
    for(final Map.Entry<Integer, Integer> e : AREA_LEVEL.entrySet()) {
      if(AREA_ITEMS.getOrDefault(e.getKey(), List.of()).isEmpty()) {
        continue;
      }
      if(e.getValue() <= level && e.getValue() > bestLevel) {
        best = e.getKey();
        bestLevel = e.getValue();
      }
      if(e.getValue() < lowestLevel) {
        lowest = e.getKey();
        lowestLevel = e.getValue();
      }
    }
    return best != -1 ? best : lowest;
  }

  @EventListener
  public void render(final RenderEvent event) {
    if(EngineStates.currentEngineState_8004dd04 instanceof SMap && submapId_800bd808 >= 0) {
      lastArea = submapId_800bd808;
    }
  }

  @EventListener(priority = Priority.LOWEST)
  public void monsterStats(final MonsterStatsEvent event) {
    if(!NewGamePlus.active()) {
      return;
    }
    final float hp = factor("hp");
    final float at = factor("at");
    event.hp = Math.round(event.hp * hp);
    event.maxHp = Math.round(event.maxHp * hp);
    event.attack = Math.round(event.attack * at);
    event.magicAttack = Math.round(event.magicAttack * at);
  }

  @EventListener(priority = Priority.LOWEST)
  public void enemyRewards(final EnemyRewardsEvent event) {
    if(!NewGamePlus.active()) {
      return;
    }
    event.xp = Math.round(event.xp * factor("xp"));
    event.gold = Math.round(event.gold * factor("gold"));
  }
}
