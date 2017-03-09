package com.zhranklin.ddd.infra.persistence

/**
 * Created by Zhranklin on 2017/2/13.
 * 基本属性/值对象的(反)序列化
 */

trait Marshaller[-A, +B] {
  def marshal(a: A): B
}

trait Unmarshaller[+A, -B] {
  def unmarshal(b: B): A
}

trait Format[A, B] extends Marshaller[A, B] with Unmarshaller[A, B]

object Unmarshaller {
  def nonChange[T] = new Unmarshaller[T, T] {
    def unmarshal(b: T) = b
  }
}

object Marshaller {
  def nonChange[T] = new Marshaller[T, T] {
    def marshal(a: T) = a
  }
}
