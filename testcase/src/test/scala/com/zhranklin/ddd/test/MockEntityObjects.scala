package com.zhranklin.ddd.test

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
object MockEntityObjects {
  @EntityObject
  case class Parent(to1: TestObj, to2: TestObj)

  @EntityObject
  case class Simple(a: String, b: String)

  @EntityObject
  case class TestObj(a: String, b: Int, c: List[String], d: List[Map[Int, List[Option[String]]]])

  @EntityObject
  case class Root(par: Parent)
}

