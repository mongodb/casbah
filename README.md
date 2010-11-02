# Casbah 
## Scala toolkit for MongoDB (com.mongodb.casbah)

Maintainers
-----------
Brendan W. McAdams <brendan@10gen.com>

Contributors
-----------
Max Afonov <max@bumnetworks.com>

Novus Partners, Inc. <http://novus.com> kindly sponsored the development of this project prior to Version 2.0, for which we offer our thanks.


Notice for users of old, Pre-2.0 versions of Casbah
---------------------------------------------------

The package has changed as of version 2.0 to `com.mongodb.casbah`; version 1.1 of Casbah used the package `com.novus.casbah` and versions 1.0 and below were packaged as `com.novus.casbah.mongodb`.  For convenience sake, a Maven artifact has been provided as `casbah-pkgbridge` for 2.0 which provides the old packagespaces from prior to 2.0.  These will throw deprecation warnings of  course but will help migration.  These *WILL* go away in the next major point release (presumably, 2.1).

About
-----
Casbah is an interface for [MongoDB][MongoDB] designed to provide more flexible access from both Java and Scala.  While the current core focus is on providing a Scala oriented wrapper interface around the Java mongo driver, support for other JVM languages may come in the future.

For the Scala side, contains series of wrappers and DSL-like functionality for utilizing MongoDB from within Scala. This currently utilises the very Java-oriented Mongo Java driver, and attempts to provide more scala-like functionality on top of it. This has been tested with MongoDB 1.2.x+ and v2.x of the Mongo java driver.

We are constantly adding new functionality, and maintain a detailed [Tutorial][Tutorial].

Please address any questions or problems to the [Casbah Mailing List][mongodb-casbah-users] on Google Groups.

GitHub kindly hosts our [Documentation][Documentation] including the [API Docs][API Docs] & [Tutorial][Tutorial].

   [mongodb]: http://mongodb.org "MongoDB"
   [github]: http://github.com/novus/casbah "Casbah on GitHub"
   [api docs]: http://novus.github.com/docs/casbah/api/ "API Docs on GitHub"
   [documentation]: http://novus.github.com/docs/casbah/ "Docs on GitHub"
   [tutorial]: http://novus.github.com/docs/casbah/sphinx/html/intro/getting_started.html "Casbah Tutorial"
   [mongodb-casbah-users]: http://groups.google.com/group/mongodb-casbah-users "Casbah Mailing List"
