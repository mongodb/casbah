===================
Whats new in Casbah
===================
.. highlight:: scala


Casbah 2.7 and Mongo DB 2.6 Features
====================================

MongoDB 2.6 introduces some new powerful features that are reflected in the 2.7.0 driver release. These include:

* Aggregation cursors
* Per query timeouts **maxTimeMS**
* Ordered and Unordered bulk operations
* A parallelCollectionScan command for fast reading of an entire collection
* Integrated text search in the query language

Moreover the driver includes a whole slew of minor and major bug fixes and features. Some of the more noteworthy
changes include.

* Added extra type checking so that ``MongoDBObject.getAs[Type]("key")`` better protects against invalid type casting
  so there are fewer scenarios where it will an invalid ``Some(value)``.
* Extended helpers for ``MongoDBObject.as[Type]("Keys"*)`` and ``MongoDBObject.getAs[Type]("keys" _*)`` for easier fetching
  of nested MongoDBObjects.
* Fixed issue with OpLog matching - thanks to Brendan W. McAdams for the pull request.
* Register the core Serialization helpers only once - thanks to Tuomas Huhtanen for the pull request.
* Updated nscala-time to 0.6.0 and specs

Please see the :doc:`full changelog <changelog>` and :doc:`upgrade documentation <upgrade>`.

Let's look at the main things in 2.6 features one by one.

Aggregation cursors
===================

MongoDB 2.6 adds the ability to return a cursor from the aggregation framework.
To do that simply use ``AggregationOptions`` with the aggregation command::

    val collection = MongoClient()("test")("aggregate")

    val aggregationOptions = AggregationOptions(AggregationOptions.CURSOR)
    val results = collection.aggregate(
      List(
        MongoDBObject("$project" ->
          MongoDBObject("author" -> 1, "tags" -> 1)
        ),
        MongoDBObject("$unwind" -> "$tags"),
        MongoDBObject("$group" ->
          MongoDBObject("_id" -> "$tags",
                        "authors" -> MongoDBObject("$addToSet" -> "$author")
          )
        )
      ),
      aggregationOptions
    );

Then the you can iterate the results of the aggregation as a normal cursor::

   for (result <- results) println(result)

To learn more about aggregation see the `aggregation tutorial
<http://docs.mongodb.org/manual/tutorial/aggregation-examples/>`_ and the
`aggregation reference <http://docs.mongodb.org/manual/reference/aggregation/>`_
documentation.

maxTime
=======

One feature that has requested often is the ability to timeout individual queries. In MongoDB 2.6 it's finally arrived
and is known as `maxTimeMS <http://docs.mongodb.org/master/reference/method/cursor.maxTimeMS/>`_. In Casbah support for
``maxTimeMS`` is via an argument or via the query api but is called ``maxTime`` and takes a
`Duration <http://www.scala-lang.org/api/2.10.3/#scala.concurrent.duration.Duration>`_

Let's take a look at a simple usage of the property with a query::

    val collection = MongoClient()("test")("maxTime")
    val oneSecond = Duration(1, SECONDS)

    collection.find().maxTime(oneSecond)
    collection.count(maxTime = oneSecond)

In the examples above the **maxTimeMS** is set to one second and the query will be aborted after the full second is up.

Ordered/Unordered bulk operations
=================================

Under the covers MongoDB is moving away from the combination of a write operation + get last error (GLE) and towards a
write commands api. These new commands allow for the execution of bulk insert/update/remove operations. The bulk api's
are abstractions on top of this that server to make it easy to build bulk operations. Bulk operations come in two main
flavors.

1. Ordered bulk operations. These operations execute all the operation in order and error out on the first write error.
2. Unordered bulk operations. These operations execute all the operations in parallel and aggregates up all the errors.
   Unordered bulk operations do not guarantee order of execution.

Let's look at two simple examples using ordered and unordered operations::

    val collection = MongoClient()("test")("bulkOperation")
    collection.drop()

    // Ordered bulk operation
    val builder = collection.initializeOrderedBulkOperation
    builder.insert(MongoDBObject("_id" -> 1))
    builder.insert(MongoDBObject("_id" -> 2))
    builder.insert(MongoDBObject("_id" -> 3))

    builder.find(MongoDBObject("_id" -> 1)).updateOne($set("x" -> 2))
    builder.find(MongoDBObject("_id" -> 2)).removeOne()
    builder.find(MongoDBObject("_id" -> 3)).replaceOne(MongoDBObject("_id" -> 3, "x" -> 4))

    val result = builder.execute()

    // Unordered bulk operation - no guarantee of order of operation
    val builder = collection.initializeUnOrderedBulkOperation
    builder.find(MongoDBObject("_id" -> 1)).removeOne()
    builder.find(MongoDBObject("_id" -> 2)).removeOne()

    val result2 = builder.execute()

For older servers than 2.6 the API will down convert the operations. However it's not possible to down convert 100% so
there might be slight edge cases where it cannot correctly report the right numbers.

parallelScan
============

The **parallelCollectionScan** command is a special command targeted at reading out an entire collection using
multiple cursors.  Casbah adds support by adding the ``MongoCollection.parallelScan(options)`` method::

    val collection = MongoClient()("test")("parallelScan")
    collection.drop()

    for(i <- 1 to 1000) collection += MongoDBObject("_id" -> i)

    val cursors = collection.parallelScan(ParallelScanOptions(3, 200))

    for (cursor <- cursors) {
      while (cursor.hasNext) {
        println(cursor.next())
      }
    }

This optimizes the IO throughput from a collection.

Integrated text search in the query language
============================================

Text indexes are now integrated into the main query language and enabled by default::

    val collection = MongoClient()("test")("textSearch")
    collection.drop()
    collection.ensureIndex( MongoDBObject("content" -> "text") )

    collection += MongoDBObject("_id" -> 0, "content" -> "textual content")
    collection += MongoDBObject("_id" -> 1, "content" -> "additional content")
    collection += MongoDBObject("_id" -> 2, "content" -> "irrelevant content")

    // Find using the text index
    val result1 =  collection.find($text("textual content -irrelevant")).count

    // Find using the $language operator
    val result2 =  collection.find($text("textual content -irrelevant") $language "english").count

    // Sort by score
    val result3 = collection.findOne($text("textual content -irrelevant"), "score" $meta)

