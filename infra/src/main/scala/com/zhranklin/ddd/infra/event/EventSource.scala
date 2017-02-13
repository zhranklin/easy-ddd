package com.zhranklin.ddd.infra.event

import com.zhranklin.ddd.infra.event.EventBus.EventRegister

/**
 * Created by Zhranklin on 2017/2/13.
 * 事件源的抽象
 */
trait EventSource extends EventRegister

object EventSource {
  trait WithSender extends EventSource {
    val sender: Sender
    private[event] def submitForSender(event: Event) = submit(event)
  }
}

class Sender(source: EventSource.WithSender) {
  def send(event: Event) = source.submitForSender(event)
}
