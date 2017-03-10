package com.zhranklin.ddd.infra.persistence

import com.zhranklin.ddd.model.{Id, entityObject}

/**
 * Created by Zhranklin on 2017/2/13.
 * 基本属性/值对象的(反)序列化
 */

trait Marshaller[-A, +B] {
  def marshal(a: A): B
}

trait Unmarshaller[+A, -B] {
  def unmarshal(b: B): A
}

trait Format[A, B] extends Marshaller[A, B] with Unmarshaller[A, B]

/**
 * Created by Zhranklin on 2017/2/12.
 * 持久化的中间对象, 用于保存实体
 */
case class Dmo[T](id: Id, table: String, attributes: Map[String, T])

trait WithRepos[T]

trait Mapper[T] {
  def read(id: Id): Dmo[T]

  def write(dmo: Dmo[T])
}


trait Repository {
  val write: entityObject => Unit

  implicit def read[E <: entityObject] = new {
    implicit def apply[K](id: Id)(implicit mapper: Mapper[K], f: Dmo[K] ⇒ E): E = mapper.read(id)
  }
}
