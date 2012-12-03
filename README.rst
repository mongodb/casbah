======
Casbah
======
:Info: Scala toolkit for MongoDB (com.mongodb.casbah). See `the mongo site <http://www.mongodb.org>`_ for more information. See `github <http://github.com/mongodb/casbah/tree>`_ for the latest source.
:Author: Brendan W. McAdams
:Maintainer: Ross Lawley <ross@10gen.com>

About
=====
Casbah is an interface for `MongoDB <http://www.mongodb.org>`_ designed to
provide more flexible access from both Java and Scala.  While the current core
focus is on providing a Scala oriented wrapper interface around the Java mongo
driver, support for other JVM languages may come in the future.

For the Scala side, contains series of wrappers and DSL-like functionality for
utilizing MongoDB from within Scala. This currently utilises the very
Java-oriented Mongo Java driver, and attempts to provide more scala-like
functionality on top of it. This has been tested with MongoDB 1.2.x+ and 2.x of
the Mongo java driver.

We are constantly adding new functionality, and maintain a detailed
`Casbah Tutorial <http://api.mongodb.org/scala/casbah/tutorial.html>`_.

Please address any questions or problems to the
`Casbah Mailing List <http://groups.google.com/group/mongodb-casbah-users>`_ on
Google Groups.

For more information about Casbah see the
`API Docs <http://api.mongodb.org/scala/casbah/scaladoc/>`_ or
`Tutorial <http://api.mongodb.org/scala/casbah/tutorial.html>`_.

Project Artifacts
-----------------

Casbah is separated out into several artifacts:

* *casbah-commons*
   Provides utilities to improve working with Scala and MongoDB together
   without dependencies on anything but the MongoDB Java Driver and
   ScalaJ-Collection.  This includes Scala Collections 2.8 compatible
   wrappers for DBList and DBObject as well as type conversion facilities to
   simplify the use of Scala types with MongoDB (and register your own custom
   types)
* *casbah-query*
   The Query DSL which provides an internal Scala DSL for querying MongoDB
   using native, MongoDB syntax operators.  This only depends upon Commons and
   can be used standalone without the rest of Casbah.
* *casbah-core*
   This is the wrappers for interacting directly with MongoDB providing more
   Scala-like interactions.  It depends upon both Commons and Query as well as
   ScalaTime for use of JodaTime (which we prefer over JDK date but you are
   welcome to use JDK Dates).
* *casbah-gridfs*
   This provides enhancement wrappers to GridFS including loan pattern
   support.  It is dependent on Core (and by transitive property, Commons &
   Query as well) but is not included in Core - you must explicitly load if it
   you want to use GridFS.

Contributing
------------

Please report all issues on the
`MongoDB Jira Scala project <http://jira.mongodb.org/browse/SCALA>`_.

We also maintain a
`Casbah Mailing List <http://groups.google.com/group/mongodb-casbah-users>`_
on Google Groups, where you can address questions and problems.

Notice for users of old, Pre-2.0 versions of Casbah
---------------------------------------------------

The package has changed as of version 2.0 to `com.mongodb.casbah`; version 1.1
of Casbah used the package `com.novus.casbah` and versions 1.0 and below were
packaged as `com.novus.casbah.mongodb`.
