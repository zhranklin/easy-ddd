package com.zhranklin.ddd.model.annotation

import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta._

/**
 * Created by Zhranklin on 2017/3/8.
 */
class Repository extends StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls @ Defn.Trait(_, typeName, tParams, ctor, Template(_, parents, _, statsOpt)) ⇒

        val parentsStr = parents.map(_.syntax.replaceAll("\\(\\)", ""))

        println(parentsStr.zipWithIndex)

        val withRepos = parentsStr.indexWhere {_.contains("WithRepos")}

        val (realParents: Seq[String], wrp :: entityTypes) = parentsStr.splitAt(withRepos)

        q"""
            trait $typeName extends ..${("com.zhranklin.ddd.infra.persistence.Repository" +: realParents ++: entityTypes.map(_+".Repo")) map {Ctor.Name.apply}} {
              import com.zhranklin.ddd.model.{EntityObject, Id}
              implicit def read[T, E <: EntityObject](id: Id)(implicit mapper: Mapper[T], f: Dmo[T] ⇒ E): E = mapper.read(id)
              implicit private def writeGen[T, E <: EntityObject](e: E)(implicit mapper: Mapper[T], f: E ⇒ Dmo[T]): Unit = mapper.write(e)

              val write: EntityObject ⇒ Unit = {
                ..case ${entityTypes map (n ⇒ p"case e0: ${Type.Name(n)} ⇒ writeGen(e0)")}
              }

            }
        """
    }
  }
}

