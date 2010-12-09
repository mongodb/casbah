Getting Started
***************

Why Casbah?
===========

Casbah grew (originally named *mongo-scala-wrappers*) from the frustration of dealing with a very structured, objects & imperative only approach which was forced by the Mongo Java drivers.  These drivers are a fine, well built tool - but best suited to the pure Java programmer.  In addition, the primary developer (`Brendan McAdams <brendan@10gen.com>`_) came from a background of working with MongoDB primarily with Python.  He missed the fluid syntax and similarity to the JS Shell which Python provided (and let's face it, `pymongo <http://api.mongodb.org/python/>`_ is awesome), and the flexibility of syntax.  As he was learning Scala he saw the ability to provide much of this functionality.

During the course of building out application infrastructure with MongoDB + Scala, with the ideas of (Python|Mongo)esque syntax with a functional bent, Casbah slowly emerged.  Casbah provides improved interfaces to GridFS, Map/Reduce and the core Mongo APIs.  It also provides a fluid query syntax which emulates an internal DSL and allows you to write code which looks like what you might write in the JS Shell.  There is also support for easily adding new serialization/deserialization mechanisms for common data types (including Joda Time, if you so choose; with some caveats - See the GridFS Section).

With version 2.0, Casbah has become an official MongoDB project and will continue to improve the interaction of Scala + MongoDB. Casbah aims to remain fully compatible with the existing Java driver---it does not talk to MongoDB directly, preferring to wrap the Java code.  This means you shouldn't see any wildly unexpected behavior from the underlying Mongo interfaces when a data bug is fixed.

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
            <groupId>com.novus<groupId>
            <artifactId>casbah_2.8.0<artifactId>                           
            <version>2.0b3p1<version>
        <dependency>

Scala 2.8.1 users::

        <dependency>
            <groupId>com.novus<groupId>
            <artifactId>casbah_2.8.1<artifactId>                           
            <version>2.0b3p1<version>
        <dependency>
        

Setting Up Ivy (w/ Ant)
-----------------------
You can add Casbah to Ivy with the following dependency block.

Scala 2.8.0 users::

        <dependency org="com.novus" name="casbah_2.8.0" rev="2.0b3p1"/>

Scala 2.8.1 users::

        <dependency org="com.novus" name="casbah_2.8.1" rev="2.0b3p1"/>
        

Setting up SBT 
---------------
Finally, you can add Casbah to SBT by adding the following to your project file::

    val casbah = "com.novus" %% "casbah" % "2.0b3p1"

The double percentages (`%%`) is not a typo---it tells SBT that the library is crossbuilt and to find the appropriate version for your project's Scala version. If you prefer to be explicit you can use this instead::
    
    // Scala 2.8.0
    val casbah = "com.novus" % "casbah_2.8.0" % "2.0b3p1"
    // Scala 2.8.1
    val casbah = "com.novus" % "casbah_2.8.1" % "2.0b3p1"

Don't forget to reload the project and run ``sbt update`` afterwards to download the dependencies (SBT doesn't check every build like Maven).
