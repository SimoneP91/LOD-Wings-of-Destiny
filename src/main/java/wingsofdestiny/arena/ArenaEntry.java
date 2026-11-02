package wingsofdestiny.arena;

import legend.core.GameEngine;
import legend.game.combat.Monsters;
import legend.game.combat.encounters.Encounter;
import legend.game.inventory.InventoryEntry;
import legend.game.inventory.ItemIcon;
import org.legendofdragoon.modloader.registries.RegistryId;

import java.util.LinkedHashSet;
import java.util.Set;

/** Voce "sfida" nei negozi delle citta': un boss gia' battuto, a un certo grado (x1/x2/x3). */
public class ArenaEntry implements InventoryEntry<ArenaEntry> {
  public final RegistryId boss;
  public final Arena.BossRecord record;
  public final int grade;
  private final String name;
  private final int cost;

  public ArenaEntry(final RegistryId boss, final Arena.BossRecord record, final int grade) {
    this.boss = boss;
    this.record = record;
    this.grade = grade;

    final Encounter enc = GameEngine.REGISTRIES.encounters.getEntry(boss).get();
    final Set<String> names = new LinkedHashSet<>();
    int gold = 0;
    for(final Encounter.Monster monster : enc.monsters) {
      final int id = monster.id & 0x1ff;
      names.add(Monsters.monsterNames_80112068[id]);
      gold += Monsters.enemyRewards_80112868[id].gold_02;
    }
    final String first = names.iterator().next();
    this.name = first + " x" + grade;
    // Iscrizione = meta' dell'oro del boss, per grado, minimo 100G (in New Game+ l'oro del boss scala, l'iscrizione pure)
    this.cost = Math.max(100, Math.round(gold * 0.5f * wingsofdestiny.ngplus.NgScaling.factorFor(record.disc)) * grade);
  }

  public int cost() {
    return this.cost;
  }

  @Override
  public RegistryId getRegistryId() {
    return Arena.arenaId(this.boss);
  }

  @Override
  public ItemIcon getIcon() {
    return ItemIcon.HORN;
  }

  @Override
  public String getNameTranslationKey() {
    return this.name;
  }

  @Override
  public String getDescriptionTranslationKey() {
    final boolean first = this.grade == 3 && !Arena.X3_CLEARED.contains(this.boss);
    final boolean heirloom = first && this.record.disc >= 2 && !wingsofdestiny.ngplus.NewGamePlus.active();
    final int medals = (this.grade == 3 ? 4 : this.grade) * (first ? 2 : 1);
    return "Scaled to your level, x" + this.grade + ".\nGold x" + this.grade + ", " + medals + " medals, no EXP." + (this.grade == 3 ? "\nDragon Stone." : "") + (heirloom ? "\nFirst win: Heirloom!" : "");
  }

  @Override
  public int getSellPrice() {
    return 0;
  }

  @Override
  public int getBuyPrice() {
    return this.cost;
  }

  @Override
  public int getSize() {
    return 1;
  }

  @Override
  public int getMaxSize() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }
}
