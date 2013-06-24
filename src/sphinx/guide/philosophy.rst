----------
Philosophy
----------

Casbah's approach is intended to add fluid, Scala-friendly syntax on top of
MongoDB and handle conversions of common types.

If you try to save a Scala List or Seq to MongoDB, we `automatically convert it
<guide/serialisation>`_ to a type the Java driver can serialise.  If you read a
Java type,  we convert it to a comparable Scala type before it hits your code.

All of this is intended to let you focus on writing the best possible Scala
code using Scala idioms.  A great deal of effort is put into providing you the
functional and implicit conversion tools you've come to expect from Scala, with
the power and flexibility of MongoDB.

Casbah provides improved interfaces to `GridFS <guide/gridfs>`_, Map/Reduce and
the core Mongo APIs.  It also provides a `fluid query syntax <guide/querying>`_
which emulates an internal DSL and allows you to write code which is more akin
to what you would write in the JS Shell.

There is also support for easily adding new
`serialisation/deserialisation <guide/serialisation>`_ mechanisms for common
data types (including Joda Time, if you so choose; with some caveats - See
the `GridFS Section <guide/gridfs>`_).