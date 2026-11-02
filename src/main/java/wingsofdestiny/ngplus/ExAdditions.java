package wingsofdestiny.ngplus;

import legend.core.GameEngine;
import legend.core.tags.ListTag;
import legend.core.tags.StringTag;
import legend.core.tags.Tag;
import legend.game.additions.Addition;
import legend.game.additions.AdditionRegistryEvent;
import legend.game.additions.UnlockState;
import legend.game.characters.AdditionUnlockCriterion;
import legend.game.characters.CharacterAdditionInfo;
import legend.game.characters.CharacterData2c;
import legend.game.types.GameState52c;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.Registrar;
import org.legendofdragoon.modloader.registries.RegistryId;
import wingsofdestiny.WingsOfDestiny;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Le additions EX del New Game+: le ultime due additions di ogni personaggio (Shana e Miranda escluse), a catena.
 * - Prima EX (penultima originale): si sblocca quando tutte le additions originali sono padroneggiate, finale compresa.
 * - Seconda EX (finale originale): si sblocca quando la prima EX arriva al livello 10.
 * Le EX esistono solo nei personaggi di una partita New Game+. Il gioco ricrea i personaggi a ogni caricamento
 * e tiene solo le additions di base, quindi livello, EXP e sblocco delle EX si salvano nei dati della mod.
 */
public class ExAdditions {
  private static final Logger LOGGER = LogManager.getFormatterLogger(ExAdditions.class);
  private static final Registrar<Addition, AdditionRegistryEvent> REGISTRAR = new Registrar<>(GameEngine.REGISTRIES.additions, WingsOfDestiny.MOD_ID);

  /** Personaggio (template retail) -> penultima e ultima addition originale. */
  private static final Map<String, List<String>> CHAINS = new LinkedHashMap<>();

  static {
    CHAINS.put("dart", List.of("moon_strike", "blazing_dynamo"));
    CHAINS.put("lavitz", List.of("gust_of_wind_dance", "flower_storm"));
    CHAINS.put("albert", List.of("albert_gust_of_wind_dance", "albert_flower_storm"));
    CHAINS.put("rose", List.of("hard_blade", "demons_dance"));
    CHAINS.put("haschel", List.of("hex_hammer", "omni_sweep"));
    CHAINS.put("meru", List.of("cats_cradle", "perky_step"));
    CHAINS.put("kongol", List.of("inferno", "bone_crush"));

    for(final List<String> chain : CHAINS.values()) {
      final String first = chain.get(0);
      final String second = chain.get(1);
      // Prima EX: deve superare la finale originale. Seconda EX: deve superare la prima EX al livello 10.
      REGISTRAR.register(first + "_ex", () -> new ExAddition(new RegistryId("lod", first), new RegistryId("lod", second)));
      REGISTRAR.register(second + "_ex", () -> new ExAddition(new RegistryId("lod", second), exId(first)));
    }
  }

  @EventListener
  public void registerAdditions(final AdditionRegistryEvent event) {
    REGISTRAR.registryEvent(event);
  }

  private static RegistryId exId(final String base) {
    return new RegistryId(WingsOfDestiny.MOD_ID, base + "_ex");
  }

  /** Le EX di un personaggio, nell'ordine della catena (vuoto per Shana, Miranda e personaggi non retail). */
  static List<RegistryId> forCharacter(final CharacterData2c character) {
    final RegistryId template = character.template.getRegistryId();
    if(!"lod".equals(template.modId()) || !CHAINS.containsKey(template.entryId())) {
      return List.of();
    }
    final List<RegistryId> ids = new ArrayList<>();
    for(final String base : CHAINS.get(template.entryId())) {
      ids.add(exId(base));
    }
    return ids;
  }

  /** Tutte le additions originali del personaggio (finale compresa) al livello massimo. */
  private static final AdditionUnlockCriterion ORIGINALS_MASTERED = (character, additionInfo) -> {
    for(final RegistryId id : character.getAllAdditions()) {
      if(!GameEngine.REGISTRIES.additions.hasEntry(id)) {
        continue;
      }
      final Addition addition = GameEngine.REGISTRIES.additions.getEntry(id).get();
      if(addition instanceof ExAddition) {
        continue;
      }
      if(!addition.isComplete(character, character.getAdditionInfo(id))) {
        return false;
      }
    }
    return true;
  };

  /** Una EX sbloccata e al livello 10. */
  private static AdditionUnlockCriterion exMastered(final RegistryId ex) {
    return (character, additionInfo) -> {
      final CharacterAdditionInfo info = character.getAdditionInfo(ex);
      return info != null && info.getUnlockState().isUsable() && info.level >= ExAddition.MAX_LEVEL;
    };
  }

  private static CharacterAdditionInfo newInfo(final List<RegistryId> chain, final int position) {
    return new CharacterAdditionInfo(List.of(position == 0 ? ORIGINALS_MASTERED : exMastered(chain.get(0))));
  }

  /** Aggiunge le EX che mancano, ancora da sbloccare, e sblocca subito quelle che hanno gia' le condizioni. */
  static void grant(final GameState52c state, final int timestamp) {
    for(final CharacterData2c character : state.charData_32c) {
      final List<RegistryId> chain = forCharacter(character);
      for(int i = 0; i < chain.size(); i++) {
        final RegistryId id = chain.get(i);
        if(character.getAdditionInfo(id) == null) {
          character.addAddition(id, newInfo(chain, i));
        }
      }
      // In ordine: la seconda guarda la prima
      for(int i = 0; i < chain.size(); i++) {
        final CharacterAdditionInfo info = character.getAdditionInfo(chain.get(i));
        if(info.checkUnlock(character)) {
          info.unlock(timestamp + i);
        }
      }
    }
  }

  /**
   * New Game+: i momenti di sblocco delle additions arrivano dalla partita precedente e sarebbero piu' avanti del tempo di gioco nuovo.
   * Li rinumeriamo 0, 1, 2... nell'ordine della lista del personaggio (quello del gioco), cosi' le EX sbloccate dopo compaiono in fondo.
   */
  static void renumberUnlocks(final CharacterData2c character) {
    int next = 0;
    for(final RegistryId id : character.getAllAdditions()) {
      final CharacterAdditionInfo info = character.getAdditionInfo(id);
      if(info.getUnlockState().isUsable()) {
        info.setUnlockState(UnlockState.UNLOCKED, next++);
      }
    }
  }

  /** Stato delle EX di ogni personaggio: "indice|addition|livello|exp|sbloccata|momento". */
  static ListTag write(final GameState52c state) {
    final ListTag list = new ListTag();
    for(int i = 0; i < state.charData_32c.size(); i++) {
      final CharacterData2c character = state.charData_32c.get(i);
      for(final RegistryId id : forCharacter(character)) {
        final CharacterAdditionInfo info = character.getAdditionInfo(id);
        if(info != null) {
          final boolean unlocked = info.getUnlockState().isUsable();
          list.add(new StringTag(i + "|" + id + "|" + info.level + "|" + info.xp + "|" + unlocked + "|" + info.getUnlockTimestamp()));
        }
      }
    }
    return list;
  }

  /** Rimette le EX salvate nei personaggi appena caricati. */
  static void read(final GameState52c state, final Tag tag) {
    if(tag == null) {
      return;
    }
    for(final Tag entry : tag.asList()) {
      try {
        final String[] p = entry.asString().get().split("\\|");
        final int index = Integer.parseInt(p[0]);
        final RegistryId id = new RegistryId(p[1]);
        if(index < 0 || index >= state.charData_32c.size()) {
          continue;
        }
        final CharacterData2c character = state.charData_32c.get(index);
        final List<RegistryId> chain = forCharacter(character);
        final int position = chain.indexOf(id);
        if(position < 0) {
          continue;
        }
        CharacterAdditionInfo info = character.getAdditionInfo(id);
        if(info == null) {
          info = newInfo(chain, position);
          character.addAddition(id, info);
        }
        info.level = Math.clamp(Integer.parseInt(p[2]), 1, ExAddition.MAX_LEVEL);
        info.xp = Math.max(0, Integer.parseInt(p[3]));
        if(Boolean.parseBoolean(p[4])) {
          info.setUnlockState(UnlockState.UNLOCKED, Integer.parseInt(p[5]));
        }
      } catch(final RuntimeException e) {
        LOGGER.warn("Wings of Destiny: addition EX non leggibile dal save: %s", entry);
      }
    }
  }

  /** Se l'addition selezionata e' una EX che il personaggio non ha o non ha sbloccato, si torna alla prima disponibile. */
  static void fixSelection(final GameState52c state) {
    for(final CharacterData2c character : state.charData_32c) {
      final RegistryId selected = character.selectedAddition_19;
      if(selected == null || !WingsOfDestiny.MOD_ID.equals(selected.modId())) {
        continue;
      }
      final CharacterAdditionInfo info = character.getAdditionInfo(selected);
      if(info == null || !info.getUnlockState().isUsable()) {
        final List<RegistryId> unlocked = character.getUnlockedAdditions();
        character.selectedAddition_19 = unlocked.isEmpty() ? null : unlocked.getFirst();
      }
    }
  }
}
