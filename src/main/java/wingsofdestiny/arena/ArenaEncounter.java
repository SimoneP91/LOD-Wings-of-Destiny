package wingsofdestiny.arena;

import legend.game.combat.Battle;
import legend.game.combat.encounters.Encounter;

import static legend.game.Scus94491BpeSegment_800b.postBattleAction_800bc974;
import static legend.game.sound.Audio.loadMusicPackage;

/**
 * Copia di uno scontro con boss per l'Arena di Lohan:
 * - dopo la battaglia si torna dove si era (niente spostamenti di trama);
 * - se perdi niente game over.
 */
public class ArenaEncounter extends Encounter {
  public final Encounter original;

  public ArenaEncounter(final Encounter original) {
    super(original.musicIndex, 0, original.playerOpeningCamera, original.monsterOpeningCamera,
      original.cameraPosIndices[0], original.cameraPosIndices[1], original.cameraPosIndices[2], original.cameraPosIndices[3],
      65535, 255, original.introWaitTicks, original.monsters.toArray(new Monster[0]));
    this.original = original;
  }

  @Override
  public void onBattleLost(final Battle battle) {
    loadMusicPackage(19);
    postBattleAction_800bc974 = Arena.LOSS.get().inst();
  }
}
