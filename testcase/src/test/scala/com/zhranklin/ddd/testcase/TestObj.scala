package com.zhranklin.ddd.testcase

import com.zhranklin.ddd.infra.event.Event.Update
import com.zhranklin.ddd.infra.event._
import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.annotation.EntityObject
import com.zhranklin.ddd.model.{Id, annotation}
import com.zhranklin.ddd.support.formats.SimpleFormatsViaString
import com.zhranklin.ddd.support.formats.support.event.SimpleUnitOfWork

/**
 * Created by Zhranklin on 2017/2/14.
 * 用于测试宏注解的类
 */
@EntityObject
case class Parent(to1: ttt.TestObj, to2: ttt.TestObj)


@EntityObject
case class Simple(a: String, b: String)

package ttt {

  @EntityObject
  case class TestObj(a: String, b: Int, c: List[String], d: List[Map[Int, List[Option[String]]]])

  @EntityObject
  case class Root(par: Parent)

}

trait RepoImplicits extends SimpleFormatsViaString with SimpleUnitOfWork {

  val mp = collection.mutable.Map("1" → Map("a" → "xx", "b" → "yy"))

  implicit val mpr: Mapper[String] = new Mapper[String] {
    def read(id: Id): Dmo[String] = Dmo(id, "simple", mp(id.id))
    def write(dmo: Dmo[String]) = mp += (dmo.id.id → dmo.attributes)
  }

  def handleUpdate(event: Event): Unit = event match {
    case Update(e) ⇒
      write(e.asInstanceOf[Simple])
    case _ ⇒
  }

  val eBus = new EventBus {
    override protected def handle(event: Event) = {
      println(s"pre mp: $mp")
      println(event)
      handleUpdate(event)
      super.handle(event)
      println(s"post mp: $mp")
      Some(event)
    }
  }

  eBus.addSource(eventSender.source)
}

@annotation.Repository
trait Repos extends RepoImplicits with WithRepos with Parent with Simple with ttt.TestObj with ttt.Root

object TestUnitOfWork extends App with SimpleUnitOfWork with Repos {
  Simple("kk", "ww")
  println(read[Simple](Id("1")))
}
