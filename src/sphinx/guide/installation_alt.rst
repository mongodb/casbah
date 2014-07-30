Alternative installation methods
================================

**Casbah** is released to the `Sonatype <http://sonatype.org/>`_ repository,
the latest Casbah build as is |release| and supports the following scala
versions: |scala_versions|.

Using Dependency/Build Managers
-------------------------------

First, you should add the package repository to your Dependency/Build Manager.
Our releases & snapshots are currently hosted at Sonatype; they should
eventually sync to the Central Maven repository.::

   https://oss.sonatype.org/content/repositories/releases/  /* For Releases */
   https://oss.sonatype.org/content/repositories/snapshots/ /* For snapshots */

Set both of these repositories up in the appropriate manner - they contain
Casbah as well as any specific dependencies you may require.

Setting Up Maven
-----------------
You can add Casbah to Maven with the following dependency block.

Please substitute $SCALA_VERSION$ with your Scala version (we support |scala_versions|)

.. parsed-literal::

        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>casbah_$SCALA_VERSION$</artifactId>
            <version> |release| </version>
            <type>pom</type>
        <dependency>


Setting Up Ivy (w/ Ant)
-----------------------
You can add Casbah to Ivy with the following dependency block.

Please substitute $SCALA_VERSION$ with your Scala version (we support |scala_versions|)

.. parsed-literal::

        <dependency org="org.mongodb" name="casbah_$SCALA_VERSION$" rev="|release|"/>

Setting up without a Dependency/Build Manager (Source + Binary)
----------------------------------------------------------------

All Dependencies Jar
''''''''''''''''''''

As Casbah is published in multiple modules installing it manually can take time,
especially as the dependencies change depending on the Scala version you are
using. To simplify this you can download a single all inclusive jar for your
scala version:

    |all_dep_urls|

Once the jar is on your class path you will be able to use Casbah.

Building from source
''''''''''''''''''''

You can always get the latest source for Casbah from
`the github repository <https://github.com/mongodb/casbah>`_::

    $ git clone git://github.com/mongodb/casbah

The `master` branch is once again the leading branch suitable for snapshots and
releases and should be considered (and kept) stable.

.. |scala_versions| replace:: 2.9.3, 2.10.x, 2.11.x
.. |all_dep_urls| replace::
    http://oss.sonatype.org/content/repositories/releases/org/mongodb/casbah_2.11/2.7.3/casbah-alldep_2.11-2.7.3.jar
    http://oss.sonatype.org/content/repositories/releases/org/mongodb/casbah_2.10/2.7.3/casbah-alldep_2.10-2.7.3.jar
    http://oss.sonatype.org/content/repositories/releases/org/mongodb/casbah_2.9.3/2.7.3/casbah-alldep_2.9.3-2.7.3.jar

