=========
Query DSL
=========
.. highlight:: scala

Casbah provides a rich fluid query syntax, that allows you to
construct ``DBObjects`` on the fly using MongoDB query operators.

Query Selectors
---------------

Comparison Operators
~~~~~~~~~~~~~~~~~~~~

* ``$all`` Matches arrays that contain all elements specified::

    "size" $all ("S", "M", "L")

* ``$eq`` Matches values that are equal to the value specified::

    "price" $eq 10

* ``$gt`` Matches values that are greater than the value specified in the query
* ``$gte`` Matches values that are equal to or greater than the value specified::

    "price" $gt 10
    "price" $gte 10

* ``$in`` Matches any of the values that exist in an array specified::

    "size" $in ("S", "M", "L")

* ``$lt`` Matches values that are less than the value specified in the query
* ``$lte`` Matches values that are less than or equal to the value specified::

    "price" $lt 100
    "price" $lte 100

* ``$ne`` Matches all values that are not equal to the value specified::

    "price" $ne 1000

* ``$nin`` Matches values that **do not** exist in an array specified::

    "size" $nin ("S", "XXL")

Logical Operators
~~~~~~~~~~~~~~~~~

* ``$or`` Joins query clauses with a logical ``OR`` returns all
  documents that match the conditions of either clause::

    $or( "price" $lt 5 $gt 1, "promotion" $eq true )
    $or( ( "price" $lt 5 $gt 1 ) :: ( "stock" $gte 1 ) )

* ``$and`` Joins query clauses with a logical ``AND`` returns all
  documents that match the conditions of both clauses::

    $and( "price" $lt 5 $gt 1, "stock" $gte 1 )
    $and( ( "price" $lt 5 $gt 1 ) :: ( "stock" $gte 1 ) )


* ``$not`` Inverts the effect of a query expression and returns
  documents that do *not* match the query expression::

    "price" $not { _ $gte 5.1 }

* ``$nor`` Joins query clauses with a logical ``NOR`` returns all
  documents that fail to match both clauses::

    $nor( "price" $eq 1.99 , "qty" $lt 20, "sale" $eq true )
    $nor( ( "price" $lt 5 $gt 1 ) :: ( "stock" $gte 1 ) )

Element Operators
~~~~~~~~~~~~~~~~~

* ``$exists`` Matches documents that have the specified field::

    "qty" $exists true

* ``$mod`` Performs a modulo operation on the value of a field and selects
  documents with a specified result::

    "qty" $mod (5, 0)

* ``$type`` Selects documents if a field is of the specified type::

    "size".$type[BasicDBList]

JavaScript Operators
~~~~~~~~~~~~~~~~~~~~

* ``$where`` Matches documents that satisfy a JavaScript expression::

    $where("function () { this.credits == this.debits }")

* ``$regex`` Selects documents where values match a specified regular expression.
  You can also use native regular expressions::

    "foo" $regex "^bar$"
    "foo" $eq "^bar$".r

Geospatial Operators
~~~~~~~~~~~~~~~~~~~~

* ``$geoWithin`` Selects geometries within a bounding `GeoJSON
  <http://docs.mongodb.org/manual/reference/glossary/#term-geojson>`_ geometry::

    // Create a GeoJson geometry document
    var geo = MongoDBObject("$geometry" ->
            MongoDBObject("$type" -> "polygon",
              "coordinates" -> (((GeoCoords(74.2332, -75.23452),
                                  GeoCoords(123, 456),
                                  GeoCoords(74.2332, -75.23452))))))

    // Example $geoWithin Queries
    "location" $geoWithin(geo)
    "location" $geoWithin $box ((74.2332, -75.23452), (123, 456))
    "location" $geoWithin $center ((50, 50), 10)
    "location" $geoWithin $centerSphere ((50, 50), 10)


* ``$geoIntersects`` Selects geometries that intersect with a  `GeoJSON
  <http://docs.mongodb.org/manual/reference/glossary/#term-geojson>`_ geometry::

    // Create a GeoJson geometry document
    var geo = MongoDBObject("$geometry" ->
                MongoDBObject("$type" -> "polygon",
                  "coordinates" -> (((GeoCoords(74.2332, -75.23452),
                                      GeoCoords(123, 456),
                                      GeoCoords(74.2332, -75.23452))))))
    val near = "location" $geoIntersects geo

* ``$near`` Returns geospatial objects in proximity to a point::

    "location" $near (74.2332, -75.23452)

* ``$nearSphere`` Returns geospatial objects in proximity to a point on a sphere::

    "location" $nearSphere (74.2332, -75.23452)

Array Query Operators
~~~~~~~~~~~~~~~~~~~~~

* ``$elemMatch`` Selects documents if element in the array field matches all
  the specified ``$elemMatch`` conditions::

    "colour" $elemMatch (MongoDBObject("base" -> "red", "flash" -> "silver")

* ``$size`` Selects documents if the array field is a specified size::

    "comments" $size 12

Update DSL Operators
--------------------

Field Operators
~~~~~~~~~~~~~~~

* ``$inc`` Increments the value of the field by the specified amount::

    $inc("sold" -> 1, "stock" -> -1)

* ``$rename`` Renames a field::

    $rename("color" -> "colour", "realize" -> "realise")

* ``$setOnInsert`` Sets the value of a field upon documentation creation
  during an upsert. Has no effect on update operations that modify existing
  documents::

    $setOnInsert("promotion" -> "new")

* ``$set`` Sets the value of a field in an existing document

    $set("promotion" -> "sale", "qty" -> 100)

* ``$unset`` Removes the specified field from an existing document

    $unset("promotion")

Array Update Operators
~~~~~~~~~~~~~~~~~~~~~~

* ``$addToSet`` Adds elements to an existing array only if they do not
  already exist in the set::

    $addToSet("sizes" -> "L", "colours" -> "Blue")
    $addToSet("sizes") $each ("S", "M", "L", "XL")

* ``$pop`` Removes the first or last item of an array::

    $pop("sizes" -> "L")

* ``$pull`` Removes items from an array that match a query statement::

    $pull("sizes" -> "L")
    $pull("widgets" $gt 2 )

* ``$pullAll`` Removes multiple values from an array::

    $pullAll("sizes" -> ("S", "XL"))

* ``$push`` Adds an item to an array::

    $push("sizes" -> "L")
    $push("widgets" $gt 2 )
    $push("sizes") $each ("S", "M", "L", "XL")


* ``$pushAll`` *Deprecated.* Adds several items to an array::

    $pushAll("sizes" -> ("S", "XL"))

Bitwise Operators
-----------------

* ``$bit`` Performs bitwise ``AND`` and ``OR`` updates of integer values::

    $bit("foo") and 5

For the full query syntax to MongoDB see the core docs at:
`docs.mongodb.org <http://docs.mongodb.org>`_
