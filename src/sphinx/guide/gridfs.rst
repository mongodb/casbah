======
GridFS
======
.. highlight:: scala

``GridFS`` is a specification for storing and retrieving files
that exceed the BSON-document `size limit
<http://docs.mongodb.org/manual/reference/limits/#BSON Document Size>`_ of 16MB.

Instead of storing a file in a single document, GridFS divides a file
into parts, or chunks, [#chunk-disambiguation]_ and stores each of
those chunks as a separate document. By default GridFS limits chunk
size to 256k. GridFS uses two collections to store files. One
collection stores the file chunks, and the other stores file metadata.

When you query a GridFS store for a file, the driver or client will
reassemble the chunks as needed. You can perform range queries on
files stored through GridFS.  You also can access information from
arbitrary sections of files, which allows you to "skip" into the
middle of a video or audio file.

GridFS is useful not only for storing files that exceed 16MB but also
for storing any files for which you want access without having to load
the entire file into memory. For more information on the indications
of GridFS, see `faq-developers-when-to-use-gridfs
<http://docs.mongodb.org/manual/faq/developers/#faq-developers-when-to-use-gridfs>`_.


.. [#chunk-disambiguation] The use of the term *chunks* in the context
   of GridFS is not related to the use of the term *chunks* in
   the context of sharding.

Using GridFS in Casbah
======================

GridFS is a separate package in Casbah and to use it you must import it
explicitly.  See the `full gridfs api docs
<http://mongodb.github.io/casbah/api/#com.mongodb.casbah.gridfs.package>`_ for
more information about the package.

Example use case::

    import java.io.FileInputStream
    import com.mongodb.casbah.Imports._
    import com.mongodb.casbah.gridfs.Imports._

    // Connect to the database
    val mongoClient = MongoClient()("test")

    // Pass the connection to the GridFS class
    val gridfs = GridFS(mongoClient)

    // Save a file to GridFS
    val logo = new FileInputStream("mongo.png")
    val id = gridfs(logo) { f =>
        f.filename = "mongodb_logo.png"
        f.contentType = "image/png"
    }

    // Find a file in GridFS by its ObjectId
    val myFile = gridfs.findOne(id.get.asInstanceOf[ObjectId])

    // Or find a file in GridFS by its filename
    val myFile = gridfs.findOne("mongodb_logo.png")

    // Print all filenames stored in GridFS
    for (f <- gridfs) println(f.filename)

Joda DateTime
-------------
Due to hardcoding in the Java GridFS driver the Joda Time
serialization hooks break with GridFS.  It tries to explicitly cast
certain date fields as a ``java.util.Date``.  To that
end, on all find ops we explicitly unload the Joda Time deserializers and
reload them when we're done (if they were loaded before we started).  This
allows GridFS to always work but *MAY* cause thread safety issues - e.g.
if you have another non-GridFS read happening at the same time in another
thread at the same time, it may fail to deserialize BSON Dates as Joda
DateTime - and blow up.  Be careful --- generally we don't recommend mixing
Joda Time and GridFS in the same JVM at the moment.
