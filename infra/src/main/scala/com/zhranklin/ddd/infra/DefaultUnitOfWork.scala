package com.zhranklin.ddd.infra

import com.zhranklin.ddd.infra.IdGenerator.UUID

/**
 * Created by Zhranklin on 2017/2/12.
 * 默认工作单元
 */
trait DefaultUnitOfWork extends IdContext with event.WithRichPipeline {
  //TODO 需要后期扩展加工 可能需要抽象
  implicit val idGenerator: IdGenerator = UUID
  implicit val eventBus: event.EventBus
}
