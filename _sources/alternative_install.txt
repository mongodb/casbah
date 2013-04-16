Alternative installation methods for Casbah
===========================================

.. note:: There is no single jar as Casbah is split into multiple modules.
    See the `casbah modules <modules>`_ documentation for more information.

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

Please substitute $SCALA_VERSION$ with your Scala version (we support 2.9.x+)

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

Please substitute $SCALA_VERSION$ with your Scala version (we support 2.9.x+)

.. parsed-literal::

        <dependency org="org.mongodb" name="casbah_$SCALA_VERSION$" rev="|release|"/>

Setting up without a Dependency/Build Manager (Source + Binary)
----------------------------------------------------------------
The builds are published to the
`Sonatype.org <https://oss.sonatype.org/content/repositories/releases/>`_
Maven repositories and should be easily available to add to an existing Scala
project.

You can always get the latest source for Casbah from
`the github repository <https://github.com/mongodb/casbah>`_::

    $ git clone git://github.com/mongodb/casbah

The `master` branch is once again the leading branch suitable for snapshots and
releases and should be considered (and kept) stable.
