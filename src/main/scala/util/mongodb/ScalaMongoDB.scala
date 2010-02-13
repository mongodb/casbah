/**
 * Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.util.mongodb

import com.mongodb._
import org.scala_tools.javautils.Imports._
import Implicits._



class ScalaMongoDB(val underlying: DB) {
  def apply(collection: String) = underlying.getCollection(collection).asScala
  def apply[A <: DBObject](collection: String, clazz: Class[A])(implicit m: scala.reflect.Manifest[A]) = underlying.getCollection(collection).asScalaTyped(m)
  def addUser(username: String, passwd: String) = underlying.addUser(username, passwd.toArray)
  def authenticate(username: String, passwd: String) = underlying.authenticate(username, passwd.toArray)
  def command(cmd: DBObject) = underlying.command(cmd)
  def createCollection(name: String, o: DBObject) = underlying.createCollection(name, o)
  def doEval(code: String, args: AnyRef*) = underlying.doEval(code, args: _*)
  //def doGetCollection(name: String) = underlying.doGetCollection(name)
  def dropDatabase() = underlying.dropDatabase()
  def eval(code: String, args: AnyRef*) = underlying.eval(code, args: _*)
  def forceError() = underlying.forceError
  def getCollection(name: String) = underlying.getCollection(name)
  def getCollectionFromFull(fullNameSpace: String) = underlying.getCollectionFromFull(fullNameSpace)
  def getCollectionFromString(s: String) = underlying.getCollectionFromString(s)
  def getCollectionNames() = underlying.getCollectionNames().asScala
  def getLastError() = underlying.getLastError
  def getName() = underlying.getName
  def getPreviousError() = underlying.getPreviousError
  def getSisterDB(name: String) = underlying.getSisterDB(name)
  def getWriteConcern() = underlying.getWriteConcern
  def requestDone() = underlying.requestDone
  def requestEnsureConnection() = underlying.requestEnsureConnection
  def requestStart() = underlying.requestStart
  def resetError() = underlying.resetError
  def resetIndexCache() = underlying.resetIndexCache
  def setReadOnly(b: Boolean) = underlying.setReadOnly(b)
  def setWriteConcern(concern: DB.WriteConcern) = underlying.setWriteConcern(concern)
  override def toString() = underlying.toString
}