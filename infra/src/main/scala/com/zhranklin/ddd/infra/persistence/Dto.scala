package com.zhranklin.ddd.infra.persistence

/**
 * Created by Zhranklin on 2017/2/12.
 * 持久化的中间对象, 用于保存实体
 */
case class Dto[T](id: String, table: String, attributes: Map[String, T])
