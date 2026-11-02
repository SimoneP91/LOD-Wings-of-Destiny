package wingsofdestiny.items;

import legend.game.combat.bent.BattleEntity27c;
import legend.game.inventory.ItemStack;
import legend.game.scripting.ScriptState;
import legend.lodmod.LodItems;
import legend.lodmod.items.AngelsPrayerItem;
import org.legendofdragoon.modloader.registries.RegistryId;

import static legend.core.GameEngine.SCRIPTS;
import static legend.lodmod.LodMod.HP_STAT;

/**
 * Archangel's Prayer: come l'Angel's Prayer (stessa animazione), ma resuscita a HP pieni e non si consuma.
 * Lo script di gioco resuscita a meta' HP: ArchangelsPrayerHandler porta poi l'HP al massimo.
 */
public class ArchangelsPrayerItem extends AngelsPrayerItem {
  @Override
  public boolean isRepeat(final ItemStack stack) {
    return true;
  }

  @Override
  protected RegistryId getDeffId() {
    return LodItems.ANGELS_PRAYER.getId();
  }

  @Override
  protected void useItemScriptLoaded(final ScriptState<BattleEntity27c> user, final int targetBentIndex) {
    super.useItemScriptLoaded(user, targetBentIndex);

    if(SCRIPTS.getState(targetBentIndex).innerStruct_00 instanceof final BattleEntity27c target && target.stats.getStat(HP_STAT.get()).getCurrent() == 0) {
      ArchangelsPrayerHandler.pending(target);
    }
  }
}
