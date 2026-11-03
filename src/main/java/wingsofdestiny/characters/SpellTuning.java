package wingsofdestiny.characters;

import legend.core.GameEngine;
import legend.game.inventory.SpellStats0c;
import legend.game.modding.events.battle.BattleStartedEvent;
import legend.game.modding.events.gamestate.GameLoadedEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import legend.lodmod.LodSpells;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Modifiche alle magie Dragoon, in tutte le partite.
 * - Red-Eyed Dragon (Dart, livello Dragoon 5): STR 75% -> 100% su tutti (moltiplicatore del gioco 0x10 = x3 -> 0x8 = x4).
 * I registri vengono ricreati a ogni avvio campagna, quindi si riapplica a nuova partita / caricamento / battaglia.
 */
public class SpellTuning {
  private static final Logger LOGGER = LogManager.getFormatterLogger(SpellTuning.class);

  /** Codici del gioco per la forza delle magie: 0x10 = 75% (x3), 0x8 = 100% (x4). */
  private static final int STR_100 = 0x8;

  @EventListener
  public void newGame(final NewGameEvent event) {
    apply();
  }

  @EventListener
  public void gameLoaded(final GameLoadedEvent event) {
    apply();
  }

  @EventListener
  public void battleStarted(final BattleStartedEvent event) {
    apply();
  }

  private static void apply() {
    try {
      final Field field = SpellStats0c.class.getDeclaredField("damageMultiplier_03");
      field.setAccessible(true);
      final SpellStats0c spell = LodSpells.RED_EYED_DRAGON.get();
      if(field.getInt(spell) != STR_100) {
        field.setInt(spell, STR_100);
      }
    } catch(final ReflectiveOperationException | RuntimeException e) {
      LOGGER.warn("Wings of Destiny: impossibile modificare Red-Eyed Dragon: %s", e);
    }

    GameEngine.addLangOverrides(Map.of("lod.spells.red_eyed_dragon.description", "Fire STR 100%% All"));
  }
}
