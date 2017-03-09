package com.zhranklin.ddd.infra.event

import com.zhranklin.ddd.infra.persistence.Dmo
import com.zhranklin.ddd.model.EntityObject

/**
 * Created by Zhranklin on 2017/2/12.
 * 事件模型 以及最基本的事件类型
 */
trait Event
object Event {
  case class Update(obj: EntityObject) extends Event
  case class Delete(obj: EntityObject) extends Event
}
