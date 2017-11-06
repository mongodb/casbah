+++
date = "2015-09-23T15:36:50Z"
title = "Connecting to MongoDB"
[menu.main]
  identifier = "Connecting to MongoDB"
  parent = "Reference"
  weight = 50
+++

# Connecting to MongoDB

The core connection class is
[MongoClient](http://mongodb.github.io/casbah/api/#com.mongodb.casbah.MongoClient).
The casbah `MongoClient` class simply wraps the [MongoClient Java
class](http://api.mongodb.org/java/current/?com/mongodb/MongoClient.html)
and provides a couple of scala helpers as well.

`MongoClient` is available in the global imports class:

~~~scala
import com.mongodb.casbah.Imports._
~~~

## Simple connections

Below are some example connecting to MongoDB with Casbah:

~~~scala
// Connect to default - localhost, 27017
val mongoClient =  MongoClient()

// connect to "mongodb01" host, default port
val mongoClient =  MongoClient("mongodb01")

// connect to "mongodb02" host, port 42017
val mongoClient =  MongoClient("mongodb02", 42017)
~~~

## MongoDB URI

As an alternative to providing host and port information, the [mongodb
URI](http://docs.mongodb.org/manual/reference/connection-string/) format
defines connections between applications and MongoDB. In Casbah the
[com.mongodb.casbah.MongoClientURI](http://mongodb.github.io/casbah/api/#com.mongodb.casbah.MongoClientURI)
class handles string URI's:

~~~scala
val uri = MongoClientURI("mongodb://localhost:27017/")
val mongoClient =  MongoClient(uri)
~~~

{{% note %}}
URI style strings supports all the various connection scenarios, such as connecting to replicasets or using authentication and as such its often considered easier to use.

The following examples show both the long hand way of connecting
purely in code and the URI style.
{{% /note %}}

## Connecting to ReplicaSets / mongos

The java driver automatically determines if it is speaking to a
[replicaset](http://docs.mongodb.org/manual/replication/) or a
[mongos](http://docs.mongodb.org/manual/sharding/) and acts accordingly.

### List of ServerAddress instances

~~~scala
val rs1 = new ServerAddress("localhost", 27017)
val rs2 = new ServerAddress("localhost", 27018)
val rs3 = new ServerAddress("localhost", 27019)
val mongoClient = MongoClient(List(rs1, rs2, rs3))
~~~

{{% note %}}
The [ServerAddress](http://api.mongodb.org/java/current/?com/mongodb/ServerAddress.html) class isn't wrapped by casbah - so you have to call *new* eg: `new ServerAddress()`.
{{% /note %}}

### URI style connections

~~~scala
val uri = MongoClientURI("mongodb://localhost:27017,localhost:27018,localhost:27019/")
val mongoClient = MongoClient(uri)
~~~

## Authentication

MongoDB currently provides two different authentication mechanisms.
Challenge response and GSSAPI authentication (available in the
subscriber edition). A commandline example of using GSSAPI
authentication can be found in the examples.

### MongoDBCredentials

~~~scala
// Automatically detect SCRAM-SHA-1 or Challenge Response protocol
val server = new ServerAddress("localhost", 27017)
val credentials = MongoCredential.createCredential(userName, source, password)
val mongoClient = MongoClient(server, List(credentials))

// SCRAM-SHA-1
val server = new ServerAddress("localhost", 27017)
val credentials = MongoCredential.createScramSha1Credential(userName, source, password)
val mongoClient = MongoClient(server, List(credentials))

// Challenge Response
val server = new ServerAddress("localhost", 27017)
val credentials = MongoCredential.createMongoCRCredential(userName, database, password)
val mongoClient = MongoClient(server, List(credentials))

// X.509 Protocol
val server = new ServerAddress("localhost", 27017)
val credentials = MongoCredential.createMongoX509Credential(userName)
val mongoClient = MongoClient(server, List(credentials))

// SASL PLAIN
val server = new ServerAddress("localhost", 27017)
val credentials = MongoCredential.createPlainCredential(userName, source, password)
val mongoClient = MongoClient(server, List(credentials))

// GSSAPI
val server = new ServerAddress("localhost", 27017)
val credentials = MongoCredential.createGSSAPICredential(userName)
val mongoClient = MongoClient(server, List(credentials))
~~~

{{% note %}}
GSSAPI requires the kerberos to be configured correctly in java. Either via flags when running scala:

~~~bash
-Djava.security.krb5.realm=EXAMPLE.COM
-Djava.security.krb5.kdc=kdc.example.com -Djavax.security.auth.useSubjectCredsOnly=false
~~~

 or in scala:

~~~scala
System.setProperty("java.security.krb5.realm", "EXAMPLE.COM")
System.setProperty("java.security.krb5.kdc", "kdc.example.com")
System.setProperty("javax.security.auth.useSubjectCredsOnly", "false")
~~~

To change Service Name (SPN) with kerberos set the mechanism property
 on the credential eg:

~~~scala
val credential = MongoCredential.createGSSAPICredential(userName)
credential.withMechanismProperty(key, value)
~~~
{{% /note %}}

### URI style connections

~~~scala
// SCRAM-SHA-1
val uri = MongoClientURI("mongodb://username:pwd@localhost/?authMechanism=SCRAM-SHA-1")
val mongoClient =  MongoClient(uri)

// GSSAPI
val uri = MongoClientURI("mongodb://username%40example.com@localhost:27017/?authMechanism=MONGODB-GSSAPI")
val mongoClient =  MongoClient(uri)
~~~

## SSL connections

By default ssl is off for mongodb, but you can [configure mongodb to
enable ssl](http://docs.mongodb.org/manual/tutorial/configure-ssl/).
Subscribers to the enterprise edition of mongodb have ssl support baked
in.

### MongoClientOptions

~~~scala
val options = MongoClientOptions(socketFactory=SSLSocketFactory.getDefault())
val client = MongoClient(serverName, options)
~~~

### URI style connections

~~~scala
val uri = MongoClientURI("mongodb://localhost:27017/?ssl=true")
val mongoClient = MongoClient(uri)
~~~

{{% note %}}
Ensure your keystore is configured correctly to validate ssl certificates
{{% /note %}}


## Connection Options

There are extra configuration options for connections, which cover
setting the default [write
concern](http://docs.mongodb.org/manual/core/write-concern/) and [read
preferences](http://docs.mongodb.org/manual/core/read-preference/) to
configuring socket timeouts.

For the more connection options see the [mongodb connection
reference](http://docs.mongodb.org/manual/reference/connection-string/#connection-string-options).

## Databases and Collections

To query mongodb you need a collection to query against. Collecions are
simple to get from a connection, first get the database the collection
is in, then get the collection:

~~~scala
val mongoClient = MongoClient()
val db = mongoClient("databaseName")
val collection = db("collectionName")
~~~
