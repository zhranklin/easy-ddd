package com.zhranklin.ddd.testcase

import com.zhranklin.ddd.infra.event.Event.Update
import com.zhranklin.ddd.infra.event._
import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.annotation.{EntityObject, Repository}
import com.zhranklin.ddd.model.Id
import com.zhranklin.ddd.support.SimpleDMCreationContext
import com.zhranklin.ddd.support.formats.SimpleFormatsViaString
import org.scalatest._

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
    def read(id: Id, clazz: Class[_]) = Dmo(id, "Simple", mp(getTableName(clazz))(id.id))
    def write(dmo: Dmo[String]) = mp(dmo.table) += (dmo.id.id → dmo.attributes)
  }

  def handleUpdate(event: Event): Unit = event match {
    case Update(e) ⇒
      write(e)
    case _ ⇒
  }

}

@Repository
trait Repos extends RepoImplicits with WithRepos[(Parent, Simple, ttt.TestObj, ttt.Root)]

class TestDMCreationContext extends FlatSpec with Matchers with SimpleDMCreationContext with Repos {

  val eBus = new EventBus {
    override protected def handle(event: Event) = {
      info(s"event: $event")
      info(s"mp before handle:")
      mp.map(tp ⇒ s"${tp._1} -> ${tp._2}").foreach(info(_))
      handleUpdate(event)
      super.handle(event)
      info(s"mp after handle:")
      mp.map(tp ⇒ s"${tp._1} -> ${tp._2}").foreach(info(_))
      Some(event)
    }
  }
  eBus.addSource(eventSender.source)
  "The repositories and event buses" should "works well" in {
    Simple("kk", "ww")
    info(s"""read[Simple]: ${read[Simple]("1")}""")
    info(s"""read[ttt.TestObj]("1"): ${read[ttt.TestObj]("1")}""")
    info(s"""read[Parent]("1"): ${read[Parent]("1")}""")
  }
}
