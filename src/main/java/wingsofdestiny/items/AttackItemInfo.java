package wingsofdestiny.items;

import legend.core.GameEngine;
import legend.game.modding.events.battle.BattleStartedEvent;
import legend.game.modding.events.gamestate.GameLoadedEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import org.legendofdragoon.modloader.events.EventListener;

import java.util.Map;

import static java.util.Map.entry;

/**
 * Oggetti d'attacco: nella descrizione in battaglia compare il moltiplicatore di danno dell'oggetto
 * (il danno parte da livello e attacco magico di chi lo lancia, come le magie).
 */
public class AttackItemInfo {
  private static final Map<String, String> DESCRIPTIONS = Map.ofEntries(
    entry("lod.items.black_rain.battle_description", "A attack(dark, multi). x1"),
    entry("lod.items.burn_out.battle_description", "S attack(flame, multi). x1.5"),
    entry("lod.items.burning_wave.battle_description", "Powerful A attack(Flame). x3"),
    entry("lod.items.dancing_ray.battle_description", "A attack(light, multi). x1"),
    entry("lod.items.dark_mist.battle_description", "S attack(dark, multi). x1.5"),
    entry("lod.items.detonate_rock.battle_description", "Detonates & attacks all. x1"),
    entry("lod.items.down_burst.battle_description", "Powerful A attack(Wind). x3"),
    entry("lod.items.fatal_blizzard.battle_description", "A attack(water, multi). x1"),
    entry("lod.items.flash_hall.battle_description", "Powerful A attack(Thndr). x3"),
    entry("lod.items.frozen_jet.battle_description", "Powerful A attack(Water). x3"),
    entry("lod.items.gravity_grabber.battle_description", "Powerful A attack(Earth). x3"),
    entry("lod.items.gushing_magma.battle_description", "A attack(flame, multi). x1"),
    entry("lod.items.meteor_fall.battle_description", "A attack(earth, multi). x1"),
    entry("lod.items.night_raid.battle_description", "Powerful A attack(Dark). x3"),
    entry("lod.items.pellet.battle_description", "S attack(earth, multi). x1.5"),
    entry("lod.items.psyche_bomb.battle_description", "A attack(non, multi). x4"),
    entry("lod.items.rave_twister.battle_description", "A attack (wind, multi). x1"),
    entry("lod.items.spark_net.battle_description", "S attack(thunder, multi). x1.5"),
    entry("lod.items.spear_frost.battle_description", "S attack(water, multi). x1.5"),
    entry("lod.items.spectral_flash.battle_description", "Powerful A attack(Light). x3"),
    entry("lod.items.spinning_gale.battle_description", "S attack(wind, multi). x1.5"),
    entry("lod.items.thunderbolt.battle_description", "A attack(thndr, multi). x1"),
    entry("lod.items.trans_light.battle_description", "S attack(light, multi). x1.5"),
    entry("lod.items.psyche_bomb_x.battle_description", "A attack (non, multi, rep). x2")
  );

  @EventListener
  public void newGame(final NewGameEvent event) {
    GameEngine.addLangOverrides(DESCRIPTIONS);
  }

  @EventListener
  public void gameLoaded(final GameLoadedEvent event) {
    GameEngine.addLangOverrides(DESCRIPTIONS);
  }

  @EventListener
  public void battleStarted(final BattleStartedEvent event) {
    GameEngine.addLangOverrides(DESCRIPTIONS);
  }
}
