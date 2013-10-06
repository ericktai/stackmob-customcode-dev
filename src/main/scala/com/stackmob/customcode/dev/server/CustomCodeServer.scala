/**
 * Copyright 2011-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.dev.server

import org.eclipse.jetty.server.Server
import java.net.InetSocketAddress
import com.stackmob.core.jar.JarEntryObject
import org.slf4j.LoggerFactory
import java.util.UUID
import com.stackmob.newman.{ApacheHttpClient, HttpClient}

object CustomCodeServer {
  private lazy val logger = LoggerFactory.getLogger(CustomCodeServer.getClass)

  /**
   * java compatability method for #serve(JarEntryObject, String, String, Int)(HttpClient)
   */
  def serve(jarEntryObject: JarEntryObject, apiKey: String, apiSecret: String, port: Int) {
    serve(jarEntryObject, apiKey, apiSecret, port)
  }

  /**
   * start a custom code server
   * @param jarEntryObject the custom code JarEntryObject to serve
   * @param apiKey the api key used to proxy requests to StackMob API v0
   * @param apiSecret the api secret used to proxy requests to StackMob API v0
   * @param port the port to serve on
   * @param httpClient the Newman HttpClient used to proxy requests to StackMob API v0
   * @return nothing. the server will continue serving forever
   */
  def serve(jarEntryObject: JarEntryObject, apiKey: String, apiSecret: String, port: Int = 8080)
           (implicit httpClient: HttpClient = new ApacheHttpClient()) {
    implicit val session = UUID.randomUUID()
    val handler = new CustomCodeHandler(apiKey, apiSecret, jarEntryObject)

    val host = "localhost"
    val addr = new InetSocketAddress(host, port)
    val svr = new Server(addr)
    svr.setHandler(handler)
    logger.info(s"Starting custom code dev server session ${session.toString} on $host:$port")
    svr.start()
    svr.join()
  }

}
