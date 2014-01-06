/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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
package com.mongodb.casbah
package commons


/**
 * Package private Castable trait - a helper that provides a typesafe castToOption method
 */
private[commons] trait Castable {

  // scalastyle:off cyclomatic.complexity
  /**
   * A utility method that allows you to cast a value to an Option[A]
   *
   * Some(value.asInstanceOf[T]) would only cause a runtime ClassCastException when calling get
   *
   * This utility ensures that the types match correctly (even boxed scala types eg Int) so we can
   * check against the type and return Some(value) if the types match.
   *
   * @param value the value to type check against
   * @tparam A the type to cast to check against
   * @return Option[A] - Some(value) if the type of instances match else None
   */
  def castToOption[A: Manifest](value: Any): Option[A] = {
    val erasure = manifest[A] match {
      case Manifest.Byte => classOf[java.lang.Byte]
      case Manifest.Short => classOf[java.lang.Short]
      case Manifest.Char => classOf[java.lang.Character]
      case Manifest.Long => classOf[java.lang.Long]
      case Manifest.Float => classOf[java.lang.Float]
      case Manifest.Double => classOf[java.lang.Double]
      case Manifest.Boolean => classOf[java.lang.Boolean]
      case Manifest.Int => classOf[java.lang.Integer]
      case m => m.erasure
    }
    if (erasure.isInstance(value)) Some(value.asInstanceOf[A]) else None
  }
}
