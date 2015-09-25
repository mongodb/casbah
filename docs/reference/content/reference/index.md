+++
date = "2015-09-23T15:36:56Z"
title = "Reference"

[menu.main]
  identifier = "Reference"
  weight = 20
  pre = "<i class='fa fa-book'></i>"
+++

# Reference

Casbah is a Scala toolkit for MongoDB — We use the term "toolkit" rather than "driver"”", as Casbah is a layer on top of the official mongo-java-driver for better integration with Scala. This is as opposed to a native implementation of the MongoDB wire protocol, which the Java driver does exceptionally well. Rather than a complete rewrite, Casbah uses implicits, and *Pimp My Library* code to enhance the existing Java code.

## Philosophy

Casbah's approach is intended to add fluid, Scala-friendly syntax on top
of MongoDB and handle conversions of common types.

If you try to save a Scala List or Seq to MongoDB, we
[automatically convert]({{< relref "reference/serialisation.md">}}) it
to a type the Java driver can serialise. If you read a Java type, we convert it
to a comparable Scala type before it hits your
code.

All of this is intended to let you focus on writing the best possible
Scala code using Scala idioms. A great deal of effort is put into
providing you the functional and implicit conversion tools you've come
to expect from Scala, with the power and flexibility of MongoDB.

Casbah provides improved interfaces to [GridFS]({{< relref "reference/gridfs.md">}}),
Map/Reduce and the core Mongo APIs. It also provides a fluid query syntax
[querying]({{< relref "reference/querying.md">}})
which emulates an internal DSL and allows you to write code which is
more akin to what you would write in the JS Shell.

There is also support for easily adding new
[serialisation/deserialisation]({{< relref "reference/serialisation.md">}})
mechanisms for common data types (including Joda Time, if you so choose;
with some caveats - See the GridFS Section  [GridFS]({{< relref "reference/gridfs.md">}})).
