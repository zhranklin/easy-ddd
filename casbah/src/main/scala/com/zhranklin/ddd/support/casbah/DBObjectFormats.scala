package com.zhranklin.ddd.support.casbah

import com.mongodb.{BasicDBList, DBObject}
import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.entityObject
import com.mongodb.casbah.Imports.{MongoDBList ⇒ $$, MongoDBObject ⇒ $, _}

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

/**
 * Created by Zhranklin on 2017/3/17.
 */
trait DBObjectFormats extends repository[DBAttr] {

  trait Not[T]
  implicit def $passNot[T]: Not[T] = null
  implicit def $ambiguousNot[T](implicit ev: T): Not[T] = null

  implicit def mEo = new Marshaller[entityObject, DBAttr] {
    def marshal(a: entityObject) = AString(a.id.id)
  }

  implicit def mVal = new Marshaller[AnyVal, DBAttr] {
    def marshal(a: AnyVal) = AVal(a)
  }

  implicit def mStr = new Marshaller[String, DBAttr] {
    def marshal(a: String) = AString(a)
  }

  implicit def mIter[T](implicit m: Marshaller[T, DBAttr], ev: Not[T <:< (String, _)]) = new Marshaller[Iterable[T], DBAttr] {
    def marshal(a: Iterable[T]) = AList($$(a.map(e ⇒ m.marshal(e).v).toSeq: _*).underlying)
  }

  implicit def mObj[T](implicit m: Marshaller[T, DBAttr]) = new Marshaller[Iterable[(String, T)], DBAttr] {
    def marshal(a: Iterable[(String, T)]) = AObject($(a.toMap.mapValues(value ⇒ m.marshal(value).v).toList))
  }

  implicit def mT2[S1, S2](implicit m1: Marshaller[S1, DBAttr], m2: Marshaller[S2, DBAttr]) = new Marshaller[(S1, S2), DBAttr] {
    def marshal(a: (S1, S2)) = AList($$(m1.marshal(a._1).v, m2.marshal(a._2).v).underlying)
  }

  implicit def mOpt[T](implicit m: Marshaller[T, DBAttr]) = new Marshaller[Option[T], DBAttr] {
    def marshal(a: Option[T]) = a.map(m.marshal).getOrElse(ANull)
  }

  implicit def uStr = new Unmarshaller[String, DBAttr] {
    def unmarshal(b: DBAttr) = b.v.asInstanceOf[String]
  }

  implicit def uEo[E <: entityObject](implicit f: Dmo[DBAttr] ⇒ E, mapper: Mapper[DBAttr], classTag: ClassTag[E]) = new Unmarshaller[E, DBAttr] {
    def unmarshal(b: DBAttr) = read[E](b.v.asInstanceOf[String])
  }

  implicit def uVal[V <: AnyVal] = new Unmarshaller[V, DBAttr] {
    def unmarshal(b: DBAttr) = b.v.asInstanceOf[V]
  }

  implicit def uList[T](implicit u: Unmarshaller[T, DBAttr]) = new Unmarshaller[List[T], DBAttr] {
    def unmarshal(b: DBAttr) = new $$(b.v.asInstanceOf[BasicDBList]).map(e ⇒ u.unmarshal(new DBAttr(e))).toList
  }

  implicit def uObj[T](implicit u: Unmarshaller[T, DBAttr]) = new Unmarshaller[Map[String, T], DBAttr] {
    def unmarshal(b: DBAttr) = b.v.asInstanceOf[DBObject].toMap
      .asScala.toMap.map(kv ⇒ (kv._1.asInstanceOf[String], u.unmarshal(new DBAttr(kv._2))))
  }

  implicit def uT2[S1, S2](implicit u1: Unmarshaller[S1, DBAttr], u2: Unmarshaller[S2, DBAttr]) = new Unmarshaller[(S1, S2), DBAttr] {
    def unmarshal(b: DBAttr) = new $$(b.v.asInstanceOf[BasicDBList]) match {
      case e1 +: e2 +: rest ⇒ (u1.unmarshal(new DBAttr(e1)), u2.unmarshal(new DBAttr(e2)))
    }
  }

  implicit def uMap[K, V](implicit u: Unmarshaller[(K, V), DBAttr]) = new Unmarshaller[Map[K, V], DBAttr] {
    def unmarshal(b: DBAttr) = new $$(b.v.asInstanceOf[BasicDBList]).map(v ⇒ u.unmarshal(new DBAttr(v))).toMap
  }

  implicit def uOpt[T](implicit u: Unmarshaller[T, DBAttr]) = new Unmarshaller[Option[T], DBAttr] {
    def unmarshal(b: DBAttr) = if (b.v != null) Some(u.unmarshal(b)) else None
  }

  FormatsTester.test[DBAttr]

}

sealed class DBAttr(val v: Any)

case class AString(str: String) extends DBAttr(str)
case class AVal(anyval: AnyVal) extends DBAttr(anyval)
case class AObject(obj: DBObject) extends DBAttr(obj)
case class AList(list: BasicDBList) extends DBAttr(list)
case object ANull extends DBAttr(null)
