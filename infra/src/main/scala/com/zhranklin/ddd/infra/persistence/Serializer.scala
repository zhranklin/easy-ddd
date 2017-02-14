package com.zhranklin.ddd.infra.persistence

import com.zhranklin.ddd.infra.event.Sender
import com.zhranklin.ddd.infra.{IdContext, IdGenerator}
import com.zhranklin.ddd.model.{EntityObject, Id}

/**
 * Created by Zhranklin on 2017/2/14.
 */

trait Serializer[D <: Dmo[T], E <: EntityObject, T] {
  def write(obj: E): D
}

trait Deserializer[D <: Dmo[_], E <: EntityObject] {
  def read(dmo: D): E
}

trait Mapper[D <: Dmo[T], T] {
  def read(id: Id): D
  def write(dmo: Dmo[T])
}

trait Repository extends IdContext {

  implicit val idGenerator: IdGenerator

  implicit val eventSender: Sender

}
