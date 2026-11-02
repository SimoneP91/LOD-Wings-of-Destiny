package wingsofdestiny.items;

import legend.core.GameEngine;
import legend.game.combat.types.CombatantStruct1a8;
import legend.game.inventory.Item;
import legend.game.inventory.ItemRegistryEvent;
import legend.game.inventory.ItemStack;
import legend.game.modding.events.battle.EnemyRewardsEvent;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.Registrar;
import org.legendofdragoon.modloader.registries.RegistryDelegate;
import wingsofdestiny.WingsOfDestiny;

import static legend.game.Scus94491BpeSegment_8005.submapCut_80052c30;

/** Oggetti consumabili nuovi + drop extra delle Rock Fireflies. */
public final class WodItems {
  private static final Registrar<Item, ItemRegistryEvent> REGISTRAR = new Registrar<>(GameEngine.REGISTRIES.items, WingsOfDestiny.MOD_ID);

  public static final RegistryDelegate<Item> ARCHANGELS_PRAYER = REGISTRAR.register("archangels_prayer", ArchangelsPrayerItem::new);
  public static final RegistryDelegate<Item> ROCK_FIREFLIES = REGISTRAR.register("rock_fireflies", RockFirefliesItem::new);
  /** Materiali della forgia */
  public static final RegistryDelegate<Item> FORGE_STONE = REGISTRAR.register("forge_stone", () -> new MaterialItem(legend.game.inventory.ItemIcon.STONE, 25));
  /** Medaglie dell'Arena, si spendono a Lohan */
  public static final RegistryDelegate<Item> ARENA_MEDAL = REGISTRAR.register("arena_medal", () -> new MaterialItem(legend.game.inventory.ItemIcon.PIN, 1));
  public static final RegistryDelegate<Item> DRAGON_STONE = REGISTRAR.register("dragon_stone", () -> new MaterialItem(legend.game.inventory.ItemIcon.SIG_STONE, 100));

  @EventListener
  public void registerItems(final ItemRegistryEvent event) {
    REGISTRAR.registryEvent(event);
  }

  /** 12% per mostro nelle zone delle lucciole (submap 48, 252, 264). */
  @EventListener
  public void rockFirefliesDrop(final EnemyRewardsEvent event) {
    if(submapCut_80052c30 == 48 || submapCut_80052c30 == 252 || submapCut_80052c30 == 264) {
      event.add(new CombatantStruct1a8.ItemDrop(12, new ItemStack(ROCK_FIREFLIES.get())));
    }
  }
}
