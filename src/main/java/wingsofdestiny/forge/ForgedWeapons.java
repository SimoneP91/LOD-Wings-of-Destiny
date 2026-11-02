package wingsofdestiny.forge;

import legend.core.GameEngine;
import legend.game.inventory.Equipment;
import legend.game.inventory.EquipmentRegistryEvent;
import legend.game.inventory.EquipmentTypes;
import legend.game.inventory.GatherEquipmentTypesEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.WingsOfDestiny;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Versioni +1..+5 delle armi elencate in data/forge_weapons.txt.
 * Ogni livello: +8% AT dell'arma base (minimo +2). Tutto il resto copiato dall'arma base.
 * Registrate con priorita' minima, cosi' le armi base (lod e nostre) esistono gia'.
 */
public class ForgedWeapons {
  private static final Logger LOGGER = LogManager.getFormatterLogger(ForgedWeapons.class);
  public static final int MAX_LEVEL = 5;
  private static final RegistryId DANCING_DAGGER = new RegistryId("lod", "dancing_dagger");
  private static final RegistryId SOUL_EATER = new RegistryId("lod", "soul_eater");

  private static final List<RegistryId> BASES = loadBases();

  /** id forgiato -> (base, livello) */
  private static final Map<RegistryId, Info> INFO = new HashMap<>();
  /** base -> id dei livelli 1..5 (indice 0 = livello 1) */
  private static final Map<RegistryId, RegistryId[]> LEVELS = new HashMap<>();

  public record Info(RegistryId base, int level) { }

  static {
    for(final RegistryId base : BASES) {
      final RegistryId[] ids = new RegistryId[MAX_LEVEL];
      for(int level = 1; level <= MAX_LEVEL; level++) {
        final RegistryId id = new RegistryId(WingsOfDestiny.MOD_ID, "forged_" + base.entryId() + "_" + level);
        ids[level - 1] = id;
        INFO.put(id, new Info(base, level));
      }
      LEVELS.put(base, ids);
    }
  }

  private static List<RegistryId> loadBases() {
    final List<RegistryId> list = new ArrayList<>();
    try(final InputStream in = ForgedWeapons.class.getResourceAsStream("/wings_of_destiny/data/forge_weapons.txt")) {
      if(in == null) {
        throw new IOException("forge_weapons.txt mancante");
      }
      try(final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        String line;
        while((line = reader.readLine()) != null) {
          line = line.strip();
          if(!line.isEmpty()) {
            list.add(new RegistryId(line));
          }
        }
      }
    } catch(final IOException e) {
      LOGGER.error("Wings of Destiny: lista armi forgiabili non leggibile, forgia disattivata", e);
    }
    return list;
  }

  /** Bonus AT per livello: 8% dell'AT base, almeno 2. */
  public static int atkPerLevel(final Equipment base) {
    return Math.max(2, Math.round(base.attack_10 * 0.08f));
  }

  @EventListener(priority = Priority.LOWEST)
  public void registerEquipment(final EquipmentRegistryEvent event) {
    int count = 0;
    for(final RegistryId baseId : BASES) {
      if(!GameEngine.REGISTRIES.equipment.hasEntry(baseId)) {
        LOGGER.warn("Wings of Destiny: arma base %s non trovata, niente forgia", baseId);
        continue;
      }

      final Equipment base = GameEngine.REGISTRIES.equipment.getEntry(baseId).get();
      final int bonus = atkPerLevel(base);
      // Lo SP x50% della Dancing Dagger viene applicato dopo: lo replichiamo sulle copie
      final int spMult = DANCING_DAGGER.equals(baseId) ? 50 : base.spMultiplier;
      // Stesso discorso per la Soul Eater: niente perdita di HP
      final int hpRegen = SOUL_EATER.equals(baseId) ? 0 : base.hpRegen;

      for(int level = 1; level <= MAX_LEVEL; level++) {
        event.register(LEVELS.get(baseId)[level - 1], copy(base, level, bonus, spMult, hpRegen));
        count++;
      }
    }
    LOGGER.info("Wings of Destiny: registrate %d armi forgiate", count);
  }

  private static Equipment copy(final Equipment b, final int level, final int bonus, final int spMult, final int hpRegen) {
    final int sell = Math.round(b.price * (1.0f + 0.5f * level));
    final Equipment e = new Equipment(
      sell, b.flags_00, b.slot, legend.lodmod.LodMod.NO_ELEMENT.get(), b.elementalResistance_06, b.elementalImmunity_07, b.statusResist_08,
      b.mpPerPhysicalHit, b.spPerPhysicalHit, b.mpPerMagicalHit, b.spPerMagicalHit,
      b.hpMultiplier, b.mpMultiplier, spMult,
      b.magicalResistance, b.physicalResistance, b.magicalImmunity, b.physicalImmunity,
      b.revive, hpRegen, b.mpRegen, b.spRegen, b.escapeBonus,
      b.icon_0e, b.speed_0f, b.attack_10 + bonus * level, b.magicAttack_11, b.defence_12, b.magicDefence_13,
      b.attackHit_14, b.magicHit_15, b.attackAvoid_16, b.magicAvoid_17, b.onHitStatusChance_18, b.onHitStatus_1b
    );
    e.attackElement_04.set(b.attackElement_04);
    return e;
  }

  @EventListener(priority = Priority.LOWEST)
  public void gatherEquipmentTypes(final GatherEquipmentTypesEvent event) {
    for(final Map.Entry<RegistryId, RegistryId[]> entry : LEVELS.entrySet()) {
      if(!GameEngine.REGISTRIES.equipment.hasEntry(entry.getKey())) {
        continue;
      }
      final Set<String> types = EquipmentTypes.getEquipmentTypesForEquipment(GameEngine.REGISTRIES.equipment.getEntry(entry.getKey()).get());
      for(final RegistryId id : entry.getValue()) {
        for(final String type : types) {
          event.add(id, type);
        }
      }
    }
  }

  /** Livello e base dell'arma (base = se stessa a livello 0); null se non forgiabile. */
  public static Info info(final Equipment equipment) {
    final RegistryId id = equipment.getRegistryId();
    if(LEVELS.containsKey(id)) {
      return new Info(id, 0);
    }
    return INFO.get(id);
  }

  public static Equipment level(final RegistryId base, final int level) {
    final RegistryId id = LEVELS.get(base)[level - 1];
    return GameEngine.REGISTRIES.equipment.hasEntry(id) ? GameEngine.REGISTRIES.equipment.getEntry(id).get() : null;
  }

  public static Equipment base(final RegistryId base) {
    return GameEngine.REGISTRIES.equipment.getEntry(base).get();
  }
}
