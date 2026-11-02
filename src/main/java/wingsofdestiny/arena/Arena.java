package wingsofdestiny.arena;

import legend.core.GameEngine;
import legend.core.tags.ListTag;
import legend.core.tags.StringTag;
import legend.core.tags.Tag;
import legend.game.EngineState;
import legend.game.EngineStates;
import legend.game.characters.CharacterData2c;
import legend.game.characters.VitalsStat;
import legend.game.combat.Battle;
import legend.game.combat.encounters.Encounter;
import legend.game.combat.encounters.EncounterRegistryEvent;
import legend.game.combat.postbattleactions.BossKillPostBattleActionInstance;
import legend.game.combat.postbattleactions.GameOverPostBattleActionInstance;
import legend.game.combat.postbattleactions.PostBattleAction;
import legend.game.combat.postbattleactions.RegisterPostBattleActionsEvent;
import legend.game.combat.postbattleactions.VictoryPostBattleActionInstance;
import legend.game.combat.types.CombatantStruct1a8;
import legend.game.modding.coremod.CorePostBattleActions;
import legend.game.modding.events.RenderEvent;
import legend.game.modding.events.battle.BattleEndedEvent;
import legend.game.modding.events.battle.BattleStartedEvent;
import legend.game.modding.events.battle.EnemyRewardsEvent;
import legend.game.modding.events.battle.MonsterStatsEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import legend.game.modding.events.submap.SubmapEncounterAccumulatorEvent;
import legend.game.modding.events.submap.SubmapEncounterEvent;
import legend.game.saves.ReadSaveDataEvent;
import legend.game.saves.WriteSaveDataEvent;
import legend.game.submap.SMap;
import legend.game.types.Flags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import org.legendofdragoon.modloader.registries.Registrar;
import org.legendofdragoon.modloader.registries.RegistryDelegate;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.WingsOfDestiny;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static legend.game.Scus94491BpeSegment_800b.battleStage_800bb0f4;
import static legend.game.Scus94491BpeSegment_800b.encounter;
import static legend.game.Scus94491BpeSegment_800b.equipmentOverflow;
import static legend.game.Scus94491BpeSegment_800b.itemsDroppedByEnemies_800bc928;
import static legend.game.Scus94491BpeSegment_8006.battleState_8006e398;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static legend.game.Scus94491BpeSegment_800b.postBattleAction_800bc974;
import static legend.game.Scus94491BpeSegment_800b.submapId_800bd808;
import static legend.lodmod.LodMod.HP_STAT;

/**
 * Arena.
 * - Ogni boss sconfitto (scontro "normale" con musica da boss) viene segnato nel save, col suo stage di battaglia.
 * - Nei negozi oggetti delle citta' principali compaiono le sfide (x1/x2/x3); pagata l'iscrizione, la battaglia parte appena ti muovi.
 * - In Arena: HP e attacco x grado, niente EXP, oro x grado, drop originali garantiti; 1 Dragon Stone solo al grado x3 (da Challenge).
 * - I flag di trama vengono salvati prima e ripristinati dopo, cosi' il boss non puo' far avanzare/rompere la storia.
 */
public class Arena {
  private static final Logger LOGGER = LogManager.getFormatterLogger(Arena.class);
  private static final RegistryId SAVE_ID = new RegistryId(WingsOfDestiny.MOD_ID, "arena_bosses");

  private static final Registrar<PostBattleAction<?, ?>, RegisterPostBattleActionsEvent> ACTIONS = new Registrar<>(GameEngine.REGISTRIES.postBattleActions, WingsOfDestiny.MOD_ID);
  public static final RegistryDelegate<ArenaLossAction> LOSS = ACTIONS.register("arena_loss", ArenaLossAction::new);

  /** Boss battuti: id dello scontro originale -> stage di battaglia. */
  static final Map<RegistryId, BossRecord> DEFEATED = new LinkedHashMap<>();

  /** Boss battuto nella storia: stage di battaglia, livello medio del party e disco in quel momento (-1 = non noto, vecchi save). */
  static final class BossRecord {
    final int stage;
    int level;
    int disc;

    BossRecord(final int stage, final int level, final int disc) {
      this.stage = stage;
      this.level = level;
      this.disc = disc;
    }

    @Override
    public String toString() {
      return this.stage + "|" + this.level + "|" + this.disc;
    }
  }

  /** Livello medio dei personaggi nel party attivo. */
  static int partyLevel() {
    if(gameState_800babc8 == null) {
      return 1;
    }
    int total = 0;
    int count = 0;
    for(int i = 0; i < gameState_800babc8.charIds_88.size(); i++) {
      final int id = gameState_800babc8.charIds_88.getInt(i);
      if(id >= 0 && id < gameState_800babc8.charData_32c.size()) {
        total += gameState_800babc8.charData_32c.get(id).level_12;
        count++;
      }
    }
    return count == 0 ? 1 : Math.max(1, Math.round((float)total / count));
  }

  static int disc() {
    return gameState_800babc8 == null ? 1 : gameState_800babc8.chapterIndex_98 + 1;
  }

  /** Forza del boss rispetto all'originale: quanto sei cresciuto da quando l'hai battuto (mai sotto 1). */
  static float levelScale(final BossRecord record) {
    if(record.level <= 0) {
      return 1.0f;
    }
    return Math.max(1.0f, (float)partyLevel() / record.level);
  }

  /** Boss gia' battuti al grado x3 e armi Heirloom gia' vinte (premio speciale). */
  private static final RegistryId SAVE_X3 = new RegistryId(WingsOfDestiny.MOD_ID, "arena_x3_cleared");
  private static final RegistryId SAVE_HEIRLOOMS = new RegistryId(WingsOfDestiny.MOD_ID, "arena_heirlooms");
  static final java.util.Set<RegistryId> X3_CLEARED = new java.util.LinkedHashSet<>();
  static final java.util.Set<RegistryId> HEIRLOOMS_AWARDED = new java.util.LinkedHashSet<>();
  private boolean rewardChecked;
  private RegistryId heirloomThisBattle;
  private final java.util.Random random = new java.util.Random();

  /** Sfida pagata, in attesa di partire. */
  private static ArenaEntry pending;

  // Battaglia d'Arena in corso
  private static ArenaEntry active;
  private Object activeBattle;
  private Flags savedFlags1;
  private Flags savedFlags2;
  private Flags savedWmapFlags;
  private boolean reviveParty;

  private static Field dropsField;

  private static int pendingArea = -1;

  static void setPending(final ArenaEntry entry) {
    pending = entry;
    pendingArea = submapId_800bd808;
  }

  static boolean hasPending() {
    return pending != null;
  }

  /** Boss "normali": scontro di classe base con musica da boss, esclusi gli scontri di prova/vuoti. */
  static boolean isArenaBoss(final RegistryId id, final Encounter enc) {
    final String name = id.entryId();
    return enc.getClass() == Encounter.class && enc.musicIndex >= 240
      && !name.contains("missing") && !name.contains("test") && !name.contains("dummy");
  }

  static RegistryId arenaId(final RegistryId original) {
    return new RegistryId(WingsOfDestiny.MOD_ID, "arena_" + original.entryId());
  }

  // ---------------------------------------------------------------- registri

  @EventListener
  public void registerActions(final RegisterPostBattleActionsEvent event) {
    ACTIONS.registryEvent(event);
  }

  /** Una copia "da Arena" per ogni boss, registrata dopo quelli del gioco. */
  @EventListener(priority = Priority.LOWEST)
  public void registerEncounters(final EncounterRegistryEvent event) {
    final List<RegistryId> ids = new ArrayList<>();
    for(final RegistryId id : GameEngine.REGISTRIES.encounters) {
      ids.add(id);
    }

    int count = 0;
    for(final RegistryId id : ids) {
      if(!"lod".equals(id.modId())) {
        continue;
      }
      final Encounter enc = GameEngine.REGISTRIES.encounters.getEntry(id).get();
      if(isArenaBoss(id, enc)) {
        event.register(arenaId(id), new ArenaEncounter(enc));
        count++;
      }
    }
    LOGGER.info("Wings of Destiny: %d boss disponibili per l'Arena", count);
  }

  // ---------------------------------------------------------------- save

  @EventListener
  public void newGame(final NewGameEvent event) {
    DEFEATED.clear();
    X3_CLEARED.clear();
    HEIRLOOMS_AWARDED.clear();
    pending = null;
  }

  @EventListener
  public void readSave(final ReadSaveDataEvent event) {
    DEFEATED.clear();
    X3_CLEARED.clear();
    HEIRLOOMS_AWARDED.clear();
    pending = null;
    readIds(event.get(SAVE_X3), X3_CLEARED);
    readIds(event.get(SAVE_HEIRLOOMS), HEIRLOOMS_AWARDED);
    final Tag tag = event.get(SAVE_ID);
    if(tag == null) {
      return;
    }
    for(final Tag t : tag.asList()) {
      final String[] parts = t.asString().get().split("\\|");
      try {
        final int level = parts.length > 2 ? Integer.parseInt(parts[2]) : -1;
        final int disc = parts.length > 3 ? Integer.parseInt(parts[3]) : -1;
        DEFEATED.put(new RegistryId(parts[0]), new BossRecord(Integer.parseInt(parts[1]), level, disc));
      } catch(final RuntimeException e) {
        LOGGER.warn("Wings of Destiny: voce arena non valida %s", t.asString().get());
      }
    }
  }

  @EventListener
  public void writeSave(final WriteSaveDataEvent event) {
    final ListTag list = new ListTag();
    for(final Map.Entry<RegistryId, BossRecord> e : DEFEATED.entrySet()) {
      list.add(new StringTag(e.getKey() + "|" + e.getValue()));
    }
    event.add(SAVE_ID, list);
    event.add(SAVE_X3, writeIds(X3_CLEARED));
    event.add(SAVE_HEIRLOOMS, writeIds(HEIRLOOMS_AWARDED));
  }

  private static void readIds(final Tag tag, final java.util.Set<RegistryId> into) {
    if(tag == null) {
      return;
    }
    for(final Tag t : tag.asList()) {
      into.add(new RegistryId(t.asString().get()));
    }
  }

  private static ListTag writeIds(final java.util.Set<RegistryId> ids) {
    final ListTag list = new ListTag();
    for(final RegistryId id : ids) {
      list.add(new StringTag(id.toString()));
    }
    return list;
  }

  // ---------------------------------------------------------------- avvio della sfida

  /** Con una sfida pagata, il prossimo controllo "incontro casuale" scatta subito (serve solo che tu ti muova). */
  @EventListener
  public void accumulator(final SubmapEncounterAccumulatorEvent event) {
    if(pending != null) {
      event.encounterAccumulatedStep = 100000.0f;
    }
  }

  @EventListener(priority = Priority.LOWEST)
  public void submapEncounter(final SubmapEncounterEvent event) {
    if(pending == null) {
      return;
    }
    final ArenaEntry entry = pending;

    final RegistryId id = arenaId(entry.boss);
    if(!GameEngine.REGISTRIES.encounters.hasEntry(id)) {
      LOGGER.error("Wings of Destiny: scontro d'arena %s mancante", id);
      return;
    }

    event.encounter = GameEngine.REGISTRIES.encounters.getEntry(id).get();
    event.battleStageId = entry.record.stage;
    // Vecchi save: livello e disco non salvati, li prendiamo adesso (alla prima sfida)
    if(entry.record.level <= 0) {
      entry.record.level = partyLevel();
    }
    if(entry.record.disc <= 0) {
      entry.record.disc = disc();
    }
    active = entry;
    LOGGER.info("Wings of Destiny: Arena %s x%d (stage %d)", entry.boss, entry.grade, entry.record.stage);
  }

  // ---------------------------------------------------------------- battaglia

  /** Grado della battaglia d'Arena in corso, 0 se non sei in Arena. */
  public static int activeGrade() {
    return inArena() ? active.grade : 0;
  }

  private static boolean inArena() {
    return active != null && encounter instanceof ArenaEncounter;
  }

  @EventListener
  public void battleStarted(final BattleStartedEvent event) {
    if(encounter instanceof ArenaEncounter) {
      pending = null;
    }
    if(!inArena()) {
      active = null;
      return;
    }
    this.activeBattle = event.battle;
    this.rewardChecked = false;
    this.heirloomThisBattle = null;
    this.savedFlags1 = copy(gameState_800babc8.scriptFlags1_13c, 8);
    this.savedFlags2 = copy(gameState_800babc8.scriptFlags2_bc, 32);
    this.savedWmapFlags = copy(gameState_800babc8.wmapFlags_15c, 8);
  }

  private static Flags copy(final Flags source, final int size) {
    final Flags flags = new Flags(size);
    flags.set(source);
    return flags;
  }

  @EventListener(priority = Priority.LOWEST)
  public void monsterStats(final MonsterStatsEvent event) {
    if(!inArena()) {
      return;
    }
    final float g = active.grade * levelScale(active.record);
    event.hp = Math.round(event.hp * g);
    event.maxHp = Math.round(event.maxHp * g);
    event.attack = Math.round(event.attack * g);
    event.magicAttack = Math.round(event.magicAttack * g);
  }

  @EventListener(priority = Priority.LOWEST)
  public void enemyRewards(final EnemyRewardsEvent event) {
    if(!inArena()) {
      return;
    }
    event.xp = 0;
    event.gold *= active.grade;

    final List<CombatantStruct1a8.ItemDrop> drops = drops(event);
    if(drops != null) {
      final List<CombatantStruct1a8.ItemDrop> sure = new ArrayList<>();
      for(final CombatantStruct1a8.ItemDrop drop : drops) {
        sure.add(new CombatantStruct1a8.ItemDrop(100, drop.item()));
      }
      drops.clear();
      drops.addAll(sure);
    }
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
      return null;
    }
  }

  @EventListener
  public void render(final RenderEvent event) {
    final EngineState<?> state = EngineStates.currentEngineState_8004dd04;

    // In Arena: niente filmati o azioni di trama a fine battaglia, solo vittoria o la nostra sconfitta
    if(state instanceof Battle && state == this.activeBattle && postBattleAction_800bc974 != null) {
      final var action = postBattleAction_800bc974;
      final boolean ok = action instanceof VictoryPostBattleActionInstance
        || action instanceof BossKillPostBattleActionInstance
        || action instanceof GameOverPostBattleActionInstance g && g.action == LOSS.get();
      if(!ok) {
        postBattleAction_800bc974 = action instanceof GameOverPostBattleActionInstance ? LOSS.get().inst() : CorePostBattleActions.VICTORY.get().inst();
      }
    }

    // Avvio della sfida: in citta' gli incontri casuali sono spenti, quindi la facciamo partire noi
    // (stessa sequenza del gioco: prepara lo scontro e passa alla battaglia), appena non ci sono menu o dialoghi aperti
    if(pending != null && state instanceof final SMap smap && smap.submap != null && submapId_800bd808 == pendingArea && this.isQuiet()) {
      if(++this.quietFrames > 45) {
        smap.submap.prepareEncounter(false);
        smap.mapTransition(-1, 0);
      }
    } else {
      this.quietFrames = 0;
    }

    // Se esci dall'area dove ti sei iscritto, l'iscrizione viene annullata e rimborsata
    if(pending != null && state instanceof SMap && submapId_800bd808 >= 0 && submapId_800bd808 != pendingArea && gameState_800babc8 != null) {
      gameState_800babc8.gold_94 += pending.cost();
      LOGGER.info("Wings of Destiny: iscrizione all'Arena annullata (uscito dall'area), rimborso %d", pending.cost());
      pending = null;
    }

    // Prima vittoria al grado x3 contro questo boss -> un'arma Heirloom non ancora vinta, nel riepilogo
    if(state instanceof Battle && state == this.activeBattle && active != null && !this.rewardChecked
      && (postBattleAction_800bc974 instanceof VictoryPostBattleActionInstance || postBattleAction_800bc974 instanceof BossKillPostBattleActionInstance)) {
      this.rewardChecked = true;
      final boolean firstX3 = active.grade == 3 && !X3_CLEARED.contains(active.boss);
      // Heirloom: solo boss dal disco 2 in poi
      if(firstX3 && active.record.disc >= 2) {
        this.awardHeirloom();
      }
      // Medaglie: 1 / 2 / 4, doppie alla prima vittoria al x3
      final int medals = (active.grade == 3 ? 4 : active.grade) * (firstX3 ? 2 : 1);
      final legend.game.inventory.ItemStack stack = new legend.game.inventory.ItemStack(wingsofdestiny.items.WodItems.ARENA_MEDAL.get(), medals);
      itemsDroppedByEnemies_800bc928.add(new legend.game.combat.types.EnemyDrop(stack, () -> gameState_800babc8.items_2e9.give(stack).isEmpty(), () -> legend.game.Scus94491BpeSegment_800b.itemOverflow.add(new legend.game.inventory.ItemStack(stack))));
      LOGGER.info("Wings of Destiny: %d medaglie d'Arena", medals);
    }

    // Dopo una sconfitta: chi e' a terra torna a 1 HP
    if(this.reviveParty && state instanceof SMap && gameState_800babc8 != null) {
      this.reviveParty = false;
      for(final CharacterData2c character : gameState_800babc8.charData_32c) {
        final VitalsStat hp = character.stats.getStat(HP_STAT.get());
        if(hp.getCurrent() <= 0) {
          hp.setCurrent(1);
        }
      }
    }
  }

  private int quietFrames;

  private boolean isQuiet() {
    if(legend.game.Menus.whichMenu_800bdc38 != legend.game.inventory.WhichMenu.NONE_0 || gameState_800babc8 == null || gameState_800babc8.indicatorsDisabled_4e3
      || legend.game.unpacker.Loader.getLoadingFileCount() != 0) {
      return false;
    }
    for(final legend.game.types.Textbox4c textbox : legend.game.Text.textboxes_800be358) {
      if(textbox != null && textbox.state_00 != legend.game.types.TextboxState.UNINITIALIZED_0) {
        return false;
      }
    }
    return true;
  }

  private void awardHeirloom() {
    final List<RegistryDelegate<legend.game.inventory.Equipment>> left = new ArrayList<>();
    for(final RegistryDelegate<legend.game.inventory.Equipment> h : wingsofdestiny.items.WodEquipment.HEIRLOOMS) {
      if(h.isValid() && !HEIRLOOMS_AWARDED.contains(h.getId())) {
        left.add(h);
      }
    }
    if(left.isEmpty()) {
      return;
    }
    final legend.game.inventory.Equipment equipment = left.get(this.random.nextInt(left.size())).get();
    this.heirloomThisBattle = equipment.getRegistryId();
    itemsDroppedByEnemies_800bc928.add(new legend.game.combat.types.EnemyDrop(equipment, () -> legend.game.SItem.giveEquipment(equipment), () -> equipmentOverflow.add(equipment)));
    LOGGER.info("Wings of Destiny: premio Heirloom %s", this.heirloomThisBattle);
  }

  @EventListener
  public void battleEnded(final BattleEndedEvent event) {
    final RegistryId id = event.encounter == null ? null : event.encounter.getRegistryId();

    if(event.battle == this.activeBattle && this.activeBattle != null) {
      // Ripristina la trama com'era prima della sfida
      gameState_800babc8.scriptFlags1_13c.set(this.savedFlags1);
      gameState_800babc8.scriptFlags2_bc.set(this.savedFlags2);
      gameState_800babc8.wmapFlags_15c.set(this.savedWmapFlags);
      this.reviveParty = postBattleAction_800bc974 instanceof GameOverPostBattleActionInstance;
      if(!this.reviveParty && active != null && active.grade == 3) {
        X3_CLEARED.add(active.boss);
        if(this.heirloomThisBattle != null) {
          HEIRLOOMS_AWARDED.add(this.heirloomThisBattle);
        }
      }
      LOGGER.info("Wings of Destiny: fine Arena %s (%s)", active == null ? "?" : active.boss, this.reviveParty ? "sconfitta" : "vittoria");
      this.activeBattle = null;
      active = null;
      return;
    }

    // Segna il boss appena battuto nella storia
    if(id != null && event.encounter != null && isArenaBoss(id, event.encounter) && !DEFEATED.containsKey(id)) {
      final var action = postBattleAction_800bc974;
      if(!(action instanceof GameOverPostBattleActionInstance)) {
        DEFEATED.put(id, new BossRecord(battleStage_800bb0f4, partyLevel(), disc()));
        LOGGER.info("Wings of Destiny: boss %s aggiunto all'Arena (stage %d, livello %d, disco %d)", id, battleStage_800bb0f4, partyLevel(), disc());
      }
    }
  }
}
