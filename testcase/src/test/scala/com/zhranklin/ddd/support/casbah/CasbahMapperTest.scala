package com.zhranklin.ddd.support.casbah

import com.mongodb.casbah.MongoClient
import com.zhranklin.ddd.infra.event.Event.Update
import com.zhranklin.ddd.infra.event._
import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.annotation.{EntityObject, Repository}
import com.zhranklin.ddd.support.SimpleDMCreationContext
import com.zhranklin.ddd.support.formats.SimpleFormatsViaString
import org.scalatest.{FlatSpec, _}

import scala.reflect.ClassTag

/**
 * Created by Zhranklin on 2017/2/14.
 * 用于测试宏注解的类
 */
@EntityObject
case class Parent(to1: ttt.TestObj, to2: ttt.TestObj)


@EntityObject
case class Simple(a: String, b: String)

trait RepoImplicits extends DBObjectFormats with SimpleDMCreationContext {

  implicit val mpr = new CasbahMapper {
    def db = MongoClient()("test")
  }

  def handleUpdate(event: Event): Unit = event match {
    case Update(e) ⇒
      write(e)
    case _ ⇒
  }

}
package ttt {

  @EntityObject
  case class TestObj(a: String, b: Int, c: List[String], d: List[Map[Int, List[Option[String]]]])

  @EntityObject
  case class Root(par: Parent)

}

@Repository
trait Repos extends RepoImplicits with WithRepos[(Parent, Simple, ttt.TestObj, ttt.Root)]

/**
 * Created by Zhranklin on 2017/3/20.
 */
class CasbahMapperTest extends FlatSpec with SimpleDMCreationContext with Repos {

  val db = MongoClient()("test")

  val eBus = new EventBus {
    override protected def handle(event: Event) = {
      info(s"event: $event")
      info(s"mp before handle:")
      List(db("Simple"), db("Parent"), db("testObj")).foreach(_.find.map(_.toString).foreach(info(_)))
      handleUpdate(event)
      super.handle(event)
      info(s"mp after handle:")
      List(db("Simple"), db("Parent"), db("testObj")).foreach(_.find.map(_.toString).foreach(info(_)))
      Some(event)
    }
  }
  eBus.addSource(eventSender.source)
  Simple("kk", "ww")
  info(s"""read[Simple]: ${read[Simple]("1")(mpr, implicitly[Dmo[DBAttr] ⇒ Simple], implicitly[ClassTag[Simple]])}""")
  info(s"""read[ttt.TestObj]("1"): ${read[ttt.TestObj]("1")}""")
  info(s"""read[Parent]("1"): ${read[Parent]("1")}""")
}

