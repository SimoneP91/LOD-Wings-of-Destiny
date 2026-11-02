package wingsofdestiny.ngplus;

import legend.game.modding.events.battle.EnemyRewardsEvent;
import legend.game.modding.events.battle.MonsterStatsEvent;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;
import wingsofdestiny.arena.Arena;

/**
 * New Game+: i mostri crescono con il party.
 * Fattore = livello medio del party / livello previsto per il disco (mai sotto 1), +25% per ogni ciclo dopo il primo.
 * Si applica a HP, attacco e attacco magico (come l'Arena) e a EXP e oro.
 * Gira per ultimo (priorita' LOWEST), dopo la tabella Hard; gli altri moltiplicatori LOWEST (elite, Arena) si combinano.
 */
public class NgScaling {
  /** Livello previsto a meta' di ciascun disco. */
  private static final int[] EXPECTED_LEVEL = {10, 22, 30, 40};
  /** Bonus per ogni ciclo dopo il primo: NG++ +25%, NG+++ +50%... */
  private static final float CYCLE_BONUS = 0.25f;

  /** Fattore per la battaglia in corso (1 fuori dal New Game+). */
  public static float factor() {
    if(!NewGamePlus.active()) {
      return 1.0f;
    }
    return factor(Arena.battleDisc(), Arena.partyLevel(), NewGamePlus.cycle());
  }

  /** Fattore per un boss di un certo disco, con il party attuale (1 fuori dal New Game+). */
  public static float factorFor(final int disc) {
    if(!NewGamePlus.active()) {
      return 1.0f;
    }
    return factor(disc, Arena.partyLevel(), NewGamePlus.cycle());
  }

  static float factor(final int disc, final int partyLevel, final int cycle) {
    final int index = Math.clamp(disc - 1, 0, EXPECTED_LEVEL.length - 1);
    final float ratio = Math.max(1.0f, (float)partyLevel / EXPECTED_LEVEL[index]);
    return ratio * (1.0f + CYCLE_BONUS * Math.max(0, cycle - 1));
  }

  @EventListener(priority = Priority.LOWEST)
  public void monsterStats(final MonsterStatsEvent event) {
    final float f = factor();
    if(f == 1.0f) {
      return;
    }
    event.hp = Math.round(event.hp * f);
    event.maxHp = Math.round(event.maxHp * f);
    event.attack = Math.round(event.attack * f);
    event.magicAttack = Math.round(event.magicAttack * f);
  }

  @EventListener(priority = Priority.LOWEST)
  public void enemyRewards(final EnemyRewardsEvent event) {
    final float f = factor();
    if(f == 1.0f) {
      return;
    }
    event.xp = Math.round(event.xp * f);
    event.gold = Math.round(event.gold * f);
  }
}
