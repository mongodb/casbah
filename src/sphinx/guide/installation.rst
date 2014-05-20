Installation
============
.. highlight:: scala

**Casbah** is released to the `Sonatype <http://sonatype.org/>`_ repository,
the latest Casbah build as is |release| and supports the following scala
versions: |scala_versions|.

The easiest way to install the latest Casbah driver (|release|) is by using
`sbt - the Scala Build Tool <http://www.scala-sbt.org/>`_.

Setting up via sbt
------------------

Once you have your sbt project setup - see the
`sbt setup guide <http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html>`_
for help there.

Add Casbah to sbt to your ``./build.sbt`` file:

.. parsed-literal::

   libraryDependencies += "org.mongodb" %% "casbah" % "|release|"

.. note :: The double percentages (`%%`) is not a typo---it tells sbt that the
    library is crossbuilt and to find the appropriate version for your
    project's Scala version.

To test your installation load the sbt console and try importing casbah::

    $ sbt console
    scala> import com.mongodb.casbah.Imports._
    import com.mongodb.casbah.Imports._

Problem solving
---------------
If sbt can't find casbah then you may have an older version of sbt and will
need to add the sonatype resolvers to your `./build.sbt` file::

    // For stable releases
    resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
    // For SNAPSHOT releases
    resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

Alternative installation methods
--------------------------------
You can install Casbah with maven, ivy or from source - see the
:doc:`alternative install <installation_alt>` documentation.

.. |scala_versions| replace:: 2.9.3, 2.10.x, 2.11.x
