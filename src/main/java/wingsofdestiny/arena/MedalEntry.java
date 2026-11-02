package wingsofdestiny.arena;

import legend.game.inventory.Equipment;
import legend.game.inventory.InventoryEntry;
import legend.game.inventory.Item;
import legend.game.inventory.ItemIcon;
import legend.game.inventory.ItemStack;
import org.legendofdragoon.modloader.registries.RegistryId;

/** Voce dello scambio Medaglie: un oggetto o un equipaggiamento che si paga in Medaglie d'Arena. */
public class MedalEntry implements InventoryEntry<MedalEntry> {
  public final InventoryEntry<?> reward;
  public final int cost;

  public MedalEntry(final InventoryEntry<?> reward, final int cost) {
    this.reward = reward;
    this.cost = cost;
  }

  static MedalEntry of(final Item item, final int cost) {
    return new MedalEntry(new ItemStack(item), cost);
  }

  static MedalEntry of(final Equipment equipment, final int cost) {
    return new MedalEntry(equipment, cost);
  }

  @Override
  public RegistryId getRegistryId() {
    return this.reward.getRegistryId();
  }

  @Override
  public ItemIcon getIcon() {
    return this.reward.getIcon();
  }

  @Override
  public String getNameTranslationKey() {
    return this.reward.getNameTranslationKey();
  }

  @Override
  public String getDescriptionTranslationKey() {
    return this.reward.getDescriptionTranslationKey();
  }

  @Override
  public int getSellPrice() {
    return 0;
  }

  @Override
  public int getBuyPrice() {
    return this.cost;
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
