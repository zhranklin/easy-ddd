package com.zhranklin.ddd.infra.persistence

import com.zhranklin.ddd.model.EntityObject

/**
 * Created by Zhranklin on 2017/2/15.
 */
trait Repo {
  def getEntityObject[T]: PartialFunction[Dmo[T], EntityObject]
}

trait WithRepos[T]
