package wingsofdestiny.items;

import legend.game.SItem;
import legend.game.inventory.Item;
import legend.game.inventory.ItemIcon;
import legend.game.inventory.ItemStack;
import legend.game.inventory.UseItemResponse;

import static legend.core.GameEngine.CONFIG;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static legend.lodmod.LodConfig.ITEM_STACK_SIZE;

/** Rock Fireflies: solo dal menu, HP/MP pieni e stati rimossi a tutto il party. */
public class RockFirefliesItem extends Item {
  public RockFirefliesItem() {
    super(ItemIcon.SACK, 10);
  }

  @Override
  public int getMaxStackSize(final ItemStack stack) {
    return CONFIG.getConfig(ITEM_STACK_SIZE.get());
  }

  @Override
  public boolean canBeUsed(final ItemStack stack, final UsageLocation location) {
    return location == UsageLocation.MENU;
  }

  @Override
  public boolean canTarget(final ItemStack stack, final TargetType type) {
    return type == TargetType.ALL;
  }

  @Override
  public void useInMenu(final ItemStack stack, final UseItemResponse response, final int charId) {
    SItem.addHp(charId, -1);
    SItem.addMp(charId, -1);
    gameState_800babc8.charData_32c.get(charId).status_10 = 0;
    response.success();
  }
}
