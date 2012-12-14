.. Casbah (MongoDB Scala Toolkit) Tutorial documentation master file, created by
   sphinx-quickstart on Thu Dec  9 12.0.19:28 2010.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to the Casbah documentation
===================================================================

Welcome to the Casbah Documentation.  Casbah is a Scala toolkit for MongoDB---We use the term "toolkit" rather than "driver", as Casbah integrates a layer on top of the official `mongo-java-driver <http://github.com/mongodb/mongo-java-driver>`_ for better integration with Scala.  This is as opposed to a native implementation of the MongoDB wire protocol, which the Java driver does exceptionally well. Rather than a complete rewrite, Casbah uses implicits, and `Pimp My Library` code to enhance the existing Java code.

Casbah's approach is intended to add fluid, Scala-friendly syntax on top of MongoDB and handle conversions of common types.  If you try to save a Scala List or Seq to MongoDB, we automatically convert it to a type the Java driver can serialize.  If you read a Java type,  we convert it to a comparable Scala type before it hits your code.  All of this is intended to let you focus on writing the best possible Scala code using Scala idioms.  A great deal of effort is put into providing you the functional and implicit conversion tools you've come to expect from Scala, with the power and flexibility of MongoDB.

Casbah provides improved interfaces to GridFS, Map/Reduce and the core Mongo APIs.  It also provides a fluid query syntax which emulates an internal DSL and allows you to write code which looks like what you might write in the JS Shell.  There is also support for easily adding new serialization/deserialization mechanisms for common data types (including Joda Time, if you so choose; with some caveats - See the GridFS Section).

With version 2.0, Casbah has become an official MongoDB project and will continue to improve the interaction of Scala + MongoDB. Casbah aims to remain fully compatible with the existing Java driver---it does not talk to MongoDB directly, preferring to wrap the Java code.  This means you shouldn't see any wildly unexpected behavior from the underlying Mongo interfaces when a data bug is fixed.


The `ScalaDocs for Casbah <./api/#com.mongodb.casbah.package>`_ along with SXR cross referenced source are available at the `MongoDB API site <http://api.mongodb.org>`_.


You may also download this documentation in other formats.

  * `ePub <./CasbahDocumentation.epub>`_
  * `PDF <./CasbahDocumentation.pdf>`_

.. toctree::
    :maxdepth: 3
    :numbered:

    API Docs <http://api.mongodb.org/scala/casbah/api/#com.mongodb.casbah.package>
    setting_up
    tutorial


..
.. Indices and tables
.. ==================
..
.. * `API Docs <./api/#com.mongodb.casbah.package>`_
.. * :ref:`genindex`
.. * :ref:`modindex`
.. * :ref:`search`

