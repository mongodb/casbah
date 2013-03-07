Getting Started
***************

The latest Casbah build as is |release| and supports scala versions: |scala_versions|.

Installing & Setting up Casbah
==============================

You should have `MongoDB <http://mongodb.org>`_ setup  and running on your machine (these docs assume you are running on *localhost* on the default port of *27017*) before proceeding. If you need help setting up MongoDB please see `the MongoDB quickstart install documentation <http://www.mongodb.org/display/DOCS/Quickstart>`_.

Now you need to install the latest Casbah driver - version is |release| and is supports scala versions; |scala_versions|.  This is normally done by configuring it in the dependency manager/build tool of your choice. The authors highly recommend the Scala `simple-build-tool <http://code.google.com/p/simple-build-tool/>`_ aka "SBT" as it makes Scala development easy.

Setting up SBT
---------------
You can easily add Casbah to SBT, the Scala Build Tool, by adding the following to your `build.sbt` file

.. parsed-literal::

   libraryDependencies += "org.mongodb" %% "casbah" % "|release|"


Alternately, if you are using a `.scala` Build definition, the following entry will work

.. parsed-literal::

    val casbah = "org.mongodb" %% "casbah" % "|release|"

The double percentages (`%%`) is not a typo---it tells SBT that the library is crossbuilt and to find the appropriate version for your project's Scala version.

Don't forget to reload the project and run ``sbt update`` afterwards to download the dependencies (SBT doesn't check every build like Maven).


Using Dependency/Build Managers
-------------------------------

First, you should add the package repository to your Dependency/Build Manager. Our releases & snapshots are currently hosted at Sonatype; they should eventually sync to the Central Maven repository.::

   https://oss.sonatype.org/content/repositories/releases/  /* For Releases */
   https://oss.sonatype.org/content/repositories/snapshots/ /* For snapshots */

Set both of these repositories up in the appropriate manner - they contain Casbah as well as any specific dependencies you may require.


Setting Up Maven
-----------------
You can add Casbah to Maven with the following dependency block.

Please substitute $SCALA_VERSION$ with your Scala version (we support 2.9.x+)

.. parsed-literal::

        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>casbah_$SCALA_VERSION$</artifactId>
            <version>|release|</version>
            <type>pom</type>
        <dependency>



Setting Up Ivy (w/ Ant)
-----------------------
You can add Casbah to Ivy with the following dependency block.

Please substitute $SCALA_VERSION$ with your Scala version (we support 2.9.x+)

.. parsed-literal::

        <dependency org="org.mongodb" name="casbah_$SCALA_VERSION$" rev="|release|"/>


Setting up without a Dependency/Build Manager (Source + Binary)
----------------------------------------------------------------


The builds are published to the `Sonatype.org <https://oss.sonatype.org/content/repositories/releases/>`_ Maven repositories and should be easily available to add to an existing Scala project.

You can always get the latest source for Casbah from `the github repository <https://github.com/mongodb/casbah>`_::

    $ git clone git://github.com/mongodb/casbah

The `master` branch is once again the leading branch suitable for snapshots and
releases and should be considered (and kept) stable.

.. _casbah-core:
.. _casbah-commons:
.. _casbah-query:
.. _casbah-gridfs:

Casbah Modules
--------------

While Casbah has a large stable of features, some users (such as those using a framework like Lift which already provides MongoDB wrappers) wanted access to certain parts of Casbah without importing the whole system.  As a result, Casbah has been broken out into several modules which make it easier to pick and choose the features you want.

If you use the individual modules you'll need to use the import statement from each of these.  If you use the import statement from the `casbah-core` module, everything except GridFS will be imported (not everyone uses GridFS so we don't load it into memory & scope unless it is needed).  The module names can be used to select which dependencies you want from maven/ivy/sbt, as we publish individual artifacts.  If you import just `casbah`, this is a master pom which includes the whole system and can be used just like 1.1.x was (that is to say, you can pretend the module system doesn't exist more or less).


This is the breakdown of dependencies and packages for the new system:

  +-------------------------------------+----------------------------+-------------------------------------------------+
  | Module                              | Package                    | Dependencies                                    |
  +=====================================+============================+=================================================+
  | :casbah-core:`Casbah Core`          | com.mongodb.casbah         | casbah-commons and casbah-query                 |
  |                                     |                            | along with their                                |
  | **NOTES**                           |                            | dependencies                                    |
  |                                     |                            | transitively                                    |
  | Provides Scala-friendly             |                            |                                                 |
  | wrappers to the Java Driver for     |                            |                                                 |
  | connections, collections and        |                            |                                                 |
  | MapReduce jobs                      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :casbah-commons:`Casbah Commons`    | com.mongodb.casbah.commons |                                                 |
  |                                     |                            |  * mongo-java-driver,                           |
  | **NOTES**                           |                            |  * nscala-time,                                 |
  |                                     |                            |  * slf4j-api,                                   |
  | Provides Scala-friendly             |                            |  * slf4j-jcl                                    |
  | :dochub:`DBObject` &                |                            |                                                 |
  | :dochub:`DBList`                    |                            |                                                 |
  | implementations as well as Implicit |                            |                                                 |
  | conversions for Scala types         |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :casbah-query:`Query DSL`           | com.mongodb.casbah.query   | casbah-commons                                  |
  |                                     |                            | along with their                                |
  | **NOTES**                           |                            | dependencies                                    |
  |                                     |                            | transitively                                    |
  | Provides a Scala syntax enhancement |                            |                                                 |
  | mode for creating MongoDB query     |                            |                                                 |
  | objects using an Internal DSL       |                            |                                                 |
  | supporting Mongo `$ Operators`      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :casbah-gridfs:`Gridfs`             | com.mongodb.casbah.gridfs  | casbah-commons and casbah-query                 |
  |                                     |                            | along with their                                |
  | **NOTES**                           |                            | dependencies                                    |
  |                                     |                            | transitively                                    |
  | Provides Scala enhanced wrappers    |                            |                                                 |
  | to MongoDB's GridFS filesystem      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+

We cover the import of each module in their appropriate tutorials, but each module has its own `Imports` object which loads all of its necessary code.  By way of example both of these statements would import the Query DSL::

    // Imports core, which grabs everything including Query DSL
    import com.mongodb.casbah.Imports._
    // Imports just the Query DSL along with Commons and its dependencies
    import com.mongodb.casbah.query.Imports._

.. |scala_versions| replace:: 2.9.1, 2.9.2 and 2.10.0