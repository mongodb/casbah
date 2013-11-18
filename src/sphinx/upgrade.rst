Upgrade
=======

Version 2.7.0
-------------

* OpLog util fixes - Opid is now an option.
* Scala 2.9.1 and 2.9.2 are no longer supported
* Updated nscala-time to 0.6.0
* `com.mongodb.casbah.MongoCollection.findOne` signatures have to changed to a main defaulted method
* `com.mongodb.casbah.MongoCollection.mapReduce` may now throw an exception if maxDuration is exceeded
* `com.mongodb.casbah.MongoCollection.count` and `com.mongodb.casbah.MongoCollection.getCount`
  signatures have changed to add `Duration` but are defaulted.
* `com.mongodb.casbah.WriteConcern.valueOf` now returns an Option as an invalid name
  would return a `null`.  Any code relying on it should be updated.


Version 2.6.1
-------------

The com.mongodb.casbah.commons.test dependencies are now marked in the test
classpath, so to install::

    "org.mongodb" %% "casbah-commons" % "2.6.2" % "test"


Version 2.6.0
-------------

No upgrade needed.

Version 2.5.1
-------------

Scala 2.10
~~~~~~~~~~

The `-Yeta-expand-keeps-star` compiler flag is no longer required.

Version 2.5.0
-------------

Scala 2.10
~~~~~~~~~~

Because of how scala 2.10 handles repeated parameters you may
need to build with the `-Yeta-expand-keeps-star` flag to upgrade your codebase.
