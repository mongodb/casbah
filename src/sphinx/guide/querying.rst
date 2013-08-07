========
Querying
========
.. highlight:: scala

Query operations
----------------

As Casbah wraps the Java driver, so querying against MongoDB is essentially the
same.  The following methods for finding data:

  * ``find`` Returns a cursor
  * ``findOne`` Returns an Option - either Some(MongoDBObject) or None
  * ``findById`` Returns an Option - either Some(MongoDBObject) or None
  * ``findAndModify`` Finds the first document in the query, updates and returns it.
  * ``findAndRemove`` Finds the first document in the query, removes and returns it.

The following methods insert and update data:

  * ``save`` Saves an object to the collection
  * ``insert`` Saves one or more documents to the collection
  * ``update`` Updates any matching documents operation

For more information about create, read, update and delete (CRUD) operations in
MongoDB see the `core operations <http://docs.mongodb.org/manual/crud/>`_
documentation.

The `collection API documentation
<http://mongodb.github.io/casbah/api/#com.mongodb.casbah.MongoCollection>`_ has
a full list of methods and their signatures for interacting with collections.

MongoDBObject
-------------

MongoDB queries work by providing a document to match against. The simplest
query object is an empty one eg: ``MongoDBObject()`` which matches
every record in the database.

MongoDBObject is a simple Map-like class, that wraps the Java driver DBObject
and provides some nice Scala interfaces::

    val query = MongoDBObject("foo" -> "bar") ++ ("baz" -> "qux")

DBObjects have a builder and as such you can also build MongoDBObjects that way::

    val builder = MongoDBObject.newBuilder
    builder += "foo" -> "bar"
    builder += "baz" -> "qux"
    val query = builder.result

.. note:: Remember to import casbah: ``import com.mongodb.casbah.Imports._``
