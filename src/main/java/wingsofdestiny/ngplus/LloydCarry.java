package wingsofdestiny.ngplus;

import legend.core.GameEngine;
import legend.core.tags.MapTag;
import legend.core.tags.Tag;
import legend.game.EngineStates;
import legend.game.characters.CharacterData2c;
import legend.game.characters.CharacterTemplate;
import legend.game.modding.events.RenderEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import legend.game.saves.ReadSaveDataEvent;
import legend.game.saves.WriteSaveDataEvent;
import legend.game.submap.SMap;
import legend.game.types.GameState52c;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.WingsOfDestiny;

import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;

/**
 * New Game+ e Lloyd mod: Lloyd non viene portato nella partita nuova, ma i suoi dati si conservano nei dati della mod.
 * Quando Lloyd compare (storia o opzione della sua mod) gli si ridanno, una volta sola: livello, EXP, livello Dragoon,
 * magie, additions ed equipaggiamento indossato. Lo stato nel party resta quello con cui e' entrato.
 * Senza la Lloyd mod i dati restano li', inutilizzati.
 */
public class LloydCarry {
  private static final Logger LOGGER = LogManager.getFormatterLogger(LloydCarry.class);
  private static final RegistryId LLOYD = new RegistryId("lloyd_mod", "lloyd");
  private static final RegistryId SAVE_ID = new RegistryId(WingsOfDestiny.MOD_ID, "ng_lloyd");

  /** Dati di Lloyd in attesa nella partita in corso. */
  private static MapTag pending;
  /** Dati da scrivere nel save New Game+ che si sta creando. */
  private static MapTag writing;
  private static boolean writingActive;

  /** Prima di creare il save New Game+: i dati di Lloyd della partita finita, o quelli ancora in attesa. */
  static void prepare(final GameState52c old) {
    writingActive = true;
    writing = pending;
    for(final CharacterData2c character : old.charData_32c) {
      if(LLOYD.equals(character.template.getRegistryId())) {
        final MapTag tag = new MapTag();
        character.template.serialize(character, tag);
        writing = tag;
        LOGGER.info("Wings of Destiny: dati di Lloyd conservati per il New Game+ (livello %d)", character.level_12);
        break;
      }
    }
  }

  static void done() {
    writingActive = false;
    writing = null;
  }

  @EventListener
  public void newGame(final NewGameEvent event) {
    pending = null;
  }

  @EventListener
  public void readSave(final ReadSaveDataEvent event) {
    final Tag tag = event.get(SAVE_ID);
    pending = tag instanceof final MapTag map ? map : null;
  }

  @EventListener
  public void writeSave(final WriteSaveDataEvent event) {
    final MapTag tag = writingActive ? writing : pending;
    if(tag != null) {
      event.add(SAVE_ID, tag);
    }
  }

  /** Fuori dalla battaglia: se Lloyd e' comparso, gli ridiamo i suoi dati. */
  @EventListener
  public void render(final RenderEvent event) {
    if(pending == null || gameState_800babc8 == null || !(EngineStates.currentEngineState_8004dd04 instanceof SMap)) {
      return;
    }
    for(final CharacterData2c lloyd : gameState_800babc8.charData_32c) {
      if(!LLOYD.equals(lloyd.template.getRegistryId())) {
        continue;
      }
      try {
        final CharacterTemplate template = GameEngine.REGISTRIES.characterTemplates.getEntry(LLOYD).get();
        final CharacterData2c saved = template.deserialize(pending).make(gameState_800babc8);
        final int partyFlags = lloyd.partyFlags_04;
        lloyd.set(saved);
        lloyd.partyFlags_04 = partyFlags;
        lloyd.stats.getStat(legend.lodmod.LodMod.HP_STAT.get()).restore();
        lloyd.stats.getStat(legend.lodmod.LodMod.MP_STAT.get()).restore();
        LOGGER.info("Wings of Destiny: Lloyd riprende i suoi dati (livello %d)", lloyd.level_12);
      } catch(final RuntimeException e) {
        LOGGER.error("Wings of Destiny: impossibile ridare i dati a Lloyd", e);
      }
      pending = null;
      return;
    }
  }
}
