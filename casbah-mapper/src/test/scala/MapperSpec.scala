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
package test

import java.util.Date

import net.lag.configgy.Configgy

import org.specs._
import org.specs.specification.PendingUntilFixed

import _root_.scala.collection.Map
import _root_.scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import _root_.scala.collection.JavaConversions._
import _root_.scala.reflect.BeanInfo

import java.math.BigInteger

import Imports._
import mapper.Mapper
import mapper.annotations._
import commons.util.Logging

import org.apache.commons.lang.RandomStringUtils.{randomAscii => rs}
import org.apache.commons.lang.math.RandomUtils.{nextInt => rn}

@BeanInfo @UseTypeHints
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

  @Key @UseTypeHints
  var badges: List[Badge] = Nil

  @Key
  var political_views: Map[String, String] = MMap.empty[String, String]

  @Key
  var family: Map[String, Piggy] = MMap.empty[String, Piggy]

  @Key
  var balance: Option[BigDecimal] = None

  @Key("days_in_year")
  var daysInYear: Option[BigDecimal] = None

  @Key
  var freshness: Freshness.Value = Freshness.Fresh
}

object Freshness extends Enumeration("fresh", "stale") {
  val Fresh, Stale = Value
}

@BeanInfo
class Chair {
  @ID(auto = true)
  var id: ObjectId = _

  @Key @UseTypeHints
  var optional_piggy: Option[Piggy] = None

  @Key var always_here: Option[String] = Some("foo")
  @Key var never_here: Option[String] = None

  @Key
  lazy val timestamp: Date = new Date

  @Key
  lazy val argh: String = { "boo" }

  @Key
  var things: Set[String] = Set.empty[String]
}

trait Badge {
  @Key var name: String = _
}

@BeanInfo
case class WorldOutsideOfManhattan(@ID scrum: String) extends Badge {
  name = "world outside of Manhattan"
}

@BeanInfo
case class OnABoat(@ID water: String) extends Badge {
  name = "I'm on a boat"
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

  "freshness".enum(Freshness)
}

object WOOM_Mapper extends Mapper[WorldOutsideOfManhattan]
object OAB_Mapper extends Mapper[OnABoat]

@BeanInfo
case class One(@Key one: Option[String] = Some("one"))

@BeanInfo
case class Two(@Key two: String = "two") {
  @Key val three: Option[String] = None
}

object OneMapper extends Mapper[One]
object TwoMapper extends Mapper[Two]

@BeanInfo
trait Node {
  val name: String
  val cheat: String
}

@BeanInfo
case class ListNode(@Key name: String, @Key @UseTypeHints children: List[Node] = Nil, @Key cheat: String = "list") extends Node

@BeanInfo
case class MapNode(@Key name: String, @Key @UseTypeHints children: Map[String, Node] = Map.empty[String, Node], @Key cheat: String = "map") extends Node

object ListNodeMapper extends Mapper[ListNode]
object MapNodeMapper extends Mapper[MapNode]

object NodeMapper {
  def asDBObject(n: Node): DBObject =
    n match {
      case l: ListNode => ListNodeMapper.asDBObject(l)
      case m: MapNode => MapNodeMapper.asDBObject(m)
    }

  def asObject(o: DBObject): Node =
    o.get("cheat") match {
      case "list" => ListNodeMapper.asObject(o)
      case "map" => MapNodeMapper.asObject(o)
    }
}

object NodeCounter {
  var n: Int = _
}

class MapperSpec extends Specification with PendingUntilFixed with Logging {
  private implicit def pimpTimes(n: Int) = new {
    def times[T](f: => T) = (1 to n).toList.map(_ => f.apply)
  }

  detailedDiffs()

  doBeforeSpec {
    Configgy.configure("casbah-commons/src/test/resources/casbah.config")
    com.novus.casbah.conversions.scala.RegisterConversionHelpers()
    ChairMapper
    WidgetMapper
    PiggyMapper
    WOOM_Mapper
    OAB_Mapper
    OneMapper
    TwoMapper
    ListNodeMapper
    MapNodeMapper
    NodeCounter.n = 0
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

    "preserve collection-level type hints" in {
      Mapper[Widget].coll.findOne(Map("_id" -> widget.name).asDBObject) must beSome[DBObject].which {
	dbo =>
	  dbo("_typeHint") must_== classOf[Widget].getName
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
      val BADGES: List[Badge] = List(WorldOutsideOfManhattan("mile high"), OnABoat("swine"))

      val POLITICAL_VIEWS = Map("wikileaks" -> "is my favorite site", "democrats" -> "have ruined the economy")
      val FAMILY = Map("father" -> new Piggy("father"), "mother" -> new Piggy("mother"))
      val BALANCE = BigDecimal("0.12345678901234")
      val DAYS_IN_YEAR = BigDecimal("365")

      val before = new Chair

      val piggy = new Piggy("foo")
      piggy.favorite_foods = FOODS
      piggy.badges = BADGES
      piggy.political_views = POLITICAL_VIEWS
      piggy.family = FAMILY
      piggy.balance = Some(BALANCE)
      piggy.daysInYear = Some(DAYS_IN_YEAR)
      piggy.freshness = Freshness.Stale
      before.optional_piggy = Some(piggy)
      before.things = Set("foo", "bar", "baz", "quux", "foo", "baz")

      val id = Mapper[Chair].upsert(before).id

      val dbo = Mapper[Chair].coll.findOne(id).get
      dbo("argh") = "ya"
      Some(ChairMapper.asObject(dbo)) must beSome[Chair].which {
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
            piggy.daysInYear must beSome[BigDecimal].which {
              b => b must_== DAYS_IN_YEAR
            }
	    piggy.freshness must_== Freshness.Stale
          }
        after.always_here must beSome[String]
        after.never_here must beNone
	after.things.size must_== before.things.size
	after.things must containAll(before.things)
	//after.argh must_== "ya"
      }
    }

    "preserve default values" in {
      val one = One()
      val _one = OneMapper.asObject(OneMapper.asDBObject(one))
      _one must_== one
      _one.one must beSome[String].which { _ == "one" }

      val two = Two()
      val _two = TwoMapper.asObject(TwoMapper.asDBObject(two))
      _two must_== two
      _two.two must_== "two"
      _two.three must beNone
    }
  }

  "not take too much time doing stuff" in {
    var tree: Option[Node] = None
    val generateTree = measure { tree = node(2) }
    log.info("generated %d nodes in %d ms", NodeCounter.n, generateTree)
    tree must beSome[Node]

    val outTimes = 10.times {
      val readTree = measure { Some(NodeMapper.asDBObject(tree.get)) }
      log.info("serialized tree in %d ms", readTree)
      readTree
    }

    log.info("serialization times: min( %d ms ) <<< avg( %d ms ) <<< max( %d ms )",
             outTimes.min, outTimes.sum / outTimes.size, outTimes.max)

    val dbo = NodeMapper.asDBObject(tree.get)
    val inTimes = 10.times {
      val writeTree = measure { NodeMapper.asObject(dbo) }
      log.info("de-serialized tree in %d ms ", writeTree)
      writeTree
    }

    log.info("de-serialization times: min( %d ms ) <<< avg( %d ms ) <<< max( %d ms )",
             inTimes.min, inTimes.sum / inTimes.size, inTimes.max)

    val tree2 = NodeMapper.asObject(dbo)
    tree2.name must_== tree.get.name
  }

  private def measure(f: => Unit): Long = {
    val start = System.currentTimeMillis
    f.apply
    val stop = System.currentTimeMillis
    stop - start
  }

  private def node(level: Int): Option[Node] =
    if (level == 0) None
    else {
      NodeCounter.n += 1
      if (rn(10) % 2 == 0) {
	Some(ListNode(crap, (1 to many).toList.map(_ => node(level - 1)).filter(_.isDefined).map(_.get)))
      } else {
	Some(MapNode(crap, Map.empty[String, Node] ++ (1 to many).toList.map {
	  _ =>
	    node(level - 1) match {
	      case Some(n) => Some(n.name -> n)
	      case _ => None
	    }
	}.filter(_.isDefined).map(_.get)))
      }
    }

  private val many = 3

  private def _many: Int =
    rn(10) match {
      case n if n < 3 => many
      case n => n
    }

  private def crap: String = rs(many)
}
