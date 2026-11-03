# Wings of Destiny

A gameplay overhaul for **The Legend of Dragoon** on **Severed Chains 3.0**: tougher monsters, Elite battles and Cursed Zones, a weapon Forge, a boss Arena in every major town and over 40 new pieces of equipment.

The story and its progression stay intact; Wings of Destiny adds reasons to explore, fight and come back. Every system can be tuned or turned off in the options.

> **Status:** beta. Back up your saves.

## Contents

- [Features](#features)
- [Installation](#installation)
- [Options](#options)
- [Difficulty and monsters](#difficulty-and-monsters)
- [Elite battles and Cursed Zones](#elite-battles-and-cursed-zones)
- [The Forge](#the-forge)
- [The Arena](#the-arena)
- [Equipment and items](#equipment-and-items)
- [New Game+](#new-game)
- [What's next](#whats-next)
- [Building from source](#building-from-source)
- [FAQ and troubleshooting](#faq-and-troubleshooting)

## Features

| Feature | What it does | Where |
| --- | --- | --- |
| Difficulty | Standard (slightly harder than the original) or Hard (built for a second playthrough) | Options |
| Monster HP display | Shows the targeted monster's HP as numbers and a coloured bar | In battle |
| Elite battles | Stronger enemies, double rewards | Any random battle |
| Cursed Zones | Whole areas become dangerous and generous | Any area with monsters |
| The Forge | Upgrade weapons from +1 to +5 | Every equipment shop |
| The Arena | Re-fight defeated bosses, scaled to your level | Item shops of the major towns |
| Arena Medals | Trade them for stones, rare items and the Seraph set | Item shops of the major towns |
| New equipment | Wyrmling, Elder and Heirloom weapons, new armour and accessories | Shops, drops, Arena |
| Reward tuning | EXP, gold and addition EXP multipliers | Options |

## Installation

**Requirements**

- Severed Chains 3.0.0 (tested on build 1816).

**Steps**

1. Download `wings_of_destiny.jar` from the [Releases](../../releases) page.
2. Close the game and copy the jar into `<Severed Chains folder>/mods/`.
3. Start the game and create a new campaign. Wings of Destiny is enabled by default.
4. Open **Options → Gameplay** and review the **[WoD]** options.

**Saves**

- Items, weapons and Arena progress are stored in your save under `wings_of_destiny:` IDs. Removing the mod from a campaign that used them will break those saves. Back up your saves before uninstalling.
- Wings of Destiny can be added to an existing campaign; monster stats, shops and systems apply from the next load.
- Updating to a newer version keeps your saves.

## Options

All options are under **Options → Gameplay** and start with **[WoD]**. Unless noted, they are saved per campaign and can be changed at any time.

| Option | Default | Range | Effect |
| --- | --- | --- | --- |
| Difficulty | Standard | Standard, Hard | Monster strength (see below) |
| Battle EXP Multiplier | ×1 | ×0 – ×10, steps of 0.25 | EXP earned from battles |
| Battle Gold Multiplier | ×1 | ×0 – ×10, steps of 0.25 | Gold earned from battles |
| Addition XP Multiplier | ×1 | ×1 – ×100 | Addition EXP earned in battle |
| Monster HP Numbers | On | On, Off | HP bar and numbers on the targeted monster (global) |
| Elite Chance (%) | 8 | 0 – 30 | Chance a random battle is Elite; 0 turns Elites off |
| Cursed Zones | On | On, Off | Turns Cursed Zones on or off |
| Cursed Zone Chance (%) | 20 | 0 – 50 | Chance an area is cursed when you enter it |

## Difficulty and monsters

Standard makes every monster a little tougher than the original game; Hard is a full rebalance meant for a second playthrough.

| | Standard | Hard |
| --- | --- | --- |
| HP, attack, magic attack, defence, magic defence, evasion | Original value + 25% of the gap to Hard | Hard values |
| Elements, immunities, status resistance, special effects | Original | Hard values |
| Final story bosses (Divine Dragon and its parts, Faust, first form of Melbu Frahma) | Original | Original |

**Monster rewards** (both difficulties)

- EXP and gold per monster: the higher of the original and the Hard value.
- Item drops come from reworked drop tables, with more and better items.
- New rare drops: Wraith Helm (100% from the Polter boss), Wraith Mail, Colossus Ring (rare, from Metal Fang), Eternal Prayer (100% from the Divine Dragoon Ghost).
- Glowmoss: 12% per monster in a few specific areas.

**Monster HP display**

When you target a monster, a bar under its name shows its current and maximum HP: blue above 50%, yellow at 50% or less, red at 25% or less. In Elite battles the bar turns gold and orange.

**Party changes**

- Ten additions hit harder and give more SP as they level up: Volcano, Flower Storm (Lavitz and Albert), Hard Blade, More & More, Demon's Dance, Cat's Cradle, Pursuit, Inferno, Bone Crush. Perky Step is unchanged.
- Shana and Miranda: Dragoon attack raised to 365%.
- Red-Eyed Dragon (Dart, Dragoon level 5): STR 100% on all enemies (was 75%).
- Wargod's Sash: SP gain +75% (was +50%). Spirit Ring: 30 SP per turn (was 20). Dancing Dagger: SP gain +50% (was none).
- Soul Eater no longer drains HP each turn.
- To leave Dragoon form at will, turn on Severed Chains' own *Extended Dragoon Actions* option.

## Elite battles and Cursed Zones

Any random battle can turn Elite, and any area can turn Cursed. Both make monsters stronger and pay back with bigger rewards, and they stack.

**Elite battles**

- 8% of random battles are Elite (option *Elite Chance*, 0–30%). The whole battle is Elite.
- **ELITE** appears in gold at the top right, and the monsters' HP bar turns gold and orange.

**Cursed Zones**

- Each time you enter an area with monsters, it has a 20% chance to be cursed (option *Cursed Zone Chance*, 0–50%). It stays cursed until you leave.
- "This area is cursed!" appears the first time you reach a screen with random battles; **Cursed Zone** stays at the top right while you are there, in battle too.
- Leaving the area (to another area or the world map) and coming back rolls again. Saving and reloading keeps the area as it was.
- **The Phantom Ship is always cursed.**
- The Divine Tree and the Moon are never cursed, except in New Game+.

**Effects**

| Battle | Monster HP | Attack and magic attack | Defence and magic defence | EXP, gold, drop chance | Chance of Elite |
| --- | --- | --- | --- | --- | --- |
| Normal | ×1 | ×1 | ×1 | ×1 | 8% |
| Elite | ×2 | ×1.3 | ×1.2 | ×2 | — |
| Cursed | ×1.5 | ×1.2 | ×1 | ×1.5 | 24% |
| Elite in a Cursed Zone | ×3 | ×1.56 | ×1.2 | ×3 | — |

Speed never changes. Drop chances are capped at 100%.

**Never Elite or Cursed:** bosses (boss music or multi-phase battles), story battles you cannot flee from, Arena challenges, battles that do not start inside an area.

## The Forge

Every equipment shop can upgrade your weapons from +1 to +5, for gold and forge stones. Each level adds 8% of the weapon's base attack.

**How to forge**

1. Unequip the weapon: only weapons in your bag can be forged.
2. Enter any equipment shop and choose **Buy**. Forge entries are at the top of the list, for example "Wyrmling Blade +1".
3. The description shows the attack gain and the stones needed, with how many you have.
4. Confirm: the weapon is replaced in the same bag slot by its upgraded version.

Bought or forged something new? Leave the shop and come back to refresh the list.

**Levels and costs**

| Level | Attack | Gold (share of the base weapon's price, min 50 G) | Stones |
| --- | --- | --- | --- |
| +1 | +8% | 10% | 1 Forge Stone |
| +2 | +16% | 12% | 2 Forge Stones |
| +3 | +24% | 14% | 3 Forge Stones |
| +4 | +32% | 16% | 1 Dragon Stone |
| +5 | +40% | 20% | 2 Dragon Stones |

- Each level adds at least +2 attack, so early weapons grow too (Broad Sword +5: 2 → 12).
- Element, status effects and every other stat stay as on the base weapon.
- Forged weapons sell for the base price × (1 + 0.5 per level).
- Forgeable: every standard weapon except those with special scripted effects (Destroyer Mace, Detonate Arrow), plus every Wings of Destiny weapon, Heirlooms included. 94 weapons in total.

**Getting stones** (at most one of each per battle won, shown in the battle results)

| Battle won | Forge Stone | Dragon Stone |
| --- | --- | --- |
| Random battle | 10% | — |
| Elite battle | 50% | — |
| Cursed Zone battle | 10% | 3% |
| Elite in a Cursed Zone | 50% | 5% |
| Story boss | — | — |
| Arena ×3 | — | 1, guaranteed |
| Story battle you cannot flee | — | — |

Stones can also be bought with Arena Medals. If a victory leads straight into a cutscene, the stone goes directly to your inventory.

## The Arena

The item shop of every major town (Bale, Lohan, Kazas, Fletz, Donau, Fueno, Furni, Deningrad, the Wingly Forest, Vellweb, Ulara, Rouge and Zenebatos) lets you re-fight any boss you have already beaten, scaled to your current level, for gold and Arena Medals, plus a Dragon Stone and a unique Heirloom weapon on the hardest grade.

**Unlocking bosses**

Every boss you defeat in the story is added to the Arena, with the battlefield you fought it on and your party's average level at the time. 52 bosses can be unlocked. Multi-phase and final battles are not available.

**Taking a challenge**

1. Go to the **item shop** of a major town and choose **Buy**. Challenges are at the top: "Urobolus x1", "Urobolus x2", "Urobolus x3".
2. Pick one and pay the entry fee.
3. Leave the shop: the battle starts on its own a moment later.
4. Win or lose, you return to the town you signed up in. Losing is not a game over: the party gets back up with 1 HP and you only lose the entry fee.

Leaving the area after signing up cancels the challenge and refunds the fee. The boss's in-battle scenes play as in the story, but nothing after the battle affects the story.

**Strength and rewards**

The boss's HP, attack and magic attack are multiplied by the grade and by how much stronger you have become since you beat it: your average level now ÷ your average level then (never below 1).

| Grade | Strength | Gold | Arena Medals | Dragon Stone | Heirloom |
| --- | --- | --- | --- | --- | --- |
| ×1 | ×1 × level ratio | ×1 | 1 | — | — |
| ×2 | ×2 × level ratio | ×2 | 2 | — | — |
| ×3 | ×3 × level ratio | ×3 | 4 (8 on the first win) | 1 | First win, bosses from disc 2 on |

- Example: Urobolus beaten at level 7, challenged at level 14 on ×3 = six times its original strength.
- No EXP. The boss's own item drop is guaranteed.
- Entry fee: half the boss's gold × grade, at least 100 G.
- The first ×3 win against each boss from disc 2 onwards awards one of the eight Heirloom weapons you don't have yet, at random.

**Medal Exchange**

Right after the challenges, the same item shops list items you buy with Arena Medals. Arena Medals are an inventory item.

| Item | Medals |
| --- | --- |
| Forge Stone | 2 |
| Glowmoss | 3 |
| Dragon Stone | 12 |
| Seraph Circlet | 30 |
| Seraph Greaves | 30 |
| Seraph Mail | 35 |
| Seraph Robe | 35 |
| Seraph Sigil | 40 |
| Eternal Prayer | 100 |

The Seraph set is only available here.

## Equipment and items

Prices are shop prices; every weapon below can be forged.

**Weapon progression**

| Series | Where | Price | When | Attack | Extras |
| --- | --- | --- | --- | --- | --- |
| Wyrmling | Bale | 300 G | Disc 1 | 14 – 48 | SP gain +20%, hit +10 |
| Greater Wyrmling | Fletz | 1,200 G | Disc 2 | 20 – 58 | SP gain +30%, hit +10 |
| Elder | Furni | 4,000 G | End of disc 2 | 32 – 65 | Element, magic attack |
| Heirloom | Arena | — | Disc 2 onwards | 70 | Element, status on hit, SP and MP each turn |

| Weapon type | Used by | Wyrmling | Greater Wyrmling | Elder (element, attack / magic attack) |
| --- | --- | --- | --- | --- |
| Blade | Dart | 15 | 30 | Fire, 55 / 15 |
| Pike | Lavitz, Albert | 15 | 28 | Wind, 50 / 15 |
| Bow | Shana, Miranda | 15 | 20 | Light, 32 / 30 |
| Estoc | Rose | 15 | 32 | Dark, 65 / 15 |
| Knuckle | Haschel | 23 | 33 | Thunder, 40 / 10 |
| Mace / Hammer | Meru | 14 | 28 | Water, 52 / 25 |
| Axe | Kongol | 48 | 58 | Earth, 65 / 10 |

**Heirloom weapons** (Arena reward) — all: attack 70, 25% status on hit, +20 SP and +5% MP each turn, maximum HP +25%, hit +25.

| Heirloom | Used by | Element | Magic attack | Status on hit | Other |
| --- | --- | --- | --- | --- | --- |
| Ember Fang | Dart | Fire | 25 | Fear | |
| Tide Fang | Dart | Water | 25 | Poison | |
| Gale Lance | Lavitz, Albert | Wind | 30 | Fear | Speed +5 |
| Nightveil Bow | Shana, Miranda | Dark | 35 | Poison | |
| Dawn Rapier | Rose | Light | 25 | Confusion | Speed +5 |
| Storm Knuckles | Haschel | Thunder | 25 | Stun | |
| Wave Mallet | Meru | Water | 10 | Confusion | |
| Giant's Cleaver | Kongol | Earth | 30 | Fear | Speed +10 |

**Elemental and short-sword weapons**

| Weapon | Used by | Element | Attack | Extras | Where | Price |
| --- | --- | --- | --- | --- | --- | --- |
| Fire / Aqua / Wind / Earth / Bolt / Light / Dark Buster | Rose | One each | 35 | Magic attack 5, SP gain +25% | Lohan | 1,000 G |
| Sabre | Rose | — | 35 | | Lohan | 800 G |
| Magma Edge | Dart | Fire | 25 | | Furni | 3,000 G |
| Umbral Estoc | Rose | Dark | 33 | | Furni | 3,000 G |
| Radiant Bow | Shana, Miranda | Light | 22 | Hit +20 | Furni | 3,000 G |
| Tidal Maul | Meru | Water | 28 | | Furni | 3,000 G |
| Granite Axe | Kongol | Earth | 31 | | Zenebatos | 3,000 G |
| Buster+ (7 elements) | Rose | One each | 70 | Magic attack 10, SP gain +25% | Ulara | 3,000 G |
| Great Sabre | Rose | — | 70 | | Ulara | 2,500 G |

**Armour, helmets, boots and accessories**

| Item | Slot | Worn by | Stats | Where | Price |
| --- | --- | --- | --- | --- | --- |
| Mystic Buckler | Accessory | All | Magic defence 8 | Bale | 200 G |
| Rogue's Slippers | Boots | All | Escape +50% | Bale | 200 G |
| Thick Robe | Armour | Women | Defence 20, magic defence 20, evasion 5 / 5 | Lohan | 150 G |
| Swift Boots | Boots | Men | Speed 5, defence 10, magic defence 10, evasion 5 | Lohan | 1,500 G |
| Moonsilk Dress | Armour | Women | Defence 27, magic defence 16, +2 MP when hit | Donau | 250 G |
| Bastion Charm | Accessory | All | Defence 16 | Fletz accessory shop | 300 G |
| Arcane Charm | Accessory | All | Magic defence 14 | Fletz accessory shop | 300 G |
| Runed Sollerets | Boots | All | Defence 18, magic defence 9 | Wingly Forest | 600 G |
| Runed Chausses | Boots | All | Evasion 15 / 15 | Wingly Forest | 600 G |
| Grandmaster's Band | Helmet | Haschel | Magic attack 25, defence 11, magic defence 11, +10 SP when hit | Vellweb | 200 G |
| War Horn | Accessory | All | Attack 30, magic attack 30 | Deningrad | 8,000 G |
| Ward Horn | Accessory | All | Defence 30, magic defence 30 | Deningrad | 8,000 G |
| Colossus Ring | Accessory | All | Attack 30 | Ulara, Moon | 600 G |
| Skyborn Suit | Armour | Meru | Defence 42, magic defence 42, MP +10%, MP each turn, +2 MP and +10 SP when hitting or hit | Ulara | 1,000 G |
| Wraith Helm | Helmet | All | Magic attack 35, defence 10, magic defence 10, HP +15%, MP +10%, evasion 5 / 5 | Drop | — |
| Wraith Mail | Armour | All | Defence 60, magic defence 40, HP +15%, +5 MP and SP when hitting or hit | Drop | — |
| Seraph Mail / Seraph Robe | Armour | Men / Women | Defence 50, magic defence 50, attack 5, magic attack 5, evasion 5 / 5, 10% chance to revive | Medal Exchange | 35 medals |
| Seraph Circlet | Helmet | All | Magic attack 50, magic defence 10, hit +25, MP +20% | Medal Exchange | 30 medals |
| Seraph Greaves | Boots | All | Speed 25, defence 10, evasion 10 / 10, HP +20% | Medal Exchange | 30 medals |
| Seraph Sigil | Accessory | All | Attack, magic attack, defence, magic defence +12, speed 10 | Medal Exchange | 40 medals |

**Items**

| Item | Effect | Where |
| --- | --- | --- |
| Eternal Prayer | In battle: revives an ally with full HP. Never used up. | Divine Dragoon Ghost (100%), Medal Exchange (100 medals) |
| Glowmoss | From the menu: full HP and MP and cures status for the whole party | 12% drop in a few areas, Medal Exchange (3 medals) |
| Forge Stone | Forges weapons to +1, +2, +3 | Battles, Medal Exchange (2 medals) |
| Dragon Stone | Forges weapons to +4, +5 | Cursed Zones, Arena ×3, Medal Exchange (12 medals) |
| Arena Medal | Currency of the Medal Exchange | Arena |

**Changes to existing shops**

- Deningrad equipment shop: Dragon Helm (4,000 G), Bandit's Ring and Dancer's Ring (4,000 G), Bandit's Shoes and Dancer's Shoes (2,500 G).
- Deningrad item shop: Psyche Bomb (500 G).
- Destroyer Mace moves from the Moon to Rouge.
- Moon item shop: Spirit Potion.

## New Game+

**Starting a New Game+**

When you reach the "The End" screen after the credits, Wings of Destiny creates a save called **New Game+** in the same campaign. Load it to start again from the beginning of disc 1. Each following cycle creates "New Game+ 2", "New Game+ 3" and so on.

**What you keep and what starts over**

| You keep | Starts over |
| --- | --- |
| Levels, EXP, Dragoon levels, magic | Gold (back to 20 G) |
| Mastered additions | The story, chests and key items |
| Equipped gear and everything in your bag, forged weapons and Heirlooms included | Dragoon Spirits: you transform again when the story gives them back |
| Forge Stones, Dragon Stones and Arena Medals | The Arena: bosses to unlock again, first ×3 bonus available again |

The party starts with Dart alone; the others join with the story, at the level you left them.

- Lavitz starts at least at Albert's level, Shana at least at Miranda's.
- When Albert and Miranda join, they keep their own data and take the better value from Lavitz and Shana for level, Dragoon level and each addition. Each keeps their own equipment.

**Monsters**

- Monsters always use the Hard values, whatever the Difficulty option.
- Monsters grow to the strength the game gives to monsters of your party's level. Every area has an expected level (Seles 4, Villude 13, Flanvel Tower 33, the Moon 46…), and the growth curves of HP, attack, EXP and gold come from the game's own monsters. Defence doesn't change.
- Example, party at level 43: a Knight of Sandora in Seles goes from 10 HP to about 2,800, attack 2 → 58, EXP 2 → 80. Monsters of the Moon at level 60: HP ×3, attack ×1.7.
- Never weaker than normal. Past level 46 (the Moon) the curves keep growing, more slowly.
- Each cycle after the first adds +25%: New Game++ ×1.25, New Game+++ ×1.5.
- Elite battles and Cursed Zones are 10% stronger (HP, attack, defence); their chance and rewards don't change.
- The Arena scales by the disc of the boss. The first ×3 win gives double medals; a set of New Game+ weapons is planned in place of the Heirlooms.

**Drops and the Forge**

- Item drops of normal battles come from the area that matches your level: a random consumable among those dropped by that area's monsters, with the same chance as the original drop. At level 43 they come from the Divine Tree, past 46 from the Moon. Bosses, the Arena and equipment drops don't change.
- Armour can be forged too, +1 to +5: +8% defence and magic defence per level (at least +2), same gold and stones as weapons.

**Dragoon magic**

- Dragoon magic does 30% more damage and healing in New Game+, and 30% more for each following cycle: ×1.3, ×1.6, ×1.9 and so on. Dragon summons, Divine Dragoon magic and Lloyd's magic included.
- Healing stays within maximum HP: in practice Gates of Heaven revives at 65% instead of 50%.
- Buffs, status effects and MP costs don't change.

**EX additions**

In New Game+ every character with additions (Shana and Miranda excluded) can learn two EX additions, enhanced versions of their last two additions, one after the other:

| Character | First EX | Second EX |
| --- | --- | --- |
| Dart | Moon Strike EX | Blazing Dynamo EX |
| Lavitz / Albert | Gust of Wind Dance EX | Flower Storm EX |
| Rose | Hard Blade EX | Demon's Dance EX |
| Haschel | Hex Hammer EX | Omni-Sweep EX |
| Meru | Cat's Cradle EX | Perky Step EX |
| Kongol | Inferno EX | Bone Crush EX |

- **First EX:** unlocks when all the character's original additions are mastered, the final one included. If you mastered them in your previous playthrough, it is there from the start.
- **Second EX:** unlocks when the first EX reaches level 10.
- Levels 1 to 10, same animation as the original. They need twice the uses of an original addition to level up, and every hit has a timing window one frame shorter.
- Each EX starts above what comes before it, in both damage and SP: the first EX at level 1 beats the original final addition mastered, the second EX at level 1 beats the first EX at level 10. Each level adds 2.5%.
- Example, Dart: Blazing Dynamo mastered 450 damage / 150 SP; Moon Strike EX 461 / 154 at level 1, 562 / 188 at level 10; Blazing Dynamo EX 577 / 192 at level 1, 703 / 234 at level 10.
- They don't count towards unlocking a character's final addition.

## What's next

Planned for future versions, in rough order:

1. **New Game+ weapons** — a set of weapons for the first ×3 Arena wins in New Game+.
2. **Dragoon rebalance** — Dragoon magic and Dragoon additions tuned for Hard difficulty and New Game+.
3. **Weapons with unique effects** — weapons that do more than add numbers, with effects tied to elements, remaining HP or SP.
4. **New Arena rewards** — items that permanently raise a stat by 1 and a unique accessory for each character, in the Medal Exchange.
5. **Quality of life** — a dedicated Forge menu separate from the Buy list, and shop lists that refresh without leaving the shop.

## Building from source

The full source code is in `src/`. To build the jar you need:

- JDK 25
- Severed Chains 3.0.0-1816 (the game jar and `libs/` folder are on the classpath)
- Python 3 (generates the names of the forged weapons)
- The Lloyd mod jar (only for the optional Lloyd compatibility)

```sh
SC=/path/to/SeveredChains JDK=/path/to/jdk-25 LLOYD_JAR=/path/to/lloyd-mod.jar ./build.sh
```

The result is `build/wings_of_destiny.jar`.

## FAQ and troubleshooting

**I don't see the Forge in the shop.** Only weapons in your bag can be forged: unequip one, then leave and re-enter the shop. Forge entries are at the top of the Buy list.

**I don't see Arena challenges.** Only bosses you defeated with Wings of Destiny installed are listed, in the item shops of the major towns.

**An Arena boss is too easy.** Bosses beaten before the Arena existed record your level at your first challenge; from then on they grow with you.

**Stones seem rare.** At most one of each per battle. About one Forge Stone every 8–9 battles is expected; Elite battles and the Medal Exchange are the faster routes.

**Known limitations**

- Arena bosses play their in-battle scenes, including death scenes, as in the story.
- Shop lists refresh only when you enter the shop.

**Reporting a problem**

Open an [issue](../../issues) and attach `debug.log` from the Severed Chains folder. The log is rewritten every time the game starts, so copy it right after the problem happens. Lines from the mod start with "Wings of Destiny:".
