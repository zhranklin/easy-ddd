package com.zhranklin.ddd.infra.event

import com.zhranklin.ddd.infra.event.EventBus._

/**
 * Created by Zhranklin on 2017/2/12.
 * 事件总线 高度简化与抽象, 以增加扩展性
 */
abstract class EventBus {
  protected def pipeline: Pipeline = e ⇒ Some(e)
  protected def handle(event: Event) = pipeline(event)
  def addSource(eventSource: EventSource) = eventSource.register(this)
}

object EventBus {
  type Pipeline = Event ⇒ Option[Event]
  type Consumer = Event ⇒ Any

  trait EventRegister {
    protected def eventBus = _eventBus
    protected def submit(event: Event) = eventBus.handle(event)

    private var _eventBus: EventBus = _
    private[event] def register(eventBus: EventBus) =
      if (_eventBus != null)
        throw new IllegalStateException("already registered.")
      else _eventBus = eventBus
  }

}



trait WithRichPipeline {
  implicit class RichPipeline(pipeline: Pipeline) {
    def ~>(p: Pipeline): Pipeline = ev ⇒ pipeline(ev).flatMap(p)
    def ~|(c: Consumer): Pipeline = ev ⇒ {pipeline(ev).foreach(c); None}
  }
}
