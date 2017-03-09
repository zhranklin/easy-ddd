package com.zhranklin.ddd.infra.persistence

import com.zhranklin.ddd.infra.event.Sender
import com.zhranklin.ddd.infra.{IdContext, IdGenerator}
import com.zhranklin.ddd.model.{EntityObject, Id}

/**
 * Created by Zhranklin on 2017/2/14.
 */

trait Mapper[T] {
  def read(id: Id): Dmo[T]
  def write(dmo: Dmo[T])
}

trait UnitOfWork extends IdContext {

  implicit val idGenerator: IdGenerator

  implicit val eventSender: Sender

}

trait RepositoryOld {
  implicit def readGen[T, E <: EntityObject](id: Id)(implicit mapper: Mapper[T], f: Dmo[T] ⇒ E): E = mapper.read(id)
  implicit def writeGen[T, E <: EntityObject](e: E)(implicit mapper: Mapper[T], f: E ⇒ Dmo[T]): Unit = mapper.write(e)
}

trait Repository {
  val write: EntityObject => Unit
  implicit def read[E <: EntityObject] = new {
    implicit def apply[K](id: Id)(implicit mapper: Mapper[K], f: Dmo[K] ⇒ E): E = mapper.read(id)
  }
}