==========
End of Life Notice
==========
MongoDB Casbah is now officially end-of-life (EOL). No further development, bugfixes, enhancements, documentation changes or maintenance will be provided by this project and pull requests will no longer be accepted.

Users are encouraged to migrate to the `Mongo Scala Driver <http://mongodb.github.io/mongo-scala-driver/>`_ for a modern idiomatic MongoDB Scala driver.

------------------------

Casbah
======

Casbah is a legacy interface for `MongoDB <http://www.mongodb.org>`_ designed to
provide more flexible access from both Java and Scala.  The core focus is on
providing a Scala oriented wrapper interface around the Java mongo driver.

For the Scala side, contains series of wrappers and DSL-like functionality for
utilizing MongoDB from within Scala. This currently utilises the very
Java-oriented Mongo Java driver, and attempts to provide more scala-like
functionality on top of it. This has been tested with MongoDB 1.2.x+ and 2.x of
the Mongo java driver.

For more information see the `Casbah documentation hub <http://mongodb.github.io/casbah/>`_.

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

