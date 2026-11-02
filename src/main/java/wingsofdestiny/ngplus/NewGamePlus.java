package wingsofdestiny.ngplus;

import legend.core.GameEngine;
import legend.core.tags.ListTag;
import legend.core.tags.StringTag;
import legend.game.EngineStates;
import legend.game.Text;
import legend.game.characters.CharacterAdditionInfo;
import legend.game.characters.CharacterData2c;
import legend.game.characters.FractionalStat;
import legend.game.characters.Stat;
import legend.game.characters.StatType;
import legend.game.characters.UnaryStat;
import legend.lodmod.LodMod;
import org.legendofdragoon.modloader.registries.RegistryDelegate;
import java.util.List;
import legend.game.inventory.ItemStack;
import legend.game.inventory.WhichMenu;
import legend.game.inventory.screens.FontOptions;
import legend.game.inventory.screens.HorizontalAlign;
import legend.game.inventory.screens.TextColour;
import legend.game.modding.events.RenderEvent;
import legend.game.modding.events.characters.CloneCharacterEvent;
import legend.game.modding.events.gamestate.GameLoadedEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import legend.game.saves.ReadSaveDataEvent;
import legend.game.saves.SaveFailedException;
import legend.game.saves.WriteSaveDataEvent;
import legend.game.submap.SMap;
import legend.game.types.GameState52c;
import legend.lodmod.LodEngineStateTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.WingsOfDestiny;
import wingsofdestiny.arena.Arena;

import static legend.game.Menus.whichMenu_800bdc38;
import static legend.game.Scus94491BpeSegment_8005.collidedPrimitiveIndex_80052c38;
import static legend.game.Scus94491BpeSegment_8005.submapCut_80052c30;
import static legend.game.Scus94491BpeSegment_800b.campaignType;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static legend.game.Scus94491BpeSegment_800b.submapId_800bd808;

/**
 * New Game+.
 * - Sulla schermata "The End" crea un salvataggio "New Game+ N" che riparte dall'inizio del disco 1.
 * - Si tengono livelli, EXP, livelli Dragoon, additions, magie, equipaggiamento e oggetti.
 *   Si azzerano oro, oggetti di trama (Dragoon Spirit compresi), trama e Arena.
 * - Lavitz/Albert e Shana/Miranda: quando uno prende il posto dell'altro vince il piu' forte, voce per voce;
 *   l'equipaggiamento resta di ciascuno.
 * - Additions EX a catena (ExAdditions), magie Dragoon +30% per ciclo (DragoonMagic),
 *   mostri piu' forti (NgScaling, tabella Hard, elite e zone maledette +10%).
 */
public class NewGamePlus {
  private static final Logger LOGGER = LogManager.getFormatterLogger(NewGamePlus.class);
  private static final RegistryId SAVE_ID = new RegistryId(WingsOfDestiny.MOD_ID, "ng_cycle");
  private static final RegistryId SAVE_EX = new RegistryId(WingsOfDestiny.MOD_ID, "ng_ex_additions");

  /** Schermata finale dopo i crediti. */
  private static final int THE_END_CUT = 673;
  /** Prima schermata di una partita nuova, e la sua area ("Intro Forest", per il nome del save). */
  private static final int OPENING_CUT = 675;
  private static final int OPENING_SUBMAP = 57;

  private static final int LAVITZ = 1, SHANA = 2, ALBERT = 5, MIRANDA = 8;
  /** Momento di sblocco delle EX gia' pronte all'inizio: dopo le additions rinumerate, prima di qualunque sblocco in gioco. */
  private static final int EX_UNLOCK_AT_START = 100;

  /** 0 = prima partita, 1 = New Game+, 2 = New Game++... */
  private static int cycle;
  /** Valore da scrivere nel save che si sta creando (-1 = quello corrente). */
  private static int writingCycle = -1;
  /** Partita che si sta salvando quando non e' quella in corso (il save New Game+). */
  private static GameState52c writingState;

  private boolean savedAtTheEnd;
  private int messageFrames;
  private String message;

  private final FontOptions font = new FontOptions().colour(TextColour.WHITE).horizontalAlign(HorizontalAlign.CENTRE);

  public static int cycle() {
    return cycle;
  }

  public static boolean active() {
    return cycle > 0;
  }

  @EventListener
  public void newGame(final NewGameEvent event) {
    cycle = 0;
  }

  /** Arriva prima dei dati della mod: un save senza dati della mod e' una prima partita. */
  @EventListener
  public void gameLoaded(final GameLoadedEvent event) {
    cycle = 0;
  }

  @EventListener
  public void readSave(final ReadSaveDataEvent event) {
    cycle = 0;
    final legend.core.tags.Tag tag = event.get(SAVE_ID);
    if(tag != null && !tag.asList().isEmpty()) {
      try {
        cycle = Integer.parseInt(tag.asList().get(0).asString().get());
      } catch(final RuntimeException e) {
        LOGGER.warn("Wings of Destiny: ciclo New Game+ non leggibile dal save");
      }
    }

    final GameState52c state = gameState_800babc8;
    if(state == null) {
      return;
    }
    if(active()) {
      ExAdditions.read(state, event.get(SAVE_EX));
      ExAdditions.grant(state, state.timestamp_a0);
    }
    ExAdditions.fixSelection(state);
  }

  @EventListener
  public void writeSave(final WriteSaveDataEvent event) {
    final ListTag tag = new ListTag();
    tag.add(new StringTag(Integer.toString(writingCycle >= 0 ? writingCycle : cycle)));
    event.add(SAVE_ID, tag);

    final GameState52c state = writingState != null ? writingState : gameState_800babc8;
    if(state != null) {
      event.add(SAVE_EX, ExAdditions.write(state));
    }
  }

  @EventListener
  public void render(final RenderEvent event) {
    if(!(EngineStates.currentEngineState_8004dd04 instanceof final SMap smap) || gameState_800babc8 == null) {
      return;
    }

    if(submapCut_80052c30 != THE_END_CUT) {
      this.savedAtTheEnd = false;
    } else if(!this.savedAtTheEnd && smap.submap != null && whichMenu_800bdc38 == WhichMenu.NONE_0) {
      this.savedAtTheEnd = true;
      this.createSave(gameState_800babc8);
    }

    if(this.messageFrames > 0) {
      this.messageFrames--;
      Text.renderText(this.message, 160.0f, 200.0f, this.font);
    }
  }

  /** In NG+ Albert e Miranda non copiano i dati di Lavitz e Shana: entrano con i loro, migliorati dove l'altro e' piu' avanti. */
  @EventListener
  public void cloneCharacter(final CloneCharacterEvent event) {
    if(!active()) {
      return;
    }
    event.cancel();
    final CharacterData2c source = event.sourceCharacter;
    final CharacterData2c dest = event.destinationCharacter;
    takeBest(dest, source);
    dest.partyFlags_04 = source.partyFlags_04;
    dest.stats.getStat(legend.lodmod.LodMod.HP_STAT.get()).restore();
    dest.stats.getStat(legend.lodmod.LodMod.MP_STAT.get()).restore();
    LOGGER.info("Wings of Destiny: NG+, %s entra con i suoi dati (livello %d)", dest.template.getRegistryId(), dest.level_12);
  }

  private void createSave(final GameState52c old) {
    final int next = cycle + 1;
    final String name = next == 1 ? "New Game+" : "New Game+ " + next;

    final GameState52c state = new GameState52c();
    state.campaign = old.campaign;
    campaignType.get().setUpNewCampaign(state);

    // Personaggi: tutto tranne lo stato nel party (riparte solo Dart) e gli stati alterati
    final int count = Math.min(state.charData_32c.size(), old.charData_32c.size());
    for(int i = 0; i < count; i++) {
      final CharacterData2c fresh = state.charData_32c.get(i);
      final int partyFlags = fresh.partyFlags_04;
      fresh.set(old.charData_32c.get(i));
      fresh.partyFlags_04 = partyFlags;
      fresh.status_10 = 0;
      fresh.stats.getStat(legend.lodmod.LodMod.HP_STAT.get()).restore();
      fresh.stats.getStat(legend.lodmod.LodMod.MP_STAT.get()).restore();
    }

    // Lavitz e Shana ripartono almeno al livello di Albert e Miranda
    if(count > MIRANDA) {
      takeBest(state.charData_32c.get(LAVITZ), state.charData_32c.get(ALBERT));
      takeBest(state.charData_32c.get(SHANA), state.charData_32c.get(MIRANDA));
    }

    // Additions: i momenti di sblocco ripartono da 0 nello stesso ordine (il tempo di gioco riparte da 0),
    // poi le EX: la prima e' gia' sbloccata se tutte le originali sono padroneggiate, la seconda se la prima e' al 10.
    for(final CharacterData2c character : state.charData_32c) {
      ExAdditions.renumberUnlocks(character);
    }
    ExAdditions.grant(state, EX_UNLOCK_AT_START);

    // Borsa: oggetti ed equipaggiamento non indossato. L'oro resta quello di una partita nuova.
    state.items_2e9.clear();
    for(final ItemStack stack : old.items_2e9) {
      state.items_2e9.give(new ItemStack(stack), true);
    }
    state.equipment_1e8.clear();
    state.equipment_1e8.addAll(old.equipment_1e8);

    // L'Arena riparte da zero
    Arena.resetForNewGamePlus();

    // Il save deve partire dalla prima schermata, come quello di una partita nuova
    final int oldCut = submapCut_80052c30;
    final int oldScene = collidedPrimitiveIndex_80052c38;
    final int oldSubmap = submapId_800bd808;
    submapCut_80052c30 = OPENING_CUT;
    collidedPrimitiveIndex_80052c38 = 0;
    submapId_800bd808 = OPENING_SUBMAP;
    writingCycle = next;
    writingState = state;
    try {
      GameEngine.SAVES.newSave(name, campaignType.get(), LodEngineStateTypes.SUBMAP.get().constructor_00.get(), state);
      this.message = "Saved \"" + name + "\".\nLoad it to start a New Game+.";
      LOGGER.info("Wings of Destiny: creato il save %s (ciclo %d)", name, next);
    } catch(final SaveFailedException | RuntimeException e) {
      this.message = "Could not create the New Game+ save.";
      LOGGER.error("Wings of Destiny: impossibile creare il save New Game+", e);
    } finally {
      writingCycle = -1;
      writingState = null;
      submapCut_80052c30 = oldCut;
      collidedPrimitiveIndex_80052c38 = oldScene;
      submapId_800bd808 = oldSubmap;
    }
    this.messageFrames = 60 * 30;
  }

  /** Statistiche che crescono con il livello. */
  private static final List<RegistryDelegate<? extends StatType<?>>> LEVEL_STATS = List.of(
    LodMod.HP_STAT, LodMod.ATTACK_STAT, LodMod.DEFENSE_STAT, LodMod.MAGIC_ATTACK_STAT, LodMod.MAGIC_DEFENSE_STAT, LodMod.SPEED_STAT
  );
  /** Statistiche che crescono con il livello Dragoon. */
  private static final List<RegistryDelegate<? extends StatType<?>>> DRAGOON_STATS = List.of(
    LodMod.MP_STAT, LodMod.SP_STAT, LodMod.DRAGOON_ATTACK_STAT, LodMod.DRAGOON_DEFENSE_STAT, LodMod.DRAGOON_MAGIC_ATTACK_STAT, LodMod.DRAGOON_MAGIC_DEFENSE_STAT
  );

  /**
   * Porta "target" al livello di "other" dove "other" e' piu' avanti, voce per voce:
   * livello (EXP e statistiche del livello), livello Dragoon (EXP Dragoon e statistiche Dragoon), ogni addition.
   */
  static void takeBest(final CharacterData2c target, final CharacterData2c other) {
    if(other.xp_00 > target.xp_00) {
      target.xp_00 = other.xp_00;
      target.level_12 = other.level_12;
      copyStats(target, other, LEVEL_STATS);
    }
    if(other.dlevel_13 > target.dlevel_13 || other.dlevel_13 == target.dlevel_13 && other.dlevelXp_0e > target.dlevelXp_0e) {
      target.dlevel_13 = other.dlevel_13;
      target.dlevelXp_0e = other.dlevelXp_0e;
      copyStats(target, other, DRAGOON_STATS);
    }

    // Additions corrispondenti: "harpoon" <-> "albert_harpoon"
    for(final RegistryId id : target.getAllAdditions()) {
      final CharacterAdditionInfo mine = target.getAdditionInfo(id);
      final CharacterAdditionInfo theirs = counterpart(other, id);
      if(mine == null || theirs == null) {
        continue;
      }
      if(theirs.level > mine.level || theirs.level == mine.level && theirs.xp > mine.xp) {
        mine.level = theirs.level;
        mine.xp = theirs.xp;
      }
    }

    // Magie e additions legate a livello e livello Dragoon
    target.template.checkUnlocks(target, null);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static void copyStats(final CharacterData2c target, final CharacterData2c other, final List<RegistryDelegate<? extends StatType<?>>> types) {
    for(final RegistryDelegate<? extends StatType<?>> type : types) {
      final Stat mine = target.stats.getStat((StatType)type.get());
      final Stat theirs = other.stats.getStat((StatType)type.get());
      if(mine instanceof final FractionalStat m && theirs instanceof final FractionalStat t) {
        m.set(t);
      } else if(mine instanceof final UnaryStat m && theirs instanceof final UnaryStat t) {
        m.set(t);
      }
    }
  }

  private static CharacterAdditionInfo counterpart(final CharacterData2c other, final RegistryId id) {
    final String entry = id.entryId();
    final String alt = entry.startsWith("albert_") ? entry.substring("albert_".length()) : "albert_" + entry;
    final RegistryId altId = new RegistryId(id.modId(), alt);
    if(other.getAllAdditions().contains(altId)) {
      return other.getAdditionInfo(altId);
    }
    return other.getAllAdditions().contains(id) ? other.getAdditionInfo(id) : null;
  }
}
