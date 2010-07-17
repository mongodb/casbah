/**
 * Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
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

package com.novus.casbah
package mongodb
package test


import net.lag.configgy.Configgy

import org.specs._
import org.specs.specification.PendingUntilFixed

import scala.collection.mutable.ArrayBuffer
import scala.reflect.BeanInfo

import Imports._
import Imports.log
import mongodb.mapper.Mapper
import mongodb.mapper.annotations._

@BeanInfo
@MappedBy(classOf[WidgetMapper])
class Widget(@ID var name: String, @Key var price: Int, @Key var tags: ArrayBuffer[String]) {
  override def toString() = "Widget(" + name + ", " + price + ", " + tags + ")"
}

@BeanInfo
@MappedBy(classOf[PiggyMapper])
class Piggy {
  @ID(auto = true)
  var id: String = _

  @Key
  var giggity: String = _
}

class WidgetMapper extends Mapper[String, Widget] {
  conn = MongoConnection()
  db = "mapper_test"
  coll = "widgets"
}

class PiggyMapper extends Mapper[String, Piggy] {
  conn = MongoConnection()
  db = "mapper_test"
  coll = "piggies"
}

class MapperSpec extends Specification with PendingUntilFixed {
  detailedDiffs()

  doBeforeSpec {
    Configgy.configure("src/test/resources/casbah.config")
    // drop db
  }

  "a mapper" should {
    shareVariables

    val widget = new Widget("something", 7824,
			    ArrayBuffer("one", "two", "three"))

    Mapper[Widget].upsert(widget)

    "discover mapper for a class" in {
      Mapper[Piggy].getClass.getSimpleName must_== classOf[PiggyMapper].getSimpleName
    }

    "compute id" in {
      Mapper[Widget].id_prop must haveClass[java.beans.PropertyDescriptor]
      Mapper[Widget].id_prop.getName must_== "name"
    }

    "cull non-ids" in {
      Mapper[Widget].non_id_props must exist(pd => pd.getName == "price")
    }

    "convert object to MongoDBObject" in {
      val dbo = Mapper[Widget].to_dbo(widget)
      log.info("MongoDBObject: %s", dbo)
      dbo must havePair("_id", "something")
    }

    "retrieve an object" in {
      Mapper[Widget].find_one(widget.name) must beSome[Widget].which {
        loaded =>
          loaded.name must_== widget.name
        loaded.price must_== widget.price
        loaded.tags.toSet must_== widget.tags.toSet
      }
    } pendingUntilFixed

    "automatically assign MongoDB OID-s" in {
      val piggy = new Piggy
      piggy.giggity = "oy vey"
      log.info("piggy dbo: %s", Mapper[Piggy].to_dbo(piggy))
      Some(Mapper[Piggy].upsert(piggy)) must beSome[Piggy].which(_.id must notBeNull)
    }

    "retrieve all stored objects" in {
      Mapper[Piggy].find must notBeEmpty
    } pendingUntilFixed
  }
}
