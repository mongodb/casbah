package com.mongodb.casbah.test.gridfs

import java.io._
import java.security.MessageDigest
import com.mongodb.casbah.test.core.CasbahDBTestSpecification


trait GridFSSpecification extends CasbahDBTestSpecification {

  def logo_fh = new FileInputStream("casbah-gridfs/src/test/resources/powered_by_mongo.png")

  def logo_bytes = {
    val data = new Array[Byte](logo_fh.available())
    logo_fh.read(data)
    data
  }

  def logo = new ByteArrayInputStream(logo_bytes)

  lazy val logo_md5 = {
    val digest = MessageDigest.getInstance("MD5")
    digest.update(logo_bytes)
    digest.digest().map("%02X" format _).mkString.toLowerCase
  }

}
