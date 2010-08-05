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

import scala.collection.mutable.{Buffer, ArrayBuffer}
import scala.reflect.BeanInfo

import Imports._
import Imports.log
import mongodb.mapper.Mapper
import mongodb.mapper.annotations._

@BeanInfo
@MappedBy(classOf[Any])
class Widget(@ID var name: String, @Key var price: Int) {
  def this() = this(null, 0)
  override def toString() = "Widget(" + name + ", " + price + ")"
}

@BeanInfo
@MappedBy(classOf[Any])
class Piggy {
  @ID(auto = true)
  var id: ObjectId = _

  @Key
  var giggity: String = _

  @Key
  var favorite_foods: List[String] = Nil

  @Key
  var this_field_is_always_null: String = null

  @Key
  var badges: Buffer[Badge] = ArrayBuffer()

  def this(g: String) = {
    this()
    giggity = g
  }
}

@BeanInfo
@MappedBy(classOf[Any])
class Chair {
  @ID(auto = true)
  var id: ObjectId = _

  @Key
  var optional_piggy: Option[Piggy] = None
}

@BeanInfo
@MappedBy(classOf[Any])
class Badge {
  @ID
  var name: String = _

  def this(n: String) = {
    this()
    name = n
  }
}

object ChairMapper extends Mapper[Chair] {
  conn = MongoConnection()
  db = "mapper_test"
  coll = "chairs"
}

object WidgetMapper extends Mapper[Widget] {
  conn = MongoConnection()
  db = "mapper_test"
  coll = "widgets"
}

object PiggyMapper extends Mapper[Piggy] {
  conn = MongoConnection()
  db = "mapper_test"
  coll = "piggies"
}

object BadgeMapper extends Mapper[Badge]

class MapperSpec extends Specification with PendingUntilFixed {
  List(ChairMapper, WidgetMapper, PiggyMapper, BadgeMapper)

  detailedDiffs()

  doBeforeSpec {
    Configgy.configure("src/test/resources/casbah.config")
    // drop db
  }

  "a mapper" should {
    shareVariables

    val widget = new Widget("something", 7824)
    Mapper[Widget].upsert(widget)

    "discover mapper for a class" in {
      Mapper[Piggy] must_== PiggyMapper
    }

    "compute id" in {
      Mapper[Widget].idProp must haveClass[java.beans.PropertyDescriptor]
      Mapper[Widget].idProp.getName must_== "name"
    }

    "cull non-ids" in {
      Mapper[Widget].nonIdProps must exist(pd => pd.getName == "price")
    }

    "convert object to MongoDBObject" in {
      val dbo = Mapper[Widget].asMongoDBObject(widget)
      dbo must havePair("_id", "something")
    }

    "retrieve an object" in {
      Mapper[Widget].findOne(widget.name) must beSome[Widget].which {
        loaded =>
          loaded.name must_== widget.name
        loaded.price must_== widget.price
      }
    }

    "automatically assign (& retrieve objects by) MongoDB OID-s" in {
      val piggy = new Piggy("oy vey")
      Some(Mapper[Piggy].upsert(piggy)) must beSome[Piggy].which {
        saved =>
          saved.id must notBeNull
        Mapper[Piggy].findOne(saved.id) must beSome[Piggy].which {
          retrieved =>
            retrieved.id must_== saved.id
          retrieved.giggity must_== piggy.giggity
        }
      }
    }

    "exclude null non-@ID fields from output" in {
      val dbo = Mapper[Piggy].asMongoDBObject(new Piggy("has a null field"))
      dbo must notHaveKey("this_field_is_always_null")
    }

    "save & de-serialize nested documents" in {
      val FOODS = "bacon" :: "steak" :: "eggs" :: "club mate" :: Nil
      val BADGES = ArrayBuffer(new Badge("mile high"), new Badge("swine"))

      val before = new Chair

      val piggy = new Piggy("foo")
      piggy.favorite_foods = FOODS
      piggy.badges = BADGES
      before.optional_piggy = Some(piggy)

      val id = Mapper[Chair].upsert(before).id

      Mapper[Chair].findOne(id) must beSome[Chair].which {
        after =>
          after.optional_piggy must beSome[Piggy].which {
            piggy =>
              piggy.giggity == before.optional_piggy.get.giggity
	    piggy.favorite_foods must containAll(FOODS)
          }
      }
    }
  }

  "a mapped collection" should {
    val coll = MongoConnection()("mapper_test").mapped[Piggy]
    "return objects of required class" in {
      coll.findOne must beSome[Piggy]
    }
  }
}
