package wingsofdestiny.ngplus;

import legend.game.combat.bent.AttackEvent;
import legend.game.combat.bent.PlayerBattleEntity;
import legend.game.combat.types.AttackType;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.events.Priority;

/**
 * New Game+: magie Dragoon +30% per ciclo (NG+ x1,3, NG++ x1,6, NG+++ x1,9...).
 * Vale per danno e cure (le cure restano entro gli HP massimi). Potenziamenti e magie di stato non cambiano:
 * il loro valore e' 0. Costi in MP invariati.
 */
public class DragoonMagic {
  private static final float BONUS_PER_CYCLE = 0.30f;

  public static float factor() {
    return NewGamePlus.active() ? 1.0f + BONUS_PER_CYCLE * NewGamePlus.cycle() : 1.0f;
  }

  /** Per ultimo, dopo le eventuali modifiche di altre mod. */
  @EventListener(priority = Priority.LOWEST)
  public void attack(final AttackEvent event) {
    if(event.attackType != AttackType.DRAGOON_MAGIC_STATUS_ITEMS || event.damage <= 0) {
      return;
    }
    if(!(event.attacker instanceof final PlayerBattleEntity player) || player.spell_94 == null || !player.isDragoon()) {
      return;
    }
    final float f = factor();
    if(f != 1.0f) {
      event.damage = Math.round(event.damage * f);
    }
  }
}
