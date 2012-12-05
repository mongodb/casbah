Getting Started
***************

The latest Casbah build as of |today| is |release|, built for Scala 2.9.0-1, 2.9.1, 2.9.2, and 2.10.0-RC1.

Installing & Setting up Casbah
==============================

You should have `MongoDB <http://mongodb.org>`_ setup  and running on your machine (these docs assume you are running on *localhost* on the default port of *27017*) before proceeding. If you need help setting up MongoDB please see `the MongoDB quickstart install documentation <http://www.mongodb.org/display/DOCS/Quickstart>`_.

To start with, you need to either download the latest Casbah driver and place it in your classpath, or set it up in the dependency manager/build tool of your choice (The authors highly recommend the Scala `simple-build-tool <http://code.google.com/p/simple-build-tool/>`_ - it makes Scala development easy).

*PLEASE NOTE*: As of the 2.0 release, Casbah has been broken into
 several modules which can be used to strip down which features you need.  For example, you can use the Query DSL independent of the GridFS implementation if you wish. The following dependency manager information uses the master artifact which downloads and uses *all* of Casbah's modules by default.

Setting up SBT 
---------------
You can easily add Casbah to SBT, the Scala Build Tool, by adding the following to your `build.sbt` file, substituting $CASBAH_VERSION$ with the latest Casbah release (as of |today|, that is |release|)::

   libraryDependencies += "org.mongodb" % "casbah" % "$CASBAH_VERSION$"


Alternately, if you are using a `.scala` Build definition, the following entry will work::

    val casbah = "org.mongodb" %% "casbah" % "$CASBAH_VERSION$"

The double percentages (`%%`) is not a typo---it tells SBT that the library is crossbuilt and to find the appropriate version for your project's Scala version.

Don't forget to reload the project and run ``sbt update`` afterwards to download the dependencies (SBT doesn't check every build like Maven).

 
Using Dependency/Build Managers
-------------------------------

First, you should add the package repository to your Dependency/Build Manager. Our releases & snapshots are currently hosted at Sonatype; they should eventually sync to the Central Maven repository.::

   https://oss.sonatype.org/content/repositories/releases/ /* For Releases */
   https://oss.sonatype.org/content/repositories/snapshots/ / /* For snapshots */

Set both of these repositories up in the appropriate manner - they contain Casbah as well as any specific dependencies you may require. (SBT users note that Scala-Tools is builtin to SBT as most Scala projects publish there)


Setting Up Maven
-----------------
You can add Casbah to Maven with the following dependency block. 

Please substitute $SCALA_VERSION$ with your Scala version (We support 2.9.x+) and $CASBAH_VERSION$ with the latest Casbah release (as of |today|, that is |release|)::

        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>casbah_$SCALA_VERSION$</artifactId>                           
            <version>$CASBAH_VERSION$</version>
        <dependency>

        

Setting Up Ivy (w/ Ant)
-----------------------
You can add Casbah to Ivy with the following dependency block.

Please substitute $SCALA_VERSION$ with your Scala version (We support 2.9.x+) and $CASBAH_VERSION$ with the latest Casbah release (as of |today|, that is |release|)::

        <dependency org="org.mongodb" name="casbah_$SCALA_VERSION$" rev="$CASBAH_VERSION$"/>
        

Setting up without a Dependency/Build Manager (Source + Binary)
----------------------------------------------------------------


The builds are published to the `Sonatype.org <https://oss.sonatype.org/content/repositories/releases/>`_ Maven repositories and should be easily available to add to an existing Scala project.

You can always get the latest source for Casbah from `the github repository <https://github.com/mongodb/casbah>`_::

    $ git clone git://github.com/mongodb/casbah

At the time of this writing, the *master* branch lies fallow, and most development is done in the `release-2.3+` branch, instead.

Casbah Modules
---------------
While Casbah has a large stable of features, some users (such as those using a framework like Lift which already provides MongoDB wrappers) wanted access to certain parts of Casbah without importing the whole system.  As a result, Casbah has been broken out into several modules which make it easier to pick and choose the features you want.

If you use the individual modules you'll need to use the import statement from each of these.  If you use the import statement from the `casbah-core` module, everything except GridFS will be imported (not everyone uses GridFS so we don't load it into memory & scope unless it is needed).  The module names can be used to select which dependencies you want from maven/ivy/sbt, as we publish individual artifacts.  If you import just `casbah`, this is a master pom which includes the whole system and can be used just like 1.1.x was (that is to say, you can pretend the module system doesn't exist more or less).


This is the breakdown of dependencies and packages for the new system:

  +-------------------------------------+----------------------------+-------------------------------------------------+
  | Module                              | Package                    | Dependencies                                    | 
  +=====================================+============================+=================================================+
  | :ref:`casbah-commons` ("Commons")   | com.mongodb.casbah.commons |                                                 |
  |                                     |                            |   mongo-java-driver,                            |
  | **NOTES**                           |                            |   scalaj-collection,                            |
  | Provides Scala-friendly             |                            |   scalaj-time,                                  |
  | :dochub:DBObject & :dochub:DBList   |                            |   JodaTime,                                     |
  | implementations as well as Implicit |                            |   slf4j-api                                     |
  | conversions for Scala types         |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :ref:`casbah-query` ("Query DSL")   | com.mongodb.casbah.query   | :ref:`casbah-commons`                           |
  |                                     |                            | along with its dependencies transitively        |
  | **NOTES**                           |                            |                                                 |
  | Provides a Scala syntax enhancement |                            |                                                 |
  | mode for creating MongoDB query     |                            |                                                 |
  | objects using an Internal DSL       |                            |                                                 |
  | supporting Mongo `$ Operators`      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :ref:`casbah-core` ("Core")         | com.mongodb.casbah         | :ref:`casbah-commons` and :ref:`casbah-query`   |
  |                                     |                            | along with their dependencies transitively      |    
  | **NOTES**                           |                            |                                                 |
  | Provides Scala-friendly             |                            |                                                 |
  | wrappers to the Java Driver for     |                            |                                                 |
  | connections, collections and        |                            |                                                 |
  | MapReduce jobs                      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :ref:`casbah-gridfs` ("GridFS")     | com.mongodb.casbah.gridfs  | :ref:`casbah-core` and :ref:`casbah-commons`    |
  |                                     |                            | along with their dependencies transitively      |
  | **NOTES**                           |                            |                                                 |
  | Provides Scala enhanced wrappers    |                            |                                                 |
  | to MongoDB's GridFS filesystem      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  
We cover the import of each module in their appropriate tutorials, but each module has its own `Imports` object which loads all of its necessary code.  By way of example both of these statements would import the Query DSL::

    // Imports core, which grabs everything including Query DSL
    import com.mongodb.casbah.Imports._ 
    // Imports just the Query DSL along with Commons and its dependencies
    import com.mongodb.casbah.query.Imports._
    
    

