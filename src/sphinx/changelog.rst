Changelog
=========

Changes in Version 2.7.0
------------------------

- Added :doc:`examples` to highlight using Casbah in various scenarios
- Added Collection helper method to support the parallelCollectionScan command (SCALA-139)
- Fixed getAs[Type]("key") to stop invalid casts to Some(value) (SCALA-136)
- Support vargs for getAs[Type]("keys") (SCALA-134)
- Support vargs for as[Type]("Keys") (SCALA-134) (pr/#61)
- Fixed issue with OpLog matching (pr/#63)
- Register the core Serialization helpers only once (SCALA-129)
- Removed scala 2.9.1 and 2.9.2 support, casbah utilises
  `scala.concurrent.duration` which was added in scala 2.9.3
- Updated nscala-time to 0.6.0 and specs
- Added support for Server automatically abort queries/commands after
  user-specified time limit (SCALA-118)
- Added support for Aggregation returning a cursor `AggregationCursor` (SCALA-117)
- Added support for `allowDiskUse: true` to the top-level of an aggregate command with the AggregationOptions helper
- Added support `$out` aggregation pipeline operator (SCALA-130)
- Casbah WriteConcern's `valueOf` now returns an Option (SCALA-127)
- Support the use of a different SPN for Kerberos Authentication (SCALA-103)
- Support SASL PLAIN authentication (SCALA-101)
- Support MONGODB-X509 authentication (SCALA-112)
- Updated Mongo Java Driver to 2.12

Changes in Version 2.6.5
------------------------
- Updated Mongo Java Driver to 2.11.4

Changes in Version 2.6.4
------------------------
- Updated Scala 2.10 series to 2.10.3

Changes in Version 2.6.3
------------------------
- Fixed JodaGridFS when registered helpers are on (SCALA-113)
- Updated Mongo Java Driver to 2.11.3

Changes in Version 2.6.2
------------------------
- Fixed MongoClientURI Implicit
- Added support for Joda-Time LocalDate serialisation (SCALA-111, #59)
- Added aggregate collection helper (SCALA-110)
- Added $each support to $pull (SCALA-109)
- Updated to the latest Java driver 2.11.2 (SCALA-106)
- Added $eq operator (SCALA-105)
- Fixed $where dsl operator (SCALA-97)

Changes in Version 2.6.1
------------------------
- Fixed $pushAll and $pullAll casting of iterables (SCALA-54)
- Fixed MongoCollection string representation (SCALA-96)
- Fixed support for jsScope (SCALA-43) (#44)
- Publish casbah.commons test helpers
- Added suport $setOnInsert to the query dsl

Changes in Version 2.6.0
------------------------

- Added suport for GSSAPI SASL mechanism and MongoDB Challenge Response protocol
- Updated support for latest Java driver 2.11.1

Changes in Version 2.5.1
------------------------

- Added 2.10.1 support
- Removed reference to scala-tools (SCALA-78)
- Added 2.9.3 support (SCALA-94)
- Removed Specs2 and Scalaz dependencies outside test (SCALA-93)
- Fixed 2.10 support, no need for -Yeta-expand-keeps-star compile flag (SCALA-89)
- Fixed distinct regression (SCALA-92)
- Fixed test data import - now in tests :)

Changes in Version 2.5.0
------------------------

-  Added support for Scala 2.10.0
-  Dropped support for Scala 2.9.0
-  Dropped support for Scala 2.8.X
-  Updated support for latest Java driver 2.10.1
-  Added support for the new MongoClient connection class
-  Removed scalaj.collections dependency
-  Updated to nscala-time
-  Updated the build file
-  Added unidoc and updated documentation
-  Migrated documentation theme
-  Updated MongoDBList to handle immutable params
-  Maven Documentation fix (SCALA-71)
-  MongoOpLog - uses new MongoClient and defaults to replciaSet oplog database

Changes in Version 2.4.1
------------------------

-  Fixed QueryDSL imports for "default" (com.mongodb.casbah.Imports)
   import so that bareword ops like $set and $inc are available.

Changes in Version 2.4.0
------------------------

-  Hide BasicDBList; now, getAs and As and related will always return a
   MongoDBList which is a Seq[\_]. Enjoy!
-  This is an API breakage - you should *never* get back a
   BasicDBList from Casbah anymore, and asking for one will cause a
   ClassCastException. This brings us more in line with sane Scala
   APIs

Changes in Version 2.3.0
------------------------

BT/Maven Package change. Casbah is now available in: "org.mongodb" %%
"casbah" % "2.3.0"

-  Update mongo-java-driver to 2.8.0 release
-  Updated to Mongo Java Driver 2.8.0-RC1
-  Changed some tests to run sequentially to avoid shared variable
   races.
-  JodaGridFS wasn’t properly checked in before.
-  Updated MongoOptions to sync up with options provided in Java Driver.
-  Pre-Beta milestone (linked against unreleased Java Driver release)
-  Dropped Scala 2.8.0 support...

   -  2.1.5-1 is the final Casbah release for 2.8.0; please migrate to
      Scala 2.8.1 or higher

-  SCALA-62: Simple solution - hack the date type on the base class.

   -  There is now a JodaGridFS implementation which works cleanly with
      Joda DateTime and will return them to you

-  Backport casbah-gridfs from 3.0

   -  Fixes SCALA-45: Allow filename and contentType to be nullable

      -  Retrieving filename or contentType on a GridFS File now returns
         Option[String] when fetched
      -  To facilitate sane usage, the
         loan-pattern/execute-around-resource methods now return the
         \_id of the created file as Option[AnyRef]

-  Backports to casbah-core from 3.0

   -  SCALA-70: Removed type alias to com.mongodb.WriteConcern and made
      method args for it explicit, as it was causing a fun post-compile
      (aka "library compiles, user code doesn’t") implosion.
   -  added socketKeepAlive option
   -  Fixes SCALA-45: Allow filename and contentType to be nullable
   -  Retrieving filename or contentType on a GridFS File now returns
      Option[String] when fetched
   -  To facilitate sane usage, the loan-pattern/execute-around-resource
      methods now return the \_id of the created file as Option[AnyRef]

-  Backports for QueryDSL

   -  Major cleanups and bugfixes to the DSL, it’s heavily and fully
      tested now and much faster/cleaner
   -  Added support for $and bareword operator
   -  SCALA-30, SCALA-59 - $or is not properly accepting nested values
      esp. from other DSL constructors

      -  Introduced proper type class filter base to fix $or, will
         implement across other operators next.

   -  SCALA-59 - Fix Bareword Query Operators to better target accepted
      values; should only accept KV Tuple Pairs or DBObjects returned
      from Core Operators

      -  Complete test suites for $and and $nor although they need to be
         updated to more appropriate contextual examples rather than
         just "compiles properly"
      -  New code logic, fixed $or, $and and $nor for proper nested list
         operations
      -  New :: list cons operator on MongoDBObject to create
         MongoDBLists on th fly (esp. for DSL)
      -  Typesafety kungfu from @jteigen

         -  enforce at compile time that type parameters used for
            casting are not Nothing
         -  enforce $pushAll & $pullAll arguments can be converted to
            Iterable at compile time
         -  switched to a type class (AsQueryParam) for queryparams to
            avoid code duplication

-  SCALA-69: Maps saved to DBObject are now eagerly converted to a
   DBObject, from factory, builder and put methods.
-  Always return MongoDBList from Factories/Builders instead of Seq[Any]
-  Backports from Casbah 3.0

   -  Refactor collections (MongoDBList and MongoDBObject)
   -  Use CanBuildFrom properly to compose more appropriate Collection
      objects
   -  As part of above, you should get seq-like objects back from
      MongoDBList builders & factories instead of the previous
      BasicDBList; this is part of attempting to "Hide" DBList and let
      people work with List/Seq
   -  SCALA-69: Immediately upon saving any None’s will be converted to
      null inside the DBObject for proper fetching later.
   -  Add toString, hashCode and equals methods to DBObject
   -  New, refactored tests for DBObject and DBList

      -  More typesafety kungfu from @jteigen

         -  enforce at *compile time* that type parameters used for
            casting ( as, getAs, getAsOrElse ) are not Nothing

-  Backport Test Helpers

   -  New MongoDB "smart" test helpers for Specs2 and ScalaTest (Thanks
      Bill Venners for the latter)

-  Added SBT Rebel cut, local runner

Changes in Version 2.1.5.0
--------------------------

-  Added support for Scala 2.9.0-1 … As this is a critical fix release
   against 2.9.0.final, 2.9.0.final is not supported. (Note that SBT,
   etc requires the artifact specified as 2.9.0-1, not 2.9.0.1)
-  Apart from BugFixes this will be the last Casbah release which
   supports Scala 2.8.0; all future releases will require Scala 2.8.1+
   (See `2.8.0 EOL Announcement`_)
-  [2.9.0 only] Adjusted dynamic settings to build against 2.9.0-1 and
   Casbah 2.1.5.0
-  [2.9.0 only] Prototype "Dynamic" module (You must enable Scala’s
   support for Dynamic)
-  [2.9.0 only] I seem to have missed project files for SBT and
   casbah-dynamic
-  [2.9.0 only] Tweaks and adjustments to get this building and testing
   solidly on 2.9.0-1
-  Disabled a few tests that weren’t passing and known to be ‘buggy’ in
   specs1. These are fixed for the upcoming 2.2. release on specs2; they
   are test bugs rather than Casbah bugs.
-  RegEx `not was just flat out wrong - was producing
   {"foo": {"foo": /<regex>/}} instead of {"foo": {"`\ not":{//}}
-  Added a getAsOrElse method

.. _2.8.0 EOL Announcement: http://groups.google.com/group/mongodb-casbah-users/browse_thread/thread/faea8dbd5f90aa25

Changes in Version 2.1.0
------------------------

-  SCALA-22 Added a dropTarget boolean option to rename collection,
   which specifies behavior if named target collection already exists,
   proxies JAVA-238
-  Removed resetIndexCache, which has also been removed from the Java
   Driver
-  SCALA-21 Added "set metadata" method to match Java Driver (See
   Java-261)
-  SCALA-20 Updated to Java Driver 2.5

   -  See Release Notes:
      http://groups.google.com/group/mongodb-user/browse\_thread/thread/a693ad4fdf9c3731/931f46f7213b6775?show\_docid=931f46f7213b6775

-  SCALA-21 - Update GridFS to use DBObject views. Holding back full
   bugfix until we have a 2.5 build to link against
-  Example adjustments to filter by start time and namespace
-  SCALA-10 - And this is why we unit test. Size was returning empty for
   cursor based results as it wasn’t pulling the right value. Fixed,
   calling cursor.size.
-  Added an alternative object construction method for MongoDBObject
   with a list of pairs, rather than varargs [philwills]
-  Making scaladoc for MongoURI more explicit. Note that the wiki markup
   for lists isn’t actually implemented in scaladoc yet. [philwills]
-  Refactor Collection and Cursors using Abstract types, explicit
   ‘DBObject’ version is always returned from DB, Collection etc now.
   Those wanting to use typed versions must code the flip around by
   hand. !!! BREAKING CHANGE, SEE CODE / EXAMPLES
-  SCALA-10 Updated MapReduce interfaces to finish 1.8 compatibility

   -  Renamed MapReduceError to MapReduceException; MapReduceError is a
      non exception which represents a failed job
   -  Changed MapReduceResult to automatically proxy ‘results’ in inline
      result sets

-  Added missing methods to GridFSDBFile necessary to access the
   underlying datastream
-  Fixed setter/getter of option on cursor
-  For several reasons changed backing trait of DBList PML from Buffer
   to LinearSeq
-  Moved to new MapReduce functionality based on MongoDB 1.7.4+ !!! You
   must now specify an output mode.

   -  See
      http://blog.evilmonkeylabs.com/2011/01/27/MongoDB-1\_8-MapReduce/

-  MapReduce failures shouldn’t throw Error which can crash the runtime
-  New MapReduceSpec updates to include tests against new MongoDB
   MapReduce logic

Changes in Version 2.0.2
------------------------

-  Fixed the MongoDBOBject ‘as’ operator to return the proper type,
   instead of Any. (philwills)

Changes in Version 2.0.1
------------------------

-  SCALA-16: Added a few additional validation tests against getAs and
   as on MongoDBObject
-  SCALA-17 - Fixed syntax of $within and its nested operators, unit
   test passes

Version 2.0 / 2011-01-03
------------------------

Notable Changes since Casbah 1.0.8.1:

-  Ownership Change: Casbah is now an officially supported MongoDB
   Driver

   -  All bugs should be reported at
      http://jira.mongodb.org/browse/SCALA
   -  Package Change: Casbah is now ``com.mongodb.casbah`` (See
      migration guide)
   -  Documentation (ScalaDocs, Migration Guide & Tutorial) is available
      at http://mongodb.github.com/casbah

-  Casbah is now broken into several submodules - see
   http://mongodb.github.com/casbah/migrating.html
-  Casbah releases are now published to http://scala-tools.org
-  SBT Build now publishes -sources and -javadoc artifacts
-  Added heavy test coverage
-  ++ additivity operator on MongoDBObject for lists of tuple pairs
-  Updates to Java Driver wrappings

   -  Casbah now wraps Java Driver 2.4 and fully supports all options &
      interfaces including Replica Set and Write Concern support
   -  added a WriteConcern helper object for Scala users w/ named &
      default args
   -  added findAndModify / findAndRemove

-  Stripped out support for implicit Product/Tuple conversions as
   they’re buggy and constantly interfere with other code.
-  Migrated Conversions code from core to commons, repackaging as
   com.mongodb.casbah.commons.conversions

   -  Moved loading of ConversionHelpers from Connection creation to
      instantiation of Commons’ Implicits (This means conversions are
      ALWAYS loaded now for everyone)

-  Switched off of configgy to slf4j as akka did

   -  Added SLF4J-JCL Bindings as a +test\* dependency (so we can print
      logging while testing without forcing you to use an slf4j
      implementation yourself)

   -  Moved Logger from core to commons

-  Massive improvements to Query DSL:

   -  Added new implementations of $in, $nin, $all and $mod with tests.
      $mod now accepts non-Int numerics and aof two differing types.
   -  Full test coverage on DSL (and heavy coverage on other modules)
   -  Migrated $each to a now functioning internal hook on $addToSet
      only exposed in certain circumstances
   -  Various cleanups to Type constraints in Query DSL
   -  Full support for all documented MongoDB query operators
   -  Added new $not syntax, along with identical support for nested
      queries in $pull
   -  Valid Date and Numeric Type boundaries introduced and used instead
      of Numeric (since Char doesn’t actually workwith Mongo and you
      can’t double up type bounds)
   -  Added full support for geospatial query.
   -  Resolved an issue where the $or wasn’t being broken into
      individual documents as expected.
   -  DSL Operators now return DBObjects rather than Product/Tuple
      (massive fixes to compatibility and performance result)
   -  Added @see linkage to each core operator’s doc page

-  GridFS Changes:

   -  GridFS’ \`files’ now returned a MongoCursor not a raw Java
      DBCursor
   -  GridFS findOne now returns an Option[\_] and detects nulls like
      Collection

-  Added "safely" resource loaning methods on Collection & DB

   -  Given an operation, uses write concern / durability on a single
      connection and throws an exception if anything goes wrong.

-  Culled casbah-mapper. Mapper now lives as an independent project at
   http://github.com/maxaf/casbah-mapper
-  Bumped version of scala-time to the 0.2 release
-  Added DBList support via MongoDBList, following 2.8 collections

-  Adjusted boundaries on getAs and expand; the view-permitting Any was
   causing ambiguity issues at runtime with non AnyRefs (e.g. AnyVal).
-  Fixed an assumption in expand which could cause runtime failure
-  Updated MongoDBObject factory & builder to explicitly return a type;
   some pieces were assuming at runtime that it was a
   MongoDBObjectBuilder$anon1 which was FUBAR

Changes in Version 1.0.7.4
--------------------------

-  Fixed some issues w/ GridFS libraries attempting to call toMap in
   iteration, which isn’t implemented on the Java side; added custom
   toString methods on the GridFS files [BWM]
-  Cleaned up log spam [BWM / MA]
-  Added serialization hook for MongoDBObject to help catch any nested
   instances [MA]
-  Cleaned up some stray references to java.lang.Object, replaced with
   AnyRef for good Scala coding practices [BWM]

Changes in Version 1.0.7
------------------------

-  Updated reference to Configgy to have a Scala version attached; this
   was causing issues on some mixed-version users’ systems.
-  Corrected massive stupidity from lack of testing on my part and
   disabled ScalaJDeserializers - in most cases these caused runtime
   ClassCastExceptions. *SERIALIZERS* still in place - Deserializers
   were just plain a bad idea.

Changes in Version 1.0.5
------------------------

-  Due to oddities and ambiguities, stripped the type parameter apply[A]
   method from MongoDBObject. If you want a cast return, please use
   MongoDBObject.getAs[A]. This should minimize odd runtime failures.
-  Added toplevel detection in MongoDBObject’s +=/put methods to try and
   convert a MongoDBObject value to DBObject for you.
-  Added "Product" arguments to $pushAll - this means you can pass a
   Tuple-style list, where previously it required an Iterable ( $pushAll
   ("foo" -> (5, 10, 23, "spam", eggs") should now work).
-  Updated to scalaj-collection 1.0 release, built against 2.8.0 final
-  Added a new ScalaJ-Collection based Deserializer and Serializer
   layer. All base types supported by ScalaJ collection now use asJava /
   asScala to cleanly ser/deser where possible. This excludes
   Comparator/Comparable and Map types for sanity reasons. See
   com.novus.casbah.mongodb.conversions.scala.ScalaConversions for
   detail. Please report bugs if this breaks your code - it’s nascent
   and a bit naive!
-  New Committer - Max Afonov
-  Removed the BitBucket Mirror; we’re purely on GitHub now. Bug tracker
   linked from Github page.
-  Created a user mailing list -
   http://groups.google.com/group/mongodb-casbah-users

Changes in Version 1.0.2
------------------------

-  Changed $in, $notin, $all to always generate an array in Any\* mode
-  Added default type alias import for com.mongodb.DBRef & Casbah’s
   MongoDB class

Changes in Version 1.0.1
------------------------

-  Updated externals to link against 2.8.0 final - 1.0 release had some
   RC/Beta built externals. (scalaj-collection is still linked against
   Beta)
-  Added an Object interface, MongoDBAddress, for static construction of
   DBAddress instances.
-  Added type aliases in MongoTypeImports for all Casbah companion
   objects - please report any odd behavior this causes.
-  Added MapReduceCommand to BaseImports

Version 1.0
-----------

-  GridFS enhanced via Loan Pattern
-  Full support for MongoDB Query operators via fluid syntax (now with
   lots of testing to minimize breakage)
-  Added support for Scala 2.8-style Map interaction w/ DBObject.
   Builder pattern, +=, etc.
-  Tutorial Available
