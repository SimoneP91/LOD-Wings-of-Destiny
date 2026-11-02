package wingsofdestiny.challenge;

import legend.game.EngineState;
import legend.game.EngineStates;
import legend.game.Menus;
import legend.game.Text;
import legend.game.combat.Battle;
import legend.game.combat.encounters.Encounter;
import legend.game.combat.types.CombatantStruct1a8;
import legend.game.combat.types.EnemyDrop;
import legend.game.inventory.WhichMenu;
import legend.game.inventory.screens.FontOptions;
import legend.game.inventory.screens.HorizontalAlign;
import legend.game.inventory.screens.TextColour;
import legend.game.modding.events.RenderEvent;
import legend.game.combat.postbattleactions.BossKillPostBattleActionInstance;
import legend.game.combat.postbattleactions.PlayFmvPostBattleActionInstance;
import legend.game.combat.postbattleactions.VictoryPostBattleActionInstance;
import legend.game.inventory.ItemStack;
import legend.game.modding.events.battle.EnemyRewardsEvent;
import legend.game.modding.events.battle.MonsterStatsEvent;
import legend.game.submap.SMap;
import legend.game.title.GameOver;
import legend.game.title.Ttle;
import legend.game.wmap.WMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import wingsofdestiny.config.WodConfigs;
import wingsofdestiny.items.WodItems;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static legend.game.Scus94491BpeSegment_800b.encounter;
import static legend.game.Scus94491BpeSegment_800b.itemOverflow;
import static legend.game.Scus94491BpeSegment_800b.postBattleAction_800bc974;
import static legend.game.Scus94491BpeSegment_800b.itemsDroppedByEnemies_800bc928;
import static legend.game.Scus94491BpeSegment_8006.battleState_8006e398;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static legend.game.Scus94491BpeSegment_800b.submapId_800bd808;

/**
 * Scontri elite e zone maledette.
 * - Elite: tutto lo scontro, mai su boss o scontri di trama senza fuga.
 * - Zona maledetta: decisa a ogni ingresso in un'area (submapId), resta finche' non esci. Phantom Ship sempre.
 */
public class Challenge {
  private static final Logger LOGGER = LogManager.getFormatterLogger(Challenge.class);

  /** Indice "Phantom Ship" nella tabella dei nomi delle aree. */
  private static final int PHANTOM_SHIP = 27;
  /** Durata del messaggio di ingresso, in frame (~4 secondi). */
  private static final int NOTICE_FRAMES = 240;

  private static final float ELITE_HP = 2.0f, ELITE_ATK = 1.3f, ELITE_DEF = 1.2f, ELITE_REWARD = 2.0f;
  private static final float CURSED_HP = 1.5f, CURSED_ATK = 1.2f, CURSED_DEF = 1.0f, CURSED_REWARD = 1.5f, CURSED_ELITE_FACTOR = 3.0f;

  private static boolean elite;
  private static boolean cursedBattle;
  private static boolean bossBattle;

  // Materiali della forgia
  // Un tiro per tipo di pietra per SCONTRO, quando muore l'ultimo mostro (ca. 1 ogni 10 scontri)
  private static final int FORGE_STONE_NORMAL = 10;
  private static final int FORGE_STONE_ELITE = 50;
  private static final int DRAGON_STONE_CURSED = 3;
  private static final int DRAGON_STONE_CURSED_ELITE = 5;
  private boolean materialsRolled;

  private final Random random = new Random();

  // Stato dello scontro corrente
  private Object rolledFor;

  // Stato della zona
  private int zone = -1;
  private boolean leftZone = true;
  private boolean zoneCursed;
  /** Dopo il caricamento di un save: lo stato della zona arriva dal save, non va ritirato. */
  private boolean restoring;
  private static final org.legendofdragoon.modloader.registries.RegistryId SAVE_ZONE = new org.legendofdragoon.modloader.registries.RegistryId(wingsofdestiny.WingsOfDestiny.MOD_ID, "cursed_zone");
  private int noticeFrames;
  private Object lastSubmap;
  private boolean cutHasEncounters;

  private final FontOptions noticeFont = new FontOptions().colour(TextColour.RED).horizontalAlign(HorizontalAlign.CENTRE);
  private final FontOptions tagFont = new FontOptions().colour(TextColour.RED).size(0.67f).horizontalAlign(HorizontalAlign.RIGHT);
  private final FontOptions eliteFont = new FontOptions().colour(TextColour.GOLD).size(0.67f).horizontalAlign(HorizontalAlign.RIGHT);

  private static Field dropsField;

  /** Usato dalla barra HP per colorarla. */
  public static boolean isElite() {
    return elite && EngineStates.currentEngineState_8004dd04 instanceof Battle;
  }

  /** Boss: musica da boss o scontri speciali (a fasi, arena, Melbu). */
  private static boolean isBoss(final Encounter encounter) {
    return encounter != null && (encounter.getClass() != Encounter.class || encounter.musicIndex >= 240);
  }

  /** Scontri esclusi: boss (musica da boss o scontri speciali) e scontri di trama senza fuga. */
  private static boolean isExcluded(final Encounter encounter) {
    return encounter == null || encounter.getClass() != Encounter.class || encounter.musicIndex >= 240 || encounter.escapeChance == 0;
  }

  /** Decide elite/maledetto una sola volta per battaglia (le ricompense arrivano prima delle statistiche). */
  private void rollBattle() {
    final EngineState<?> state = EngineStates.currentEngineState_8004dd04;
    if(state == this.rolledFor) {
      return;
    }

    this.rolledFor = state;
    // Nuova battaglia: si riparte da zero
    bossBattle = isBoss(encounter);
    this.materialsRolled = false;
    this.directGive = false;

    if(isExcluded(encounter)) {
      elite = false;
      cursedBattle = false;
      return;
    }

    cursedBattle = this.zoneCursed && !this.leftZone;
    float chance = WodConfigs.eliteChance();
    if(cursedBattle) {
      chance *= CURSED_ELITE_FACTOR;
    }
    elite = chance > 0 && this.random.nextFloat() * 100.0f < Math.min(100.0f, chance);

    if(elite || cursedBattle) {
      LOGGER.info("Wings of Destiny: scontro %s%s%s", encounter.getRegistryId(), elite ? " ELITE" : "", cursedBattle ? " (zona maledetta)" : "");
    }
  }

  /** In New Game+ elite e zone maledette sono il 10% piu' forti (solo statistiche, non ricompense). */
  private static final float NG_PLUS_BOOST = 1.1f;

  private static float boost() {
    return wingsofdestiny.ngplus.NewGamePlus.active() ? NG_PLUS_BOOST : 1.0f;
  }

  private static float hpMult() {
    return (elite ? ELITE_HP * boost() : 1.0f) * (cursedBattle ? CURSED_HP * boost() : 1.0f);
  }

  private static float atkMult() {
    return (elite ? ELITE_ATK * boost() : 1.0f) * (cursedBattle ? CURSED_ATK * boost() : 1.0f);
  }

  private static float defMult() {
    return (elite ? ELITE_DEF * boost() : 1.0f) * (cursedBattle ? CURSED_DEF * boost() : 1.0f);
  }

  private static float rewardMult() {
    return (elite ? ELITE_REWARD : 1.0f) * (cursedBattle ? CURSED_REWARD : 1.0f);
  }

  @EventListener(priority = Priority.LOWEST)
  public void enemyRewards(final EnemyRewardsEvent event) {
    this.rollBattle();

    final float mult = rewardMult();
    if(mult != 1.0f) {
      event.xp = Math.round(event.xp * mult);
      event.gold = Math.round(event.gold * mult);
      this.boostDrops(event, mult);
    }

  }

  private void boostDrops(final EnemyRewardsEvent event, final float mult) {
    final List<CombatantStruct1a8.ItemDrop> drops = drops(event);
    if(drops != null) {
      final List<CombatantStruct1a8.ItemDrop> boosted = new ArrayList<>();
      for(final CombatantStruct1a8.ItemDrop drop : drops) {
        boosted.add(new CombatantStruct1a8.ItemDrop(Math.min(100, Math.round(drop.chance() * mult)), drop.item()));
      }
      drops.clear();
      drops.addAll(boosted);
    }
  }

  @EventListener(priority = Priority.LOWEST)
  public void monsterStats(final MonsterStatsEvent event) {
    this.rollBattle();

    final float hp = hpMult();
    final float atk = atkMult();
    final float def = defMult();
    if(hp == 1.0f && atk == 1.0f && def == 1.0f) {
      return;
    }

    event.hp = Math.round(event.hp * hp);
    event.maxHp = Math.round(event.maxHp * hp);
    event.attack = Math.round(event.attack * atk);
    event.magicAttack = Math.round(event.magicAttack * atk);
    event.defence = Math.round(event.defence * def);
    event.magicDefence = Math.round(event.magicDefence * def);
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
      LOGGER.warn("Wings of Destiny: drop non modificabili", e);
      return null;
    }
  }

  @EventListener
  public void render(final RenderEvent event) {
    final EngineState<?> state = EngineStates.currentEngineState_8004dd04;

    if(state instanceof Battle) {
      if(this.rolledFor == state) {
        this.renderBattleTags();
        this.checkMaterials();
      }
      return;
    }

    if(!(state instanceof final SMap smap)) {
      // Mappa del mondo, titolo o game over: sei uscito dalla zona (le transizioni e i filmati non contano)
      if(state instanceof WMap || state instanceof Ttle || state instanceof GameOver) {
        if(!this.restoring) {
          this.leftZone = true;
        }
      }
      return;
    }

    final int area = submapId_800bd808;
    if(area < 0) {
      return;
    }

    if(this.restoring) {
      this.restoring = false;
      if(area == this.zone) {
        this.leftZone = false;
        this.noticeFrames = 0;
      }
    }

    if(area != this.zone || this.leftZone) {
      this.enterZone(area);
    }

    // Al cambio schermata submapCut cambia subito, ma smap.submap resta quello vecchio finche' il nuovo non e' caricato:
    // si ricalcola solo quando arriva l'oggetto submap nuovo, altrimenti si leggerebbero gli scontri della schermata precedente
    if(smap.submap != this.lastSubmap) {
      this.lastSubmap = smap.submap;
      this.cutHasEncounters = smap.submap != null && smap.submap.hasEncounters();
    }

    if(!this.zoneCursed || !this.cutHasEncounters || Menus.whichMenu_800bdc38 != WhichMenu.NONE_0) {
      return;
    }

    if(this.noticeFrames > 0) {
      this.noticeFrames--;
      Text.renderText("This area is cursed!", 160.0f, 100.0f, this.noticeFont);
    }

    Text.renderText("Cursed Zone", 314.0f, 12.0f, this.tagFont);
  }

  /** La maledizione della zona in cui ti trovi viene salvata nel save, cosi' ricaricando resta com'era. */
  @EventListener
  public void writeSave(final legend.game.saves.WriteSaveDataEvent event) {
    final legend.core.tags.ListTag tag = new legend.core.tags.ListTag();
    tag.add(new legend.core.tags.StringTag(this.zone + "|" + (this.zoneCursed && !this.leftZone)));
    event.add(SAVE_ZONE, tag);
  }

  @EventListener
  public void readSave(final legend.game.saves.ReadSaveDataEvent event) {
    this.zone = -1;
    this.zoneCursed = false;
    this.leftZone = true;
    this.restoring = false;
    final legend.core.tags.Tag tag = event.get(SAVE_ZONE);
    if(tag == null || tag.asList().isEmpty()) {
      return;
    }
    try {
      final String[] parts = tag.asList().get(0).asString().get().split("\\|");
      this.zone = Integer.parseInt(parts[0]);
      this.zoneCursed = Boolean.parseBoolean(parts[1]);
      this.restoring = true;
    } catch(final RuntimeException e) {
      LOGGER.warn("Wings of Destiny: stato della zona non leggibile dal save");
    }
  }

  private void enterZone(final int area) {
    this.zone = area;
    this.leftZone = false;
    this.lastSubmap = null;

    final boolean wasCursed = this.zoneCursed;
    if(!WodConfigs.cursedZones()) {
      this.zoneCursed = false;
    } else if(area == PHANTOM_SHIP) {
      this.zoneCursed = true;
    } else {
      this.zoneCursed = this.random.nextFloat() * 100.0f < WodConfigs.cursedChance();
    }

    this.noticeFrames = this.zoneCursed ? NOTICE_FRAMES : 0;
    if(this.zoneCursed || wasCursed) {
      LOGGER.info("Wings of Destiny: area %d %s", area, this.zoneCursed ? "MALEDETTA" : "normale");
    }
  }

  /** Quando l'ultimo mostro muore: un tiro per la Forge Stone e uno per la Dragon Stone, nel riepilogo di fine battaglia. */
  private void checkMaterials() {
    if(this.materialsRolled) {
      return;
    }
    // Il segnale affidabile della vittoria e' l'azione di fine battaglia: i boss spesso non "muoiono" via script
    final var action = postBattleAction_800bc974;
    final boolean won = action instanceof VictoryPostBattleActionInstance || action instanceof BossKillPostBattleActionInstance || action instanceof PlayFmvPostBattleActionInstance;
    if(!won) {
      return;
    }
    this.materialsRolled = true;
    // Con un filmato a fine battaglia non c'e' il riepilogo: la pietra va direttamente nell'inventario
    this.directGive = action instanceof PlayFmvPostBattleActionInstance;

    final Encounter enc = encounter;
    if(bossBattle) {
      // Boss della storia: niente pietre. Arena: 1 Dragon Stone solo al grado x3
      if(wingsofdestiny.arena.Arena.activeGrade() == 3) {
        addDrop(WodItems.DRAGON_STONE.get());
        LOGGER.info("Wings of Destiny: Arena x3 vinta, 1 Dragon Stone");
      }
      return;
    }
    if(isExcluded(enc)) {
      return;
    }

    final boolean forge = this.random.nextInt(100) < (elite ? FORGE_STONE_ELITE : FORGE_STONE_NORMAL);
    final boolean dragon = cursedBattle && this.random.nextInt(100) < (elite ? DRAGON_STONE_CURSED_ELITE : DRAGON_STONE_CURSED);
    if(forge) {
      addDrop(WodItems.FORGE_STONE.get());
    }
    if(dragon) {
      addDrop(WodItems.DRAGON_STONE.get());
    }
    LOGGER.info("Wings of Destiny: tiro pietre (elite=%b, maledetto=%b) -> Forge Stone %b, Dragon Stone %b", elite, cursedBattle, forge, dragon);
  }

  private boolean directGive;

  private void addDrop(final legend.game.inventory.Item item) {
    final ItemStack stack = new ItemStack(item);
    if(this.directGive) {
      gameState_800babc8.items_2e9.give(stack);
      return;
    }
    itemsDroppedByEnemies_800bc928.add(new EnemyDrop(stack, () -> gameState_800babc8.items_2e9.give(stack).isEmpty(), () -> itemOverflow.add(new ItemStack(stack))));
  }

  private void renderBattleTags() {
    float y = 12.0f;
    if(elite) {
      Text.renderText("ELITE", 314.0f, y, this.eliteFont);
      y += 10.0f;
    }
    if(cursedBattle) {
      Text.renderText("Cursed Zone", 314.0f, y, this.tagFont);
    }
  }
}
