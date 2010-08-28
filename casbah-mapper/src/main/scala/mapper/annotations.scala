package com.novus.casbah.mapper

import scala.reflect.BeanInfo
import scala.annotation.target.{getter, setter}

package object annotations {
  type ID           = raw.ID           @getter
  type Key          = raw.Key          @getter
  type UseTypeHints = raw.UseTypeHints @getter
  type KeyStrategy  = raw.KeyStrategy  @getter
}
