package wingsofdestiny.forge;

import legend.game.sound.Audio;
import legend.game.i18n.I18n;
import legend.game.inventory.Equipment;
import legend.game.inventory.InventoryEntry;
import legend.game.inventory.Item;
import legend.game.inventory.ItemStack;
import legend.game.inventory.screens.GatherShopExtensionsEvent;
import legend.game.inventory.screens.MessageBoxScreen;
import legend.game.inventory.screens.ShopExtension;
import legend.game.inventory.screens.ShopScreen;
import legend.game.modding.events.inventory.ShopContentsEvent;
import legend.game.types.GameState52c;
import legend.game.types.MessageBoxResult;
import legend.game.types.MessageBoxType;
import legend.game.types.Shop;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.items.WodItems;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static legend.game.SItem.menuStack;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;

/** In ogni negozio di equipaggiamento, una voce di forgia per ogni arma forgiabile che hai nella borsa (in New Game+ anche le armature). */
public class ForgeShop {
  private static final Logger LOGGER = LogManager.getFormatterLogger(ForgeShop.class);

  @EventListener(priority = Priority.LOWEST)
  public void shopContents(final ShopContentsEvent event) {
    if(event.shop.shopType_00 != 0 || gameState_800babc8 == null) {
      return;
    }

    final List<ShopScreen.ShopEntry<InventoryEntry<?>>> contents = event.contents;
    final Set<RegistryId> seen = new HashSet<>();
    // Le voci di forgia vanno in cima alla lista, nell'ordine in cui sono nella borsa
    int insertAt = 0;

    for(final Equipment equipment : gameState_800babc8.equipment_1e8) {
      final ForgedWeapons.Info info = ForgedWeapons.info(equipment);
      if(info == null || info.level() >= ForgedWeapons.MAX_LEVEL || !seen.add(equipment.getRegistryId())) {
        continue;
      }
      // Armature: solo in New Game+
      if(ForgedWeapons.isArmor(info.base()) && !wingsofdestiny.ngplus.NewGamePlus.active()) {
        continue;
      }

      final ForgeEntry entry = new ForgeEntry(info.base(), equipment, info.level() + 1);
      if(!entry.isMaxed()) {
        contents.add(insertAt++, new ShopScreen.ShopEntry<>(entry, entry.goldCost()));
      }
    }
  }

  @EventListener
  public void gatherExtensions(final GatherShopExtensionsEvent event) {
    event.addExtension(new Extension(), 500);
  }

  /** Quante Forge Stone (true) o Dragon Stone (false) hai. */
  static int count(final boolean forgeStone) {
    final Item item = (forgeStone ? WodItems.FORGE_STONE : WodItems.DRAGON_STONE).get();
    int count = 0;
    for(final ItemStack stack : gameState_800babc8.items_2e9) {
      if(stack.isSameItem(item)) {
        count += stack.getSize();
      }
    }
    return count;
  }

  private static void alert(final ShopScreen screen, final String text) {
    screen.deferAction(() -> menuStack.pushScreen(new MessageBoxScreen(text, MessageBoxType.ALERT, result -> { })));
  }

  public static class Extension extends ShopExtension<ForgeEntry> {
    @Override
    public String getName(final ShopScreen.ShopEntry<ForgeEntry> entry) {
      return "Forge";
    }

    @Override
    public boolean accepts(final ShopScreen.ShopEntry<?> entry) {
      return entry.item instanceof ForgeEntry;
    }

    @Override
    public boolean selectEntry(final ShopScreen screen, final Shop shop, final GameState52c gameState, final ShopScreen.ShopEntry<ForgeEntry> shopEntry, final int index) {
      final ForgeEntry entry = shopEntry.item;

      if(entry.isMaxed()) {
        alert(screen, "Already at the\nmaximum level.");
        return false;
      }
      if(!gameState.equipment_1e8.contains(entry.source)) {
        alert(screen, "You no longer carry\nthis item.");
        return false;
      }
      if(gameState.gold_94 < shopEntry.price) {
        alert(screen, I18n.translate("lod_core.ui.shop.not_enough_gold"));
        return false;
      }
      if(count(true) < entry.forgeStones() || count(false) < entry.dragonStones()) {
        alert(screen, entry.forgeStones() > 0 ? "Not enough\nForge Stones." : "Not enough\nDragon Stones.");
        return false;
      }

      final String question = "Forge into\n" + I18n.translate(entry.target.getNameTranslationKey()) + "?";
      menuStack.pushScreen(new MessageBoxScreen(question, MessageBoxType.CONFIRMATION, result -> {
        if(result == MessageBoxResult.YES) {
          forge(gameState, shopEntry);
        }
      }));
      return false;
    }

    private static void forge(final GameState52c gameState, final ShopScreen.ShopEntry<ForgeEntry> shopEntry) {
      final ForgeEntry entry = shopEntry.item;
      final int index = gameState.equipment_1e8.indexOf(entry.source);
      if(index == -1) {
        return;
      }

      // L'arma base viene sostituita da quella forgiata, nella stessa posizione della borsa
      gameState.equipment_1e8.set(index, entry.target);
      gameState.gold_94 -= shopEntry.price;
      if(entry.forgeStones() > 0) {
        gameState.items_2e9.take(new ItemStack(WodItems.FORGE_STONE.get(), entry.forgeStones()));
      }
      if(entry.dragonStones() > 0) {
        gameState.items_2e9.take(new ItemStack(WodItems.DRAGON_STONE.get(), entry.dragonStones()));
      }
      LOGGER.info("Wings of Destiny: forgiato %s -> %s", entry.source.getRegistryId(), entry.target.getRegistryId());

      // La voce passa al livello successivo (se hai un'altra copia dell'arma base, resta comunque valida per quella)
      entry.set(entry.target, entry.level + 1);
      shopEntry.price = entry.goldCost();
      Audio.playMenuSound(2);
    }
  }
}
