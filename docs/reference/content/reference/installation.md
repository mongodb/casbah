+++
date = "2015-09-23T15:36:12Z"
title = "Installation"
[menu.main]
  identifier = "Installation"
  parent = "Reference"
  weight = 20
+++

# Installation

**Casbah** is released to the [Sonatype](http://sonatype.org/)
repository, the latest Casbah build as is 3.1.0-rc0 and supports the
following scala versions: 2.10.x, 2.11.x, 2.12.0-M2.

The easiest way to install the latest Casbah driver (3.0.0) is by
using [sbt - the Scala Build Tool](http://www.scala-sbt.org/).

## Setting up via sbt

Once you have your sbt project setup - see the [sbt setup
guide](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html)
for help there.

Add Casbah to sbt to your `./build.sbt` file:

To test your installation load the sbt console and try importing casbah:

    $ sbt console
    scala> import com.mongodb.casbah.Imports._
    import com.mongodb.casbah.Imports._

## Problem solving

If sbt can't find casbah then you may have an older version of sbt and
will need to add the sonatype resolvers to your ./build.sbt file:

~~~scala
// For stable releases
resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
// For SNAPSHOT releases
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
~~~

## Alternative installation methods

**Casbah** is released to the [Sonatype](http://sonatype.org/)
repository, the latest Casbah build as is 3.0.0 and supports the
following scala versions: 2.10.x, 2.11.x, 2.12.0-M2.

## Using Dependency/Build Managers

First, you should add the package repository to your Dependency/Build
Manager. Our releases & snapshots are currently hosted at Sonatype; they
should eventually sync to the Central Maven repository.:

~~~bash
https://oss.sonatype.org/content/repositories/releases/  /* For Releases */
https://oss.sonatype.org/content/repositories/snapshots/ /* For snapshots */
~~~

Set both of these repositories up in the appropriate manner - they
contain Casbah as well as any specific dependencies you may require.

## Setting Up Maven

You can add Casbah to Maven with the following dependency block.

~~~xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>casbah_$SCALA_VERSION$</artifactId>
    <version> 3.0.0 </version>
    <type>pom</type>
<dependency>
~~~

Please substitute `$SCALA_VERSION$` with your Scala version (we support 2.10.x, 2.11.x, 2.12.0-M2.)

## Setting Up Ivy (w/ Ant)

You can add Casbah to Ivy with the following dependency block.

Please substitute `$SCALA_VERSION$` with your Scala version (we support 2.10.x, 2.11.x, 2.12.0-M2.)

## Setting up without a Dependency/Build Manager (Source + Binary)

There are two choices:

### All Dependencies Jar

As Casbah is published in multiple modules installing it manually can
take time, especially as the dependencies change depending on the Scala
version you are using. To simplify this you can download a single all
inclusive jar for your scala version:

 * [Casbah Scala 2.12.0-M2](http://oss.sonatype.org/content/repositories/releases/org/mongodb/casbah_2.12/3.1.0-rc0/casbah-alldep_2.12.0-M2-3.1.0-rc0.jar)
 * [Casbah Scala 2.11](http://oss.sonatype.org/content/repositories/releases/org/mongodb/casbah_2.11/3.1.0-rc0/casbah-alldep_2.11-3.1.0-rc0.jar)
 * [Casbah Scala 2.10](http://oss.sonatype.org/content/repositories/releases/org/mongodb/casbah_2.10/3.1.0-rc0/casbah-alldep_2.10-3.1.0-rc0.jar)

Once the jar is on your class path you will be able to use Casbah.

### Building from source

You can always get the latest source for Casbah from [the github
repository](https://github.com/mongodb/casbah):

    $ git clone git://github.com/mongodb/casbah

The master branch is once again the leading branch suitable for
snapshots and releases and should be considered (and kept) stable.
