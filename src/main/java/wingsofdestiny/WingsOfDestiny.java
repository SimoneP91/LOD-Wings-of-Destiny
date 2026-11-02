package wingsofdestiny;

import legend.core.GameEngine;
import legend.game.characters.CharacterAdditionInfo;
import legend.game.characters.CharacterData2c;
import legend.game.modding.events.battle.BattleEndedEvent;
import legend.game.modding.events.battle.BattleStartedEvent;
import legend.game.modding.events.battle.EnemyRewardsEvent;
import legend.game.saves.ConfigRegistryEvent;
import org.legendofdragoon.modloader.Mod;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.arena.Arena;
import wingsofdestiny.arena.ArenaShop;
import wingsofdestiny.challenge.Challenge;
import wingsofdestiny.forge.ForgeShop;
import wingsofdestiny.forge.ForgedWeapons;
import wingsofdestiny.characters.AdditionTuning;
import wingsofdestiny.characters.DragoonMultipliers;
import wingsofdestiny.config.WodConfigs;
import wingsofdestiny.items.ArchangelsPrayerHandler;
import wingsofdestiny.items.EquipmentBuffs;
import wingsofdestiny.items.ShopChanges;
import wingsofdestiny.items.WodEquipment;
import wingsofdestiny.items.WodItems;
import wingsofdestiny.monsters.MonsterDifficulty;
import wingsofdestiny.rewards.MonsterRewards;
import wingsofdestiny.ui.MonsterHpDisplay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;

@Mod(id = WingsOfDestiny.MOD_ID, version = "^3.0.0")
public class WingsOfDestiny {
  public static final String MOD_ID = "wings_of_destiny";

  // Il bus eventi tiene solo riferimenti deboli ai listener: questi campi li tengono in vita.
  private final MonsterDifficulty monsterDifficulty = new MonsterDifficulty();
  private final MonsterHpDisplay monsterHpDisplay = new MonsterHpDisplay();
  private final MonsterRewards monsterRewards = new MonsterRewards();
  private final AdditionTuning additionTuning = new AdditionTuning();
  private final DragoonMultipliers dragoonMultipliers = new DragoonMultipliers();
  private final WodEquipment equipment = new WodEquipment();
  private final WodItems items = new WodItems();
  private final ArchangelsPrayerHandler archangelsPrayer = new ArchangelsPrayerHandler();
  private final ShopChanges shops = new ShopChanges();
  private final EquipmentBuffs equipmentBuffs = new EquipmentBuffs();
  private final Challenge challenge = new Challenge();
  private final ForgedWeapons forgedWeapons = new ForgedWeapons();
  private final ForgeShop forgeShop = new ForgeShop();
  private final Arena arena = new Arena();
  private final ArenaShop arenaShop = new ArenaShop();
  private final wingsofdestiny.ngplus.NewGamePlus newGamePlus = new wingsofdestiny.ngplus.NewGamePlus();
  private final wingsofdestiny.ngplus.NgScaling ngScaling = new wingsofdestiny.ngplus.NgScaling();
  private final wingsofdestiny.ngplus.ExAdditions exAdditions = new wingsofdestiny.ngplus.ExAdditions();
  private final wingsofdestiny.ngplus.DragoonMagic dragoonMagic = new wingsofdestiny.ngplus.DragoonMagic();
  private final wingsofdestiny.compat.LegacyModCleanup legacyCleanup = new wingsofdestiny.compat.LegacyModCleanup();
  /** Integrazione con la Lloyd mod (solo se installata). */
  private Object lloydCompat;

  /** EXP delle additions di ogni personaggio all'inizio della battaglia (indice personaggio -> addition -> xp). */
  private final Map<Integer, Map<RegistryId, Integer>> additionXpAtStart = new HashMap<>();

  public WingsOfDestiny() {
    GameEngine.EVENTS.register(this);
    GameEngine.EVENTS.register(this.monsterDifficulty);
    GameEngine.EVENTS.register(this.monsterHpDisplay);
    GameEngine.EVENTS.register(this.monsterRewards);
    GameEngine.EVENTS.register(this.additionTuning);
    GameEngine.EVENTS.register(this.dragoonMultipliers);
    GameEngine.EVENTS.register(this.equipment);
    GameEngine.EVENTS.register(this.items);
    GameEngine.EVENTS.register(this.archangelsPrayer);
    GameEngine.EVENTS.register(this.shops);
    GameEngine.EVENTS.register(this.equipmentBuffs);
    GameEngine.EVENTS.register(this.challenge);
    GameEngine.EVENTS.register(this.forgedWeapons);
    GameEngine.EVENTS.register(this.forgeShop);
    GameEngine.EVENTS.register(this.arena);
    GameEngine.EVENTS.register(this.arenaShop);
    GameEngine.EVENTS.register(this.newGamePlus);
    GameEngine.EVENTS.register(this.ngScaling);
    GameEngine.EVENTS.register(this.exAdditions);
    GameEngine.EVENTS.register(this.dragoonMagic);
    GameEngine.EVENTS.register(this.legacyCleanup);
  }

  /** La presenza della Lloyd mod si controlla qui, quando l'elenco delle mod e' pronto. */
  @EventListener
  public void addRegistry(final legend.core.AddRegistryEvent event) {
    if(this.lloydCompat == null && GameEngine.MODS.isLoaded("lloyd_mod")) {
      try {
        this.lloydCompat = Class.forName("wingsofdestiny.compat.LloydCompat").getConstructor().newInstance();
        GameEngine.EVENTS.register(this.lloydCompat);
        org.apache.logging.log4j.LogManager.getLogger(WingsOfDestiny.class).info("Wings of Destiny: integrazione Lloyd attiva");
      } catch(final ReflectiveOperationException | LinkageError e) {
        org.apache.logging.log4j.LogManager.getLogger(WingsOfDestiny.class).error("Wings of Destiny: integrazione Lloyd non attiva", e);
      }
    }
  }

  public static RegistryId id(final String entryId) {
    return new RegistryId(MOD_ID, entryId);
  }

  @EventListener
  public void registerConfigs(final ConfigRegistryEvent event) {
    WodConfigs.register(event);
  }

  // --- Moltiplicatori di EXP e oro ---

  /** Moltiplicatori delle opzioni: dopo la tabella delle ricompense (priorita' LOW). */
  @EventListener(priority = Priority.LOW)
  public void enemyRewards(final EnemyRewardsEvent event) {
    event.xp = Math.round(event.xp * WodConfigs.xpMultiplier());
    event.gold = Math.round(event.gold * WodConfigs.goldMultiplier());
  }

  // --- Moltiplicatore EXP additions ---

  @EventListener
  public void battleStarted(final BattleStartedEvent event) {
    this.additionXpAtStart.clear();
    final List<CharacterData2c> characters = gameState_800babc8.charData_32c;

    for(int i = 0; i < characters.size(); i++) {
      final CharacterData2c character = characters.get(i);
      final Map<RegistryId, Integer> xp = new HashMap<>();

      for(final RegistryId addition : character.getAllAdditions()) {
        xp.put(addition, character.getAdditionInfo(addition).xp);
      }

      this.additionXpAtStart.put(i, xp);
    }
  }

  @EventListener
  public void battleEnded(final BattleEndedEvent event) {
    final int multiplier = WodConfigs.additionXpMultiplier();

    if(multiplier > 1) {
      final List<CharacterData2c> characters = gameState_800babc8.charData_32c;

      for(final Map.Entry<Integer, Map<RegistryId, Integer>> entry : this.additionXpAtStart.entrySet()) {
        if(entry.getKey() >= characters.size()) {
          continue;
        }

        final CharacterData2c character = characters.get(entry.getKey());

        for(final Map.Entry<RegistryId, Integer> addition : entry.getValue().entrySet()) {
          final CharacterAdditionInfo info = character.getAdditionInfo(addition.getKey());

          if(info == null) {
            continue;
          }

          final int gained = info.xp - addition.getValue();

          if(gained > 0) {
            info.xp += gained * (multiplier - 1);
          }
        }
      }
    }

    this.additionXpAtStart.clear();
  }
}
