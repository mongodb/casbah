=====================
Aggregation Framework
=====================
.. highlight:: scala

Overview
--------

The MongoDB aggregation framework provides a means to calculate
aggregated values without having to use `map-reduce
<http://docs.mongodb.org/manual/core/map-reduce/>`_. While
map-reduce is powerful, it is often more difficult than
necessary for many simple aggregation tasks, such as totaling or
averaging field values.

If you're familiar with SQL, the aggregation framework
provides similar functionality to ``GROUP BY`` and related SQL
operators as well as simple forms of "self joins." Additionally, the
aggregation framework provides projection capabilities to reshape the
returned data. Using the projections in the aggregation framework, you
can add computed fields, create new virtual sub-objects, and extract
sub-fields into the top-level of results.

Aggregation Syntax
------------------

Conceptually, documents from a collection pass through an aggregation pipeline,
which transforms these objects as they pass through. For those familiar with
UNIX-like shells (e.g. bash,) the concept is analogous to the pipe (i.e. \|)
used to string text filters together:

.. code-block:: bash

    db.people.aggregate( [<pipeline>] )
    db.runCommand( { aggregate: "people", pipeline: [<pipeline>] } )

See the `aggregation reference <http://docs.mongodb.org/manual/reference/aggregation/>`_
for information about aggregation operations.

Aggregation By Example
----------------------

First, consider a collection of documents named articles using the following
format::

    import com.mongodb.casbah.Imports._
    val db = MongoClient()("test")
    val coll = db("aggregate")
    coll.drop()

    coll += MongoDBObject("title" -> "Programming in Scala" ,
                          "author" -> "Martin",
                          "pageViews" ->  50,
                          "tags" ->  ("scala", "functional", "JVM") ,
                          "body" ->  "...")

    coll += MongoDBObject("title" -> "Programming Clojure" ,
                          "author" -> "Stuart",
                          "pageViews" ->  35,
                          "tags" ->  ("clojure", "functional", "JVM") ,
                          "body" ->  "...")

    coll += MongoDBObject("title" -> "MongoDB: The Definitive Guide" ,
                          "author" -> "Kristina",
                          "pageViews" ->  90,
                          "tags" ->  ("databases", "nosql", "future") ,
                          "body" ->  "...")

The following example aggregation operation pivots data to create a set of
author names grouped by tags applied to an article. Call the aggregation
framework by issuing the following command::

    val db = MongoClient()("test")
    val coll = db("aggregate")

    val results = coll.aggregate(
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
      )
    );

The results of the aggregation themselves can be accessed via ``results``.

Aggregation Cursor Interface - new in casbah 2.7
=================================================

MongoDB 2.6 adds the ability to return a cursor from the aggregation framework.
To do that simply use `AggregationOptions` with the aggregation command::

    val db = MongoClient()("test")
    val coll = db("aggregate")

    val aggregationOptions = AggregationOptions(AggregationOptions.CURSOR)
    val results = coll.aggregate(
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
