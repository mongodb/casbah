/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 *     http://github.com/mongodb/casbah
 *
 */

package com.mongodb.casbah

import com.mongodb.casbah.Imports._

import scala.collection.JavaConverters._

import com.mongodb.{MongoCredential => JavaMongoCredential}


/**
 * Helper class for creating MongoCredential instances
 *
 * @since 2.6
 * @see com.mongodb.MongoCredential
 */
object MongoCredential {

    /**
     *
     * Creates a MongoCredential instance for the GSSAPI SASL mechanism
     *
     * @param userName the user name
     */
    @deprecated("Please use MongoCredential.createGSSAPICredential", "2.7")
    def apply(userName: String) =
      JavaMongoCredential.createGSSAPICredential(userName)

    /**
     *
     * Creates a MongoCredential instance for the MongoDB Challenge Response protocol
     *
     * @param userName the user name
     * @param database the source of the user name, typically a database name
     * @param password the password
     */
    @deprecated("Please use MongoCredential.createMongoCRCredential", "2.7")
    def apply(userName: String, database: String, password: Array[Char]) =
      JavaMongoCredential.createMongoCRCredential(userName, database, password)

    /**
     *
     * Creates a MongoCredential instance for the GSSAPI SASL mechanism
     *
     * @param userName the user name
     */
    def createGSSAPICredential(userName: String) =
      JavaMongoCredential.createGSSAPICredential(userName)

    /**
     *
     * Creates a MongoCredential instance for the MongoDB Challenge Response protocol
     *
     * @param userName the user name
     * @param database the source of the user name, typically a database name
     * @param password the password
     */
    def createMongoCRCredential(userName: String, database: String, password: Array[Char]) =
      JavaMongoCredential.createMongoCRCredential(userName, database, password)

    /**
     * Creates a MongoCredential instance for the MongoDB X.509 protocol.
     *
     * @param userName the non-null user name
     * @return the credential
     */
    def createMongoX509Credential(userName: String) =
      JavaMongoCredential.createMongoX509Credential(userName)

    /**
     * Creates a MongoCredential instance for the PLAIN SASL mechanism.
     *
     * @param userName the non-null user name
     * @param source the source where the user is defined.  This can be either `"$external"` or the name of a database.
     * @return the credential
     */
    def createPlainCredential(userName: String, source: String, password: Array[Char]) =
      JavaMongoCredential.createPlainCredential(userName, source, password)

}
