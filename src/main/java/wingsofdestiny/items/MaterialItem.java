package wingsofdestiny.items;

import legend.game.inventory.Item;
import legend.game.inventory.ItemIcon;
import legend.game.inventory.ItemStack;

import static legend.core.GameEngine.CONFIG;
import static legend.lodmod.LodConfig.ITEM_STACK_SIZE;

/** Materiale della forgia: non si usa, si accumula e si spende dal fabbro. */
public class MaterialItem extends Item {
  public MaterialItem(final ItemIcon icon, final int sellPrice) {
    super(icon, sellPrice);
  }

  @Override
  public int getMaxStackSize(final ItemStack stack) {
    return CONFIG.getConfig(ITEM_STACK_SIZE.get());
  }

  @Override
  public boolean isProtected(final ItemStack stack) {
    return true;
  }

  @Override
  public boolean canBeUsed(final ItemStack stack, final UsageLocation location) {
    return false;
  }

  @Override
  public boolean canTarget(final ItemStack stack, final TargetType type) {
    return false;
  }
}
