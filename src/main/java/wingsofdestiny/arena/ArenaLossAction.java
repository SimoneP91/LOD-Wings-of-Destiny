package wingsofdestiny.arena;

import legend.game.combat.Battle;
import legend.game.combat.postbattleactions.GameOverPostBattleAction;
import legend.game.combat.postbattleactions.GameOverPostBattleActionInstance;
import legend.game.inventory.WhichMenu;

import static legend.game.Menus.whichMenu_800bdc38;

/** Sconfitta nell'Arena: stessa uscita della sconfitta nel torneo di Lohan, ma senza toccare i flag di trama. */
public class ArenaLossAction extends GameOverPostBattleAction {
  @Override
  protected void performAction(final Battle battle, final GameOverPostBattleActionInstance inst) {
    whichMenu_800bdc38 = WhichMenu.NONE_0;
  }
}
