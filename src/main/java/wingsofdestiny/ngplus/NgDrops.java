package wingsofdestiny.ngplus;

import legend.core.GameEngine;
import legend.game.combat.types.CombatantStruct1a8;
import legend.game.inventory.ItemStack;
import legend.game.modding.events.battle.EnemyRewardsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.arena.Arena;
import wingsofdestiny.challenge.Challenge;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static legend.game.Scus94491BpeSegment_800b.encounter;

/**
 * New Game+: i consumabili droppati dai mostri normali arrivano dall'area del tuo livello
 * (uno a caso tra quelli che droppano i mostri di quell'area). Stessa probabilita' del drop originale.
 * Boss, Arena ed equipaggiamento restano come sono.
 * Gira dopo la tabella delle ricompense (HIGH) e prima dei bonus di elite e zone maledette (LOWEST).
 */
public class NgDrops {
  private static final Logger LOGGER = LogManager.getFormatterLogger(NgDrops.class);
  private static Field dropsField;
  private final Random random = new Random();

  @EventListener
  public void enemyRewards(final EnemyRewardsEvent event) {
    if(!NewGamePlus.active() || Arena.inArenaBattle() || Challenge.isBoss(encounter)) {
      return;
    }
    final List<CombatantStruct1a8.ItemDrop> drops = drops(event);
    if(drops == null || drops.isEmpty()) {
      return;
    }

    final List<RegistryId> pool = pool(NgScaling.areaForLevel(Arena.partyLevel()));
    if(pool.isEmpty()) {
      return;
    }

    final List<CombatantStruct1a8.ItemDrop> upgraded = new ArrayList<>();
    for(final CombatantStruct1a8.ItemDrop drop : drops) {
      if(drop.item() instanceof ItemStack) {
        final RegistryId id = pool.get(this.random.nextInt(pool.size()));
        upgraded.add(new CombatantStruct1a8.ItemDrop(drop.chance(), new ItemStack(GameEngine.REGISTRIES.items.getEntry(id).get())));
      } else {
        upgraded.add(drop);
      }
    }
    drops.clear();
    drops.addAll(upgraded);
  }

  /** I consumabili dell'area (l'equipaggiamento della lista non entra). */
  private static List<RegistryId> pool(final int area) {
    final List<RegistryId> out = new ArrayList<>();
    for(final String id : NgScaling.AREA_ITEMS.getOrDefault(area, List.of())) {
      final RegistryId registryId = new RegistryId(id);
      if(GameEngine.REGISTRIES.items.hasEntry(registryId)) {
        out.add(registryId);
      }
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  private static List<CombatantStruct1a8.ItemDrop> drops(final EnemyRewardsEvent event) {
    try {
      if(dropsField == null) {
        dropsField = EnemyRewardsEvent.class.getDeclaredField("drops");
        dropsField.setAccessible(true);
      }
      return (List<CombatantStruct1a8.ItemDrop>)dropsField.get(event);
    } catch(final ReflectiveOperationException e) {
      LOGGER.warn("Wings of Destiny: drop del New Game+ non modificabili", e);
      return null;
    }
  }
}
