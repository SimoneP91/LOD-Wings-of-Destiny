package wingsofdestiny.items;

import legend.game.EngineStates;
import legend.game.characters.VitalsStat;
import legend.game.combat.Battle;
import legend.game.combat.bent.BattleEntity27c;
import legend.game.modding.events.RenderEvent;
import legend.game.modding.events.battle.BattleEndedEvent;
import legend.game.modding.events.battle.BattleStartedEvent;
import org.legendofdragoon.modloader.events.EventListener;

import static legend.lodmod.LodMod.HP_STAT;

/** Quando il bersaglio dell'Archangel's Prayer torna in vita (a meta' HP), gli riempie gli HP. */
public class ArchangelsPrayerHandler {
  /** Circa 20 secondi: se in questo tempo non risorge, lascia perdere. */
  private static final int TIMEOUT_FRAMES = 1200;

  private static BattleEntity27c target;
  private static int frames;

  static void pending(final BattleEntity27c bent) {
    target = bent;
    frames = 0;
  }

  @EventListener
  public void render(final RenderEvent event) {
    if(target == null) {
      return;
    }

    if(!(EngineStates.currentEngineState_8004dd04 instanceof Battle) || ++frames > TIMEOUT_FRAMES) {
      target = null;
      return;
    }

    final VitalsStat hp = target.stats.getStat(HP_STAT.get());
    if(hp.getCurrent() > 0) {
      hp.setCurrent(hp.getMax());
      target = null;
    }
  }

  @EventListener
  public void battleStarted(final BattleStartedEvent event) {
    target = null;
  }

  @EventListener
  public void battleEnded(final BattleEndedEvent event) {
    target = null;
  }
}
