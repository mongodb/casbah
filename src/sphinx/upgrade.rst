Upgrade
=======


Version 2.6.1
-------------

The com.mongodb.casbah.commons.test dependencies are now marked in the test
classpath, so to install::

    "org.mongodb" %% "casbah-commons" % "2.6.2" % "test"


Version 2.6.0
-------------

No upgrade needed.

Version 2.5.1
-------------

Scala 2.10
~~~~~~~~~~

The `-Yeta-expand-keeps-star` compiler flag is no longer required.

Version 2.5.0
-------------

Scala 2.10
~~~~~~~~~~

Because of how scala 2.10 handles repeated parameters you may
need to build with the `-Yeta-expand-keeps-star` flag to upgrade your codebase.
