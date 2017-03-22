package com.zhranklin.ddd.support.casbah

import com.mongodb.casbah.MongoClient
import com.zhranklin.ddd.infra.event.Event.Update
import com.zhranklin.ddd.infra.event._
import com.zhranklin.ddd.infra.persistence._
import com.zhranklin.ddd.model.annotation.{EntityObject, Repository}
import com.zhranklin.ddd.support.SimpleDMCreationContext
import org.scalatest.FlatSpec

import com.zhranklin.ddd.test.MockEntityObjects._

object client {
  val db = MongoClient()("easy-ddd_test")
}

trait RepoImplicits extends DBObjectFormats with SimpleDMCreationContext {

  implicit val mpr = new CasbahMapper {
    def db = client.db
  }

  def handleUpdate(event: Event): Unit = event match {
    case Update(e) ⇒
      write(e)
    case _ ⇒
  }

}

@Repository
trait Repos extends RepoImplicits with WithRepoOf[(Parent, Simple, TestObj, Root)]

/**
 * Created by Zhranklin on 2017/3/20.
 */
class CasbahMapperTest extends FlatSpec with SimpleDMCreationContext with Repos {

  import client._
  db.dropDatabase()

  val simples = db("Simple")

  val eBus = new EventBus {
    override protected def handle(event: Event) = {
      info(s"event: $event")
      info(s"mp before handle:")
      List(db("Simple"), db("Parent"), db("testObj")).foreach(col ⇒ info(col.find.toList.toString))
      handleUpdate(event)
      super.handle(event)
      info(s"mp after handle:")
      List(db("Simple"), db("Parent"), db("testObj")).foreach(col ⇒ info(col.find.toList.toString))
      Some(event)
    }
  }
  eBus.addSource(eventSender.source)
  val simple1 = Simple("kk", "ww")
  val testObj1 = TestObj("testobj1", 1, List("a", "b"), List(Map(1 → List(Some("1")), 2 → List(Some("2")))))
  val testObj2 = TestObj("testobj2", 2, List("c", "d"), List(Map(1 → List(None), 2 → List(Some("2")))))
  val parent1 = Parent(testObj1, testObj1)
  val root1 = Root(parent1)
  info(s"""read[Simple]: ${read[Simple](simple1.id.id)}""")
  info(s"""read[ttt.TestObj]: ${read[TestObj](testObj1.id.id)}""")
  info(s"""read[Parent]: ${read[Parent](parent1.id.id)}""")
  info(s"""read[ttt.Root]: ${read[Root](root1.id.id)}""")
}

