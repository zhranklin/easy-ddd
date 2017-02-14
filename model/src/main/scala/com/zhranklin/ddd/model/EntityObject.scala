package com.zhranklin.ddd.model

/**
 * Created by Zhranklin on 2017/2/12.
 * 实体模型
 */
abstract class EntityObject(implicit val id: Id) extends Product
case class Id(id: String)
