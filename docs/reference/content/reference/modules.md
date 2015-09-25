+++
date = "2015-09-24T15:36:12Z"
title = "Casbah Modules"
[menu.main]
  identifier = "Casbah Modules"
  parent = "Reference"
  weight = 30
+++

# Casbah Modules

While Casbah has many stable of features, some users (such as those
using a framework like Lift which already provides MongoDB wrappers)
wanted access to certain parts of Casbah without importing the whole
system. As a result, Casbah has been broken out into several modules
which make it easier to pick and choose the features you want.

If you use the individual modules you'll need to use the import
statement from each of these. If you use the import statement from the
casbah-core module, everything except GridFS will be imported (not
everyone uses GridFS so we don't load it into memory & scope unless it
is needed).

The module names can be used to select which dependencies you want from
maven/ivy/sbt, as we publish individual artifacts. If you import just
casbah, this is a master pom which includes the whole system and will
install all its dependencies, as such there is no single jar file for
Casbah.

This is the breakdown of dependencies and packages:

<table border="1" class="docutils">
  <colgroup>
    <col width="30%">
    <col width="25%">
    <col width="45%">
  </colgroup>
  <thead valign="bottom">
    <tr class="row-odd">
      <th class="head">Module</th>
      <th class="head">Package</th>
      <th class="head">Dependencies</th>
    </tr>
  </thead>
  <tbody valign="top">
    <tr class="row-even">
      <td>
        <h4><a class="reference external" href="http://mongodb.github.com/casbah/api/#com.mongodb.casbah.package?Casbah Core">Casbah Core</a></h4>
        <p>
          <h5>NOTES</h5>
          Provides Scala-friendly wrappers to the Java Driver for connections, collections and MapReduce jobs
        </p>
      </td>
      <td>com.mongodb.casbah</td>
      <td>casbah-commons and casbah-query along with their dependencies transitively
      </td>
    </tr>
    <tr class="row-odd">
      <td>
        <h4><a class="reference external" href="http://mongodb.github.com/casbah/api/#com.mongodb.casbah.commons.package?Casbah Commons">Casbah Commons</a></h4>
        <p>
          <h5>NOTES</h5>
          Provides Scala-friendly
          <a class="reference external" href="http://docs.mongodb.org/manual/?q=DBObject">DBObject</a> &amp;
          <a class="reference external" href="http://docs.mongodb.org/manual/?q=DBList">DBList</a> implementations as well as Implicit conversions for Scala types</p>
      </td>
      <td>com.mongodb.casbah.commons</td>
      <td>
        <ul class="first last simple">
          <li>mongo-java-driver,</li>
          <li>nscala-time,</li>
          <li>slf4j-api,</li>
          <li>slf4j-jcl</li>
        </ul>
      </td>
    </tr>
    <tr class="row-even">
      <td>
        <h4><a class="reference external" href="http://mongodb.github.com/casbah/api/#com.mongodb.casbah.query.package?Query DSL">Query DSL</a></h4>
        <p>
          <h5>NOTES</h5>
          Provides a Scala syntax enhancement mode for creating MongoDB query objects using an Internal DSL supporting Mongo
          <cite>$ Operators</cite>
        </p>
      </td>
      <td>com.mongodb.casbah.query</td>
      <td>casbah-commons along with their dependencies transitively
      </td>
    </tr>
    <tr class="row-odd">
      <td>
        <h4><a class="reference external" href="http://mongodb.github.com/casbah/api#com.mongodb.casbah.gridfs.package?Gridfs">Gridfs</a></h4>
        <p>
          <h5>NOTES</h5>
          Provides Scala enhanced wrappers to MongoDB’s GridFS filesystem</p>
      </td>
      <td>com.mongodb.casbah.gridfs</td>
      <td>casbah-commons and casbah-query along with their dependencies transitively
      </td>
    </tr>
  </tbody>
</table>

We cover the import of each module in their appropriate tutorials, but
each module has its own Imports object which loads all of its necessary
code. By way of example both of these statements would import the Query
DSL:

~~~scala
// Imports core, which grabs everything including Query DSL
import com.mongodb.casbah.Imports._

// Imports just the Query DSL along with Commons and its dependencies
import com.mongodb.casbah.query.Imports._

// Import GridFS modules
import com.mongodb.casbah.gridfs.Imports._
~~~
