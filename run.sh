#! /bin/sh

cd $(dirname "$0")

java -ea -jar target/bible-scraper-0.1-SNAPSHOT-jar-with-dependencies.jar --cachePath=tmp/ $@
