# Wings of Destiny — Mod compatibility

Wings of Destiny is built and tested for **Severed Chains 3.0.0-1816** with no other gameplay mod installed. This page lists what is known about running it next to other mods.

"Checked in the code" means we read how the other mod hooks into the game. "Presumed" means it follows from what the mod changes, but nobody has tested the pair.

## Summary

| Mod | Status | Basis |
| --- | --- | --- |
| Lloyd mod | Supported | Tested |
| Dragoon Modifier | Not supported — don't use together | Checked in the code, not play-tested |
| Battle Rewards | Not supported — don't use together | Presumed |
| Additional Additions | Untested — may affect New Game+ | Presumed |
| Additional Additions Editor | Unrelated to Wings of Destiny, but see below | Crash report |
| Visual / UI-only mods (Fashigoon, Turn Order, Stardust Indicators…) | No conflict expected | Presumed |

## Lloyd mod — supported

Wings of Destiny detects it and turns on Lloyd's Dragoon magic. In New Game+ Lloyd gets his previous data back when he joins again.

## Dragoon Modifier — not supported

Both mods load, and the game starts, but they rebalance the same things and Wings of Destiny was never balanced on top of Dragoon Modifier.

- **Monster stats.** Both mods rewrite them. Dragoon Modifier runs first, Wings of Destiny runs after it, so for most monsters the Wings of Destiny values win and your Dragoon Modifier difficulty setting is partly ignored. Monsters Wings of Destiny doesn't cover keep the Dragoon Modifier values: the result is a mix of the two.
- **EXP, gold and drops.** Same order: Wings of Destiny replaces the rewards Dragoon Modifier just set.
- **Additions, spells, equipment and items.** Dragoon Modifier loads its own tables for all of them. Wings of Destiny then adjusts additions and some spells and equipment assuming the standard game values. What you end up with is unpredictable. (Presumed.)
- **Shops.** Both mods add and remove stock in the same shops, so expect duplicated or missing entries. (Presumed.)
- **Duplicate items.** Several items exist in both mods under different internal names, with different stats.
- **Elite battles, Cursed Zones, the Arena and New Game+** multiply whatever stats they find, so they would run — but their numbers were tuned for Wings of Destiny's own tables and may be far too easy or too hard.

Pick one of the two for a campaign. Don't add one to a save started with the other.

## Battle Rewards — not supported

It multiplies EXP and gold. Wings of Destiny already has its own multipliers (Options → `[WoD] EXP Multiplier`, `[WoD] Gold Multiplier`, `[WoD] Addition EXP`) and its own reward table, so rewards would be multiplied twice. Use the Wings of Destiny options instead.

## Additional Additions — untested

It gives characters extra additions. Two things may be affected in New Game+:

- The first EX addition unlocks when **all** of a character's additions are mastered. Extra additions would count too, so the unlock takes longer.
- "Final" additions changed by another mod may not match the EX versions' numbers.

Outside New Game+ no conflict is expected.

## Additional Additions Editor — crash unrelated to Wings of Destiny

If the game closes right after you load a campaign with this error:

```
NoSuchFieldError: Class legend.game.SItem does not have member field 'java.lang.String[] characterNames_801142dc'
    at additionaladditions.screens.TopLevelScreen.<init>
```

the editor was built for a different Severed Chains version. Remove it from the `mods` folder, or get a build made for 3.0.0-1816. The same happens with or without Wings of Destiny.

## Reporting a problem

Send the crash log (`CRASHLOG-….log` in the game folder) or `debug.log`, and the list of mods you have installed. The line `Loaded mods: …` in the log is enough.
