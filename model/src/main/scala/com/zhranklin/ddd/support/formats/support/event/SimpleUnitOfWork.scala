package com.zhranklin.ddd.support.formats.support.event

import com.zhranklin.ddd.infra.IdGenerator
import com.zhranklin.ddd.infra.event.EventSource.WithSender
import com.zhranklin.ddd.infra.event.Sender
import com.zhranklin.ddd.infra.persistence.UnitOfWork

/**
 * Created by Zhranklin on 2017/3/9.
 */
trait SimpleUnitOfWork extends UnitOfWork { outer ⇒

  implicit lazy val eventSender: Sender = new Sender(new WithSender {
    lazy val sender = outer.eventSender
  })

  implicit val idGenerator = IdGenerator.UUID
}
