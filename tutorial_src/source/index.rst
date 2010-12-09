.. Casbah (MongoDB Scala Toolkit) Tutorial documentation master file, created by
   sphinx-quickstart on Thu Dec  9 12:09:28 2010.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to the Casbah documentation
===================================================================

Welcome to the Casbah Documentation.  Casbah is a Scala toolkit for MongoDB---We use the term "toolkit" rather than "driver", as Casbah integrates a layer on top of the official `mongo-java-driver <http://github.com/mongodb/mongo-java-driver>`_ for better integration with Scala.  This is as opposed to a native implementation of the MongoDB wire protocol, which the Java driver does exceptionally well. Rather than a complete rewrite, Casbah uses implicits, and `Pimp My Library` code to enhance the existing Java code. 

Casbah's approach is intended to add fluid, Scala-friendly syntax on top of MongoDB and handle conversions of common types.  If you try to save a Scala List or Seq to MongoDB, we automatically convert it to a type the Java driver can serialize.  If you read a Java type,  we convert it to a comparable Scala type before it hits your code.  All of this is intended to let you focus on writing the best possible Scala code using Scala idioms.  A great deal of effort is put into providing you the functional and implicit conversion tools you've come to expect from Scala, with the power and flexibility of MongoDB.

The `ScalaDocs for Casbah <http://api.mongodb.org/scala/casbah/>`_ along with SXR cross referenced source are available at the `MongoDB API site <http://api.mongodb.org>`_.


.. toctree::
    :maxdepth: 3
    
    setting_up
    tutorial
.. 
.. Indices and tables
.. ==================
.. 
.. * :ref:`genindex`
.. * :ref:`modindex`
.. * :ref:`search`

