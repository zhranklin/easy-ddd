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
case class Dmo[T](id: Id, table: String, attributes: Map[String, T])

trait WithRepoOf[T]

trait Mapper[T] {

  def getTableName(clazz: Class[_]) = clazz.getSimpleName

  def read(id: Id, clazz: Class[_]): Dmo[T]

  def write(dmo: Dmo[T])
}

trait repository[K] {
  val write: entityObject => Unit
  implicit def read[E <: entityObject](id: String)(implicit mapper: Mapper[K], f: Dmo[K] ⇒ E, classTag: ClassTag[E]): E = mapper.read(Id(id), classTag.runtimeClass)
}

object FormatsTester {
  def test[T](implicit
              m1: Marshaller[Byte, T],
              m2: Marshaller[Short, T],
              m3: Marshaller[Char, T],
              m4: Marshaller[Int, T],
              m5: Marshaller[Long, T],
              m6: Marshaller[Float, T],
              m7: Marshaller[Double, T],
              m8: Marshaller[String, T],
              m9: Marshaller[List[String], T],
              m10: Marshaller[Option[String], T],
              m11: Marshaller[(String, String), T],
              m13: Marshaller[Map[String, String], T],
              m14: Marshaller[Map[Int, String], T],
              mm: Marshaller[List[Option[Map[Double, (Int, (Int, List[Float]))]]], T],
              u1: Unmarshaller[Byte, T],
              u2: Unmarshaller[Short, T],
              u3: Unmarshaller[Char, T],
              u4: Unmarshaller[Int, T],
              u5: Unmarshaller[Long, T],
              u6: Unmarshaller[Float, T],
              u7: Unmarshaller[Double, T],
              u8: Unmarshaller[String, T],
              u9: Unmarshaller[List[String], T],
              u10: Unmarshaller[Option[String], T],
              u11: Unmarshaller[(String, String), T],
              u13: Unmarshaller[Map[String, String], T],
              u14: Unmarshaller[Map[Int, String], T],
              uu: Unmarshaller[List[Option[Map[Double, (Int, (Int, List[Float]))]]], T]
             ) = {}
}