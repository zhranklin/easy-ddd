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
      case cls @ q"trait $typeName extends ..$parents" ⇒

        val (parentNames1, entityTypes1) = {
          val parentsStr = parents.map(_.syntax.replaceAll("\\(\\)", ""))
          println(parentsStr.zipWithIndex)
          val withRepos = parentsStr.indexWhere {_.contains("WithRepoOf")}
          val (realParents: Seq[String], wrp :: entityTypes) = parentsStr.splitAt(withRepos)
          ("com.zhranklin.ddd.infra.persistence.repository" +: realParents ++: entityTypes.map(_ + ".Repo"),
            entityTypes)
        }

        val (parentNames, entityTypes) = {
          val parentNameTps: Seq[(String, Boolean)] = parents flatMap {
            case t @ ctor"WithRepoOf[(..$types)]()" ⇒
              println(types)
              println(s"t: $t")
              types map (tpe ⇒ (tpe.syntax + ".Repo", true))
            case t @ ctor"$typename" ⇒
              println(s"tt: $t")
              List((typename.syntax.replaceAll("\\(\\)", ""), false))
          }
          (parentNameTps map (_._1), parentNameTps filter(_._2 == true) map (_._1 dropRight 5))
        }

        q"""
            trait $typeName extends ..${parentNames map {Ctor.Name.apply}} {
              import com.zhranklin.ddd.model.{entityObject, Id}
              implicit def read[T, E <: entityObject](id: Id)(implicit mapper: Mapper[T], f: Dmo[T] ⇒ E, classTag: scala.reflect.ClassTag[E]): E = mapper.read(id, classTag.runtimeClass)
              implicit private def writeGen[T, E <: entityObject](e: E)(implicit mapper: Mapper[T], f: E ⇒ Dmo[T]): Unit = mapper.write(e)

              override val write: entityObject ⇒ Unit = {
                ..case ${entityTypes map (n ⇒ p"case e0: ${Type.Name(n)} ⇒ writeGen(e0)")}
              }

            }
        """
    }
  }
}

