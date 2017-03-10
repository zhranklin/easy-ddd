package com.zhranklin.ddd.infra

import com.zhranklin.ddd.infra.event.Sender
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

    def generate = Id(util.UUID.randomUUID().toString)
  }

}

/**
 * Created by Zhranklin on 2017/3/10.
 * 领域模型对象的创建上下文
 */
trait DMCreationContext {

  implicit def getId(implicit idGenerator: IdGenerator): Id = idGenerator.generate

  implicit val idGenerator: IdGenerator

  implicit val eventSender: Sender

}
