package wingsofdestiny.compat;

import legend.game.modding.coremod.CoreMod;
import legend.game.modding.events.gamestate.GameLoadedEvent;
import legend.game.saves.ConfigStorage;
import legend.game.saves.ConfigStorageLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;

import java.util.Arrays;
import java.util.Set;

import static legend.core.GameEngine.CONFIG;

/**
 * La mod si chiamava "supermod": le campagne create allora la ricordano tra le mod attive e,
 * non trovandola, chiedono "Missing mods, continue?" a ogni avvio. La togliamo dall'elenco della campagna.
 */
public class LegacyModCleanup {
  private static final Logger LOGGER = LogManager.getFormatterLogger(LegacyModCleanup.class);
  private static final Set<String> LEGACY = Set.of("supermod");

  @EventListener
  public void gameLoaded(final GameLoadedEvent event) {
    final boolean changedEnabled = clean(CoreMod.ENABLED_MODS_CONFIG.get());
    final boolean changedKnown = clean(CoreMod.KNOWN_MODS_CONFIG.get());

    // Qui il nuovo stato non e' ancora in gameState_800babc8: si usa quello dell'evento
    if((changedEnabled || changedKnown) && event.gameState != null && event.gameState.campaign != null) {
      ConfigStorage.saveConfig(CONFIG, ConfigStorageLocation.CAMPAIGN, event.gameState.campaign.path.resolve("campaign_config.dcnf"));
      LOGGER.info("Wings of Destiny: rimossa la vecchia 'supermod' dalle mod della campagna");
    }
  }

  private static boolean clean(final legend.game.modding.coremod.config.EnabledModsConfigEntry entry) {
    if(!CONFIG.hasConfig(entry)) {
      return false;
    }
    final String[] mods = CONFIG.getConfig(entry);
    final String[] cleaned = Arrays.stream(mods).filter(m -> !LEGACY.contains(m)).toArray(String[]::new);
    if(cleaned.length == mods.length) {
      return false;
    }
    CONFIG.setConfig(entry, cleaned);
    return true;
  }
}
