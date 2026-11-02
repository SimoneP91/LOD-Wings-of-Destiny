#!/bin/sh
# Compila la Wings of Destiny contro il jar del gioco.
# Uso: SC=<cartella di Severed Chains> JDK=<JDK 25> LLOYD_JAR=<jar della Lloyd mod> ./build.sh
set -e
SC=${SC:?imposta SC sulla cartella di Severed Chains}
JDK=${JDK:-$JAVA_HOME}
LLOYD_JAR=${LLOYD_JAR:-libs/lloyd-mod-1.0-SNAPSHOT.jar}
CP="$SC/lod-game-ab5dfe5ba6d4c07464a2f0e67985b9e77e2f7bdb.jar:$SC/libs/mod-loader-4.3.5.jar:$SC/libs/log4j-api-2.26.1.jar:$SC/libs/joml-1.10.8.jar:$SC/libs/fastutil-8.5.15.jar:$SC/libs/jspecify-1.0.0.jar:$SC/libs/jsr305-3.0.2.jar:$SC/libs/annotations-13.0.jar:$LLOYD_JAR"
rm -rf build && mkdir -p build/classes
"$JDK/bin/javac" --release 25 -Xlint:all,-processing,-serial -cp "$CP" -d build/classes $(find src/main/java -name '*.java')
cp -r src/main/resources/. build/classes/
# Nomi delle armi forgiate (+1..+5), generati dalla lista delle armi base
python3 tools/gen_forge_lang.py "$SC/lod-game-ab5dfe5ba6d4c07464a2f0e67985b9e77e2f7bdb.jar" src/main/resources/wings_of_destiny/lang/en.lang src/main/resources/wings_of_destiny/data/forge_weapons.txt build/forge.lang
cat build/forge.lang >> build/classes/wings_of_destiny/lang/en.lang
(cd build/classes && "$JDK/bin/jar" --create --file ../wings_of_destiny.jar .)
echo "OK -> build/wings_of_destiny.jar"
