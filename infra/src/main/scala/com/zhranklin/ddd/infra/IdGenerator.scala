package com.zhranklin.ddd.infra

import com.zhranklin.ddd.model.Id

abstract class IdGenerator {
  def generate: Id
}

object IdGenerator {
  class Static(id: Id) extends IdGenerator {
    def generate = id
  }

  object UUID extends IdGenerator {
    import java.util

    import scala.math.random
    def generate = Id(new util.UUID(random.toLong, random.toLong).toString)
  }

}
