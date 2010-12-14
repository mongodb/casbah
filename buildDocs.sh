#!/bin/sh

rm -rf docs/scaladoc
mkdir -p docs/scaladoc/casbah-commons
mkdir -p docs/scaladoc/casbah-core
mkdir -p docs/scaladoc/casbah-gridfs
mkdir -p docs/scaladoc/casbah-query

sbt "+update" "+clean" "+doc"

cp -R casbah-commons/target/scala_2.8.1/classes.sxr docs/scaladoc/casbah-commons/sxr
cp -R casbah-commons/target/scala_2.8.1/doc/main/api docs/scaladoc/casbah-commons/api
cp -R casbah-core/target/scala_2.8.1/classes.sxr docs/scaladoc/casbah-core/sxr
cp -R casbah-core/target/scala_2.8.1/doc/main/api docs/scaladoc/casbah-core/api
cp -R casbah-gridfs/target/scala_2.8.1/classes.sxr docs/scaladoc/casbah-gridfs/sxr
cp -R casbah-gridfs/target/scala_2.8.1/doc/main/api docs/scaladoc/casbah-gridfs/api
cp -R casbah-query/target/scala_2.8.1/classes.sxr docs/scaladoc/casbah-query/sxr
cp -R casbah-query/target/scala_2.8.1/doc/main/api docs/scaladoc/casbah-query/api

cp doc_index.html docs/scaladoc/index.html

cd tutorial_src

make clean html epub latexpdf

cp build/epub/CasbahMongoDBScalaToolkitDocumentation.epub ../docs/CasbahDocumentation.epub
cp build/latex/CasbahDocumentation.pdf ../docs/CasbahDocumentation.pdf
cp -R build/html/* ../docs

cd ..


cd docs/scaladoc
perl -p -i -e 's#a href="http://api.mongodb.org/scala/casbah-(.*)/casbah-(.*)/sxr/.*/casbah-\2/.*src/main/scala/(.*)"#a href="/scala/casbah/\1/scaladoc/casbah-\2/sxr/\3.scala.html"#gi' `find ./ -name \*.html`

