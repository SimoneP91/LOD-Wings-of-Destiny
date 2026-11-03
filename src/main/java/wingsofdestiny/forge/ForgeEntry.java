package wingsofdestiny.forge;

import legend.game.i18n.I18n;
import legend.game.inventory.Equipment;
import legend.game.inventory.InventoryEntry;
import legend.game.inventory.ItemIcon;
import org.legendofdragoon.modloader.registries.RegistryId;

/**
 * Voce "forgia" nella lista del negozio: porta l'arma {@link #source} al livello {@link #level}.
 * Si aggiorna sul posto dopo ogni forgiatura, cosi' la lista non va ricaricata.
 */
public class ForgeEntry implements InventoryEntry<ForgeEntry> {
  public final RegistryId base;
  public Equipment source;
  public Equipment target;
  public int level;

  public ForgeEntry(final RegistryId base, final Equipment source, final int level) {
    this.base = base;
    this.set(source, level);
  }

  /** Imposta la prossima forgiatura; target null = livello massimo raggiunto. */
  void set(final Equipment source, final int level) {
    this.source = source;
    this.level = level;
    this.target = level <= ForgedWeapons.MAX_LEVEL ? ForgedWeapons.level(this.base, level) : null;
  }

  public boolean isMaxed() {
    return this.target == null;
  }

  /** Costo: 10/12/14/16/20% del prezzo d'acquisto della base, minimo 50G. */
  public int goldCost() {
    if(this.isMaxed()) {
      return 0;
    }
    final int[] percent = {10, 12, 14, 16, 20};
    final int buy = ForgedWeapons.base(this.base).getBuyPrice();
    return Math.max(50, Math.round(buy * percent[this.level - 1] / 100.0f));
  }

  /** Materiali: +1/+2/+3 = 1/2/3 Forge Stone, +4/+5 = 1/2 Dragon Stone. */
  public int forgeStones() {
    return this.level <= 3 ? this.level : 0;
  }

  public int dragonStones() {
    return this.level == 4 ? 1 : this.level == 5 ? 2 : 0;
  }

  @Override
  public RegistryId getRegistryId() {
    return this.isMaxed() ? this.source.getRegistryId() : this.target.getRegistryId();
  }

  @Override
  public ItemIcon getIcon() {
    return this.source.getIcon();
  }

  /** Il gioco traduce questa "chiave": una chiave che non esiste viene mostrata cosi' com'e'. */
  @Override
  public String getNameTranslationKey() {
    if(this.isMaxed()) {
      return I18n.translate(this.source.getNameTranslationKey()) + " MAX";
    }
    return I18n.translate(this.target.getNameTranslationKey());
  }

  @Override
  public String getDescriptionTranslationKey() {
    if(this.isMaxed()) {
      return "Already at the\nmaximum level.";
    }
    final StringBuilder sb = new StringBuilder();
    if(ForgedWeapons.isArmor(this.base)) {
      sb.append("Forge: DF ").append(this.source.defence_12).append(" > ").append(this.target.defence_12)
        .append(", MDF ").append(this.source.magicDefence_13).append(" > ").append(this.target.magicDefence_13);
    } else {
      sb.append("Forge: AT ").append(this.source.attack_10).append(" > ").append(this.target.attack_10);
    }
    if(this.forgeStones() > 0) {
      sb.append("\nNeeds ").append(this.forgeStones()).append(" Forge Stone (").append(ForgeShop.count(true)).append(')');
    } else {
      sb.append("\nNeeds ").append(this.dragonStones()).append(" Dragon Stone (").append(ForgeShop.count(false)).append(')');
    }
    return sb.toString();
  }

  @Override
  public int getSellPrice() {
    return 0;
  }

  @Override
  public int getBuyPrice() {
    return this.goldCost();
  }

  @Override
  public int getSize() {
    return 1;
  }

  @Override
  public int getMaxSize() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }
}
