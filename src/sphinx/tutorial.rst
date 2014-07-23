======================
Tutorial: Using Casbah
======================
.. highlight:: scala

This quick tutorial should get you up and running doing basic create, read,
update, delete (CRUD) operations with Casbah.

Prerequisites
-------------

Please ensure you have downloaded and installed
`mongodb <http://docs.mongodb.org/manual/installation/>`_ and have it running on
its default host  (localhost) and port (27107).

Getting started
---------------

The next step is to get and install sbt, create an sbt project and install
casbah.  I recommend using `sbt-extras <https://github.com/paulp/sbt-extras>`_
- a special sbt script for installing and running sbt.

1. Create a project directory: ``mkdir casbah_tutorial && cd casbah_tutorial``
2. Install sbt-extras script:

.. code-block:: bash

    curl https://raw.github.com/paulp/sbt-extras/master/sbt > sbt
    chmod +ux sbt

2. Create an sbt build file: ``build.sbt``:

.. parsed-literal::

    name := "Casbah Tutorial"

    version := "0.1"

    scalaVersion := "2.10.4"

    libraryDependencies += "org.mongodb" %% "casbah" % "|release|"

3. Run the console and test (sbt will automatically install the dependencies)::

    $ ./sbt console
    scala> import com.mongodb.casbah.Imports._
    import com.mongodb.casbah.Imports._

If you had any errors installing casbah please refer to the
:doc:`installation guide <guide/installation>`, otherwise you are ready to start
using casbah!

Connecting to MongoDB
---------------------

The first step of using Casbah is to connect to MongoDB.  Remember, this
tutorial expects MongoDB to be running on localhost on port 27017.
`MongoClient <http://mongodb.github.io/casbah/api/#com.mongodb.casbah.MongoClient>`_
is the connection class.


Load the scala shell ``./sbt console``

.. code-block:: scala

    import com.mongodb.casbah.Imports._
    val mongoClient = MongoClient("localhost", 27017)

There are various connection configuration options see the
:doc:`connection guide <guide/connecting>` for more information.

.. note:: The scala repl has tab completion type: ``mongoClient.<tab>``
    for a list of all the operations you can run on a connection.

Getting databases and collections
---------------------------------

In MongoDB a database doesn't need to exist prior to connecting to it, simply
adding  documents to a collection is enough to create the database.

Try connecting to the "test" database and getting a list all the collections ::

    val db = mongoClient("test")
    db.collectionNames

If your database is new then ``db.collectionNames`` will return an empty ``Set``,
otherwise it will list the collections in the database.


The next step before starting to add, update and remove documents is to get a
collection ::

    val coll = db("test")

``coll`` is the "test" collection in the "test" database. You are now ready to
begin adding documents.

.. note:: If you had an existing "test" collection drop it first:
    ``coll.drop()``

Doing CRUD operations
---------------------

Inserting, reading, updating and deleting documents in MongoDB is simple.
The ``MongoDBObject`` is a Map-like object that represents a MongoDB Document.

Create
^^^^^^

Create two documents ``a`` and ``b``::

    val a = MongoDBObject("hello" -> "world")
    val b = MongoDBObject("language" -> "scala")

Insert the documents::

    coll.insert( a )
    coll.insert( b )

Read
^^^^

Count the number of documents in the "test" collection::

    coll.count()

Use ``find`` to query the database and return an iterable cursor, then print
out the string representation of each document::

    val allDocs = coll.find()
    println( allDocs )
    for(doc <- allDocs) println( doc )

.. note:: You may notice an extra field in the document: ``_id``.
    This is the primary key for a document, if you don't supply an ``_id`` an
    ``ObjectId`` will be created for you.

By providing a ``MongoDBObject`` to the ``find`` method you can filter the
results::

    val hello = MongoDBObject("hello" -> "world")
    val helloWorld coll.findOne( hello )

    // Find a document that doesn't exist
    val goodbye = MongoDBObject("goodbye" -> "world")
    val goodbyeWorld coll.findOne( goodbye )

.. note:: Notice that ``find`` returns a Cursor and ``findOne`` returns an
    ``Option``.

Update
^^^^^^

Now you have some data in MongoDB, how do you change it?  MongoDB provides a
powerful ``update`` method that allows you to change single or multiple
documents.

First, find the scala document and add its platform::

    val query = MongoDBObject("language" -> "scala")
    val update = MongoDBObject("platform" -> "JVM")
    val result = coll.update( query, update )

    println("Number updated: " + result.getN)
    for (c <- coll.find) println(c)

.. warning:: You will notice that the document is now missing
    ``"language" -> "scala"``! This is because when using update if you provide
    a simple document it will replace the existing one with the new document.

    This is the most common gotcha for newcomers to MongoDB.

MongoDB comes with a host of
`update operators <http://docs.mongodb.org/manual/core/update/#crud-update-operators>`_
to modify documents.  Casbah has a powerful :doc:`DSL <guide/querying>` for creating
these update documents. Lets set the language to scala for the JVM document::

    val query = MongoDBObject("platform" -> "JVM")
    val update = $set("language" -> "Scala")
    val result = coll.update( query, update )

    println( "Number updated: " + result.getN )
    for ( c <- coll.find ) println( c )

.. note:: By default ``update`` will only update a single document - to update
    *all* the documents set the multi flag: ``.update( query, update, multi=true)``.

Another useful feature of the ``update`` command is it also allows you to
``upsert`` documents on the fly.  Setting ``upsert=True`` will insert the
document if doesn't exist, otherwise update it::

    val query = MongoDBObject("language" -> "clojure")
    val update = $set("platform" -> "JVM")
    val result = coll.update( query, update, upsert=true )

    println( "Number updated: " + result.getN )
    for (c <- coll.find) println(c)


Removing
^^^^^^^^

The final part of the tutorial is removing documents.  Remove is the similar to
``find``, in that you provide a query of documents to match against::

    val query = MongoDBObject("language" -> "clojure")
    val result = coll.remove( query )

    println("Number removed: " + result.getN)
    for (c <- coll.find) println(c)

To remove all documents, provide a blank document to match all items in the
database::

    val query = MongoDBObject()
    val result = coll.remove( query )

    println( "Number removed: " + result.getN )
    println( coll.count() )

Rather than iterating the collection and removing each document, its more
efficient to drop the collection::

    coll.drop()

Learning more about Casbah
--------------------------

If you got this far you've made a great start, so well done!  The next step on
your Casbah journey is the :doc:`full user guide <guide/index>`, where you
can learn indepth about how to use casbah and mongodb.
