package com.zhranklin.ddd.infra.persistence

import com.zhranklin.ddd.model.Id

/**
 * Created by Zhranklin on 2017/2/12.
 * 持久化的中间对象, 用于保存实体
 */
case class Dmo[T](id: Id, table: String, attributes: Map[String, T])
