==========
User Guide
==========

Casbah is a Scala toolkit for MongoDB --- We use the term "toolkit" rather than
"driver", as Casbah is a layer on top of the official `mongo-java-driver
<http://github.com/mongodb/mongo-java-driver>`_ for better integration with
Scala.  This is as opposed to a native implementation of the MongoDB wire
protocol, which the Java driver does exceptionally well. Rather than a complete
rewrite, Casbah uses implicits, and `Pimp My Library` code to enhance the
existing Java code.

.. toctree::
   :maxdepth: 2

   philosophy
   modules
   installation
   installation_alt
   connecting
   querying
   query_dsl
   aggregation
   gridfs
   serialisation
