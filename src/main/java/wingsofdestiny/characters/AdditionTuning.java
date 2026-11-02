package wingsofdestiny.characters;

import legend.core.GameEngine;
import legend.game.additions.Addition;
import legend.game.additions.AdditionHitProperties10;
import legend.game.additions.SimpleAddition;
import legend.game.modding.events.battle.BattleStartedEvent;
import legend.game.modding.events.gamestate.GameLoadedEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.RegistryId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Additions normali con i valori di data/additions_hard.csv (Perky Step escluso, resta retail).
 * Il gioco ricrea le additions a ogni avvio di partita, quindi i valori si riapplicano
 * a nuova partita, caricamento e inizio battaglia. I valori sono assoluti: riapplicarli non accumula.
 */
public class AdditionTuning {
  private static final Logger LOGGER = LogManager.getFormatterLogger(AdditionTuning.class);

  private final Map<String, Tuning> tunings = new LinkedHashMap<>();
  private Field levelMultipliersField;
  private Field hitsField;

  public AdditionTuning() {
    try(final InputStream in = AdditionTuning.class.getResourceAsStream("/wings_of_destiny/data/additions_hard.csv")) {
      if(in == null) {
        throw new IOException("tabella additions mancante");
      }

      final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line;
      while((line = reader.readLine()) != null) {
        if(line.isBlank() || line.startsWith("#")) {
          continue;
        }

        final String[] parts = line.split("\\|");
        this.tunings.put(parts[0].trim(), new Tuning(pairs(parts[1]), pairs(parts[2])));
      }

      this.levelMultipliersField = SimpleAddition.class.getDeclaredField("levelMultipliers");
      this.levelMultipliersField.setAccessible(true);
      this.hitsField = SimpleAddition.class.getDeclaredField("hits");
      this.hitsField.setAccessible(true);
    } catch(final IOException | ReflectiveOperationException | RuntimeException e) {
      LOGGER.error("Wings of Destiny: additions non modificabili, restano retail", e);
      this.tunings.clear();
    }
  }

  @EventListener
  public void newGame(final NewGameEvent event) {
    this.apply();
  }

  @EventListener
  public void gameLoaded(final GameLoadedEvent event) {
    this.apply();
  }

  @EventListener
  public void battleStarted(final BattleStartedEvent event) {
    this.apply();
  }

  private void apply() {
    int applied = 0;

    for(final Map.Entry<String, Tuning> entry : this.tunings.entrySet()) {
      try {
        final RegistryId id = new RegistryId("lod", entry.getKey());
        if(!GameEngine.REGISTRIES.additions.hasEntry(id)) {
          continue;
        }

        final Addition addition = GameEngine.REGISTRIES.additions.getEntry(id).get();
        if(!(addition instanceof final SimpleAddition simple)) {
          continue;
        }

        final SimpleAddition.LevelMultipliers[] levels = (SimpleAddition.LevelMultipliers[])this.levelMultipliersField.get(simple);
        final AdditionHitProperties10[] hits = (AdditionHitProperties10[])this.hitsField.get(simple);
        final Tuning tuning = entry.getValue();

        if(levels.length != tuning.levels.length || hits.length != tuning.hits.length) {
          LOGGER.warn("Wings of Destiny: addition %s ha una struttura diversa, lasciata retail", entry.getKey());
          continue;
        }

        for(int i = 0; i < levels.length; i++) {
          levels[i] = new SimpleAddition.LevelMultipliers(tuning.levels[i][0], tuning.levels[i][1]);
        }

        for(int i = 0; i < hits.length; i++) {
          hits[i].damageMultiplier_04 = Math.round(tuning.hits[i][0]);
          hits[i].sp_05 = Math.round(tuning.hits[i][1]);
        }

        applied++;
      } catch(final ReflectiveOperationException | RuntimeException e) {
        LOGGER.warn("Wings of Destiny: errore sull'addition %s, lasciata retail", entry.getKey(), e);
      }
    }

    LOGGER.debug("Wings of Destiny: additions aggiornate: %d", applied);
  }

  private static float[][] pairs(final String text) {
    final String[] items = text.split(";");
    final float[][] out = new float[items.length][2];
    for(int i = 0; i < items.length; i++) {
      final String[] p = items[i].split(":");
      out[i][0] = Float.parseFloat(p[0]);
      out[i][1] = Float.parseFloat(p[1]);
    }
    return out;
  }

  private record Tuning(float[][] levels, float[][] hits) { }
}
