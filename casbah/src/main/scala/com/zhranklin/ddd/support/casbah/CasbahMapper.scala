package com.zhranklin.ddd.support.casbah

import java.util.Date

import com.mongodb.casbah.Imports.{MongoDBObject ⇒ $, _}
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.commons.TypeImports
import com.zhranklin.ddd.infra.persistence.{Dmo, Mapper}
import com.zhranklin.ddd.model.Id
import org.bson.types.ObjectId

/**
 * Created by Zhranklin on 2017/3/17.
 */
trait CasbahMapper extends Mapper[DBAttr] {

  def db: MongoDB

  def read(id: Id, clazz: Class[_]) = {
    val table = getTableName(clazz)
    val mongoObj: Option[TypeImports.DBObject] = db(table).findOneByID(id.id)
    mongoObj.map(obj ⇒
      Dmo[DBAttr](id, table, obj.mapValues(v ⇒ new DBAttr(v)).toMap)
    ).orNull
  }
  def write(dmo: Dmo[DBAttr]) = {
    val mongoObj = dmo.attributes.toSeq :+ ("_id", dmo.id.id)
    db(dmo.table).update("_id" $eq dmo.id.id, $(mongoObj: _*), upsert = true)
  }
}
