#!/bin/bash

CDI_TCK_CLONE=/home/lthon/projects/cdi/tck/

rm -rf src/main/java
mkdir -p src/main/java
cp -a $CDI_TCK_CLONE/lang-model/src/main/java src/main

for F in $(find src/main/java -name *.java) ; do
  sed -i -e 's|jakarta.enterprise|javax.enterprise|' $F
done
