package wingsofdestiny.items;

import legend.game.inventory.Equipment;
import legend.game.inventory.InventoryEntry;
import legend.game.inventory.Item;
import legend.game.inventory.ItemStack;
import legend.game.inventory.screens.ShopScreen;
import legend.game.modding.events.inventory.ShopContentsEvent;
import legend.lodmod.LodEquipment;
import legend.lodmod.LodItems;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.RegistryDelegate;

import java.util.List;

import static wingsofdestiny.items.WodEquipment.*;

/** Contenuto dei negozi. I prezzi qui sono quelli di ACQUISTO. */
public class ShopChanges {
  @EventListener
  public void shopContents(final ShopContentsEvent event) {
    final List<ShopScreen.ShopEntry<InventoryEntry<?>>> c = event.contents;

    switch(event.shop.getRegistryId().toString()) {
      case "lod:bale_equipment_shop" -> {
        eq(c, SAGES_SHIELD, 200);
        eq(c, THIEFS_SHOES, 200);
        for(final RegistryDelegate<Equipment> w : WYRMLINGS) {
          eq(c, w, 300);
        }
      }
      case "lod:lohan_equipment_shop" -> {
        eq(c, HEAVY_ROBE, 150);
        for(final RegistryDelegate<Equipment> buster : LOHAN_BUSTERS) {
          eq(c, buster, 1000);
        }
        eq(c, SABRE, 800);
        eq(c, SWIFT_BOOTS, 1500);
      }
      case "lod:donau_equipment_shop" -> eq(c, VELVET_DRESS, 250);
      case "lod:fletz_accessory_shop" -> {
        eq(c, TEMPLAR_SHIELD, 300);
        eq(c, WIZARD_SHIELD, 300);
      }
      case "lod:fletz_equipment_shop" -> {
        for(final RegistryDelegate<Equipment> w : GREATER_WYRMLINGS) {
          eq(c, w, 1200);
        }
      }
      case "lod:furni_equipment_shop" -> {
        for(final RegistryDelegate<Equipment> w : ELDERS) {
          eq(c, w, 4000);
        }
        eq(c, LAVA_BLADE, 3000);
        eq(c, VOID_ESTOC, 3000);
        eq(c, SPECTRAL_ARROW, 3000);
        eq(c, HYDRO_HAMMER, 3000);
      }
      case "lod:deningrad_equipment_shop" -> {
        eq(c, ATTACK_HORN, 8000);
        eq(c, DEFENSE_HORN, 8000);
        eq(c, LodEquipment.DRAGON_HELM, 4000);
        eq(c, LodEquipment.BANDITS_RING, 4000);
        eq(c, LodEquipment.DANCERS_RING, 4000);
        eq(c, LodEquipment.BANDITS_SHOES, 2500);
        eq(c, LodEquipment.DANCERS_SHOES, 2500);
      }
      case "lod:deningrad_item_shop" -> item(c, LodItems.PSYCHE_BOMB, 500);
      case "lod:wingly_forest_equipment_shop" -> {
        eq(c, MAGICAL_SOLLERETS, 600);
        eq(c, MAGICAL_CHAUSSES, 600);
      }
      case "lod:vellweb_equipment_shop" -> eq(c, MASTERS_HEADBAND, 200);
      case "lod:zenebatos_equipment_shop" -> eq(c, STONE_AXE, 3000);
      case "lod:ulara_equipment_shop" -> {
        eq(c, TITAN_RING, 600);
        eq(c, WINGLY_SUIT, 1000);
        for(final RegistryDelegate<Equipment> buster : ULARA_BUSTERS) {
          eq(c, buster, 3000);
        }
        eq(c, GREAT_SABRE, 2500);
      }
      case "lod:rouge_equipment_shop" -> eq(c, LodEquipment.DESTROYER_MACE, -1);
      case "lod:moon_equipment_shop" -> {
        c.removeIf(e -> LodEquipment.DESTROYER_MACE.getId().equals(e.item.getRegistryId()));
        eq(c, TITAN_RING, 600);
      }
      case "lod:moon_item_shop" -> item(c, LodItems.SPIRIT_POTION, -1);
      default -> { }
    }
  }

  /** price -1 = prezzo normale del gioco. */
  private static void eq(final List<ShopScreen.ShopEntry<InventoryEntry<?>>> c, final RegistryDelegate<Equipment> equipment, final int price) {
    if(!equipment.isValid()) {
      return;
    }

    final Equipment e = equipment.get();
    c.add(new ShopScreen.ShopEntry<>(e, price < 0 ? e.getBuyPrice() : price));
  }

  private static void item(final List<ShopScreen.ShopEntry<InventoryEntry<?>>> c, final RegistryDelegate<Item> item, final int price) {
    if(!item.isValid()) {
      return;
    }

    final ItemStack stack = new ItemStack(item.get());
    c.add(new ShopScreen.ShopEntry<>(stack, price < 0 ? stack.getBuyPrice() : price));
  }
}
