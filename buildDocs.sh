#!/bin/sh

SCALA=2.9.2
WEBSITE_ROOT=rozza.github.com/casbah/

./sbt "update" "clean" "make-site" "unidoc"

mkdir -p ./target/site/api.sxr/casbah-commons
mkdir -p ./target/site/api.sxr/casbah-core
mkdir -p ./target/site/api.sxr/casbah-gridfs
mkdir -p ./target/site/api.sxr/casbah-query

cp ./casbah-commons/target/scala-$SCALA/classes.sxr/* ./target/site/api.sxr/casbah-commons
cp ./casbah-core/target/scala-$SCALA/classes.sxr/* ./target/site/api.sxr/casbah-core
cp ./casbah-gridfs/target/scala-$SCALA/classes.sxr/* ./target/site/api.sxr/casbah-gridfs
cp ./casbah-query/target/scala-$SCALA/classes.sxr/* ./target/site/api.sxr/casbah-query

touch ./target/site/.nojekyll

# Update the sxr in url
find ./target/site/api/ -name \*html -exec sed -i 's#/src\(.*\)/\(.*scala.html\)#\2#' {} \;
# Update WEBSITE ROUTE
find ./target/site/api/ -name \*html -exec sed -i "s#/{{WEBSITE_ROOT}}#/$WEBSITE_ROOT#g" {} \;