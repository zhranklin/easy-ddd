package com.zhranklin.ddd.model

import scala.reflect.ClassTag

/**
 * Created by Zhranklin on 2017/2/12.
 * 实体模型
 */
abstract class entityObject(implicit val id: Id[_ <: entityObject]) extends Product
case class Id[E](id: String)/*(implicit classTag: ClassTag[E]) {
  val cls = classTag.runtimeClass
}*/
