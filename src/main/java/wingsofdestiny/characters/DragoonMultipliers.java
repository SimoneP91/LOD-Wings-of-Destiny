package wingsofdestiny.characters;

import legend.game.modding.events.characters.PreCharacterDragoonLevelUpEvent;
import legend.lodmod.LodMod;
import legend.lodmod.characters.ShanaTemplate;
import org.legendofdragoon.modloader.events.EventListener;

/**
 * Moltiplicatori in forma Dragoon: retail per tutti, tranne Shana e Miranda con AT al 365%.
 * Il moltiplicatore base si assegna al primo livello Dragoon; il gioco rigioca i livelli quando carica
 * un salvataggio, quindi vale anche per le partite gia' in corso.
 */
public class DragoonMultipliers {
  public static final int SHANA_DRAGOON_ATTACK = 365;

  @EventListener
  public void dragoonLevelUp(final PreCharacterDragoonLevelUpEvent event) {
    if(event.character.template instanceof ShanaTemplate && event.character.dlevel_13 == 0) {
      event.statsToAdd.put(LodMod.DRAGOON_ATTACK_STAT.get(), SHANA_DRAGOON_ATTACK);
    }
  }
}
