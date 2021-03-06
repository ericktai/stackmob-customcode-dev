package com.stackmob.customcode.dev
package test
package server
package sdk
package data

import com.stackmob.sdk.api.{StackMobOptions, StackMobQuery, StackMobDatastore}
import com.stackmob.sdk.callback.StackMobRawCallback
import com.stackmob.sdk.net.{HttpVerbWithoutPayload, HttpVerb, HttpVerbWithPayload}
import com.stackmob.customcode.dev.server.sdk.{JavaMap, JavaList}
import collection.JavaConverters._
import java.util.concurrent.CopyOnWriteArrayList
import scalaz.Scalaz._
import com.stackmob.customcode.dev.server.sdk.EntryW

private[data] class MockStackMobDatastore(val getResponse: ResponseDetails,
                                          val postResponse: ResponseDetails,
                                          val putResponse: ResponseDetails,
                                          val deleteResponse: ResponseDetails)
  extends StackMobDatastore(datastoreExecutorService,datastoreSession, "MockStackMobDatastoreHost", datastoreRedirectedCallback) {
  import MockStackMobDatastore._

  lazy val getCalls = new CopyOnWriteArrayList[RequestDetails]()
  def numGetCalls = getCalls.size()

  lazy val postCalls = new CopyOnWriteArrayList[RequestDetails]()
  def numPostCalls = postCalls.size()

  lazy val putCalls = new CopyOnWriteArrayList[RequestDetails]()
  def numPutcalls = putCalls.size()

  lazy val deleteCalls = new CopyOnWriteArrayList[RequestDetails]()
  def numDeleteCalls = deleteCalls.size()

  private val requestURL = "http://testurl.com"
  private val emptyRequestHeaders = List[JavaMap.Entry[String, String]]().asJava
  private val getVerb = HttpVerbWithoutPayload.GET
  private val postVerb = HttpVerbWithPayload.POST
  private val putVerb = HttpVerbWithPayload.PUT
  private val deleteVerb = HttpVerbWithoutPayload.DELETE

  override def post(schema: String,
                    body: String,
                    cb: StackMobRawCallback) {
    cb.setDone(postVerb,
      requestURL,
      emptyRequestHeaders,
      body,
      postResponse.code,
      postResponse.headerEntries,
      postResponse.body)

    postCalls.add(new RequestDetails(postVerb, schema, emptyRequestHeaders.toTuples, body.some))
  }

  override def postRelated(path: String,
                           primaryId: String,
                           relatedField: String,
                           relatedObject: Object,
                           cb: StackMobRawCallback) {
    cb.setDone(postVerb,
      requestURL,
      emptyRequestHeaders,
      relatedObject.toString,
      postResponse.code,
      postResponse.headerEntries,
      postResponse.body)
    postCalls.add(new RequestDetails(postVerb, s"$path/$primaryId/$relatedField", emptyRequestHeaders.toTuples, relatedObject.toString.some))
  }

  override def get(query: StackMobQuery, cb: StackMobRawCallback) {
    cb.setDone(getVerb, requestURL, emptyRequestHeaders, "", getResponse.code, getResponse.headerEntries, getResponse.body)
    getCalls.add(new RequestDetails(getVerb,
      query.getObjectName,
      query.getHeaders.asScala.toList,
      None,
      query.getArguments.asScala.map(_.tup).toList))
  }

  override def get(query: StackMobQuery, options: StackMobOptions, cb: StackMobRawCallback) {
    cb.setDone(getVerb, requestURL, emptyRequestHeaders, "", getResponse.code, getResponse.headerEntries, getResponse.body)
    getCalls.add(new RequestDetails(getVerb,
      query.getObjectName,
      query.getHeaders.asScala.toList ++ options.getHeaders.asScala.map(_.tup).toList,
      None,
      query.getArguments.asScala.map(_.tup).toList))
  }

  override def put(path: String, id: String, body: Object, cb: StackMobRawCallback) {
    put(path, id, body.toString, cb)
  }

  override def put(path: String, id: String, body: String, cb: StackMobRawCallback) {
    cb.setDone(putVerb, requestURL, emptyRequestHeaders, body.toString, putResponse.code, putResponse.headerEntries, putResponse.body)
    val reqDetails = new RequestDetails(putVerb,
      s"$path/$id",
      Nil,
      body.toString.some,
      Nil
    )
    putCalls.add(reqDetails)
  }

  override def putRelated[T](path: String, primaryId: String, relatedField: String, relatedIds: JavaList[T], cb: StackMobRawCallback) {
    cb.setDone(putVerb, requestURL, emptyRequestHeaders, "", putResponse.code, putResponse.headerEntries, putResponse.body)
    val reqDetails = new RequestDetails(putVerb, s"$path/$primaryId/$relatedField", Nil, None, Nil)
    putCalls.add(reqDetails)
  }


    override def delete(path: String, id: String, cb: StackMobRawCallback) {
    cb.setDone(deleteVerb, requestURL, emptyRequestHeaders, "", deleteResponse.code, deleteResponse.headerEntries, deleteResponse.body)
    val reqDetails = new RequestDetails(deleteVerb, s"$path/$id", Nil, None, Nil)
    deleteCalls.add(reqDetails)
  }

  override def deleteIdsFrom[T](path: String, primaryId: String, field: String, idsToDelete: JavaList[T], cascadeDeletes: Boolean, cb: StackMobRawCallback) {
    cb.setDone(deleteVerb, requestURL, emptyRequestHeaders, "", deleteResponse.code, deleteResponse.headerEntries, deleteResponse.body)
    val reqDetails = new RequestDetails(deleteVerb, s"$path/$primaryId/$field", Nil, None, Nil)
    deleteCalls.add(reqDetails)
  }
}

private[data] class ResponseDetails(val code: Int,
                                    val headers: List[(String, String)] = Nil,
                                    val body: Array[Byte] = Array[Byte]()) {
  def headerEntries: JavaList[JavaMap.Entry[String, String]] = headers.toEntries
}
object ResponseDetails {
  def apply(code: Int) = new ResponseDetails(code)
}

object MockStackMobDatastore {
  class RequestDetails(val verb: HttpVerb,
                       val schema: String,
                       val headers: List[(String, String)],
                       val body: Option[String],
                       val queryStringParams: List[(String, String)] = Nil)
}
