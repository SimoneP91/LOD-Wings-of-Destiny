package wingsofdestiny.ui;

import legend.core.GameEngine;
import legend.core.gte.MV;
import legend.core.renderer.QueuedModelStandard;
import legend.core.renderer.Translucency;
import legend.game.EngineState;
import legend.game.EngineStates;
import legend.game.Scus94491BpeSegment_8006;
import legend.game.Text;
import legend.game.characters.VitalsStat;
import legend.game.combat.Battle;
import legend.game.combat.bent.MonsterBattleEntity;
import legend.game.combat.ui.BattleMenuStruct58;
import legend.game.inventory.screens.FontOptions;
import legend.game.inventory.screens.TextColour;
import legend.game.modding.events.RenderEvent;
import legend.lodmod.LodMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import wingsofdestiny.challenge.Challenge;
import wingsofdestiny.config.WodConfigs;

/**
 * Mostra "HP attuali/HP massimi" del mostro selezionato, subito sotto il riquadro con il nome del bersaglio
 * (in alto a sinistra finirebbe sotto il riquadro Turn Order).
 * Barra blu sopra il 50%, gialla fino al 25%, rossa sotto.
 */
public class MonsterHpDisplay {
  private static final Logger LOGGER = LogManager.getFormatterLogger(MonsterHpDisplay.class);
  private static final float X = 44.0f;
  private static final float Y = 46.0f;
  private static final float W = 232.0f;
  private static final float H = 14.0f;
  private boolean logged;

  private final FontOptions font = new FontOptions().colour(TextColour.WHITE).size(0.67f);
  private final MV transforms = new MV();

  @EventListener
  public void render(final RenderEvent event) {
    if(!WodConfigs.monsterHpNumbers()) {
      return;
    }

    final EngineState<?> engineState = EngineStates.currentEngineState_8004dd04;
    if(!(engineState instanceof final Battle battle) || Scus94491BpeSegment_8006.battleState_8006e398 == null) {
      return;
    }

    final BattleMenuStruct58 menu = battle.hud.battleMenu_800c6c34;
    final int target = menu.combatantIndex_54;
    if(!menu.displayTargetArrowAndName_4c || target == -1) {
      return;
    }

    // Stessa logica del gioco per il riquadro col nome: tipo 1 = mostri vivi, tipo 2 = tutti i combattenti
    final MonsterBattleEntity monster;
    final var state = Scus94491BpeSegment_8006.battleState_8006e398;
    if(menu.targetType_50 == 1 && target < state.aliveMonsterBents_ebc.size()) {
      monster = state.aliveMonsterBents_ebc.get(target).innerStruct_00;
    } else if(menu.targetType_50 == 2 && target < state.allBents_e0c.size() && state.allBents_e0c.get(target).innerStruct_00 instanceof final MonsterBattleEntity m) {
      monster = m;
    } else {
      return;
    }

    final VitalsStat hp = monster.stats.getStat(LodMod.HP_STAT.get());
    final float ratio = hp.getMax() > 0 ? Math.clamp((float)hp.getCurrent() / hp.getMax(), 0.0f, 1.0f) : 0.0f;

    final float r;
    final float g;
    final float b;
    if(ratio <= 0.25f) {
      r = 0.85f; g = 0.0f; b = 0.0f;
    } else if(Challenge.isElite()) {
      // Scontro elite: barra oro/arancio invece di blu/giallo
      if(ratio <= 0.5f) {
        r = 0.9f; g = 0.45f; b = 0.0f;
      } else {
        r = 0.95f; g = 0.75f; b = 0.0f;
      }
    } else if(ratio <= 0.5f) {
      r = 0.85f; g = 0.85f; b = 0.0f;
    } else {
      r = 0.0f; g = 0.0f; b = 0.85f;
    }

    if(!this.logged) {
      LOGGER.info("Wings of Destiny: barra HP attiva (%s %d/%d)", monster.getName(), hp.getCurrent(), hp.getMax());
      this.logged = true;
    }

    this.transforms.identity();
    this.transforms.transfer.set(X, Y, 999.0f);
    this.transforms.scaling(W, H, 999.0f);
    GameEngine.RENDERER.queueOrthoModel(GameEngine.RENDERER.opaqueQuad, this.transforms, QueuedModelStandard.class)
      .monochrome(0.0f)
      .translucency(Translucency.HALF_B_PLUS_HALF_F);

    this.transforms.scaling(W * ratio, H, 1.0f);
    GameEngine.RENDERER.queueOrthoModel(GameEngine.RENDERER.opaqueQuad, this.transforms, QueuedModelStandard.class)
      .colour(r, g, b)
      .translucency(Translucency.B_PLUS_QUARTER_F);

    Text.renderText(hp.getCurrent() + "/" + hp.getMax(), X + 4.0f, Y + 2.0f, this.font);
  }
}
