Casbah Modules
**************

While Casbah has many stable of features, some users (such as those using a
framework like Lift which already provides MongoDB wrappers) wanted access to
certain parts of Casbah without importing the whole system.  As a result,
Casbah has been broken out into several modules which make it easier to pick
and choose the features you want.

If you use the individual modules you'll need to use the import statement from
each of these.  If you use the import statement from the `casbah-core` module,
everything except GridFS will be imported (not everyone uses GridFS so we don't
load it into memory & scope unless it is needed).

The module names can be used to select which dependencies you want from
maven/ivy/sbt, as we publish individual artifacts.
If you import just `casbah`, this is a master pom which includes the whole
system and will install all its dependencies, as such there is no single jar
file for Casbah.

This is the breakdown of dependencies and packages:

.. _casbah-core:
.. _casbah-commons:
.. _casbah-query:
.. _casbah-gridfs:

  +-------------------------------------+----------------------------+-------------------------------------------------+
  | Module                              | Package                    | Dependencies                                    |
  +=====================================+============================+=================================================+
  | :casbah-core:`Casbah Core`          | com.mongodb.casbah         | casbah-commons and casbah-query                 |
  |                                     |                            | along with their                                |
  | **NOTES**                           |                            | dependencies                                    |
  |                                     |                            | transitively                                    |
  | Provides Scala-friendly             |                            |                                                 |
  | wrappers to the Java Driver for     |                            |                                                 |
  | connections, collections and        |                            |                                                 |
  | MapReduce jobs                      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :casbah-commons:`Casbah Commons`    | com.mongodb.casbah.commons |                                                 |
  |                                     |                            |  * mongo-java-driver,                           |
  | **NOTES**                           |                            |  * nscala-time,                                 |
  |                                     |                            |  * slf4j-api,                                   |
  | Provides Scala-friendly             |                            |  * slf4j-jcl                                    |
  | :dochub:`DBObject` &                |                            |                                                 |
  | :dochub:`DBList`                    |                            |                                                 |
  | implementations as well as Implicit |                            |                                                 |
  | conversions for Scala types         |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :casbah-query:`Query DSL`           | com.mongodb.casbah.query   | casbah-commons                                  |
  |                                     |                            | along with their                                |
  | **NOTES**                           |                            | dependencies                                    |
  |                                     |                            | transitively                                    |
  | Provides a Scala syntax enhancement |                            |                                                 |
  | mode for creating MongoDB query     |                            |                                                 |
  | objects using an Internal DSL       |                            |                                                 |
  | supporting Mongo `$ Operators`      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+
  | :casbah-gridfs:`Gridfs`             | com.mongodb.casbah.gridfs  | casbah-commons and casbah-query                 |
  |                                     |                            | along with their                                |
  | **NOTES**                           |                            | dependencies                                    |
  |                                     |                            | transitively                                    |
  | Provides Scala enhanced wrappers    |                            |                                                 |
  | to MongoDB's GridFS filesystem      |                            |                                                 |
  +-------------------------------------+----------------------------+-------------------------------------------------+

We cover the import of each module in their appropriate tutorials, but each
module has its own `Imports` object which loads all of its necessary code.
By way of example both of these statements would import the Query DSL::

    // Imports core, which grabs everything including Query DSL
    import com.mongodb.casbah.Imports._
    // Imports just the Query DSL along with Commons and its dependencies
    import com.mongodb.casbah.query.Imports._

