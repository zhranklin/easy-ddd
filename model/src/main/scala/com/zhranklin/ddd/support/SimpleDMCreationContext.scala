package com.zhranklin.ddd.support

import com.zhranklin.ddd.infra.event.EventSource.WithSender
import com.zhranklin.ddd.infra.event.Sender
import com.zhranklin.ddd.infra.{IdGenerator, DMCreationContext}

/**
 * Created by Zhranklin on 2017/3/9.
 */
trait SimpleDMCreationContext extends DMCreationContext { outer ⇒

  implicit lazy val eventSender: Sender = new Sender(new WithSender {
    lazy val sender = outer.eventSender
  })

  implicit val idGenerator = IdGenerator.UUID
}
