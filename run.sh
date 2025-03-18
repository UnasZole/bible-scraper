#! /bin/sh

cd $(dirname "$0")

java -ea -jar scraper-cli/target/scraper-cli-*-jar-with-dependencies.jar --cachePath=tmp/ $@
