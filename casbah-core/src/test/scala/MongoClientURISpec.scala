/**
 * Copyright 2015 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/mongodb/casbah
 *
 */

package com.mongodb.casbah.test.core

import com.mongodb.casbah.Imports._

import org.specs2.mutable.Specification


class MongoClientURISpec extends Specification {

  "MongoClientURI" should {
    "define equality" in {
      MongoClientURI("mongodb://localhost") should beEqualTo(MongoClientURI(new com.mongodb.MongoClientURI("mongodb://localhost")))
      MongoClientURI("mongodb://localhost") must not equalTo (MongoClientURI(new com.mongodb.MongoClientURI("mongodb://127.0.0.1")))
    }
    "define hashcode" in {
      MongoClientURI("mongodb://localhost").hashCode() should beEqualTo(MongoClientURI(new com.mongodb.MongoClientURI("mongodb://localhost")).hashCode())
      MongoClientURI("mongodb://localhost").hashCode() should not equalTo (MongoClientURI(new com.mongodb.MongoClientURI("mongodb://127.0.0.1")).hashCode())
    }
  }
}

