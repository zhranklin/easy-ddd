package com.zhranklin.ddd.testcase

import com.zhranklin.ddd.infra.event.Event.Update
import com.zhranklin.ddd.infra.event._
import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.annotation.EntityObject
import com.zhranklin.ddd.model.{Id, annotation}
import com.zhranklin.ddd.support.SimpleDMCreationContext
import com.zhranklin.ddd.support.formats.SimpleFormatsViaString

import scala.collection.mutable

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

trait RepoImplicits extends SimpleFormatsViaString with SimpleDMCreationContext {

  val simple = mutable.Map("1" → Map("a" → "xx", "b" → "yy"))
  val parent = mutable.Map("1" → Map("to1" → "1", "to2" → "1"))
  val testobj = mutable.Map("1" → Map("a" → "xx", "b" → "3", "c" → "[x,y]", "d" → "[[(1,[k,w])],[(4,[s,,])]]"))

  val mp = mutable.Map(
    "Simple" → simple,
    "Parent" → parent,
    "TestObj" → testobj)

  implicit val mpr: Mapper[String] = new Mapper[String] {
    def read(id: Id[_]): Dmo[String] = Dmo(id, "Simple", mp("Simple")(id.id))
    def write(dmo: Dmo[String]) = mp(dmo.table) += (dmo.id.id → dmo.attributes)
  }

  def handleUpdate(event: Event): Unit = event match {
    case Update(e) ⇒
      write(e)
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
trait Repos extends RepoImplicits with WithRepos[(Parent, Simple, ttt.TestObj, ttt.Root)]

object TestDMCreationContext extends App with SimpleDMCreationContext with Repos {
  Simple("kk", "ww")
  println(read[Simple]("1"))
  println(read[ttt.TestObj]("1"))
  println(read[Parent]("1"))
}
