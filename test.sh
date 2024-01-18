#! /bin/sh

cd $(dirname "$0")

java -cp target/osis-builder-0.1-SNAPSHOT.jar com.github.unaszole.bible.osisbuilder.Entrypoint tmp $1 $2 $3

