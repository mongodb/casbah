^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Briefly: Automatic Type Conversions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
As we mentioned, as soon as you construct a ``MongoClient`` object, a few type conversions will be loaded automatically for you - Scala's builtin regular expressions (e.g. ``"\\d{4}-\\d{2}-\\d{2}".r`` will now serialize to MongoDB automatically with no work from you), as well as a few other things.  The general idea is that common Java types (such as ArrayList) will be returned as the equivalent Scala type.

As many Scala developers tend to prefer `Joda time <http://joda-time.sourceforge.net/>`_ over JDK Dates, you can also explicitly enable serialization and deserialization of them (w/ full support for the `Scala-Time wrappers <http://github.com/jorgeortiz85/scala-time>`_) by an explicit call::

    import com.mongodb.casbah.commons.conversions.scala._
    RegisterJodaTimeConversionHelpers()

Once these are loaded, Joda Time (and Scala Time wrappers) will be saved to MongoDB as proper BSON Dates, and on retrieval/deserialization all BSON Dates will be returned as Joda ``DateTime`` instead of a JDK Date (aka `java.util.Date`).  Because this can cause problems in some instances, you can explicitly unload the Joda Time helpers::

    import com.mongodb.casbah.commons.conversions.scala._
    DeregisterJodaTimeConversionHelpers()

And reload them later as needed.  If you find you need to unload the other helpers as well, you can load and unload them just as easily::

    import com.mongodb.casbah.commons.conversions.scala._
    DeregisterConversionHelpers()
    RegisterConversionHelpers()
