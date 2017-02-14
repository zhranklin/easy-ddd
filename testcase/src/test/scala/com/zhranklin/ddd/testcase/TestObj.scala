package com.zhranklin.ddd.testcase

import com.zhranklin.ddd.infra.IdGenerator
import com.zhranklin.ddd.infra.event.EventSource.WithSender
import com.zhranklin.ddd.infra.event._
import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.Id
import com.zhranklin.ddd.model.annotation.EntityObject
import com.zhranklin.ddd.testcase.ttt.TestObj

import scala.xml.Null

/**
 * Created by Zhranklin on 2017/2/14.
 * 用于测试宏注解的类
 */
@EntityObject
class Parent(to1: ttt.TestObj, to2: ttt.TestObj)


@EntityObject
case class Simple(a: String, b: String)

package ttt {
  @EntityObject
  class TestObj(a: String, b: Int, c: List[String], d: List[Map[Int, List[Option[String]]]])

  @EntityObject
  class Root(par: Parent)

}


object UnitOfWork extends App with Repository {

  import Simple._
  implicit val idGenerator = IdGenerator.UUID
  implicit lazy val eventSender: Sender = new Sender(new WithSender {
    lazy val sender = eventSender
  })

  val eBus = new EventBus {
    override protected def handle(event: Event) = {
      println(event)
      super.handle(event)
    }
  }

  eBus.addSource(eventSender.source)

  implicit object mar extends Format[String, String] {
    def marshal(a: String) = a
    def unmarshal(a: String) = a
  }

  implicit object mpr extends Mapper[Dmo[String], String] {

    val mp = collection.mutable.Map("1" → Map("a" → "xx", "b" → "yy"))

    def read(id: Id): Dmo[String] = Dmo(id, "simple", mp(id.id))
    def write(dmo: Dmo[String]) = mp += (dmo.id.id → dmo.attributes)
  }

  mpr.write(Simple("kk", "ww"))

  println(mpr.read(Id("1")): Simple)

  def t(m: Map[String, String]) = 1
}
