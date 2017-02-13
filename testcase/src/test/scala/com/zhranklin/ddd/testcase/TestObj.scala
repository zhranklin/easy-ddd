package com.zhranklin.ddd.testcase

import com.zhranklin.ddd.model.annotation.EntityObject

/**
 * Created by Zhranklin on 2017/2/14.
 * 用于测试宏注解的类
 */
@EntityObject
class TestObj(a: String, b: Int, c: List[String], d: List[Map[Int, List[Option[String]]]])
