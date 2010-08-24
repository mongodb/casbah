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

import java.util.Date

import net.lag.configgy.Configgy

import org.specs._
import org.specs.specification.PendingUntilFixed

import scala.collection.Map
import scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import scala.collection.JavaConversions._
import scala.reflect.BeanInfo

import java.math.BigInteger

import Imports._
import Imports.log
import mongodb.mapper.Mapper
import mongodb.mapper.annotations._

@BeanInfo
class Widget(@ID var name: String, @Key var price: Int) {
  def this() = this(null, 0)
  override def toString() = "Widget(" + name + ", " + price + ")"
}

@BeanInfo
class Piggy(@Key val giggity: String) {
  @ID(auto = true)
  var id: ObjectId = _

  @Key
  var favorite_foods: List[String] = Nil

  @Key
  var this_field_is_always_null: String = null

  @Key
  var badges: Buffer[Badge] = ArrayBuffer()

  @Key
  var political_views: Map[String, String] = MMap.empty[String, String]

  @Key
  var family: Map[String, Piggy] = MMap.empty[String, Piggy]

  @Key
  var balance: Option[BigDecimal] = None
}

@BeanInfo
class Chair {
  @ID(auto = true)
  var id: ObjectId = _

  @Key
  var optional_piggy: Option[Piggy] = None

  @Key var always_here: Option[String] = Some("foo")
  @Key var never_here: Option[String] = None

  @Key
  lazy val timestamp: Date = new Date
}

@BeanInfo
case class Badge(@ID val name: String) extends Foo

trait Foo {
  @Key var bar: String = _
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
      Mapper[Widget].idProp.get.name must_== "name"
    }

    "cull non-ids" in {
      Mapper[Widget].nonIdProps must exist(pd => pd.name == "price")
    }

    "convert object to MongoDBObject" in {
      val dbo: MongoDBObject = Mapper[Widget].asDBObject(widget)
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
      val dbo: MongoDBObject = Mapper[Piggy].asDBObject(new Piggy("has a null field"))
      dbo must notHaveKey("this_field_is_always_null")
    }

    "save & de-serialize nested documents" in {
      val FOODS = "bacon" :: "steak" :: "eggs" :: "club mate" :: Nil
      val BADGES = ArrayBuffer(Badge("mile high"), Badge("swine"))

      val POLITICAL_VIEWS = Map("wikileaks" -> "is my favorite site", "democrats" -> "have ruined the economy")
      val FAMILY = Map("father" -> new Piggy("father"), "mother" -> new Piggy("mother"))
      val BALANCE = BigDecimal("0.12345678901234")

      val before = new Chair

      val piggy = new Piggy("foo")
      piggy.favorite_foods = FOODS
      piggy.badges = BADGES
      piggy.political_views = POLITICAL_VIEWS
      piggy.family = FAMILY
      piggy.balance = Some(BALANCE)
      before.optional_piggy = Some(piggy)

      val id = Mapper[Chair].upsert(before).id

      Mapper[Chair].findOne(id) must beSome[Chair].which {
        after =>
          after.optional_piggy must beSome[Piggy].which {
            piggy =>
              piggy.giggity == before.optional_piggy.get.giggity
	    piggy.favorite_foods must containAll(FOODS)
	    piggy.political_views must havePairs(POLITICAL_VIEWS.toList : _*)
	    piggy.family.get("father") must beSome[Piggy].which {
	      father => father.giggity must_== "father"
	    }
	    piggy.balance must beSome[BigDecimal].which {
	      b => b must_== BALANCE
	    }
          }
	after.always_here must beSome[String]
	after.never_here must beNone
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
