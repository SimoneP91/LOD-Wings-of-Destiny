package wingsofdestiny.ngplus;

import legend.core.GameEngine;
import legend.game.additions.Addition;
import legend.game.additions.AdditionHitProperties10;
import legend.game.characters.CharacterAdditionInfo;
import legend.game.characters.CharacterData2c;
import legend.game.unpacker.FileData;
import org.legendofdragoon.modloader.registries.RegistryId;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Addition EX: stessa animazione dell'addition originale, livelli 1-10.
 * - Riferimento: per danno e SP separatamente, il valore piu' alto tra l'originale padroneggiata e il "pavimento"
 *   (l'addition che viene prima nella catena, al suo livello massimo). Cosi' ogni EX parte gia' sopra.
 * - Livello L = riferimento x (1 + 2,5% x L), livello 1 compreso.
 * - Servono il doppio degli usi dell'originale (livello x 40 invece di livello x 20).
 * - Finestra di ogni colpo piu' stretta di 1 frame (mai sotto 1).
 * - Non conta per sbloccare l'addition finale.
 * I valori si leggono ogni volta, cosi' seguono le modifiche fatte da AdditionTuning.
 */
public class ExAddition extends Addition {
  public static final int MAX_LEVEL = 10;
  private static final float STEP_PER_LEVEL = 0.025f;
  private static final int XP_PER_LEVEL = 40;

  public final RegistryId baseId;
  /** Addition da superare: la finale originale per la prima EX, la prima EX per la seconda. */
  public final RegistryId floorId;

  /** Copie dei colpi originali con la finestra ristretta, una per colpo. */
  private final Map<AdditionHitProperties10, AdditionHitProperties10> tightHits = new IdentityHashMap<>();

  public ExAddition(final RegistryId baseId, final RegistryId floorId) {
    this.baseId = baseId;
    this.floorId = floorId;
  }

  private Addition base() {
    return GameEngine.REGISTRIES.additions.getEntry(this.baseId).get();
  }

  private Addition floor() {
    return GameEngine.REGISTRIES.additions.hasEntry(this.floorId) ? GameEngine.REGISTRIES.additions.getEntry(this.floorId).get() : null;
  }

  /** Dati dell'originale padroneggiata (livello massimo). */
  private static CharacterAdditionInfo mastered(final Addition base, final CharacterData2c character) {
    final CharacterAdditionInfo info = new CharacterAdditionInfo(List.of());
    info.level = base.getMaxLevel(character, info);
    return info;
  }

  private static int level(final CharacterAdditionInfo info) {
    return Math.clamp(info.level, 1, MAX_LEVEL);
  }

  private static float levelFactor(final CharacterAdditionInfo info) {
    return 1.0f + STEP_PER_LEVEL * level(info);
  }

  /**
   * Moltiplicatore del danno: porta la somma dei colpi al riferimento, poi aggiunge il 2,5% a livello.
   * Il riferimento e' il piu' alto tra l'originale padroneggiata e il pavimento al suo livello massimo.
   */
  @Override
  public float getDamageMultiplier(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    final Addition base = this.base();
    final CharacterAdditionInfo mastered = mastered(base, character);
    final float baseMulti = base.getDamageMultiplier(character, mastered);
    int hitsTotal = 0;
    for(int i = 0; i < base.getHitCount(character, mastered); i++) {
      hitsTotal += base.getHit(character, mastered, i).damageMultiplier_04;
    }
    if(hitsTotal <= 0) {
      return baseMulti * levelFactor(additionInfo);
    }
    float reference = hitsTotal * baseMulti;
    final Addition floor = this.floor();
    if(floor != null) {
      reference = Math.max(reference, floor.getDamage(character, mastered(floor, character)));
    }
    return reference / hitsTotal * levelFactor(additionInfo);
  }

  /** Come il danno, per l'SP. */
  @Override
  public float getSpMultiplier(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    final Addition base = this.base();
    final CharacterAdditionInfo mastered = mastered(base, character);
    final float baseMulti = base.getSpMultiplier(character, mastered);
    int hitsTotal = 0;
    for(int i = 0; i < base.getHitCount(character, mastered); i++) {
      hitsTotal += base.getHit(character, mastered, i).sp_05;
    }
    if(hitsTotal <= 0) {
      return baseMulti * levelFactor(additionInfo);
    }
    float reference = base.getSp(character, mastered);
    final Addition floor = this.floor();
    if(floor != null) {
      reference = Math.max(reference, floor.getSp(character, mastered(floor, character)));
    }
    return reference / hitsTotal * levelFactor(additionInfo);
  }

  /** Come SimpleAddition: somma dei danni dei colpi per il moltiplicatore. */
  @Override
  public int getDamage(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    final Addition base = this.base();
    final CharacterAdditionInfo mastered = mastered(base, character);
    int damage = 0;
    for(int i = 0; i < base.getHitCount(character, mastered); i++) {
      damage += base.getHit(character, mastered, i).damageMultiplier_04;
    }
    return (int)(damage * this.getDamageMultiplier(character, additionInfo));
  }

  /** Come SimpleAddition: SP di ogni colpo per il moltiplicatore. */
  @Override
  public int getSp(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    final Addition base = this.base();
    final CharacterAdditionInfo mastered = mastered(base, character);
    final float multi = this.getSpMultiplier(character, additionInfo);
    int sp = 0;
    for(int i = 0; i < base.getHitCount(character, mastered); i++) {
      sp += (int)(base.getHit(character, mastered, i).sp_05 * multi);
    }
    return sp;
  }

  @Override
  public int getXpToNextLevel(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    return additionInfo.level * XP_PER_LEVEL;
  }

  @Override
  public int getMaxLevel(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    return MAX_LEVEL;
  }

  @Override
  public boolean isComplete(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    return additionInfo.level >= MAX_LEVEL;
  }

  @Override
  public boolean countsTowardsMastery(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    return false;
  }

  @Override
  public int getHitCount(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    final Addition base = this.base();
    return base.getHitCount(character, mastered(base, character));
  }

  @Override
  public AdditionHitProperties10 getHit(final CharacterData2c character, final CharacterAdditionInfo additionInfo, final int index) {
    final Addition base = this.base();
    final AdditionHitProperties10 original = base.getHit(character, mastered(base, character), index);
    final AdditionHitProperties10 tight = this.tightHits.computeIfAbsent(original, ExAddition::tighten);
    // Danno e SP per colpo seguono l'originale (AdditionTuning li riscrive a ogni partita)
    tight.damageMultiplier_04 = original.damageMultiplier_04;
    tight.sp_05 = original.sp_05;
    return tight;
  }

  @Override
  public CompletableFuture<List<FileData>> loadAnimations(final CharacterData2c character, final CharacterAdditionInfo additionInfo) {
    final Addition base = this.base();
    return base.loadAnimations(character, mastered(base, character));
  }

  private static AdditionHitProperties10 tighten(final AdditionHitProperties10 o) {
    final int window = o.totalSuccessFrames_03 > 1 ? o.totalSuccessFrames_03 - 1 : o.totalSuccessFrames_03;
    final AdditionHitProperties10 copy = new AdditionHitProperties10(
      o.flags_00, o.totalFrames_01, o.overlayHitFrameOffset_02, window, o.damageMultiplier_04, o.sp_05, o.audioFile_06, o.isFinalHit_07,
      o.cameraMovementX_08, o.cameraMovementZ_09, o.cameraMovementTicks_0a, o.hitDistanceFromTarget_0b, o.framesToHitPosition_0c, o._0d,
      o.additionFailAnimationIndex_0e, o.overlayStartingFrameOffset_0f, o.sounds
    );
    copy.animationScale = o.animationScale;
    return copy;
  }
}
