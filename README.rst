Casbah
======
:Info: Scala toolkit for MongoDB (com.mongodb.casbah). See `the mongo site <http://www.mongodb.org>`_ for more information. See `github <http://github.com/mongodb/casbah/tree/master>`_ for the latest source.
:Author: Brendan W. McAdams
:Maintainer: Ross Lawley <ross@mongodb.com>

.. image:: https://travis-ci.org/mongodb/casbah.png?branch=master
  :target: https://travis-ci.org/mongodb/casbah

About
=====
Casbah is an interface for `MongoDB <http://www.mongodb.org>`_ designed to
provide more flexible access from both Java and Scala.  The core focus is on
providing a Scala oriented wrapper interface around the Java mongo driver.

For the Scala side, contains series of wrappers and DSL-like functionality for
utilizing MongoDB from within Scala. This currently utilises the very
Java-oriented Mongo Java driver, and attempts to provide more scala-like
functionality on top of it. This has been tested with MongoDB 1.2.x+ and 2.x of
the Mongo java driver.

We are constantly adding new functionality, and maintain a detailed
`Casbah Tutorial <http://mongodb.github.io/casbah/tutorial.html>`_.

Please address any questions or problems to the
`Casbah Mailing List <http://groups.google.com/group/mongodb-casbah-users>`_ on
Google Groups.

For more information about Casbah see the
`API Docs <http://mongodb.github.io/casbah/api/>`_ or
`Tutorial <http://mongodb.github.io/casbah/guide/>`_.

Project Artifacts
-----------------

Casbah is separated out into several artifacts:

* *casbah-commons*
   Provides utilities to improve working with Scala and MongoDB together
   without dependencies on anything but the MongoDB Java Driver and
   ScalaJ-Collection.  This includes Scala Collections 2.8 compatible
   wrappers for DBList and DBObject as well as type conversion facilities to
   simplify the use of Scala types with MongoDB (and register your own custom
   types)
* *casbah-query*
   The Query DSL which provides an internal Scala DSL for querying MongoDB
   using native, MongoDB syntax operators.  This only depends upon Commons and
   can be used standalone without the rest of Casbah.
* *casbah-core*
   This is the wrappers for interacting directly with MongoDB providing more
   Scala-like interactions.  It depends upon both Commons and Query as well as
   ScalaTime for use of JodaTime (which we prefer over JDK date but you are
   welcome to use JDK Dates).
* *casbah-gridfs*
   This provides enhancement wrappers to GridFS including loan pattern
   support.  It is dependent on Core (and by transitive property, Commons &
   Query as well) but is not included in Core - you must explicitly load if it
   you want to use GridFS.


Support / Feedback
==================

For issues with, questions about, or feedback for Casbah, please look into
our `support channels <http://www.mongodb.org/about/support>`_. Please
do not email any of the Casbah developers directly with issues or
questions - you're more likely to get an answer on the
`casbah mailing list <http://groups.google.com/group/mongodb-casbah-users>`_
or the `mongodb-user <http://groups.google.com/group/mongodb-user>`_ list on
Google Groups.

Bugs / Feature Requests
=======================

Think you’ve found a bug? Want to see a new feature in Casbah? Please open a
case in our issue management tool, JIRA:

- `Create an account and login <https://jira.mongodb.org>`_.
- Navigate to `the CASBAH project <https://jira.mongodb.org/browse/CASBAH>`_.
- Click **Create Issue** - Please provide as much information as possible about
  the issue type and how to reproduce it.

Bug reports in JIRA for all driver projects (i.e. CASBAH, JAVA, CSHARP) and the
Core Server (i.e. SERVER) project are **public**.

Security Vulnerabilities
========================

If you’ve identified a security vulnerability in a driver or any other
MongoDB project, please report it according to the `instructions here
<http://docs.mongodb.org/manual/tutorial/create-a-vulnerability-report>`_.

