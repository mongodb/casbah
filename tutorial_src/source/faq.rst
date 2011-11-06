At the moment, Rogue only works against [Lift's MongoDB-Record][1] system.

Part of the reason it offers full type safety is that it uses a strong, well defined object structure for Lift-Record.  You have a lot less ability to do 'free form' queries as you have a full structure in place for it.  Here's what I mean by way of a demo of Lift-Record + Rogue I did for a recent Scala Webinar.  Some stuff has changed in Lift & Rogue since I did this so code might be slightly out of date but is still representative.  This is the Lift-Record model for MongoDB:

   object LiftRecordDemo extends Application {
      // We'll use enums for Type and Subtype
      object EventType extends Enumeration {
        type EventType = Value
        val Conference, Webinar = Value
      }

      object EventSubType extends Enumeration {
        type EventSubType = Value
        val FullDay = Value("Full Day")
        val HalfDay = Value("Half Day")
      }

      

      class MongoEvent extends MongoRecord[MongoEvent] with MongoId[MongoEvent] {
        def meta = MongoEvent

        object name extends StringField(this, 255)
        object eventType extends EnumField(this, EventType) 
        object eventSubType extends OptionalEnumField(this, EventSubType)
        object location extends JsonObjectField[MongoEvent, EventLocation](this, EventLocation)  {
          def defaultValue = EventLocation(None, None, None, None, None, None, None)
        }

        object hashtag extends OptionalStringField(this, 32)
        object language extends OptionalStringField(this, 32)
        object date extends JsonObjectField[MongoEvent, EventDate](this, EventDate) {
          def defaultValue = EventDate(new DateTime, None)
        }

        object url extends OptionalStringField(this, 255)
        object presenter extends OptionalStringField(this, 255)

      }

      object MongoEvent extends MongoEvent with MongoMetaRecord[MongoEvent] {
        override def collectionName = "mongoEvents"
        override def formats = super.formats + new EnumSerializer(EventType) + new EnumSerializer(EventSubType)
      }


      case class EventLocation(val venueName: Option[String], val url: Option[String], val address: Option[String], val city: Option[String], val state: Option[String], val zip: Option[String], val country: Option[String]) extends JsonObject[EventLocation] {
        def meta = EventLocation
      }

      object EventLocation extends JsonObjectMeta[EventLocation]

      case class EventDate(start: DateTime, end: Option[DateTime]) extends JsonObject[EventDate] {
        def meta = EventDate
      }

      object EventDate extends JsonObjectMeta[EventDate]
    }

As you can see, you are required to define your MongoDB Data model ahead of time in order to gain the benefit of strongly typed, safe queries... Rogue enforces most of it at compile time.   Here are some Rogue examples against this model:


      // Tell Lift about our DB
      val mongoAddr = MongoAddress(MongoHost("127.0.0.1", 27017), "scalaWebinar")

      MongoDB.defineDb(DefaultMongoIdentifier, mongoAddr)

      // Rogue gives us a saner approach, although still hobbled by some
      // of Lift-MongoDB-Record's limits on embedded docs

      val q = MongoEvent where (_.eventType eqs EventType.Webinar)

      println("Rogue created a Query '%s'\n\n".format(q))

      for (x <- MongoEvent where (_.eventType eqs EventType.Webinar)) {
        println("Name: %s Presenter: %s\n".format(x.name, x.presenter))
      }

      // Rogue can also do sorting for you, which is useful

      println("\n\n\n")

      for (x <- MongoEvent where (_.eventType eqs EventType.Conference) 
                           orderAsc(_.language) andDesc(_.name)) { 
        println("Name: %s Language: %s\n".format(x.name, x.language))
      }
      val start = new DateTime(2011, 2, 1, 0, 0, 0, 0)
      val end = new DateTime(2011, 3, 1, 0, 0, 0, 0)

        /** The following would be nice but unfortunately, 
          doesn't work because of lift's current embedded doc
          implementation
        */
      //val dateQ = MongoEvent where (_.date.start after start) 
                               //and (_.date.end before end)

Notice I am not saying Rogue and Lift-Record aren't awesome, just that they work off of a strongly defined compile time data model.

If instead you'd like to use a similar context with Casbah, we do have a builtin DSL which is designed to mimic MongoDB's inbuilt Query model as closely as possible.  It works against any arbitrary underlying model, but does a LOT to enforce levels of type safety where possible.  Here is a (slightly dated as its from the same presentation) example of Casbah's Querying:


      // What about querying?  Lets find all the non-US events

      for (x <- mongo.find(MongoDBObject("location.country" -> 
                            MongoDBObject("$ne" -> "USA")))) println(x)

      /* There's a problem here: We got back the Webinars too because 
         They don't have a country at all, so they aren't "USA"
        */
      println("\n\nTesting for existence of Location.Country:")

      for (x <- mongo.find(MongoDBObject("location.country" -> MongoDBObject(
                           "$ne" -> "USA",
                           "$exists" -> true 
                          )))) println(x)
      
      // This is getting a bit unwieldy.  Thankfully, Casbah offers a DSL
      val q = $or ("location.country" -> "USA", "location.country" -> "Japan")

      println("\n Created a DBObject: %s".format(q))
      
      println("\n Querying using DSL Object...")

      for (x <- mongo.find(q)) println(x)

      // It's possible to construct more complex queries too.

      // Lets find everything in February

      println("\n February Events...")
      val start = new DateTime(2011, 2, 1, 0, 0, 0, 0)
      val end = new DateTime(2011, 3, 1, 0, 0, 0, 0)
      val dateQ = "date.start" $gte start $lt end 

      println("\n Date Query: %s".format(dateQ))

      for (x <- mongo.find(dateQ, MongoDBObject("name" -> true, "date" -> true))) println(x)

Notably we are querying against a free form model but using DSL operators instead of nested MongoDB definitions.  There are lots of fantastic mappings of operators, right down to the $type operator to test a type using class manifests for compiletime safety:


    "Casbah's $type operator" should {
      "Accept raw Byte indicators (e.g. from org.bson.BSON)" in {
        // Don't need to test every value here since it's just a byte
        val typeOper = "foo" $type org.bson.BSON.NUMBER_LONG
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NUMBER_LONG))
      }

      "Accept manifested Type arguments" in {
        "Doubles" in {
          val typeOper = "foo".$type[Double]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NUMBER))
        }
        "Strings" in {
          val typeOper = "foo".$type[String]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.STRING))
        }
        "Object" in {
          "via BSONObject" in {
            val typeOper = "foo".$type[org.bson.BSONObject]
            typeOper must notBeNull
            typeOper.toString must notBeNull
            typeOper must haveSuperClass[DBObject]
            typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.OBJECT))
          }
          "via DBObject" in {
            val typeOper = "foo".$type[DBObject]
            typeOper must notBeNull
            typeOper.toString must notBeNull
            typeOper must haveSuperClass[DBObject]
            typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.OBJECT))
          }
        }
        "Array" in {
          "via BasicDBList" in {
            val typeOper = "foo".$type[BasicDBList]
            typeOper must notBeNull
            typeOper.toString must notBeNull
            typeOper must haveSuperClass[DBObject]
            typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.ARRAY))
          }
          "via BasicBSONList" in {
            val typeOper = "foo".$type[org.bson.types.BasicBSONList]
            typeOper must notBeNull
            typeOper.toString must notBeNull
            typeOper must haveSuperClass[DBObject]
            typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.ARRAY))
          }
        }
        "OID" in {
          val typeOper = "foo".$type[ObjectId]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.OID))
        }
        "Boolean" in {
          val typeOper = "foo".$type[Boolean]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.BOOLEAN))
        }
        "Date" in {
          "via JDKDate" in {
            val typeOper = "foo".$type[java.util.Date]
            typeOper must notBeNull
            typeOper.toString must notBeNull
            typeOper must haveSuperClass[DBObject]
            typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.DATE))
          }
          "via Joda DateTime" in {
            val typeOper = "foo".$type[org.joda.time.DateTime]
            typeOper must notBeNull
            typeOper.toString must notBeNull
            typeOper must haveSuperClass[DBObject]
            typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.DATE))
          }
        }
        "None (null)" in {
          // For some reason you can't use NONE 
          val typeOper = "foo".$type[Option[Nothing]]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NULL))
        }
        "Regex" in {
          "Scala Regex" in {
            val typeOper = "foo".$type[scala.util.matching.Regex]
            typeOper must notBeNull
            typeOper.toString must notBeNull
            typeOper must haveSuperClass[DBObject]
            typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.REGEX))
          }
        }
        "Symbol" in {
          val typeOper = "foo".$type[Symbol]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.SYMBOL))
        }
        "Number (integer)" in {
          val typeOper = "foo".$type[Int]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NUMBER_INT))
        }
        "Number (Long)" in {
          val typeOper = "foo".$type[Long]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NUMBER_LONG))
        }
        "Timestamp" in {
          val typeOper = "foo".$type[java.sql.Timestamp]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.TIMESTAMP))
        }
        "Binary" in {
          val typeOper = "foo".$type[Array[Byte]]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.BINARY))
        }

      }

(Note: You need to either import the casbah-query package and its related imports to your code or use the default 'casbah' import from pre-modulisation). The Specs for Casbah currently have full coverage for every DSL operator; the docs lag behind at the moment but the Specs serve as a great introduction to their usage.  Note there are *two* kinds of operators in Casbah, just like in MongoDB. 

> **Bareword Operators**, in which the leftmost part of the statement is the *$ Operator*. Examples of this are `$set`, `$rename`, etc.  See [the Specs for Bareword Operators][2] for more.

> **"Core" Operators**, are operators that exist on the right hand side of the statement, such as `$type`.  See [the Specs for Core Operators][3] for more.


I know this is a rather detailed answer but I wanted to make sure that you understand your options, as well as the limitations of both solutions.  Casbah will give you a DSL that maps closely to MongoDB and removes some of the syntactic cruft (Keep in mind also Casbah provides a `getAs[T]` method on `DBObject` to ask for a value from the `DBObject` as a *specific* type, which people often overlook); many users are unaware the DSL exists before they go seeking something to do what is built in. *However*, Casbah's Query DSL also looks a bit "crufty" in some peoples opinions... as the author of it I prefer it's simplicity and elegance as I need only remember the **MONGODB* syntax for querying, and not both MongoDB and another DSL.  It is also based on free form querying and doesn't provide the same structured, compile time type safe & aware facilities that Rogue does.

By contrast though, Rogue also requires a fully defined Record model against it, which doesn't fit into every application.

I'd love to hear however where things can be improved on either product if there's a "middle ground" for your needs which either product doesn't properly meet.


  [1]: http://www.assembla.com/spaces/liftweb/wiki/MongoDB
  [2]: https://github.com/mongodb/casbah/blob/master/casbah-query/src/test/scala/BarewordOperatorsSpec.scala
  [3]: https://github.com/mongodb/casbah/blob/master/casbah-query/src/test/scala/DSLCoreOperatorsSpec.scala
