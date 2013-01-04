#!/bin/sh

SCALA=2.9.2
WEBSITE_ROOT=rozza.github.com/casbah/
SPHINX_DIR=./src/sphinx
SITE_DIR=./target/site/

./sbt ++2.9.2 "update" "clean" "make-site" "unidoc"

mkdir -p $SITE_DIR/api.sxr/casbah-commons
mkdir -p $SITE_DIR/api.sxr/casbah-core
mkdir -p $SITE_DIR/api.sxr/casbah-gridfs
mkdir -p $SITE_DIR/api.sxr/casbah-query

cp ./casbah-commons/target/scala-$SCALA/classes.sxr/* $SITE_DIR/api.sxr/casbah-commons
cp ./casbah-core/target/scala-$SCALA/classes.sxr/* $SITE_DIR/api.sxr/casbah-core
cp ./casbah-gridfs/target/scala-$SCALA/classes.sxr/* $SITE_DIR/api.sxr/casbah-gridfs
cp ./casbah-query/target/scala-$SCALA/classes.sxr/* $SITE_DIR/api.sxr/casbah-query

touch $SITE_DIR/.nojekyll

# Update the sxr in url
find $SITE_DIR/api/ -name \*html -exec sed -i 's#/src\(.*\)/\(.*scala.html\)#\2#' {} \;
# Update WEBSITE ROUTE
find $SITE_DIR/api/ -name \*html -exec sed -i "s#/{{WEBSITE_ROOT}}#/$WEBSITE_ROOT#g" {} \;

# Make pdf / epub
make -C $SPHINX_DIR clean epub latexpdf

cp $SPHINX_DIR/_build/epub/CasbahMongoDBScalaToolkitDocumentation.epub $SITE_DIR/CasbahDocumentation.epub
cp $SPHINX_DIR/_build/latex/CasbahDocumentation.pdf $SITE_DIR/CasbahDocumentation.pdf

if git diff-index --quiet HEAD --; then
    echo " ========================== "
    echo " Updating `gh-pages` branch"
    echo " ========================== "

    git co gh-pages
    mv target .target
    rm * -rf
    mv .target/site/* .
    mv .target target

    echo " Please check the new docs and checkin ..."
else
    echo "You have changes not checked-in - cannot automatically update gh-pages"
fi


