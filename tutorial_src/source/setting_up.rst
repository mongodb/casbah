Getting Started
***************

Installing & Setting up Casbah
==============================

You should have `MongoDB <http://mongodb.org>`_ setup  and running on your machine (these docs assume you are running on *localhost* on the default port of *27017*) before proceeding. If you need help setting up MongoDB please see `the MongoDB quickstart install documentation <http://www.mongodb.org/display/DOCS/Quickstart>`_.

To start with, you need to either download the latest Casbah driver and place it in your classpath, or set it up in the dependency manager/build tool of your choice (The authors highly recommend the Scala `simple-build-tool <http://code.google.com/p/simple-build-tool/>`_ - it makes Scala development easy).

Setting up without a Dependency/Build Manager (Source + Binary)
----------------------------------------------------------------

The latest build as of |today| is |release|, cross-built for both Scala 2.8.0 (final) and Scala 2.8.1 (final). 

The builds are published to the `Scala-tools.org <http://scala-tools.org>`_ Maven repositories and should be easily available to add to an existing Scala project.

You can always get the latest source for Casbah from `the github repository <https://github.com/mongodb/casbah>`_::

    $ git clone git://github.com/mongodb/casbah

*PLEASE NOTE*: As of the 2.0 release, Casbah has been broken into
 several modules which can be used to strip down which features you need.  For example, you can use the Query DSL independent of the GridFS implementation if you wish; please see :ref:`casbah-modules`.  The following dependency manager information uses the master artifact which downloads and uses *all* of Casbah's modules by default.
 
Using Dependency/Build Managers
-------------------------------

First, you should add the package repository to your Dependency/Build Manager. Our releases & snapshots are currently hosted at::

   http://scala-tools.org/repo-releases/ /* For Releases */
   http://scala-tools.org/repo-snapshots/ /* For snapshots */

Set both of these repositories up in the appropriate manner - they contain Casbah as well as any specific dependencies you may require. (SBT users note that Scala-Tools is builtin to SBT as most Scala projects publish there)


Setting Up Maven
-----------------
You can add Casbah to Maven with the following dependency block. 

Scala 2.8.0 users::

        <dependency>
            <groupId>com.mongodb.casbah<groupId>
            <artifactId>casbah_2.8.0<artifactId>                           
            <version>2.0rc0<version>
        <dependency>

Scala 2.8.1 users::

        <dependency>
            <groupId>com.mongodb.casbah<groupId>
            <artifactId>casbah_2.8.1<artifactId>                           
            <version>2.0rc0<version>
        <dependency>
        

Setting Up Ivy (w/ Ant)
-----------------------
You can add Casbah to Ivy with the following dependency block.

Scala 2.8.0 users::

        <dependency org="com.mongodb.casbah" name="casbah_2.8.0" rev="2.0rc0"/>

Scala 2.8.1 users::

        <dependency org="com.mongodb.casbah" name="casbah_2.8.1" rev="2.0rc0"/>
        

Setting up SBT 
---------------
Finally, you can add Casbah to SBT by adding the following to your project file::

    val casbah = "com.mongodb.casbah" %% "casbah" % "2.0rc0"

The double percentages (`%%`) is not a typo---it tells SBT that the library is crossbuilt and to find the appropriate version for your project's Scala version. If you prefer to be explicit you can use this instead::
    
    // Scala 2.8.0
    val casbah = "com.mongodb.casbah" % "casbah_2.8.0" % "2.0rc0"
    // Scala 2.8.1
    val casbah = "com.mongodb.casbah" % "casbah_2.8.1" % "2.0rc0"

Don't forget to reload the project and run ``sbt update`` afterwards to download the dependencies (SBT doesn't check every build like Maven).

Migrating to Casbah 2.0 from Casbah 1.x
========================================

If you used Casbah before, and are looking to migrate from Casbah 1.x to Casbah 2.x
there are some things which have changed and you should be aware of to effectively update your code.

Base Package Name 
------------------
For starters, the base package has changed.  The now abandoned 1.1.x branch which 
became 2.0 was already doing a package change, and with 2.0 Casbah has become a
supported MongoDB project. As a result, Casbah's package has changed for the 2.0 
release and you will need to update your code accordingly:

===========================  ===============================  =====================
Casbah 1.0.x                  Casbah 1.1.x (never released)    Casbah 2.0
===========================  ===============================  =====================
 com.novus.casbah.mongodb      com.novus.casbah                 com.mongodb.casbah
===========================  ===============================  =====================

.. _casbah-modules:

Removed Features
----------------
A number of features existed in Casbah as artifacts of early prototyping.  They were buggy, poorly tested and because of their nature often introduced weird problems for users.

As a result, they have been removed now that replacement versions of their functionality exist.

Removal of Implicit Tuple -> DBObject Conversions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Previously, it was possible with Casbah to cast Tuples to :dochub:`DBObject`::
    
    val x: DBObject = ("foo" -> "bar", "x" -> 5, "y" -> 238.1)

This feature was provided by implicit conversions which attempt to target `Product` which is the base class of all Tuples.  Unfortunately, this functionality was often unreliable and targeted the wrong things for conversion (Such as instances of `Option[_]`).  After a lot of evaluation and attempts to create a better approach a decision was made to remove this feature.  Casbah 2.0 includes wrappers for :dochub:`DBObject` which follow Scala 2.8's Collection interfaces including Scala compatible builders and constructors.  As such, the same previous syntax is possible by passing the Tuple pairs to `MongoDBObject.apply`::

    val x: DBObject = MongoDBObject("foo" -> "bar", "x" -> 5, "y" -> 238.1)
    /* x: com.mongodb.casbah.Imports.DBObject = { "foo" : "bar" , "x" : 5 , "y" : 238.1} */
    val y = MongoDBObject("foo" -> "bar", "x" -> 5, "y" -> 238.1)          
    /* y: com.mongodb.casbah.commons.Imports.DBObject = { "foo" : "bar" , "x" : 5 , "y" : 238.1} */

We also provide a builder pattern which follows Scala 2.8's Map Builder::

    val b = MongoDBObject.newBuilder
    /* b: com.mongodb.casbah.commons.MongoDBObjectBuilder = com.mongodb.casbah.commons.MongoDBObjectBuilder@113f25e3 */
    b += "x" -> 5
    b += "y" -> 238.1
    b += "foo" -> "bar"
    val x: DBObject = b.result
    /* x: com.mongodb.casbah.commons.Imports.DBObject = { "x" : 5 , "y" : 238.1 , "foo" : "bar"} */

Finally, any Scala map can still be cast to a DBObject without issue::

    val x: DBObject = Map("foo" -> "bar", "x" -> 5, "y" -> 238.1)
    /* x: com.mongodb.casbah.Imports.DBObject = { "foo" : "bar" , "x" : 5 , "y" : 238.1} */

It is *still* possible to use Tuples in the :ref:`Query DSL <casbah-query>` however, as there is less need for broad implicit conversions to accomplish that functionality.

`batchSafely` Removed
^^^^^^^^^^^^^^^^^^^^^
Casbah 1.1.x introduced a `batchSafely` command which used the Java Driver's `requestStart()`, `requestDone()` and `getPrevErrors()` functions.  MongoDB is deprecating the use of `getPrevErrors()` and as such, Casbah has removed the functionality in anticipation of that feature going away in a near future release.

New Features
-------------

Query DSL Operators
^^^^^^^^^^^^^^^^^^^^
Casbah previously lagged behind the official MongoDB server in supported :ref:`Query DSL <casbah-query>` `$ Operators`.  As of 2.0, all `$ Operators` currently documented as supported in MongoDB are provided.  A list of some of the new operators added in 2.0 include:

    * :dochub:`$slice`
    * :dochub:`$or`
    * :dochub:`$not`
    * :dochub:`$each` (*special operator only supported nested inside :dochub:`$addToSet`*)
    * :dochub:`$type` (*Uses type arguments and class manifests to allow a nice fluid Scala syntax*)
    * :dochub:`$elemMatch`
    * Array Operators
    * All GeoSpatial Operators including :dochub:`$near` and :dochub:`$within`

Further, the DSL system has been completely overhauled.  As part of adding test coverage a number of edge cases were discovered with the DSL that caused inconsistent behavior.  The majority of the Query DSL should continue to work the same, but we have started moving to the use of Type Classes and Context Boundaries to limit what a valid input to any given operator is (2.1 will include expanded use of these merged with custom serializers).  

New syntax for $not
~~~~~~~~~~~~~~~~~~~
In order to fix a number of bugs and readability issues with the :dochub:`$not` operator, it has been modified.

Previously, the correct syntax for using :dochub:`$not` was::

    "foo".$not $gte 15 $lt 35.2 $ne 16
    
With Casbah 2.0, this syntax has been modified to be more clear to both the developer *and* the compiler::

    "foo" $not { _ $gte 15 $lt 35.2 $ne 16 }
    
The same syntax is supported for the special version of :dochub:`$pull` which allows for nested operator tests.

Casbah Modules
---------------
While Casbah has a large stable of features, some users (such as those using a framework like Lift which already provides MongoDB wrappers) wanted access to certain parts of Casbah without importing the whole system.  As a result, Casbah has been broken out into several modules which make it easier to pick and choose the features you want.

If you use the individual modules you'll need to use the import statement from each of these.  If you use the import statement from the `casbah-core` module, everything except GridFS will be imported (not everyone uses GridFS so we don't load it into memory & scope unless it is needed).  The module names can be used to select which dependencies you want from maven/ivy/sbt, as we publish individual artifacts.  If you import just `casbah`, this is a master pom which includes the whole system and can be used just like 1.1.x was (that is to say, you can pretend the module system doesn't exist more or less).


This is the breakdown of dependencies and packages for the new system:

  +-------------------------------------+----------------------------+------------------------------------------------------------------------------------+
  | Module                              | Package                    | Dependencies                                                                       | 
  +=====================================+============================+====================================================================================+
  | :ref:`casbah-commons` ("Commons")   | com.mongodb.casbah.commons |                                                                                    |
  |                                     |                            |   `mongo-java-driver <https://github.com/mongodb/mongo-java-driver/>`_,            |
  | **NOTES**                           |                            |   `scalaj-collection <https://github.com/scalaj/scalaj-collection/>`_,             |
  | Provides Scala-friendly             |                            |   `scalaj-time <https://github.com/scalaj/scalaj-time/>`_                          |
  | :dochub:DBObject & :dochub:DBList   |                            |   `JodaTime <http://joda-time.sourceforge.net/>`_,                                 |
  | implementations as well as Implicit |                            |   `slf4j-api <http://www.slf4j.org/>`_                                             |
  | conversions for Scala types         |                            |                                                                                    |
  +-------------------------------------+----------------------------+------------------------------------------------------------------------------------+
  | :ref:`casbah-query` ("Query DSL")   | com.mongodb.casbah.query   | :ref:`casbah-commons` along with its dependencies transitively                     |
  |                                     |                            |                                                                                    |
  | **NOTES**                           |                            |                                                                                    |
  | Provides a Scala syntax enhancement |                            |                                                                                    |
  | mode for creating MongoDB query     |                            |                                                                                    |
  | objects using an Internal DSL       |                            |                                                                                    |
  | supporting Mongo `$ Operators`      |                            |                                                                                    |
  +-------------------------------------+----------------------------+------------------------------------------------------------------------------------+
  | :ref:`casbah-core` ("Core")         | com.mongodb.casbah         | :ref:`casbah-commons` and :ref:`casbah-query` along with their dependencies        |
  |                                     |                            | transitively                                                                       |    
  | **NOTES**                           |                            |                                                                                    |
  | Provides Scala-friendly             |                            |                                                                                    |
  | wrappers to the Java Driver for     |                            |                                                                                    |
  | connections, collections and        |                            |                                                                                    |
  | MapReduce jobs                      |                            |                                                                                    |
  +-------------------------------------+----------------------------+------------------------------------------------------------------------------------+
  | :ref:`casbah-gridfs` ("GridFS")     | com.mongodb.casbah.gridfs  | :ref:`casbah-core` and :ref:`casbah-commons` along with their dependencies         |
  |                                     |                            | transitively                                                                       |
  | **NOTES**                           |                            |                                                                                    |
  | Provides Scala enhanced wrappers    |                            |                                                                                    |
  | to MongoDB's GridFS filesystem      |                            |                                                                                    |
  +-------------------------------------+----------------------------+------------------------------------------------------------------------------------+
  
We cover the import of each module in their appropriate tutorials, but each module has its own `Imports` object which loads all of its necessary code.  By way of example both of these statements would import the Query DSL::

    // Imports core, which grabs everything including Query DSL
    import com.mongodb.casbah.Imports._ 
    // Imports just the Query DSL along with Commons and its dependencies
    import com.mongodb.casbah.query.Imports._
    
    
