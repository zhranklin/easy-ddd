package com.zhranklin.ddd.support.formats

import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.{Id, entityObject}

import scala.reflect.ClassTag

/**
 * Created by Zhranklin on 2017/3/9.
 */
trait SimpleFormatsViaString extends Repository[String] {
  implicit val mar = new Format[String, String] {
    def unmarshal(a: String) = a
    def marshal(a: String) = a
  }

  implicit def mEo = new Marshaller[entityObject, String] {
    def marshal(a: entityObject) = a.id.id
  }

  implicit def mInt = new Marshaller[Int, String] {
    def marshal(a: Int) = a.toString
  }

  implicit def mIter[T](implicit m: Marshaller[T, String]) = new Marshaller[Iterable[T], String] {
    def marshal(a: Iterable[T]) = a.map(e ⇒ m.marshal(e)).mkString("[", ",", "]")
  }

  implicit def mT2[S1, S2](implicit m1: Marshaller[S1, String], m2: Marshaller[S2, String]) = new Marshaller[(S1, S2), String] {
    def marshal(a: (S1, S2)) = s"(${m1.marshal(a._1)},${m2.marshal(a._2)})"
  }

  implicit def mOpt[T](implicit m: Marshaller[T, String]) = new Marshaller[Option[T], String] {
    def marshal(a: Option[T]) = a.map(m.marshal).getOrElse("")
  }

  implicit def uEo[E <: entityObject](implicit f: Dmo[String] ⇒ E, mapper: Mapper[String], classTag: ClassTag[E]) = new Unmarshaller[E, String] {
    def unmarshal(b: String) = read[E](b)
  }

  implicit def uInt = new Unmarshaller[Int, String] {
    def unmarshal(b: String) = Integer.parseInt(b)
  }

  implicit def uList[T](implicit u: Unmarshaller[T, String]) = new Unmarshaller[List[T], String] {
    def unmarshal(b: String) = extractList(b).map(u.unmarshal)
  }

  def extractList(lstStr: String): List[String] = {
    println(lstStr)
    val tuple = ((List[String](""), 0) /: lstStr.drop(1).dropRight(1)) {
      case ((lst, 0), ',') ⇒ ("" :: lst, 0)
      case ((head :: lst, i), c) ⇒
        val newI = if ("([" contains c) i + 1 else if (")]" contains c) i - 1 else i
        ((head + c) :: lst, newI)
    }
    if (tuple._1 == List(""))
      Nil
    else tuple._1.reverse
  }

  implicit def uMap[K, V](implicit u: Unmarshaller[(K, V), String]) = new Unmarshaller[Map[K, V], String] {
    def unmarshal(b: String) = extractList(b).map(u.unmarshal).toMap
    //.split("\\,").map(u.unmarshal).toMap
  }

  implicit def uT2[S1, S2](implicit u1: Unmarshaller[S1, String], u2: Unmarshaller[S2, String]) = new Unmarshaller[(S1, S2), String] {
    def unmarshal(b: String) = extractList(b) match {
      case List(s1, s2) ⇒ (u1.unmarshal(s1), u2.unmarshal(s2))
    }
  }

  implicit def uOpt[T](implicit u: Unmarshaller[T, String]) = new Unmarshaller[Option[T], String] {
    def unmarshal(b: String) = if (b == "") None else Some(u.unmarshal(b))
  }

}
