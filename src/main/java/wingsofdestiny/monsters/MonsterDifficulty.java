package wingsofdestiny.monsters;

import legend.game.characters.Element;
import legend.game.modding.events.battle.MonsterStatsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import wingsofdestiny.config.Difficulty;
import wingsofdestiny.config.WodConfigs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Difficolta' dei mostri.
 *  - Standard: retail + 25% della differenza con la tabella Hard (per mostro e per statistica).
 *  - Hard: valori della tabella Hard, compresi elemento, immunita' elementale, resistenze e flag speciali.
 * I mostri senza riga nella tabella (segnaposto e boss con IA particolare) restano retail.
 * In New Game+ si usa sempre Hard, qualunque sia l'opzione.
 */
public class MonsterDifficulty {
  private static final Logger LOGGER = LogManager.getFormatterLogger(MonsterDifficulty.class);

  /** Quota della differenza Hard - retail applicata in Standard. */
  public static final float STANDARD_SHARE = 0.25f;

  private final Map<Integer, HardStats> hardStats = new HashMap<>();

  public MonsterDifficulty() {
    try(final InputStream in = MonsterDifficulty.class.getResourceAsStream("/wings_of_destiny/data/monster_stats_hard.csv")) {
      if(in == null) {
        throw new IOException("tabella mostri mancante");
      }

      final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line;
      while((line = reader.readLine()) != null) {
        if(line.isBlank() || line.startsWith("#")) {
          continue;
        }

        final String[] c = line.split(",");
        final int[] v = new int[12];
        for(int i = 0; i < 12; i++) {
          v[i] = Integer.parseInt(c[i].trim());
        }

        this.hardStats.put(v[0], new HardStats(v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11]));
      }
    } catch(final IOException | RuntimeException e) {
      LOGGER.error("Wings of Destiny: impossibile leggere la tabella dei mostri, restano retail", e);
    }

    LOGGER.info("Wings of Destiny: caricate %d statistiche Hard dei mostri", this.hardStats.size());
  }

  @EventListener
  public void monsterStats(final MonsterStatsEvent event) {
    final HardStats hard = this.hardStats.get(event.enemyId);

    if(hard == null) {
      return;
    }

    if(WodConfigs.difficulty() == Difficulty.HARD || wingsofdestiny.ngplus.NewGamePlus.active()) {
      event.maxHp = hard.hp;
      event.hp = hard.hp;
      event.attack = hard.at;
      event.magicAttack = hard.mat;
      event.defence = hard.df;
      event.magicDefence = hard.mdf;
      event.attackAvoid = hard.aav;
      event.magicAvoid = hard.mav;
      event.specialEffectFlag = hard.special;
      event.elementFlag = Element.fromFlag(hard.element).get();
      event.elementalImmunityFlag.clear();
      if(hard.elementNull > 0) {
        event.elementalImmunityFlag.add(Element.fromFlag(hard.elementNull).get());
      }
      event.statusResistFlag = hard.statusResist;
      return;
    }

    final int hp = blend(event.maxHp, hard.hp);
    event.maxHp = hp;
    event.hp = hp;
    event.attack = blend(event.attack, hard.at);
    event.magicAttack = blend(event.magicAttack, hard.mat);
    event.defence = blend(event.defence, hard.df);
    event.magicDefence = blend(event.magicDefence, hard.mdf);
    event.attackAvoid = blend(event.attackAvoid, hard.aav);
    event.magicAvoid = blend(event.magicAvoid, hard.mav);
  }

  /** retail + 25% della differenza, mai sotto 1 se il retail era positivo. */
  static int blend(final int retail, final int hard) {
    final int value = Math.round(retail + (hard - retail) * STANDARD_SHARE);
    return retail > 0 ? Math.max(1, value) : Math.max(0, value);
  }

  private record HardStats(int hp, int at, int mat, int df, int mdf, int aav, int mav, int special, int element, int elementNull, int statusResist) { }
}
