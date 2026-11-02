package wingsofdestiny.arena;

import legend.game.i18n.I18n;
import legend.game.inventory.InventoryEntry;
import legend.game.inventory.screens.GatherShopExtensionsEvent;
import legend.game.inventory.screens.MessageBoxScreen;
import legend.game.inventory.screens.ShopExtension;
import legend.game.inventory.screens.ShopScreen;
import legend.game.modding.events.inventory.ShopContentsEvent;
import legend.game.sound.Audio;
import legend.game.types.GameState52c;
import legend.game.types.MessageBoxResult;
import legend.game.types.MessageBoxType;
import legend.game.types.Shop;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.items.WodEquipment;
import wingsofdestiny.items.WodItems;

import java.util.List;
import java.util.Map;

import static legend.game.SItem.menuStack;

/** Le sfide d'Arena compaiono nel negozio oggetti di Lohan. */
public class ArenaShop {
  private static final String SHOP = "lod:lohan_item_shop";

  @EventListener(priority = Priority.LOWEST)
  public void shopContents(final ShopContentsEvent event) {
    if(!SHOP.equals(event.shop.getRegistryId().toString())) {
      return;
    }

    final List<ShopScreen.ShopEntry<InventoryEntry<?>>> contents = event.contents;
    int at = 0;

    // 1. Sfide d'Arena (in oro)
    for(final Map.Entry<RegistryId, Arena.BossRecord> boss : Arena.DEFEATED.entrySet()) {
      for(int grade = 1; grade <= 3; grade++) {
        final ArenaEntry entry = new ArenaEntry(boss.getKey(), boss.getValue(), grade);
        contents.add(at++, new ShopScreen.ShopEntry<>(entry, entry.cost()));
      }
    }

    // 2. Scambio Medaglie
    for(final MedalEntry entry : catalog()) {
      contents.add(at++, new ShopScreen.ShopEntry<>(entry, entry.cost));
    }
  }

  /** Catalogo dello scambio Medaglie. */
  private static List<MedalEntry> catalog() {
    final List<MedalEntry> list = new java.util.ArrayList<>();
    list.add(MedalEntry.of(WodItems.FORGE_STONE.get(), 2));
    list.add(MedalEntry.of(WodItems.DRAGON_STONE.get(), 12));
    list.add(MedalEntry.of(WodItems.ROCK_FIREFLIES.get(), 3));
    list.add(MedalEntry.of(WodItems.ARCHANGELS_PRAYER.get(), 100));
    list.add(MedalEntry.of(WodEquipment.GOD_SLAYER.get(), 40));
    list.add(MedalEntry.of(WodEquipment.GODS_CROWN.get(), 30));
    list.add(MedalEntry.of(WodEquipment.GOD_SPEED.get(), 30));
    list.add(MedalEntry.of(WodEquipment.GOD_DEVOURER.get(), 35));
    list.add(MedalEntry.of(WodEquipment.GOD_EATER.get(), 35));
    return list;
  }

  static int medals() {
    final legend.game.inventory.Item medal = WodItems.ARENA_MEDAL.get();
    int count = 0;
    for(final legend.game.inventory.ItemStack stack : legend.game.Scus94491BpeSegment_800b.gameState_800babc8.items_2e9) {
      if(stack.isSameItem(medal)) {
        count += stack.getSize();
      }
    }
    return count;
  }

  @EventListener
  public void gatherExtensions(final GatherShopExtensionsEvent event) {
    event.addExtension(new Extension(), 500);
    event.addExtension(new MedalExtension(), 500);
  }

  private static void alert(final ShopScreen screen, final String text) {
    screen.deferAction(() -> menuStack.pushScreen(new MessageBoxScreen(text, MessageBoxType.ALERT, result -> { })));
  }

  public static class Extension extends ShopExtension<ArenaEntry> {
    @Override
    public String getName(final ShopScreen.ShopEntry<ArenaEntry> entry) {
      return "Arena";
    }

    @Override
    public boolean accepts(final ShopScreen.ShopEntry<?> entry) {
      return entry.item instanceof ArenaEntry;
    }

    @Override
    public boolean selectEntry(final ShopScreen screen, final Shop shop, final GameState52c gameState, final ShopScreen.ShopEntry<ArenaEntry> shopEntry, final int index) {
      if(Arena.hasPending()) {
        alert(screen, "You already signed up.\nLeave the shop\nto start.");
        return false;
      }
      if(gameState.gold_94 < shopEntry.price) {
        alert(screen, I18n.translate("lod_core.ui.shop.not_enough_gold"));
        return false;
      }

      menuStack.pushScreen(new MessageBoxScreen("Challenge\n" + shopEntry.item.getNameTranslationKey() + "?", MessageBoxType.CONFIRMATION, result -> {
        if(result == MessageBoxResult.YES) {
          gameState.gold_94 -= shopEntry.price;
          Arena.setPending(shopEntry.item);
          Audio.playMenuSound(2);
          screen.deferAction(() -> menuStack.pushScreen(new MessageBoxScreen("Leave the shop:\nthe battle will begin\nin a moment.", MessageBoxType.ALERT, r -> { })));
        }
      }));
      return false;
    }
  }

  /** Scambio Medaglie: in alto mostra "Medals" e quante ne hai, invece dell'oro. */
  public static class MedalExtension extends ShopExtension<MedalEntry> {
    private static final legend.game.inventory.screens.FontOptions FONT = new legend.game.inventory.screens.FontOptions().set(legend.game.SItem.UI_TEXT).size(0.8f);

    @Override
    public String getName(final ShopScreen.ShopEntry<MedalEntry> entry) {
      return "Medals";
    }

    @Override
    public boolean accepts(final ShopScreen.ShopEntry<?> entry) {
      return entry.item instanceof MedalEntry;
    }

    @Override
    public void drawShopHeader(final ShopScreen screen, final Shop shop, final GameState52c gameState, final ShopScreen.ShopEntry<MedalEntry> entry, final int x, final int y) {
      final legend.game.types.Renderable58 renderable = legend.game.SItem.allocateOneFrameGlyph(94, x, y);
      renderable.metricsCount = 16;
      legend.game.Text.renderText("Medal Exchange", x + 7, y + 7, FONT);
      legend.game.Text.renderText("Medals", x + 7, y + 19.5f, FONT);
      legend.game.SItem.renderRightAlignedNumber(x + 120, y + 20, medals(), 0);
    }

    @Override
    public boolean selectEntry(final ShopScreen screen, final Shop shop, final GameState52c gameState, final ShopScreen.ShopEntry<MedalEntry> shopEntry, final int index) {
      final MedalEntry entry = shopEntry.item;
      if(medals() < entry.cost) {
        alert(screen, "Not enough\nArena Medals.");
        return false;
      }

      final String name = I18n.translate(entry.reward.getNameTranslationKey());
      menuStack.pushScreen(new MessageBoxScreen("Trade " + entry.cost + " medals\nfor " + name + "?", MessageBoxType.CONFIRMATION, result -> {
        if(result != MessageBoxResult.YES) {
          return;
        }
        final boolean given;
        if(entry.reward instanceof final legend.game.inventory.ItemStack stack) {
          given = gameState.items_2e9.give(new legend.game.inventory.ItemStack(stack.getItem(), 1)).isEmpty();
        } else {
          given = legend.game.SItem.giveEquipment((legend.game.inventory.Equipment)entry.reward);
        }
        if(!given) {
          screen.deferAction(() -> menuStack.pushScreen(new MessageBoxScreen(I18n.translate("lod_core.ui.shop.inventory_full"), MessageBoxType.ALERT, r -> { })));
          return;
        }
        gameState.items_2e9.take(new legend.game.inventory.ItemStack(WodItems.ARENA_MEDAL.get(), entry.cost));
        Audio.playMenuSound(2);
      }));
      return false;
    }
  }
}
