package wingsofdestiny.items;

import legend.core.GameEngine;
import legend.game.characters.Element;
import legend.game.inventory.Equipment;
import legend.game.inventory.EquipmentRegistryEvent;
import legend.game.inventory.GatherEquipmentTypesEvent;
import legend.game.inventory.ItemIcon;
import legend.lodmod.LodMod;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.Registrar;
import org.legendofdragoon.modloader.registries.RegistryDelegate;
import wingsofdestiny.WingsOfDestiny;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static legend.game.inventory.EquipmentTypes.AXE;
import static legend.game.inventory.EquipmentTypes.BOW;
import static legend.game.inventory.EquipmentTypes.FEMALE;
import static legend.game.inventory.EquipmentTypes.HAMMER;
import static legend.game.inventory.EquipmentTypes.POLEARM;
import static legend.game.inventory.EquipmentTypes.HAND;
import static legend.game.inventory.EquipmentTypes.HASCHEL;
import static legend.game.inventory.EquipmentTypes.LONGSWORD;
import static legend.game.inventory.EquipmentTypes.MALE;
import static legend.game.inventory.EquipmentTypes.MEDIUM;
import static legend.game.inventory.EquipmentTypes.MERU;
import static legend.game.inventory.EquipmentTypes.NEUTRAL;
import static legend.game.inventory.EquipmentTypes.SHORTSWORD;
import static legend.game.types.EquipmentSlot.ACCESSORY;
import static legend.game.types.EquipmentSlot.ARMOUR;
import static legend.game.types.EquipmentSlot.BOOTS;
import static legend.game.types.EquipmentSlot.HELMET;
import static legend.game.types.EquipmentSlot.WEAPON;

/**
 * Equipaggiamento nuovo della Wings of Destiny (armi, armature e accessori nuovi).
 * Il numero passato a of(...) e' il prezzo di VENDITA: in negozio costa il doppio.
 */
public final class WodEquipment {
  private static final Registrar<Equipment, EquipmentRegistryEvent> REGISTRAR = new Registrar<>(GameEngine.REGISTRIES.equipment, WingsOfDestiny.MOD_ID);
  private static final Map<RegistryDelegate<Equipment>, String[]> TYPES = new LinkedHashMap<>();

  private static RegistryDelegate<Equipment> reg(final String id, final Supplier<EquipmentBuilder> builder, final String... types) {
    final RegistryDelegate<Equipment> delegate = REGISTRAR.register(id, () -> builder.get().build());
    TYPES.put(delegate, types);
    return delegate;
  }

  // --- Oggetti nuovi ---
  public static final RegistryDelegate<Equipment> SAGES_SHIELD = reg("sages_shield", () -> EquipmentBuilder.of(ACCESSORY, ItemIcon.RING, 100).mdef(8), NEUTRAL);
  public static final RegistryDelegate<Equipment> THIEFS_SHOES = reg("thiefs_shoes", () -> EquipmentBuilder.of(BOOTS, ItemIcon.SHOES, 100).escape(50), NEUTRAL);
  public static final RegistryDelegate<Equipment> LAVA_BLADE = reg("lava_blade", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 1500).element(LodMod.FIRE_ELEMENT).atk(25), LONGSWORD);
  public static final RegistryDelegate<Equipment> VOID_ESTOC = reg("void_estoc", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 1500).element(LodMod.DARK_ELEMENT).atk(33), SHORTSWORD);
  public static final RegistryDelegate<Equipment> SPECTRAL_ARROW = reg("spectral_arrow", () -> EquipmentBuilder.of(WEAPON, ItemIcon.BOW, 1500).element(LodMod.LIGHT_ELEMENT).atk(22).aHit(20), BOW);
  public static final RegistryDelegate<Equipment> HYDRO_HAMMER = reg("hydro_hammer", () -> EquipmentBuilder.of(WEAPON, ItemIcon.HAMMER, 1500).element(LodMod.WATER_ELEMENT).atk(28), HAMMER);
  public static final RegistryDelegate<Equipment> STONE_AXE = reg("stone_axe", () -> EquipmentBuilder.of(WEAPON, ItemIcon.AXE, 1500).element(LodMod.EARTH_ELEMENT).atk(31), AXE);
  public static final RegistryDelegate<Equipment> ATTACK_HORN = reg("attack_horn", () -> EquipmentBuilder.of(ACCESSORY, ItemIcon.HORN, 4000).atk(30).matk(30), NEUTRAL);
  public static final RegistryDelegate<Equipment> DEFENSE_HORN = reg("defense_horn", () -> EquipmentBuilder.of(ACCESSORY, ItemIcon.HORN, 4000).def(30).mdef(30), NEUTRAL);
  public static final RegistryDelegate<Equipment> HEAVY_ROBE = reg("heavy_robe", () -> EquipmentBuilder.of(ARMOUR, ItemIcon.ROBE, 75).def(20).mdef(20).aAv(5).mAv(5), MEDIUM, FEMALE);
  public static final RegistryDelegate<Equipment> VELVET_DRESS = reg("velvet_dress", () -> EquipmentBuilder.of(ARMOUR, ItemIcon.RED_DRESS, 125).mpPerHit(2, 2).def(27).mdef(16), MEDIUM, FEMALE);
  public static final RegistryDelegate<Equipment> TEMPLAR_SHIELD = reg("templar_shield", () -> EquipmentBuilder.of(ACCESSORY, ItemIcon.RING, 150).def(16), NEUTRAL);
  public static final RegistryDelegate<Equipment> WIZARD_SHIELD = reg("wizard_shield", () -> EquipmentBuilder.of(ACCESSORY, ItemIcon.RING, 150).mdef(14), NEUTRAL);
  public static final RegistryDelegate<Equipment> MAGICAL_SOLLERETS = reg("magical_sollerets", () -> EquipmentBuilder.of(BOOTS, ItemIcon.BOOTS, 300).def(18).mdef(9), NEUTRAL);
  public static final RegistryDelegate<Equipment> MAGICAL_CHAUSSES = reg("magical_chausses", () -> EquipmentBuilder.of(BOOTS, ItemIcon.BOOTS, 300).aAv(15).mAv(15), NEUTRAL);
  public static final RegistryDelegate<Equipment> MASTERS_HEADBAND = reg("masters_headband", () -> EquipmentBuilder.of(HELMET, ItemIcon.HAIRBAND, 100).spPerHit(10, 10).matk(25).def(11).mdef(11), HASCHEL);
  public static final RegistryDelegate<Equipment> POLTER_HELM = reg("polter_helm", () -> EquipmentBuilder.of(HELMET, ItemIcon.HELM, 4000).hpMult(15).mpMult(10).matk(35).def(10).mdef(10).aAv(5).mAv(5), NEUTRAL);
  public static final RegistryDelegate<Equipment> POLTER_ARMOR = reg("polter_armor", () -> EquipmentBuilder.of(ARMOUR, ItemIcon.ARMOR, 4000).mpPerHit(5, 5).spPerHit(5, 5).hpMult(15).def(60).mdef(40), NEUTRAL);
  public static final RegistryDelegate<Equipment> TITAN_RING = reg("titan_ring", () -> EquipmentBuilder.of(ACCESSORY, ItemIcon.RING, 300).atk(30), NEUTRAL);
  public static final RegistryDelegate<Equipment> WINGLY_SUIT = reg("wingly_suit", () -> EquipmentBuilder.of(ARMOUR, ItemIcon.ARMOR, 500).mpPerHit(2, 2).spPerHit(10, 10).mpMult(10).mpRegen(1).def(42).mdef(42), MERU);

  // --- Busters (opzione C): Lohan = base, Ulara = "+" ---
  public static final RegistryDelegate<Equipment> FIRE_BUSTER = buster("fire_buster", LodMod.FIRE_ELEMENT, false);
  public static final RegistryDelegate<Equipment> AQUA_BUSTER = buster("aqua_buster", LodMod.WATER_ELEMENT, false);
  public static final RegistryDelegate<Equipment> WIND_BUSTER = buster("wind_buster", LodMod.WIND_ELEMENT, false);
  public static final RegistryDelegate<Equipment> EARTH_BUSTER = buster("earth_buster", LodMod.EARTH_ELEMENT, false);
  public static final RegistryDelegate<Equipment> BOLT_BUSTER = buster("bolt_buster", LodMod.THUNDER_ELEMENT, false);
  public static final RegistryDelegate<Equipment> LIGHT_BUSTER = buster("light_buster", LodMod.LIGHT_ELEMENT, false);
  public static final RegistryDelegate<Equipment> DARK_BUSTER = buster("dark_buster", LodMod.DARK_ELEMENT, false);
  public static final RegistryDelegate<Equipment> SABRE = reg("sabre", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 400).atk(35), SHORTSWORD);

  public static final RegistryDelegate<Equipment> FIRE_BUSTER_PLUS = buster("fire_buster_plus", LodMod.FIRE_ELEMENT, true);
  public static final RegistryDelegate<Equipment> AQUA_BUSTER_PLUS = buster("aqua_buster_plus", LodMod.WATER_ELEMENT, true);
  public static final RegistryDelegate<Equipment> WIND_BUSTER_PLUS = buster("wind_buster_plus", LodMod.WIND_ELEMENT, true);
  public static final RegistryDelegate<Equipment> EARTH_BUSTER_PLUS = buster("earth_buster_plus", LodMod.EARTH_ELEMENT, true);
  public static final RegistryDelegate<Equipment> BOLT_BUSTER_PLUS = buster("bolt_buster_plus", LodMod.THUNDER_ELEMENT, true);
  public static final RegistryDelegate<Equipment> LIGHT_BUSTER_PLUS = buster("light_buster_plus", LodMod.LIGHT_ELEMENT, true);
  public static final RegistryDelegate<Equipment> DARK_BUSTER_PLUS = buster("dark_buster_plus", LodMod.DARK_ELEMENT, true);
  public static final RegistryDelegate<Equipment> GREAT_SABRE = reg("great_sabre", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 1250).atk(70), SHORTSWORD);

  // --- Swift Boots ---
  public static final RegistryDelegate<Equipment> SWIFT_BOOTS = reg("swift_boots", () -> EquipmentBuilder.of(BOOTS, ItemIcon.KNEEPIECE, 750).spd(5).def(10).mdef(10).aAv(5), MALE);

  // =====================================================================
  // Armi SP - prezzo = VENDITA, in negozio costa il doppio
  // =====================================================================

  // Wyrmling, Bale 300G - AT 15 (Knuckle 23, Mace 14, Axe 48), SP +20%, mira +10
  public static final RegistryDelegate<Equipment> WYRMLING_BLADE = reg("wyrmling_blade", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 150).atk(15).spMult(20).aHit(10).mHit(10), LONGSWORD);
  public static final RegistryDelegate<Equipment> WYRMLING_PIKE = reg("wyrmling_pike", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SPEAR, 150).atk(15).spMult(20).aHit(10).mHit(10), POLEARM);
  public static final RegistryDelegate<Equipment> WYRMLING_BOW = reg("wyrmling_bow", () -> EquipmentBuilder.of(WEAPON, ItemIcon.BOW, 150).atk(15).spMult(20).aHit(10).mHit(10), BOW);
  public static final RegistryDelegate<Equipment> WYRMLING_ESTOC = reg("wyrmling_estoc", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 150).atk(15).spMult(20).aHit(10).mHit(10), SHORTSWORD);
  public static final RegistryDelegate<Equipment> WYRMLING_KNUCKLE = reg("wyrmling_knuckle", () -> EquipmentBuilder.of(WEAPON, ItemIcon.KNUCKLE, 150).atk(23).spMult(20).aHit(10).mHit(10), HAND);
  public static final RegistryDelegate<Equipment> WYRMLING_MACE = reg("wyrmling_mace", () -> EquipmentBuilder.of(WEAPON, ItemIcon.MACE, 150).atk(14).spMult(20).aHit(10).mHit(10), HAMMER);
  public static final RegistryDelegate<Equipment> WYRMLING_AXE = reg("wyrmling_axe", () -> EquipmentBuilder.of(WEAPON, ItemIcon.AXE, 150).atk(48).spMult(20).aHit(10).mHit(10), AXE);

  // Greater Wyrmling, Fletz 1.200G - SP +30%, mira +10
  public static final RegistryDelegate<Equipment> GREATER_WYRMLING_BLADE = reg("greater_wyrmling_blade", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 600).atk(30).spMult(30).aHit(10).mHit(10), LONGSWORD);
  public static final RegistryDelegate<Equipment> GREATER_WYRMLING_PIKE = reg("greater_wyrmling_pike", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SPEAR, 600).atk(28).spMult(30).aHit(10).mHit(10), POLEARM);
  public static final RegistryDelegate<Equipment> GREATER_WYRMLING_BOW = reg("greater_wyrmling_bow", () -> EquipmentBuilder.of(WEAPON, ItemIcon.BOW, 600).atk(20).spMult(30).aHit(10).mHit(10), BOW);
  public static final RegistryDelegate<Equipment> GREATER_WYRMLING_ESTOC = reg("greater_wyrmling_estoc", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 600).atk(32).spMult(30).aHit(10).mHit(10), SHORTSWORD);
  public static final RegistryDelegate<Equipment> GREATER_WYRMLING_KNUCKLE = reg("greater_wyrmling_knuckle", () -> EquipmentBuilder.of(WEAPON, ItemIcon.KNUCKLE, 600).atk(33).spMult(30).aHit(10).mHit(10), HAND);
  public static final RegistryDelegate<Equipment> GREATER_WYRMLING_MACE = reg("greater_wyrmling_mace", () -> EquipmentBuilder.of(WEAPON, ItemIcon.MACE, 600).atk(28).spMult(30).aHit(10).mHit(10), HAMMER);
  public static final RegistryDelegate<Equipment> GREATER_WYRMLING_AXE = reg("greater_wyrmling_axe", () -> EquipmentBuilder.of(WEAPON, ItemIcon.AXE, 600).atk(58).spMult(30).aHit(10).mHit(10), AXE);

  // Elder: Furni 4.000G
  public static final RegistryDelegate<Equipment> ELDER_BLADE = reg("elder_blade", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 2000).element(LodMod.FIRE_ELEMENT).atk(55).matk(15), LONGSWORD);
  public static final RegistryDelegate<Equipment> ELDER_PIKE = reg("elder_pike", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SPEAR, 2000).element(LodMod.WIND_ELEMENT).atk(50).matk(15), POLEARM);
  public static final RegistryDelegate<Equipment> ELDER_BOW = reg("elder_bow", () -> EquipmentBuilder.of(WEAPON, ItemIcon.BOW, 2000).element(LodMod.LIGHT_ELEMENT).atk(32).matk(30), BOW);
  public static final RegistryDelegate<Equipment> ELDER_ESTOC = reg("elder_estoc", () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 2000).element(LodMod.DARK_ELEMENT).atk(65).matk(15), SHORTSWORD);
  public static final RegistryDelegate<Equipment> ELDER_KNUCKLE = reg("elder_knuckle", () -> EquipmentBuilder.of(WEAPON, ItemIcon.KNUCKLE, 2000).element(LodMod.THUNDER_ELEMENT).atk(40).matk(10), HAND);
  public static final RegistryDelegate<Equipment> ELDER_HAMMER = reg("elder_hammer", () -> EquipmentBuilder.of(WEAPON, ItemIcon.HAMMER, 2000).element(LodMod.WATER_ELEMENT).atk(52).matk(25), HAMMER);
  public static final RegistryDelegate<Equipment> ELDER_AXE = reg("elder_axe", () -> EquipmentBuilder.of(WEAPON, ItemIcon.AXE, 2000).element(LodMod.EARTH_ELEMENT).atk(65).matk(10), AXE);

  // Set Seraph (fuori dai negozi) - niente SP a turno
  public static final RegistryDelegate<Equipment> GOD_DEVOURER = reg("god_devourer", () -> EquipmentBuilder.of(ARMOUR, ItemIcon.ARMOR, 750).revive(10).atk(5).matk(5).def(50).mdef(50).aAv(5).mAv(5), MALE);
  public static final RegistryDelegate<Equipment> GOD_EATER = reg("god_eater", () -> EquipmentBuilder.of(ARMOUR, ItemIcon.CLOAK, 750).revive(10).atk(5).matk(5).def(50).mdef(50).aAv(5).mAv(5), FEMALE);
  public static final RegistryDelegate<Equipment> GODS_CROWN = reg("gods_crown", () -> EquipmentBuilder.of(HELMET, ItemIcon.CROWN, 625).mpMult(20).matk(50).mdef(10).aHit(25).mHit(25), NEUTRAL);
  public static final RegistryDelegate<Equipment> GOD_SPEED = reg("god_speed", () -> EquipmentBuilder.of(BOOTS, ItemIcon.BOOTS, 625).hpMult(20).spd(25).def(10).aAv(10).mAv(10), NEUTRAL);
  public static final RegistryDelegate<Equipment> GOD_SLAYER = reg("god_slayer", () -> EquipmentBuilder.of(ACCESSORY, ItemIcon.PIN, 1000).atk(12).matk(12).def(12).mdef(12).spd(10), NEUTRAL);

  // =====================================================================
  // Heirloom: premio dell'Arena x3 - AT 70, stato 25%, +20 SP e +5% MP a turno, HP +25%
  // =====================================================================
  private static RegistryDelegate<Equipment> heirloom(final String id, final ItemIcon icon, final RegistryDelegate<legend.game.characters.Element> element, final int matk, final int spd, final int status, final String... types) {
    return reg(id, () -> EquipmentBuilder.of(WEAPON, icon, 500).element(element).atk(70).matk(matk).spd(spd).hpMult(25).mpRegen(5).spRegen(20).aHit(25).mHit(25).onHit(25, status), types);
  }

  public static final RegistryDelegate<Equipment> EMBER_FANG = heirloom("ember_fang", ItemIcon.SWORD, LodMod.FIRE_ELEMENT, 25, 0, 0x8, LONGSWORD);
  public static final RegistryDelegate<Equipment> TIDE_FANG = heirloom("tide_fang", ItemIcon.SWORD, LodMod.WATER_ELEMENT, 25, 0, 0x80, LONGSWORD);
  public static final RegistryDelegate<Equipment> GALE_LANCE = heirloom("gale_lance", ItemIcon.SPEAR, LodMod.WIND_ELEMENT, 30, 5, 0x8, POLEARM);
  public static final RegistryDelegate<Equipment> NIGHTVEIL_BOW = heirloom("nightveil_bow", ItemIcon.BOW, LodMod.DARK_ELEMENT, 35, 0, 0x80, BOW);
  public static final RegistryDelegate<Equipment> DAWN_RAPIER = heirloom("dawn_rapier", ItemIcon.SWORD, LodMod.LIGHT_ELEMENT, 25, 5, 0x6, SHORTSWORD);
  public static final RegistryDelegate<Equipment> STORM_KNUCKLES = heirloom("storm_knuckles", ItemIcon.KNUCKLE, LodMod.THUNDER_ELEMENT, 25, 0, 0x10, HAND);
  public static final RegistryDelegate<Equipment> WAVE_MALLET = heirloom("wave_mallet", ItemIcon.MACE, LodMod.WATER_ELEMENT, 10, 0, 0x4, HAMMER);
  public static final RegistryDelegate<Equipment> GIANTS_CLEAVER = heirloom("giants_cleaver", ItemIcon.AXE, LodMod.EARTH_ELEMENT, 30, 10, 0x8, AXE);

  public static final RegistryDelegate<Equipment>[] HEIRLOOMS = arr(EMBER_FANG, TIDE_FANG, GALE_LANCE, NIGHTVEIL_BOW, DAWN_RAPIER, STORM_KNUCKLES, WAVE_MALLET, GIANTS_CLEAVER);
  public static final RegistryDelegate<Equipment>[] WYRMLINGS = arr(WYRMLING_BLADE, WYRMLING_PIKE, WYRMLING_BOW, WYRMLING_ESTOC, WYRMLING_KNUCKLE, WYRMLING_MACE, WYRMLING_AXE);
  public static final RegistryDelegate<Equipment>[] GREATER_WYRMLINGS = arr(GREATER_WYRMLING_BLADE, GREATER_WYRMLING_PIKE, GREATER_WYRMLING_BOW, GREATER_WYRMLING_ESTOC, GREATER_WYRMLING_KNUCKLE, GREATER_WYRMLING_MACE, GREATER_WYRMLING_AXE);
  public static final RegistryDelegate<Equipment>[] ELDERS = arr(ELDER_BLADE, ELDER_PIKE, ELDER_BOW, ELDER_ESTOC, ELDER_KNUCKLE, ELDER_HAMMER, ELDER_AXE);
  public static final RegistryDelegate<Equipment>[] GOD_SET = arr(GOD_DEVOURER, GOD_EATER, GODS_CROWN, GOD_SPEED, GOD_SLAYER);

  private static RegistryDelegate<Equipment> buster(final String id, final RegistryDelegate<Element> element, final boolean plus) {
    return plus
      ? reg(id, () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 1500).element(element).atk(70).matk(10).spMult(25), SHORTSWORD)
      : reg(id, () -> EquipmentBuilder.of(WEAPON, ItemIcon.SWORD, 500).element(element).atk(35).matk(5).spMult(25), SHORTSWORD);
  }

  public static final RegistryDelegate<Equipment>[] LOHAN_BUSTERS = arr(FIRE_BUSTER, AQUA_BUSTER, WIND_BUSTER, EARTH_BUSTER, BOLT_BUSTER, LIGHT_BUSTER, DARK_BUSTER);
  public static final RegistryDelegate<Equipment>[] ULARA_BUSTERS = arr(FIRE_BUSTER_PLUS, AQUA_BUSTER_PLUS, WIND_BUSTER_PLUS, EARTH_BUSTER_PLUS, BOLT_BUSTER_PLUS, LIGHT_BUSTER_PLUS, DARK_BUSTER_PLUS);

  @SafeVarargs
  private static RegistryDelegate<Equipment>[] arr(final RegistryDelegate<Equipment>... values) {
    return values;
  }

  @EventListener
  public void registerEquipment(final EquipmentRegistryEvent event) {
    REGISTRAR.registryEvent(event);
  }

  @EventListener
  public void gatherEquipmentTypes(final GatherEquipmentTypesEvent event) {
    for(final Map.Entry<RegistryDelegate<Equipment>, String[]> entry : TYPES.entrySet()) {
      event.add(entry.getKey().getId(), entry.getValue());
    }
  }
}
