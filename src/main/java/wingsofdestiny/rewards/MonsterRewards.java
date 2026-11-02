package wingsofdestiny.rewards;

import legend.core.GameEngine;
import legend.game.combat.types.CombatantStruct1a8;
import legend.game.inventory.InventoryEntry;
import legend.game.inventory.ItemStack;
import legend.game.modding.events.battle.EnemyRewardsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import org.legendofdragoon.modloader.registries.RegistryId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Ricompense dei mostri, uguali in Standard e Hard:
 *  - EXP e oro: il valore piu' alto tra retail e tabella Hard
 *  - drop: oggetto e probabilita' della tabella (senza Legend Casque e Armor of Legend).
 *    Se l'oggetto non esiste (oggetto Wings of Destiny non ancora aggiunto) si usa il drop retail.
 * Gira prima dei moltiplicatori delle opzioni (priorita' HIGH).
 */
public class MonsterRewards {
  private static final Logger LOGGER = LogManager.getFormatterLogger(MonsterRewards.class);

  private final Map<Integer, Reward> rewards = new HashMap<>();

  public MonsterRewards() {
    try(final InputStream in = MonsterRewards.class.getResourceAsStream("/wings_of_destiny/data/monster_rewards.csv")) {
      if(in == null) {
        throw new IOException("tabella ricompense mancante");
      }

      final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line;
      while((line = reader.readLine()) != null) {
        if(line.isBlank() || line.startsWith("#")) {
          continue;
        }

        final String[] c = line.split(",");
        this.rewards.put(Integer.parseInt(c[0]), new Reward(
          Integer.parseInt(c[1]), Integer.parseInt(c[2]),
          Integer.parseInt(c[3]), c[4].trim(),
          Integer.parseInt(c[5]), c[6].trim()
        ));
      }
    } catch(final IOException | RuntimeException e) {
      LOGGER.error("Wings of Destiny: impossibile leggere la tabella delle ricompense, restano retail", e);
    }

    LOGGER.info("Wings of Destiny: caricate %d ricompense dei mostri", this.rewards.size());
  }

  @EventListener(priority = Priority.HIGH)
  public void enemyRewards(final EnemyRewardsEvent event) {
    final Reward reward = this.rewards.get(event.enemyId);

    if(reward == null) {
      return;
    }

    event.xp = reward.exp;
    event.gold = reward.gold;
    event.clear();

    InventoryEntry<?> drop = resolve(reward.item);
    int chance = reward.chance;

    if(drop == null && !"none".equals(reward.item)) {
      drop = resolve(reward.fallbackItem);
      chance = reward.fallbackChance;
    }

    if(drop != null && chance > 0) {
      event.add(new CombatantStruct1a8.ItemDrop(chance, drop));
    }
  }

  /** Equipaggiamento o oggetto dal registro del gioco, null se non esiste. */
  private static InventoryEntry<?> resolve(final String id) {
    if("none".equals(id)) {
      return null;
    }

    final RegistryId registryId = new RegistryId(id);

    if(GameEngine.REGISTRIES.equipment.hasEntry(registryId)) {
      return GameEngine.REGISTRIES.equipment.getEntry(registryId).get();
    }

    if(GameEngine.REGISTRIES.items.hasEntry(registryId)) {
      return new ItemStack(GameEngine.REGISTRIES.items.getEntry(registryId).get(), 1);
    }

    return null;
  }

  private record Reward(int exp, int gold, int chance, String item, int fallbackChance, String fallbackItem) { }
}
