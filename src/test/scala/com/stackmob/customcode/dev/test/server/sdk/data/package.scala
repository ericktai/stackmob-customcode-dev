package com.stackmob.customcode.dev
package test
package server
package sdk

import org.scalacheck.Gen
import com.stackmob.customcode.dev.server._
import com.stackmob.sdk.api._
import java.util.concurrent.Executors
import com.stackmob.sdk.api.StackMob.OAuthVersion
import com.stackmob.sdk.callback._
import java.util
import net.liftweb.json._

package object data {
  //don't use large numbers here because it may affect runtime of construction of nested structures
  val genOverMaxDepth = Gen.choose(maxDepth, maxDepth + 10)
  val genUnderMaxDepth = Gen.choose(0, maxDepth - 1)

  private[data] val datastoreExecutorService = Executors.newCachedThreadPool()
  private[data] val datastoreSession = new StackMobSession(OAuthVersion.One, 0, "test-key", "test-secret", "test-user", "test-userid")

  private[data] val datastoreRedirectedCallback = new StackMobRedirectedCallback {
    override def redirected(originalUrl: String, redirectHeaders: util.Map[String, String], redirectBody: String, newURL: String) {}
  }

  private[data] def createNestedJValue[T <: JValue](depth: Int, base: T)(build: T => T): T = {
    (0 until depth).foldLeft(base) { (agg, num) =>
      build(agg)
    }
  }

  private[data] def createNestedJObject(depth: Int, base: JObject, key: String): JObject = {
    createNestedJValue(depth, base) { jValue: JValue =>
      JObject(List(JField(key, jValue)))
    }
  }

  private[data] def createNestedJArray(depth: Int, base: JArray): JArray = {
    createNestedJValue(depth, base) { jValue: JValue =>
      JArray(List(jValue))
    }
  }

  private[data] implicit class JValueW(jValue: JValue) {
    def toList: Option[List[Any]] = {
      jValue match {
        case j: JArray => Some(j.values)
        case _ => None
      }
    }
    def toMap: Option[Map[String, Any]] = {
      jValue match {
        case j: JObject => Some(j.values)
        case _ => None
      }
    }
  }
}
