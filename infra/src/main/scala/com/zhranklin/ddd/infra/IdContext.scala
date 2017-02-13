package com.zhranklin.ddd.infra

import com.zhranklin.ddd.model.Id

/**
 * Created by Zhranklin on 2017/2/12.
 * 一个用于自动生成Id的implicit
 */
trait IdContext {
  implicit def getId(implicit idGenerator: IdGenerator): Id = idGenerator.generate
}

object IdContext extends IdContext