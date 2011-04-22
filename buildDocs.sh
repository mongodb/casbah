#!/bin/sh

rm -rf docs/scaladoc

./.sbt "+update" "+clean" "+doc" "+all-docs"

cp -R casbah-core/scaladocBuild docs/scaladoc

mkdir -p docs/scaladoc/modules/casbah-commons
mkdir -p docs/scaladoc/modules/casbah-core
mkdir -p docs/scaladoc/modules/casbah-gridfs
mkdir -p docs/scaladoc/modules/casbah-query

cp -R casbah-commons/target/scala_2.8.1/classes.sxr docs/scaladoc/modules/casbah-commons/sxr
cp -R casbah-commons/target/scala_2.8.1/doc/main/api docs/scaladoc/modules/casbah-commons/api
cp -R casbah-core/target/scala_2.8.1/classes.sxr docs/scaladoc/modules/casbah-core/sxr
cp -R casbah-core/target/scala_2.8.1/doc/main/api docs/scaladoc/modules/casbah-core/api
cp -R casbah-gridfs/target/scala_2.8.1/classes.sxr docs/scaladoc/modules/casbah-gridfs/sxr
cp -R casbah-gridfs/target/scala_2.8.1/doc/main/api docs/scaladoc/modules/casbah-gridfs/api
cp -R casbah-query/target/scala_2.8.1/classes.sxr docs/scaladoc/modules/casbah-query/sxr
cp -R casbah-query/target/scala_2.8.1/doc/main/api docs/scaladoc/modules/casbah-query/api

cp doc_index.html docs/scaladoc/modules/index.html

cd tutorial_src

make clean html #epub latexpdf

#cp build/epub/CasbahMongoDBScalaToolkitDocumentation.epub ../docs/CasbahDocumentation.epub
#cp build/latex/CasbahDocumentation.pdf ../docs/CasbahDocumentation.pdf
cp -R build/html/* ../docs

cd ..

cd docs/scaladoc
perl -p -i -e 's#a href="http://api.mongodb.org/scala/casbah-(.*)/casbah-(.*)/sxr/.*/casbah-\2/src/main/scala/(.*)"#a href="/scala/casbah/\1/scaladoc/modules/casbah-\2/sxr/\3.scala.html"#gi' `find ./ -name \*.html`
perl -p -i -e 's#a href="http://api.mongodb.org/scala/casbah-(.*)/casbah-casbah-core/sxr/.*/casbah-(.*)/src/main/scala/(.*)"#a href="/scala/casbah/\1/scaladoc/modules/casbah-\2/sxr/\3.scala.html"#gi' `find ./ -name \*.html`

