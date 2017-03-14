package com.zhranklin.ddd.infra.persistence

import com.zhranklin.ddd.model.{Id, entityObject}

import scala.reflect.ClassTag

/**
 * Created by Zhranklin on 2017/2/13.
 * 基本属性/值对象的(反)序列化
 */

trait Marshaller[-A, +B] {
  def marshal(a: A): B
}

//todo 由于scala编译器的bug, 只能暂时使用A, 而不是+A
trait Unmarshaller[A, -B] {
  def unmarshal(b: B): A
}

trait Format[A, B] extends Marshaller[A, B] with Unmarshaller[A, B]

/**
 * Created by Zhranklin on 2017/2/12.
 * 持久化的中间对象, 用于保存实体
 */
case class Dmo[T](id: Id[_], table: String, attributes: Map[String, T])

trait WithRepos[T]

trait Mapper[T] {
  def read(id: Id[_], clazz: Class[_]): Dmo[T]

  def write(dmo: Dmo[T])
}

trait Repository[K] {
  val write: entityObject => Unit
  implicit def read[E <: entityObject](id: String)(implicit mapper: Mapper[K], f: Dmo[K] ⇒ E, classTag: ClassTag[E]): E = mapper.read(Id[E](id), classTag.runtimeClass)
}
