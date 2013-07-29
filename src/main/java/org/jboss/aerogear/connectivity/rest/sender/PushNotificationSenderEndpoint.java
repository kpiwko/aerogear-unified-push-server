/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.connectivity.rest.sender;

import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.rest.security.util.HttpBasicHelper;
import org.jboss.aerogear.connectivity.service.PushApplicationService;
import org.jboss.aerogear.connectivity.service.sender.SenderService;
import org.jboss.aerogear.connectivity.service.sender.message.BroadcastMessage;
import org.jboss.aerogear.connectivity.service.sender.message.SelectiveSendMessage;

@Stateless
@Path("/sender")
@TransactionAttribute
public class PushNotificationSenderEndpoint {

    @Inject
    private Logger logger;
    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private SenderService senderService;

    @POST
    @Path("/broadcast")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response broadcast(final Map<String, Object> message, @Context HttpServletRequest request) {

        final PushApplication pushApplication = loadPushApplicationWhenAuthorized(request);
        if (pushApplication == null) {
            return Response.status(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                    .entity("Unauthorized Request")
                    .build();
        }

        // transform map to service object:
        final BroadcastMessage payload = new BroadcastMessage(message);

        // submitted to @Async EJB:
        senderService.broadcast(pushApplication, payload);
        logger.info("Message submitted to PushNetworks for further processing");

        return Response.status(Status.OK)
                .entity("Job submitted").build();
    }

    @POST
    @Path("/selected")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response selectedSender(final Map<String, Object> message, @Context HttpServletRequest request) {

        final PushApplication pushApplication = loadPushApplicationWhenAuthorized(request);
        if (pushApplication == null) {
            return Response.status(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                    .entity("Unauthorized Request")
                    .build();
        }

        // transform map to service object:
        final SelectiveSendMessage payload = new SelectiveSendMessage(message);

        // submitted to @Async EJB:
        senderService.selectiveSend(pushApplication, payload);
        logger.info("Message submitted to PushNetworks for further processing");

        return Response.status(Status.OK)
                .entity("Job submitted").build();
    }

    /**
     * returns application if the masterSecret is valid for the request PushApplication
     */
    private PushApplication loadPushApplicationWhenAuthorized(HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic header:
        String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        String pushApplicationID = credentials[0];
        String secret = credentials[1];

        final PushApplication pushApplication = pushApplicationService.findByPushApplicationID(pushApplicationID);
        if (pushApplication != null && pushApplication.getMasterSecret().equals(secret)) {
            return pushApplication;
        }

        // unauthorized...
        return null;
    }
}
