package wingsofdestiny.config;

import legend.core.GameEngine;
import legend.game.saves.BoolConfigEntry;
import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigEntry;
import legend.game.saves.ConfigRegistryEvent;
import legend.game.saves.ConfigStorageLocation;
import legend.game.saves.EnumConfigEntry;
import legend.game.saves.FloatConfigEntry;
import legend.game.saves.IntConfigEntry;
import org.legendofdragoon.modloader.registries.Registrar;
import org.legendofdragoon.modloader.registries.RegistryDelegate;
import wingsofdestiny.WingsOfDestiny;

/** Opzioni della Wings of Destiny (Opzioni -> Gameplay), salvate per partita. */
public final class WodConfigs {
  private static final Registrar<ConfigEntry<?>, ConfigRegistryEvent> REGISTRAR = new Registrar<>(GameEngine.REGISTRIES.config, WingsOfDestiny.MOD_ID);

  public static final RegistryDelegate<DifficultyConfig> DIFFICULTY = REGISTRAR.register("difficulty", DifficultyConfig::new);
  public static final RegistryDelegate<MultiplierConfig> XP_MULTIPLIER = REGISTRAR.register("xp_multiplier", MultiplierConfig::new);
  public static final RegistryDelegate<MultiplierConfig> GOLD_MULTIPLIER = REGISTRAR.register("gold_multiplier", MultiplierConfig::new);
  public static final RegistryDelegate<AdditionXpConfig> ADDITION_XP_MULTIPLIER = REGISTRAR.register("addition_xp_multiplier", AdditionXpConfig::new);
  public static final RegistryDelegate<BoolConfigEntry> MONSTER_HP_NUMBERS = REGISTRAR.register("monster_hp_numbers", () -> new BoolConfigEntry(true, ConfigStorageLocation.GLOBAL, ConfigCategory.GAMEPLAY));

  public static final RegistryDelegate<ChanceConfig> ELITE_CHANCE = REGISTRAR.register("elite_chance", () -> new ChanceConfig(8, 30));
  public static final RegistryDelegate<BoolConfigEntry> CURSED_ZONES = REGISTRAR.register("cursed_zones", () -> new BoolConfigEntry(true, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY) {
    @Override
    public boolean hasHelp() {
      return true;
    }
  });
  public static final RegistryDelegate<ChanceConfig> CURSED_CHANCE = REGISTRAR.register("cursed_chance", () -> new ChanceConfig(20, 50));


  private WodConfigs() { }

  public static void register(final ConfigRegistryEvent event) {
    REGISTRAR.registryEvent(event);
  }

  public static Difficulty difficulty() {
    return GameEngine.CONFIG.getConfig(DIFFICULTY.get());
  }

  public static float xpMultiplier() {
    return GameEngine.CONFIG.getConfig(XP_MULTIPLIER.get());
  }

  public static float goldMultiplier() {
    return GameEngine.CONFIG.getConfig(GOLD_MULTIPLIER.get());
  }

  public static int additionXpMultiplier() {
    return GameEngine.CONFIG.getConfig(ADDITION_XP_MULTIPLIER.get());
  }

  public static boolean monsterHpNumbers() {
    return GameEngine.CONFIG.getConfig(MONSTER_HP_NUMBERS.get());
  }

  public static int eliteChance() {
    return GameEngine.CONFIG.getConfig(ELITE_CHANCE.get());
  }

  public static boolean cursedZones() {
    return GameEngine.CONFIG.getConfig(CURSED_ZONES.get());
  }

  public static int cursedChance() {
    return GameEngine.CONFIG.getConfig(CURSED_CHANCE.get());
  }

  /** Probabilita' in percentuale. */
  public static class ChanceConfig extends IntConfigEntry {
    public ChanceConfig(final int defaultValue, final int max) {
      super(defaultValue, 0, max, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY);
    }

    @Override
    public boolean hasHelp() {
      return true;
    }
  }

  public static class DifficultyConfig extends EnumConfigEntry<Difficulty> {
    public DifficultyConfig() {
      super(Difficulty.class, Difficulty.STANDARD, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY);
    }

    @Override
    public boolean hasHelp() {
      return true;
    }
  }

  /** Moltiplicatore 0-10 a passi di 0,25 (EXP e oro di fine battaglia). */
  public static class MultiplierConfig extends FloatConfigEntry {
    public MultiplierConfig() {
      super(1.0f, 0.25f, 1.0f, 0.0f, 10.0f, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY);
    }
  }

  /** Moltiplicatore dell'EXP delle additions, 1-100. */
  public static class AdditionXpConfig extends IntConfigEntry {
    public AdditionXpConfig() {
      super(1, 1, 100, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY);
    }

    @Override
    public boolean hasHelp() {
      return true;
    }
  }
}
