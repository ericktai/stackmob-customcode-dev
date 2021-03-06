package com.stackmob.customcode.dev
package test

import org.specs2.Specification
import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.sdkapi.SDKServiceProvider
import collection.JavaConverters._
import concurrent.duration._
import org.specs2.mock.Mockito
import concurrent.TimeoutException
import java.util.UUID

class CustomCodeMethodExecutorSpecs extends Specification with Mockito { def is =
  "CustomCodeMethodExecutorSpecs".title                                                                                  ^ end ^
  "CustomCodeMethodExecutor executes CustomCodeMethods"                                                                  ^ end ^
  "The handler should fail if a method takes too long to execute"                                                       ! timeout ^ end ^
                                                                                                                        end
  implicit private val session = UUID.randomUUID()
  private val method = new CustomCodeMethod {
    override def getMethodName: String = "testMethod"

    override def execute(request: ProcessedAPIRequest, serviceProvider: SDKServiceProvider): ResponseToProcess = {
      Thread.sleep(100)
      new ResponseToProcess(200)
    }

    override def getParams = List[String]().asJava
  }

  val req = mock[ProcessedAPIRequest]



  def timeout = {
    val res = CustomCodeMethodExecutor(method,
      mock[ProcessedAPIRequest],
      mock[SDKServiceProvider],
      Duration.MinusInf)
    res.failed.toOption must beSome.like {
      case t => t must beAnInstanceOf[TimeoutException]
    }
  }

}
