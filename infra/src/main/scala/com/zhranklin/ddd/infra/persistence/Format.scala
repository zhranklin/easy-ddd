package com.zhranklin.ddd.infra.persistence

/**
 * Created by Zhranklin on 2017/2/13.
 * 基本属性/值对象的(反)序列化
 */
trait Format[A, B] {
  def marshal(a: A): B
  def unmarshal(b: B): A
}
