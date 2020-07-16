/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.component.as2.it;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.as2.api.entity.DispositionNotificationMultipartReportEntity;
import org.apache.camel.component.as2.internal.AS2Constants;
import org.apache.camel.quarkus.component.as2.it.transport.ClientResult;
import org.apache.camel.quarkus.component.as2.it.transport.Request;
import org.apache.camel.quarkus.component.as2.it.transport.ServerResult;
import org.apache.http.protocol.HttpCoreContext;
import org.jboss.logging.Logger;

@Path("/as2")
@ApplicationScoped
public class As2Resource {

    public static String CLIENT_PORT_PARAMETER = As2Resource.class.getSimpleName() + "-client-port";
    public static String SERVER_PORT_PARAMETER = As2Resource.class.getSimpleName() + "-server-port";

    private static final Logger LOG = Logger.getLogger(As2Resource.class);

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    ConsumerTemplate consumerTemplate;

    @Path("/client")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ClientResult client(Request request) throws Exception {
        LOG.infof("Sending to as2: %s", request.getHeaders());
        final Object response = producerTemplate.requestBodyAndHeaders("as2-client://client/send?inBody=ediMessage",
                request.getEdiMessage(), request.collectHeaders());
        ClientResult clientResult = new ClientResult();
        if (response instanceof DispositionNotificationMultipartReportEntity) {
            clientResult.setDispositionNotificationMultipartReportEntity(true);
            clientResult.setPartsCount(((DispositionNotificationMultipartReportEntity) response).getPartCount());
            if (clientResult.getPartsCount() > 1) {
                clientResult.setSecondPartClassName(
                        ((DispositionNotificationMultipartReportEntity) response).getPart(1).getClass().getSimpleName());
            }
        }

        LOG.infof("Got response from as2: %s", response);
        return clientResult;
    }

    @Path("/server")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResult server() throws Exception {
        LOG.info("Receiving from as2.");
        Exchange exchange = consumerTemplate.receive("as2-server://server/listen?requestUriPattern=/");
        if (exchange == null) {
            return null;
        }
        ServerResult serverResult = new ServerResult();
        serverResult.setResult(exchange.getIn().getBody(String.class));
        HttpCoreContext coreContext = exchange.getProperty(AS2Constants.AS2_INTERCHANGE, HttpCoreContext.class);
        if (coreContext != null) {
            serverResult.setRequestClass(coreContext.getRequest().getClass().getSimpleName());
        }
        return serverResult;
    }
}
