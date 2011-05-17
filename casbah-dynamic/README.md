This is a highly, highly experimental module for using Scala 2.9's "Dynamic" trait with Casbah 2.2.0+

Dynamic is *NOT* enabled by default in Scala 2.9 and must be turned on with a special flag.  Because we aren't convinced Dynamic is entirely a good idea and that you will shoot yourself or others in the foot if you use it without understanding, we are providing
this add on module as is, with no warranty.  We also aren't going to tell you how to turn on Dynamic support --- If you can't figure out how to do so you shouldn't be using this module.

That policy is likely to change in the near future, but this module is highly experimental.

It depends on the full Casbah project underneath it.  You will be given a "DynamicDBObject" class and an Object factory which has a builder and the usual constructor stuff.  DynamicDBObject extends MongoDBObject and the OBject Factory is identical in pattern to MongoDBObject.  However, the factory explicitly returns DynamicDBObject instead of DBObject.

DynamicDBObject uses Dynamic to dispatch any methods it can't find to a special resolver.  This means you can do dot notation and all the other fun.  However, due to the nature of Dynamic there is no explicit casting or typing that goes on.  You get, in short, a wrapper value back.  Think of it like an AST.  THis wrapper value is what lets us do recursive walking of the Object hierarchy too.

The `typed[TYPE]` method on returned values will attempt to cast them to a value.  You should probably know what they're supposed to be first.  Later versions will have more intelligent casting of "like" types.

Thanks to @jorgeortiz85 who put the first prototype of this together; my adjustments are only minor to fit into Casbah instead of on top of the Java Driver as his version was.

All mistakes are mine, and no warranty blah, blah , blah.

You can add this to your SBT project the same way you add Casbah, just tack an extra module in.  Remember - you MUST have 2.9:

    val casbahDynamic = "com.mongodb.casbah" %% "casbah-dynamic" % "2.2.0-SNAPSHOT"

Here's an example!

    import com.mongodb.casbah.Imports._
    import com.mongodb.casbah.dynamic._

    val d = DynamicDBObject("_id" -> new ObjectId, "name" -> "Brendan McAdams",
                            "address" -> MongoDBObject("street" -> "134 5th Ave Fl 3",
                                                       "city" -> "New York",
                                                       "state" -> "NY",
                                                        "zip" -> 10011),
                             "email" -> "brendan@10gen.com")


    d.name.typed[String]
    // res4: Option[String] = Some(Brendan McAdams)

    d.name.typed[Int]
    // res5: Option[Int] = None

    d.address.city.typed[String]
    // res6: Option[String] = Some(New York)

    d.address.zip.typed[Int]
    // res7: Option[Int] = Some(10011)

    d.address.zip.typed[String]
    // res8: Option[String] = None

    d.address.typed[DBObject]
    // res9: Option[com.mongodb.casbah.Imports.DBObject] = Some({ "street" : "134 5th Ave Fl 3" , "city" : "New York" , "state" : "NY" , "zip" : 10011})

    d.foo.bar.baz.bang.more.stuff.that.dont.exist.typed[String]
    // res10: Option[String] = None

-b
