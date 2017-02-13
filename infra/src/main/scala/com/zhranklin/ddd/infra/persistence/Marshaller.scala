package com.zhranklin.ddd.infra.persistence

/**
 * Created by Zhranklin on 2017/2/13.
 * 基本属性/值对象的(反)序列化
 */
trait Marshaller[-A, +B] {
  def marshal(a: A): B
}

trait Unmarshaller[-A, +B] {
  def unmarshal(a: A): B
}
