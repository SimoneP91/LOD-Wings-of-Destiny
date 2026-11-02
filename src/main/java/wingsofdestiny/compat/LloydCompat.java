package wingsofdestiny.compat;

import legend.core.GameEngine;
import legend.game.characters.CharacterData2c;
import legend.game.characters.CharacterSpellInfo;
import legend.game.characters.SpellDragoonLevelUnlockCriterion;
import legend.game.characters.SpellDragoonSpiritUnlockCriterion;
import legend.game.characters.VitalsStat;
import legend.game.modding.events.gamestate.GameLoadedEvent;
import lod.lloydmod.LloydSpells;
import java.util.List;
import legend.game.inventory.Equipment;
import legend.game.inventory.GoodsSource;
import legend.game.modding.events.gamestate.NewGameEvent;
import legend.game.types.EquipmentSlot;
import legend.game.types.GameState52c;
import legend.lodmod.LodMod;
import lod.lloydmod.LloydMod;
import lod.lloydmod.events.LloydJoinedPartyEvent;
import lod.lloydmod.events.LloydVersionEvent;
import lod.lloydmod.stats.LloydTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;

import static legend.game.Scus94491BpeSegment_8005.submapCut_80052c30;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;

/**
 * Lloyd mod: attiva la modalita' con le magie in forma Dragoon (Running Slash, Spectral Sword...).
 * - rispondiamo "si'" alla domanda della Lloyd mod sulla modalita' (campo draModVersion, nome dell'API della Lloyd mod);
 * - quando Lloyd entra nel party lo aggiungiamo noi con equipaggiamento retail
 *   (in quella modalita' la Lloyd mod cercherebbe oggetti che qui non esistono).
 * Caricata solo se la Lloyd mod e' installata.
 */
public class LloydCompat {
  private static final Logger LOGGER = LogManager.getFormatterLogger(LloydCompat.class);

  /** Durante la creazione di una nuova partita la Lloyd mod equipaggia un Lloyd "nascosto": li' deve usare gli oggetti retail. */
  private boolean inNewGame;

  @EventListener(priority = Priority.HIGHEST)
  public void version(final LloydVersionEvent event) {
    event.draModVersion = !this.inNewGame;
  }

  @EventListener(priority = Priority.HIGHEST)
  public void newGameStart(final NewGameEvent event) {
    this.inNewGame = true;
  }

  @EventListener(priority = Priority.LOWEST)
  public void newGameEnd(final NewGameEvent event) {
    this.inNewGame = false;
    LloydMod.DRAMOD_VERSION = true;
    applySpellsToAll(event.gameState);
  }

  @EventListener(priority = Priority.LOWEST)
  public void gameLoaded(final GameLoadedEvent event) {
    // Attenzione: qui il gioco non ha ancora messo il nuovo stato in gameState_800babc8, va usato quello dell'evento
    applySpellsToAll(event.gameState);
  }

  private static void applySpellsToAll(final GameState52c state) {
    if(state == null) {
      return;
    }
    for(final CharacterData2c character : state.charData_32c) {
      if(character.template instanceof LloydTemplate) {
        applySpells(character, state.timestamp_a0);
      }
    }
  }

  /**
   * Le magie di Lloyd per livello Dragoon (la Lloyd mod da sola non le assegna):
   * D1 Running Slash, Spectral Sword, Sword Rush, Spinning Gale - D2 Fatal Blizzard, Aerial Crash -
   * D3 Ball Barrage, Trans Light - D4 Rave Twister - D5 Sigil of Explosion - D6 Dancing Ray - D7 Draconic Impale.
   */
  private static void applySpells(final CharacterData2c character, final int ts) {
    character.getAllSpells().clear();
    add(character, LloydSpells.RUNNING_SLASH.getId(), 1, ts);
    add(character, LloydSpells.SPECTRAL_SWORD.getId(), 1, ts + 1);
    add(character, LloydSpells.SWORD_RUSH.getId(), 1, ts + 2);
    add(character, LloydSpells.SPINNING_GALE.getId(), 1, ts + 3);
    add(character, LloydSpells.FATAL_BLIZZARD.getId(), 2, ts + 4);
    add(character, LloydSpells.AERIAL_CRASH.getId(), 2, ts + 5);
    add(character, LloydSpells.BALL_BARRAGE.getId(), 3, ts + 6);
    add(character, LloydSpells.TRANS_LIGHT.getId(), 3, ts + 7);
    add(character, LloydSpells.RAVE_TWISTER.getId(), 4, ts + 8);
    add(character, LloydSpells.SIGIL_OF_EXPLOSION.getId(), 5, ts + 9);
    add(character, LloydSpells.DANCING_RAY.getId(), 6, ts + 10);
    add(character, LloydSpells.DRACONIC_IMPALE.getId(), 7, ts + 11);
  }

  private static void add(final CharacterData2c character, final org.legendofdragoon.modloader.registries.RegistryId id, final int dlevel, final int timestamp) {
    final CharacterSpellInfo info = dlevel <= 1
      ? new CharacterSpellInfo(List.of(new SpellDragoonSpiritUnlockCriterion()))
      : new CharacterSpellInfo(List.of(new SpellDragoonSpiritUnlockCriterion(), new SpellDragoonLevelUnlockCriterion(dlevel)));
    character.addSpell(id, info);
    if(dlevel <= 1 || info.checkUnlock(character)) {
      info.unlock(timestamp);
    }
  }

  @EventListener(priority = Priority.HIGHEST)
  public void joined(final LloydJoinedPartyEvent event) {
    try {
      for(int i = gameState_800babc8.charData_32c.size() - 1; i >= 0; i--) {
        if(gameState_800babc8.charData_32c.get(i).template instanceof LloydTemplate) {
          gameState_800babc8.charData_32c.remove(i);
        }
      }

      gameState_800babc8.goods_19c.give(LloydMod.CHAOS_DIVINE_DRAGOON_SPIRIT.get(), GoodsSource.GAMEPLAY);
      final CharacterData2c lloyd = LloydMod.LLOYD.get().make(gameState_800babc8);
      lloyd.partyFlags_04 = 3;
      gameState_800babc8.addCharacter(lloyd);
      equip(lloyd, EquipmentSlot.WEAPON, "broad_sword");
      equip(lloyd, EquipmentSlot.HELMET, "bandana");
      equip(lloyd, EquipmentSlot.ARMOUR, "leather_armor");
      equip(lloyd, EquipmentSlot.BOOTS, "leather_boots");
      equip(lloyd, EquipmentSlot.ACCESSORY, "bracelet");

      if(submapCut_80052c30 == 441) {
        levelUp(lloyd, 27);
      }

      applySpells(lloyd, gameState_800babc8.timestamp_a0);
      event.override = true;
      LOGGER.info("Wings of Destiny: Lloyd entrato nel party (versione con magie Dragoon)");
    } catch(final RuntimeException e) {
      LOGGER.error("Wings of Destiny: errore aggiungendo Lloyd, lascio fare alla Lloyd mod", e);
    }
  }

  /** Rete di sicurezza: se Lloyd non ha magie sbloccate, niente menu magie (la Lloyd mod andrebbe in crash con la lista vuota). */
  @EventListener(priority = Priority.LOWEST)
  public void battleActions(final legend.game.combat.ui.GatherBattleActionsEvent event) {
    if(!(event.player.character.template instanceof LloydTemplate)) {
      return;
    }
    boolean hasMagic = false;
    for(final org.legendofdragoon.modloader.registries.RegistryId id : event.player.character.getUnlockedSpells()) {
      if(GameEngine.REGISTRIES.spells.hasEntry(id) && GameEngine.REGISTRIES.spells.getEntry(id).get().targetType_00 == 0) {
        hasMagic = true;
        break;
      }
    }
    if(!hasMagic) {
      event.actions.keySet().removeIf(action -> action == LloydMod.LLOYD_DRAGOON_SPELLS.get());
    }
  }

  private static void equip(final CharacterData2c character, final EquipmentSlot slot, final String id) {
    final Equipment equipment = GameEngine.REGISTRIES.equipment.getEntry("lod", id).get();
    character.equip(slot, equipment);
  }

  private static void levelUp(final CharacterData2c character, final int level) {
    while(character.level_12 < level - 1) {
      character.applyLevelUp(null);
    }
    character.xp_00 = character.getXpToNextLevel();
    character.applyLevelUp(null);
    ((VitalsStat)character.stats.getStat(LodMod.HP_STAT.get())).restore();
    ((VitalsStat)character.stats.getStat(LodMod.MP_STAT.get())).restore();
  }
}
