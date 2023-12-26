#! /bin/sh

cd $(dirname "$0")

java -cp target/osis-builder-0.1-SNAPSHOT.jar com.github.unaszole.bible.implementations.Chouraqui tmp/Chouraqui "$1" "$2"

