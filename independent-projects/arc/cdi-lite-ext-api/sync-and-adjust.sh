#!/bin/bash

CDI_SPEC_CLONE=/home/lthon/projects/cdi-spec/

rm -rf src/main/java/javax

mkdir -p src/main/java/javax/enterprise/lang/model
cp -a $CDI_SPEC_CLONE/api/src/main/java/jakarta/enterprise/lang/model/* src/main/java/javax/enterprise/lang/model

mkdir -p src/main/java/javax/enterprise/inject/build/compatible/spi
cp -a $CDI_SPEC_CLONE/api/src/main/java/jakarta/enterprise/inject/build/compatible/spi/* src/main/java/javax/enterprise/inject/build/compatible/spi

for F in $(find src/main/java -name *.java) ; do
  sed -i -e 's|jakarta.enterprise|javax.enterprise|' $F
done
