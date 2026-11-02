package wingsofdestiny.items;

import legend.core.GameEngine;
import legend.game.inventory.Equipment;
import legend.game.modding.events.battle.BattleStartedEvent;
import legend.game.modding.events.gamestate.GameLoadedEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import legend.lodmod.LodEquipment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.RegistryDelegate;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Modifiche all'equipaggiamento retail:
 * Wargod's Sash SP x50% -> x75%, Spirit Ring SP/turno 20 -> 30, Dancing Dagger SP x0% -> x50%,
 * Soul Eater senza perdita di HP a turno.
 * I registri vengono ricreati a ogni avvio campagna, quindi si riapplica a nuova partita / caricamento / battaglia.
 */
public class EquipmentBuffs {
  private static final Logger LOGGER = LogManager.getFormatterLogger(EquipmentBuffs.class);

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
    set(LodEquipment.WARGODS_SASH, "spMultiplier", 75);
    set(LodEquipment.SPIRIT_RING, "spRegen", 30);
    set(LodEquipment.DANCING_DAGGER, "spMultiplier", 50);
    set(LodEquipment.SOUL_EATER, "hpRegen", 0);

    // Aggiorna anche le descrizioni che citano il valore
    GameEngine.addLangOverrides(Map.of(
      "lod.equipment.wargods_sash.description", "Raises SP 75%%.",
      "lod.equipment.dancing_dagger.description", "Raises SP 50%%.",
      "lod.equipment.soul_eater.description", "A dark blade\nthat thirsts\nfor battle."
    ));
  }

  private static void set(final RegistryDelegate<Equipment> equipment, final String fieldName, final int value) {
    try {
      final Field field = Equipment.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      final Equipment e = equipment.get();
      if(field.getInt(e) != value) {
        field.setInt(e, value);
      }
    } catch(final ReflectiveOperationException | RuntimeException ex) {
      LOGGER.warn("Wings of Destiny: impossibile modificare %s.%s: %s", equipment.getId(), fieldName, ex);
    }
  }
}
