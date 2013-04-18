package com.stackmob.customcode.dev
package localrunner

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.stackmob.core.jar.JarEntryObject
import collection.JavaConverters._
import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.ProcessedAPIRequest
import com.stackmob.core.MethodVerb
import sdk.SDKServiceProviderImpl
import com.stackmob.sdk.api.StackMob
import com.stackmob.sdk.api.StackMob.OAuthVersion
import com.stackmob.sdk.push.StackMobPush
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import scala.util.Try
import com.stackmob.customcode.dev.CustomCodeMethodExecutor

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 2:47 PM
 */
class CustomCodeHandler(jarEntry: JarEntryObject,
                        maxMethodDuration: Duration = 25.seconds)
                       (implicit executionContext: ExecutionContext = CustomCodeMethodExecutor.DefaultExecutionContext)
  extends AbstractHandler {

  private lazy val logger = LoggerFactory.getLogger(classOf[CustomCodeHandler])

  private val methods = jarEntry.methods.asScala.foldLeft(Map[String, CustomCodeMethod]()) { (running, method) =>
    running ++ Map(method.getMethodName -> method)
  }

  /**
   * create a ProcessedAPIRequest
   * @param methodName the name of the method to execute
   * @param baseReq the Request
   * @param servletReq the servlet request
   * @param body the entire body of the request
   * @return the new ProcessedAPIRequest
   */
  private def processedAPIRequest(methodName: String, baseReq: Request, servletReq: HttpServletRequest, body: String): ProcessedAPIRequest = {
    val requestedVerb = MethodVerb.valueOf(servletReq.getMethod)
    val httpURI = baseReq.getUri
    val mbQueryString = Option(httpURI.getQuery)
    val mbQueryParams = for {
      queryString <- mbQueryString
    } yield {
      queryString.split("&").toList.foldLeft(Map[String, String]()) { (agg, cur) =>
        cur.split("=").toList match {
          case key :: value :: Nil => agg ++ Map(key -> value)
          case _ => agg
        }
      }
    }
    val queryParams = mbQueryParams.getOrElse(Map[String, String]())

    val apiVersion = 0
    val counter = 0

    new ProcessedAPIRequest(requestedVerb,
      httpURI.toString,
      loggedInUser,
      queryParams.asJava,
      body,
      appName,
      apiVersion,
      methodName,
      counter)
  }

  //TODO: get these from config file
  private lazy val apiKey = "cc-test-api-key"
  private lazy val apiSecret = "cc-test-api-secret"

  private lazy val stackMob = stackMobClient(apiKey, apiSecret)
  private lazy val stackMobPush = stackMobPushClient(apiKey, apiSecret)

  override def handle(target: String,
                      baseRequest: Request,
                      servletRequest: HttpServletRequest,
                      response: HttpServletResponse) {
    val writer = response.getWriter
    val realPath = baseRequest.getPathInfo.replaceFirst("/", "")

    methods.get(realPath).map { method =>
      val body = baseRequest.getReader.exhaust().toString()
      val apiReq = processedAPIRequest(realPath, baseRequest, servletRequest, body)
      val sdkServiceProvider = new SDKServiceProviderImpl(stackMob, stackMobPush)

      val resTry = for {
        resp <- CustomCodeMethodExecutor(method,
          apiReq,
          sdkServiceProvider,
          maxMethodDuration)(executionContext)
        respJSON <- Try {
          val respMap = resp.getResponseMap
          json.write(respMap.asScala)
        }
        _ <- Try(response.setStatus(resp.getResponseCode))
        _ <- Try(writer.print(respJSON))
      } yield ()

      try {
        resTry.get
      } catch {
        case t: TimeoutException => {
          //note - if the future is in an infinite loop, it will continue to take up the thread on which it's executing until the server is killed
          logger.warn(s"${method.getMethodName} took over ${maxMethodDuration.toSeconds} seconds to execute")
          writer.print(s"${method.getMethodName} took over ${maxMethodDuration.toSeconds} seconds) to execute. Please shorten its execution time")
        }
        case t: Throwable => {
          logger.warn(s"${method.getMethodName} threw ${t.getMessage}", t)
          writer.print(s"${method.getMethodName} threw ${t.getMessage}. see logs for details")
        }
      }
    }.getOrElse {
      writer.println("unknown custom code method %s".format(realPath))
      response.setStatus(404)
    }

    baseRequest.setHandled(true)
  }

}
