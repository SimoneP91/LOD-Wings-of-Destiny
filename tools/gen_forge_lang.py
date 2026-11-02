#!/usr/bin/env python3
"""Genera le voci di lingua delle armi forgiate (+1..+5) dal file forge_weapons.txt."""
import sys, zipfile
game_jar, wod_lang_path, list_path, out_path = sys.argv[1:5]

def props(text):
    d = {}
    for line in text.splitlines():
        if '=' in line and not line.lstrip().startswith('#'):
            k, v = line.split('=', 1)
            d[k.strip()] = v
    return d

lod = props(zipfile.ZipFile(game_jar).read('lod/lang/en.lang').decode('utf-8'))
wod = props(open(wod_lang_path, encoding='utf-8').read())
names = {**lod, **wod}
# Descrizioni cambiate dalla mod (vedi EquipmentBuffs)
names['lod.equipment.soul_eater.description'] = r'A dark blade\nthat thirsts\nfor battle.'
out = ['', '# --- Armi forgiate (generato da tools/gen_forge_lang.py) ---']
for line in open(list_path, encoding='utf-8'):
    rid = line.strip()
    if not rid:
        continue
    ns, entry = rid.split(':')
    name = names[f'{ns}.equipment.{entry}.name']
    desc = names.get(f'{ns}.equipment.{entry}.description', '')
    for lvl in range(1, 6):
        key = f'wings_of_destiny.equipment.forged_{entry}_{lvl}'
        out.append(f'{key}.name={name} +{lvl}')
        out.append(f'{key}.description={desc}' if desc.strip() else f'{key}.description=Forged weapon.')
open(out_path, 'w', encoding='utf-8').write('\n'.join(out) + '\n')
