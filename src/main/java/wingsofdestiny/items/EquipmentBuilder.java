package wingsofdestiny.items;

import legend.game.characters.Element;
import legend.game.characters.ElementSet;
import legend.game.inventory.Equipment;
import legend.game.inventory.ItemIcon;
import legend.game.types.EquipmentSlot;
import legend.lodmod.LodMod;
import org.legendofdragoon.modloader.registries.RegistryDelegate;

/**
 * Evita di scrivere a mano i 35 parametri del costruttore di Equipment.
 * Tutto parte da 0; il prezzo e' quello di VENDITA (il negozio lo raddoppia per l'acquisto).
 */
final class EquipmentBuilder {
  private final EquipmentSlot slot;
  private final ItemIcon icon;
  private final int sellPrice;
  private RegistryDelegate<Element> element = LodMod.NO_ELEMENT;
  private int mpPerPhysHit, spPerPhysHit, mpPerMagHit, spPerMagHit;
  private int hpMult, mpMult, spMult;
  private int hpRegen, mpRegen, spRegen, escape;
  private int spd, atk, matk, def, mdef, aHit, mHit, aAv, mAv;
  private int revive, onHitChance, onHitStatus;

  private EquipmentBuilder(final EquipmentSlot slot, final ItemIcon icon, final int sellPrice) {
    this.slot = slot;
    this.icon = icon;
    this.sellPrice = sellPrice;
  }

  static EquipmentBuilder of(final EquipmentSlot slot, final ItemIcon icon, final int sellPrice) {
    return new EquipmentBuilder(slot, icon, sellPrice);
  }

  EquipmentBuilder element(final RegistryDelegate<Element> element) { this.element = element; return this; }
  EquipmentBuilder mpPerHit(final int phys, final int mag) { this.mpPerPhysHit = phys; this.mpPerMagHit = mag; return this; }
  EquipmentBuilder spPerHit(final int phys, final int mag) { this.spPerPhysHit = phys; this.spPerMagHit = mag; return this; }
  EquipmentBuilder hpMult(final int v) { this.hpMult = v; return this; }
  EquipmentBuilder mpMult(final int v) { this.mpMult = v; return this; }
  EquipmentBuilder spMult(final int v) { this.spMult = v; return this; }
  EquipmentBuilder hpRegen(final int v) { this.hpRegen = v; return this; }
  EquipmentBuilder mpRegen(final int v) { this.mpRegen = v; return this; }
  EquipmentBuilder escape(final int v) { this.escape = v; return this; }
  EquipmentBuilder spd(final int v) { this.spd = v; return this; }
  EquipmentBuilder atk(final int v) { this.atk = v; return this; }
  EquipmentBuilder matk(final int v) { this.matk = v; return this; }
  EquipmentBuilder def(final int v) { this.def = v; return this; }
  EquipmentBuilder mdef(final int v) { this.mdef = v; return this; }
  EquipmentBuilder aHit(final int v) { this.aHit = v; return this; }
  EquipmentBuilder aAv(final int v) { this.aAv = v; return this; }
  EquipmentBuilder mHit(final int v) { this.mHit = v; return this; }
  EquipmentBuilder spRegen(final int v) { this.spRegen = v; return this; }
  EquipmentBuilder revive(final int v) { this.revive = v; return this; }
  /** Probabilita' (%) di causare lo stato (bit) al colpo. */
  EquipmentBuilder onHit(final int chance, final int status) { this.onHitChance = chance; this.onHitStatus = status; return this; }
  EquipmentBuilder mAv(final int v) { this.mAv = v; return this; }

  Equipment build() {
    return new Equipment(
      this.sellPrice, 0x0, this.slot, this.element.get(), new ElementSet(), new ElementSet(), 0x0,
      this.mpPerPhysHit, this.spPerPhysHit, this.mpPerMagHit, this.spPerMagHit,
      this.hpMult, this.mpMult, this.spMult,
      false, false, false, false,
      this.revive, this.hpRegen, this.mpRegen, this.spRegen, this.escape,
      this.icon, this.spd, this.atk, this.matk, this.def, this.mdef, this.aHit, this.mHit, this.aAv, this.mAv,
      this.onHitChance, this.onHitStatus
    );
  }
}
