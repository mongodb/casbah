
2.3.0-M1 / 2012-05-28 
==================
  * Pre-Beta milestone (linked against unreleased Java Driver release)
  * Built against Mongo-Java-Driver 2.8.0-SNAPSHOT
  * Dropped Scala 2.8.0 support...     
    * 2.1.5-1 is the final Casbah release for 2.8.0; please migrate to Scala 2.8.1 or higher
  * SCALA-62: Simple solution - hack the date type on the base class.           
    + There is now a JodaGridFS implementation which works cleanly with Joda DateTime and will return them to you
  * Backport casbah-gridfs from 3.0 
    + Fixes SCALA-45: Allow filename and contentType to be nullable   
        - Retrieving filename or contentType on a GridFS File now returns Option[String] when fetched   
        - To facilitate sane usage, the loan-pattern/execute-around-resource methods now return the _id of the created file as Option[AnyRef]
  * Backports to casbah-core from 3.0 
    + SCALA-70: Removed type alias to com.mongodb.WriteConcern and made method args for it explicit, as it was causing a fun post-compile (aka "library compiles, user code doesn't") implosion. 
    +  added socketKeepAlive option 
    + Fixes SCALA-45: Allow filename and contentType to be nullable     
    + Retrieving filename or contentType on a GridFS File now returns Option[String] when fetched     
    + To facilitate sane usage, the loan-pattern/execute-around-resource methods now return the _id of the created file as Option[AnyRef]
  * Backports for QueryDSL     
    + Major cleanups and bugfixes to the DSL, it's heavily and fully tested now and much faster/cleaner     
    + Added support for $and bareword operator     
    + SCALA-30, SCALA-59 - $or is not properly accepting nested values esp. from other DSL constructors       
        - Introduced proper type class filter base to fix $or, will implement across other operators next.     
    + SCALA-59 - Fix Bareword Query Operators to better target accepted values; should only accept KV Tuple Pairs or DBObjects 
                 returned from Core Operators       
        - Complete test suites for $and and $nor although they need to be updated to more appropriate contextual examples rather than 
          just "compiles properly"       
        - New code logic, fixed $or, $and and $nor for proper nested list operations       
        - New :: list cons operator on MongoDBObject to create MongoDBLists on th fly (esp. for DSL)     
        - Typesafety kungfu from @jteigen       
            ~ enforce at compile time that type parameters used for casting are not Nothing       
            ~ enforce  $pushAll & $pullAll arguments can be converted to Iterable at compile time       
            ~ switched to a type class (AsQueryParam) for queryparams to avoid code duplication
  * SCALA-69: Maps saved to DBObject are now eagerly converted to a DBObject, from factory, builder and put methods.
  * Always return MongoDBList from Factories/Builders instead of Seq[Any]
  * Backports from Casbah 3.0     
    + Refactor collections (MongoDBList and MongoDBObject)         
    + Use CanBuildFrom properly to compose more appropriate Collection objects         
    + As part of above, you should get seq-like objects back from MongoDBList builders & factories instead of the previous BasicDBList; 
      this is part of attempting to "Hide" DBList and let people work with List/Seq         
    + SCALA-69: Immediately upon saving any None's will be converted to null inside the DBObject for proper fetching later.         
    + Add toString, hashCode and equals methods to DBObject         
    + New, refactored tests for DBObject and DBList     
        - More typesafety kungfu from @jteigen           
            + enforce at *compile time* that type parameters used for casting ( as, getAs, getAsOrElse ) are not Nothing
  * Backport Test Helpers
    + New MongoDB "smart" test helpers for Specs2 and ScalaTest (Thanks Bill Venners for the latter)
  * Added SBT Rebel cut, local runner

2.1.5.0 / 2011-05-26 
==================
  * [2.9.0 only] Adjusted dynamic settings to build against 2.9.0-1 and Casbah 2.1.5.0
  * [2.9.0 only] Prototype "Dynamic" module (You must enable Scala's support for Dynamic)
  * [2.9.0 only] I seem to have missed project files for SBT and casbah-dynamic
  * [2.9.0 only] Tweaks and adjustments to get this building and testing solidly on 2.9.0-1
  * Disabled a few tests that weren't passing and known to be 'buggy' in specs1.  These are fixed for the upcoming 2.2. release on specs2; they are test bugs rather than Casbah bugs.
  * RegEx $not was just flat out wrong - was producing {"foo": {"foo": /<regex>/}} instead of {"foo": {"$not": {/<regex>/}}
  * Added a getAsOrElse method

2.1.2 / 2011-04-09 
==================

  * SCALA-28 Updated Java Driver to Release 2.5.3
    + JAVA-315 several exception cases do not put back the dbport in pool, resulting in forever hanging pool to that server
    + JAVA-312 Added UUID support to JSON Serialization
    + JAVA-318 Fixed JMX issues for similar ServerAddresses
  * SCALA-27 Properly Serialize Option[T] so that None is null and Some expands out
  * SCALA-26: findOneByID(id) and findOneById(id, fields) had different casing; when findOneById(id, fields) was called as byID an implicit caused the single arg version to be called instead, yielding unexpected results.

2.1.1 / 2011-03-29
==================

  * SCALA-25 Added support for $bit operator, with and and or options
  * Updated to Java Driver 2.5.2 (Bugfixes)
    + JAVA-301 - Replicaset w/ auth warnings too verbose when not authd against local/admin db
    + JAVA-304 - Problem with closing cursors - killCursors
  

2.1.0 / 2011-03-15 
==================

  * SCALA-22 Added a dropTarget boolean option to rename collection, which specifies behavior if named target collection already exists, proxies JAVA-238
  * Removed resetIndexCache, which has also been removed from the Java Driver
  * SCALA-21 Added "set metadata" method to match Java Driver (See Java-261)
  * SCALA-20 Updated to Java Driver 2.5
    + See Release Notes: http://groups.google.com/group/mongodb-user/browse_thread/thread/a693ad4fdf9c3731/931f46f7213b6775?show_docid=931f46f7213b6775
  * SCALA-21 - Update GridFS to use DBObject views.  Holding back full bugfix until we have a 2.5 build to link against
  * Example adjustments to filter by start time and namespace
  * SCALA-10 - And this is why we unit test.  Size was returning empty for cursor based results as it wasn't pulling the right value.  Fixed, calling cursor.size.
  * Added an alternative object construction method for MongoDBObject with a list of pairs, rather than varargs [philwills]
  * Making scaladoc for MongoURI more explicit. Note that the wiki markup for lists isn't actually implemented in scaladoc yet. [philwills]
  * Refactor Collection and Cursors using Abstract types, explicit 'DBObject' version is always returned from DB, Collection etc now. Those wanting to use typed versions must code the flip around by hand. !!! BREAKING CHANGE, SEE CODE / EXAMPLES 
  * SCALA-10 Updated MapReduce interfaces to finish 1.8 compatibility     
    + Renamed MapReduceError to MapReduceException; MapReduceError is a non exception which represents a failed job     
    + Changed MapReduceResult to automatically proxy 'results' in inline       result sets
  * Added missing methods to GridFSDBFile necessary to access the underlying datastream
  * Fixed setter/getter of option on cursor
  * For several reasons changed backing trait of DBList PML from Buffer to LinearSeq
  * Moved to new MapReduce functionality based on MongoDB 1.7.4+ !!! You must now specify an output mode.
    + See http://blog.evilmonkeylabs.com/2011/01/27/MongoDB-1_8-MapReduce/
  * MapReduce failures shouldn't throw Error which can crash the runtime
  * New MapReduceSpec updates to include tests against new MongoDB MapReduce logic

2.0.2 / 2011-01-25 
==================

  * Fixed the MongoDBOBject 'as' operator to return the proper type, instead of Any. (philwills)

2.0.1 / 2011-01-04 
==================

  * SCALA-16: Added a few additional  validation tests against getAs and as on MongoDBObject
  * SCALA-17 - Fixed syntax of $within and its nested operators, unit test passes

2.0 / 2011-01-03 
==================

  * Fixed SBT build to properly publish -sources and -javadoc artifacts
  * Ignore doc build artifacts

2.0rc3 / 2010-12-30 
==================

  * Migrated MongoCursorBase[T] chained methods to use this.type which returns their proper SUBTYPE instead of undifferentiated superclass MongoCursorBase[T]
  * Moved copy() method to properly return the given subtype in MongoCursor/MongoTypedCursor
  * Replaced explicit asInstanceOf[DBObject] casts of view findOne values to compiler-checked ': DBObject' type annotation

2.0rc2 / 2010-12-29 
==================

  * Minor adjustment to @philwills' patch so that any VIEW of a DBObject can be taken into ++
  * Added a ++ method which explicitly takes and returns a MongoDBObject to avoid odd MongoDBObject ++ MongoDBObject == Map behaviour (@philwills)
  * Updated doc build including single Scaladoc buildout

2.0rc1 / 2010-12-27 
==================

  * Pull Java Driver 2.4 from Maven (and include it in the POM)
  * SCALA-11: Added new findOneByID methods to match the Java driver's Object taking ambiguous findOne methods.
  * SCALA-11: findOne in Java has methods which take an Object to allow ID matching.  Because of the view boundary, Scala was selecting the Object version instead of DBObject, causing breakage. findOne methods now explicitly cast the query arg to DBObject to hook the right java method.

2.0rc0/ 2010-12-23 
==================

  * Added publishing of source and docs artifacts to SBT Project
  * Update to Java Driver 2.4 (Not showing up in main maven so adding as hard lib for now)
  * SCALA-2: Added WriteConcern to Insert 
  * SCALA-4: Added set/reset/getOptions to MongoCursor
  * SCALA-6: Refactored Type classes  for Collection and Cursor to minimize duplication and insanity
  * Refactored MongoCursor to be cleaner, no more Typed/Untyped subimplementations
  * SCALA-7: Lists, etc passed to MongoDBList are no longer flattened, and now included as is.  A bare pair e.g. "foo" -> "bar" will be saved as a MongoDBObject (mostly useful for DSL work).  If you want a long DBObject, construct it explicitly. This IS a breaking change from previous versions where a List passed to MongoDBList() was flattened---that was incorrect behavior.
  * SCALA-3: Removed MonkeyPatches and replaced with builtin Java 2.4 methods for encode/decode removal
  * SCALA-9: Added 'as' method to MongoDBObject which does non-Optioned castable like 'getAs'
  * SCALA-5: Clarify docs that rename does NOT mutate, make sure it returns a PIMPED Collection instance rather than bare Java type.
  * SCALA-1: Added support for a $maxDistance(Numeric) argument on $near queries in DSL 
  * Added some type import fixes for Query DSL, related to ValidType tree
  * Removed type clash by rolling back DBObject/DBList aliasing in Imports
  * Added $ne support for Boolean, ObjectId, DBref
  * optWrap -> Option
  * bumNetworks repos shouldn't be needed for build anymore
  * Updating docs to include migration guide
  * Added new sphinx tutorial and api docs build

2.0b3p1 / 2010-12-09 
==================

  * Removed $eq operator as it was a test spike and seems to be causing possible external issues

2.0b3 / 2010-12-08 
==================

  * Added heavy test coverage
  * Tests/demos of all additivity of DBObject and Tuple
  * ++ additivity operator on MongoDBObject for lists of tuple pairs
  * Added test for $nor demonstrating additivity between a DSLed DBObject and a tuple
  * Added val aliasing for MongoDBObject -> DBObject and MongoDBList -> DBList (only for the 'object' singletons)
  * Added bareword DSL operator for $nor
  * Added new $not syntax, along with identical support for nested queries in $pull:
  * Added sbt-growl-plugin for testing results notification
  * Added SLF4J-JCL Bindings as a *test* dependency (so we can print logging while testing without forcing you to use an slf4j implementation yourself)
  * $elemMatch shouldn't have been a nestable, and only accept DBObject. Fixed.
  * Full test coverage on DSL ...nested query chains are heavily broken at the moment.
  * Migrated $each to a now functioning internal hook on $addToSet only exposed in certain circumstances
  * Various cleanups to docs and type constraints in BarewordOperators
  * Changed Geo related stuff to use ValidNumericType instead of Numeric as some Numerics are not valid, and we'll be using Valid*Type for auto conversion reg soon
  * Upgraded Specs to 1.6.6 against 2.8.1. Closes #28
  * GridFS' `files' should have returned a MongoCursor not a raw Java DBCursor.  Closes #25
  * Replaced optWrap call removed accidentally from findAndModify
  * GridFS findOne wasn't returning Option[_] and detecting nulls like Collection, which has been corrected.  Closes #26
  * Removed FluidMongoSyntaxSpec from old tests, now migrated to DSL Specs
  * Added a false $eq operator to emulate key = value easily in the DSL
  * Restored ability to chain DSL operators (First pass - may change)
  * Base tests for Core Operators
  * Fixed RegEx and Binary $type tests
  * Differentiated GeoCoords to use two different Manifested Numerics for differing datatypes
  * Timestamp must take precedence over date to match properly
  * Changed precedence so DBList is first as it's also a dbobject and test failed
  * Added Joda DateTime to Type testing in $type
  * Updates and test coverage for $size and $exists
  * Added new implementations of $in, $nin, $all and $mod with tests. $mod now accepts non-Int numerics and of two differing types.
  * Stripped out support for implicit Product/Tuple conversions as they're buggy as hell and constantly interfere with other code.
  * Implemented new code and tests for $gt, $gte, $lt, $lte.
  * Added Date tests to $Ne and added testing for $lt
  * Valid Date and Numeric Type boundaries introduced and used instead of Numeric (since Char doesn't actually workwith Mongo and you can't double up type bounds)
  * Refactor of $ne to a new baseline, new unit test for DSLCoreOperators to test it
  * Checkpoint, ValidProduct won't work either
  * mid-refactor of core operators code, checkpoint for if we keep aritied tuple code
  * Removed crufty extra logging in distinctKeySet
  * Ported MapReduce test to Specs in new directory structure; needs more test methods
  * Removing GridFSSpec as it has been ported to Specs in the new modules
  * Changed sub$project names back to start with casbah- to reduce confusion with maven artifacts
  * Updated documentation buildout and migrated default build version to 2.8.1, removing 2.8.1rc4

2.0b2 / 2010-11-09 
==================

  * Added documentation build script
  * Moved loading of ConversionHelpers from Connection creation to instantiation of Commons' Implicits 
    - This means conversions are ALWAYS loaded now for everyone
  * Added registered conversion for Product (tuples) which fixes open issues with serialization
  * Migrated Conversions code from core to commons, repackaging as com.mongodb.casbah.commons.conversions   
    - Unit Test remains in Core as it uses core code to test loading/unloading which is fine for buildtime tests
  * Moved Logger from core to commons, package changed from com.mongodb.casbah.util.Logging to com.mongodb.casbah.commons.Logging

2.0b1 / 2010-11-05 
==================

  * Switched off of configgy to slf4j as akka did; fixed project build and released as 2.0b1
  * Fixes #12 Collection is now inline with the 2.3 java driver
  * Refs gh-12, updated MongoDB for API compatibility with Java driver 2.3 and added some necessary docs.
  * Refs gh-12, added a WriteConcern helper object for Scala users w/ named & default args
  * refs gh-12 - Updated connection to match 2.3, added support for specifying writeconcern and slaveOK
  * Update .vimrc data for new header, etc
  * Update .vimrc data for new header, etc
  * Culling old mercurial artifacts
  * Removed references to package bridge in README and updated with info on each component
  * Fixes #18 corrected pop implementation
  * Fixes #13 - added findAndModify / findAndRemove
  * Fixes #19 Added full support for geospatial query.  Needs some docs and tests
  * - Updated to Java 2.3 driver #12
  * Fixes #2 - ElemMatch works much like $not
  * Fixes #11 - Context Bound and Byte Enum supported $type queries
  * Fixes #17 added $rename and fixed a bug caused by $or implementation
  * References #4 , some scratch work towards creating a context bound
  * Fixes #1, added $each target and improved $addtoSet impl $each is a bit of an unfortunate stray in that it CAN be invoked elsewhere but should only work with $addToSet
  * Culled useless version javadoc cruft & added @seen to all bareword query ops
  * Fixes #3 Resolved an issue where the $or wasn't being broken into individual documents as expected.  Should be heavily tested.
  * Fixes #15, Wrap the return of operator calls in a DBObject constructor.
  * Fixed anchoring on CoreOperators - things like $mod and such needed to be callable
  * $mod was just flat out wrong - corrected.
  * Fixes #3 - Added $or operator with proper right-hand array creation.
  * Added @see linkage to each core operator's doc page
  * Fixes #10, added $slice operator
  * New Spec test for GridFS, ported from old tests and now using Mongo powered by logo
  * Removed 'batchSafely' methods as they are now deprecated and counterindicated
  * Renamed 'safely' methods to 'request' as they are NOT safe and 10gen convention is moving off 'safe' as a term.
  * Configuring output of SXR crosslinking
  * Added additional test for explicit and cast conversions of Maps to DBObject
  * Removed 'Package Bridge' and DeprecatedTypeAliases code.  Migration guide will be provided instead.
  * - moving 'bridge' code to a bridge package  which may or may not get   used
  * - Added SXR source builds
  * Changed publishing to Scala Tools... Obviously you need credentials there which are maintained outside the project
  * Updated project to cross build for 2.8.1.RC4 as 2.8.1 is coming soon. We will crossbuild going forward for 2.8.0 and 2.8.1.
  * 
  * Tweak README, add note about novus' prior sponsorship.
  * Updated README addressing start of move to MongoDB space
  * Migration of codebase for 10gen ownership   - Package changed from com.novus.* to com.mongodb.*     * Package aliases provided for bridging so you shouldn't need to migrate your 1.0 or 1.1-snapshot code forcibly right away   - Copyright updated to include 10gen   - Version changed from 1.1-SNAPSHOT to 2.0-SNAPSHOT     * Because of package change and modularisation 1.1 will be replaced with a 2.0 release
  * Cull casbah-mapper.  Mapper now lives as an independent project at http://github.com/maxaf/casbah-mapper ...
  * bumped version of scala-time to the 0.2 release
  * publish POM-s
  * stop spamming the logs when JodaDateTimeDeserializer is a NOOP
  * Merge branch 'master' of github.com:novus/casbah
  * more thorough BigDecimal conversions & test
  * Fixes #24 adds DBList support via MongoDBList, following 2.8 collections
  * - First pass at a DBList implementation
  * Merge branch 'master' of github.com:novus/casbah
  * - Migrated Logging into core and out of commons - Fixed an issue with Bareword Query operators not functioning due to where they were placed.
  * added @Ignore, @Key(pri: Int), and better logging of exceptional conditions
  * forgot to unwrap option from propValue
  * added ensureID: a method that'll make sure an object mapped with @ID(auto=true) has an ObjectId
  * added support for top-level type hints
  * make them pay attention when enums aren't properly declared
  * added helpful fall-back conversions which are supposed to catch any mapped objects that somehow "fall through the cracks"
  * I call this "just make it happen" conversion
  * report malformed props that are claimed as enums
  * much saner publishing over SFTP
  * added support for Scala enums
  * type hints only carry through on non-iterable props; better unwrapping of parameterized types
  * added support for Set-s
  * improved fetching of type params; added more helpful error reporting
  * report location of MissingMapper exceptions
  * test
  * force-fed a very contrived example that better exercises polymorphic lists/maps
  * added a timing test case; fixed deserialization bug uncovered by accident during performance testing
  * HALF_UP rounding is required in the math context
  * - Forward port from 1.0.8.1 bugfix branch     * Factory & Builder for MongoDBObject always return as DBObject     * Unit test for reported issue
  * Merge branch 'master' of github.com:novus/casbah
  * added ability to specify strategy for turning non-string Map keys into strings
  * disabled parallelExecution because it confuses SSH key passphrase entry prompts while publishing
  * - Added "safely" and "batchSafely" resource loaning methods on Collection & DB     * Given an operation, uses write concern / durability on a single       connection and throws an exception if anything goes wrong.
  * - Adjusted boundaries on getAs and expand; the view-permitting Any was   causing ambiguity issues at runtime with non AnyRefs (e.g. AnyVal). - Fixed an assumption in expand which could cause runtime failure - Updated MongoDBObject factory & builder to explicitly return a type;   some pieces were assuming at runtime that it was a   MongoDBObjectBuilder$anon1 which fucked things up beyond all   recognition - New basic spec test for MongoDBObject, just validates expand behavior   ATM
  * defend Option[_] props from nulls that crop up w/ missing keys
  * rsync publishing is cool
  * moved common deps into common subproject base class
  * Merge branch 'master' into mapper
  * replaced with MongoCursor from master
  * mapper spec runs
  * Typo in the BaseImports for GRidFS was aliasing MongoConnection to GridFS; fixed.
  * Merge branch 'master' into mapper
  * moved mapper stuff into own module
  * merged in re-org work from master
  * - Fixed Type parameters on DeprecatedTypeAliases - Removed old_tests/ConversionSpec as this has been replaced with a new,   cleaner test
  * failing test case narrows down issue with default values in constructor bodies
  * added def _id: Option[ObjectId] which DWIM
  * protect Option fields from nulls
  * choose correct impl when writing to Map props
  * trace logging was causing NoSuchElementException-s on val-s
  * exposed propValue
  * will demand whaaambulance in case of missing mappers
  * polymorphic lists work using selectively persisted type hints
  * Bridge package for the old package space - attempts to throw warnings  when people use the name while still allowing most of their code to  compile.
  * Functioning Specs port of the Conversions unit test, much better test now as well.
  * RichPropertyDescriptor#idProp is now an Option
  * Full module breakdown of Casbah.
  * keep Unix user name in rsync destination
  * exercising case class support
  * added support for BigDecimal
  * added support for Map-s
  * prettier names
  * added support for annotated val-s
  * Beginning a massive refactor for submodules, which will become 1.1
