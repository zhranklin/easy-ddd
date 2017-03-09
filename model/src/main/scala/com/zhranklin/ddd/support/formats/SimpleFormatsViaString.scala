package com.zhranklin.ddd.support.formats

import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.Id

/**
 * Created by Zhranklin on 2017/3/9.
 */
trait SimpleFormatsViaString extends Repository {
  implicit val mar = new Format[String, String] {
    def unmarshal(a: String) = a
    def marshal(a: String) = a
  }

  implicit def mEo = new Marshaller[com.zhranklin.ddd.model.EntityObject, String] {
    def marshal(a: com.zhranklin.ddd.model.EntityObject) = a.id.id
  }

  implicit def mInt = new Marshaller[Int, String] {
    def marshal(a: Int) = a.toString
  }

  implicit def mIter[T](implicit m: Marshaller[T, String]) = new Marshaller[Iterable[T], String] {
    def marshal(a: Iterable[T]) = a.map(e ⇒ m.marshal(e)).mkString(",")
  }

  implicit def mT2[S1, S2](implicit m1: Marshaller[S1, String], m2: Marshaller[S2, String]) = new Marshaller[(S1, S2), String] {
    def marshal(a: (S1, S2)) = s"(${m1.marshal(a._1)},${m2.marshal(a._2)})"
  }

  implicit def mOpt[T](implicit m: Marshaller[T, String]) = new Marshaller[Option[T], String] {
    def marshal(a: Option[T]) = a.map(m.marshal).getOrElse("")
  }

  implicit def uEo[E <: com.zhranklin.ddd.model.EntityObject](implicit f: Dmo[String] ⇒ E, mapper: Mapper[String]) = new Unmarshaller[E, String] {
    def unmarshal(b: String) = read[E][String](Id(b))
  }
}
