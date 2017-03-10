package com.zhranklin.ddd.model.annotation

import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta.Term.{Param, Select}
import scala.meta._

/**
 * Created by Zhranklin on 2017/2/12.
 * 实体的元注解
 */
class EntityObject extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls @ Defn.Class(_, typeName, _, Ctor.Primary(_, _, paramss), template) ⇒
        def tpeToIdentifier(tpe: Type.Arg): String = tpe match {
          case Type.Select(qual, Type.Name(name)) ⇒
            qual.syntax.split('.').toSeq :+ name map Type.Name.apply map tpeToIdentifier mkString "_"
          case Type.Name(name) ⇒ name.replaceAll("\\.", "_")
          case Type.Apply(Type.Name(name), args) ⇒
            s"${name}_l_${args.map(tpeToIdentifier).mkString("_a_")}_r_"
        }

        val stats = template.stats.getOrElse(Nil)
        val params = paramss.head
        val updateParams: Seq[Param] = params.map{ param ⇒
          param.copy(default = Some(q"this.${Term.Name(param.name.syntax)}"))
        }
        val updateArgs: Seq[Term.Arg] = params.map { param ⇒
//          Term.Arg.Named(Term.Name(param.name.syntax))
          arg"${Term.Name(param.name.syntax): Term}"
        }
        def typeToVal(typeName: String) = (("", false) /: typeName) {
          case ((str, replaced), c) ⇒
            if (!replaced && c.isUpper)
              (str + c.toLower, true)
            else (str + c, replaced)
        } match {
          case (str, true) ⇒ str
          case (str, false) ⇒ str + "1"
        }

        val types = params.map(_.decltpe.get).groupBy(_.syntax).map(_._2.head).toList

        def impParams(tpef: Type ⇒ Type.Arg) = types.map {
          tpe ⇒
            param"${Term.Name("_m" + tpeToIdentifier(tpe))}: ${tpef(tpe.asInstanceOf[Type])}"
        }

        def marshallTuples(ref: Term) = params.map {
          case Param(_, Term.Name(name), Some(tpe), _) ⇒
            q"(${Lit(name)}, ${Term.Name("_m" + tpeToIdentifier(tpe))}.marshal($ref.${Term.Name(name)}))"
        }

        q"""
           case class $typeName(..${paramss.head})
                               (implicit sender: com.zhranklin.ddd.infra.event.Sender,
                                id: com.zhranklin.ddd.model.Id[$typeName])
             extends com.zhranklin.ddd.model.entityObject {
             import com.zhranklin.ddd.infra.event.Event.{Update, Delete}

             sender.send(Update(this))

             def update(..$updateParams) = {
               val updated = ${Term.Name(typeName.syntax)}(..$updateArgs)
               sender.send(Update(updated))
               updated
             }

             def delete() = {
               sender.send(Delete(this))
             }

             ..$stats
           }
           object ${Term.Name(typeName.syntax)} {
             trait Repo {
               import com.zhranklin.ddd.infra.persistence.{Unmarshaller, Marshaller}
               import com.zhranklin.ddd.model.entityObject
               implicit def ${Term.Name(typeToVal(typeName.syntax+"ToDmo"))}[T](obj: $typeName)
                           (implicit ..${impParams(t ⇒ t"Marshaller[$t, T]")}, sender: com.zhranklin.ddd.infra.event.Sender) = {
                 val attr = Map[String, T](..${marshallTuples(q"obj")})
                 Dmo[T](obj.id,${Lit(typeName.syntax)}, attr)
               }

               implicit def ${Term.Name(typeToVal(typeName.syntax+"FromDmo"))}[T](dmo: Dmo[T])
                             (implicit ..${impParams(t ⇒ t"Unmarshaller[$t, T]")},
                              sender: com.zhranklin.ddd.infra.event.Sender)= {
                 implicit val id = com.zhranklin.ddd.model.Id[$typeName](dmo.id.id)
                 ${Term.Name(typeName.syntax)}(..${
                      params.map {
                        case Param(_, Term.Name(name), Some(tpe), _) ⇒
                          arg"""${Term.Name(name)} = ${Term.Name("_m" + tpeToIdentifier(tpe))}.unmarshal(dmo.attributes(${Lit(name)}))"""
                      }
                    })
               }
             }
           }
         """
    }
  }
}
