/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.connectivity.rest.sender

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable

import javax.inject.Inject
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.connectivity.message.cache.GCMCache
import org.jboss.aerogear.connectivity.model.AndroidVariant
import org.jboss.aerogear.connectivity.model.InstallationImpl
import org.jboss.aerogear.connectivity.model.PushApplication
import org.jboss.aerogear.connectivity.model.SimplePushVariant
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.impl.enricher.resource.ArquillianResourceTestEnricher;
import org.jboss.connectivity.common.AndroidVariantUtils
import org.jboss.connectivity.common.AuthenticationUtils
import org.jboss.connectivity.common.InstallationUtils
import org.jboss.connectivity.common.PushApplicationUtils
import org.jboss.connectivity.common.PushNotificationSenderUtils
import org.jboss.connectivity.common.SimplePushVariantUtils
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter

import spock.lang.Shared
import spock.lang.Specification

import com.google.android.gcm.server.Sender
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration


@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils, AndroidVariantUtils,
    SimplePushVariantUtils, InstallationUtils, PushNotificationSenderUtils])
class PushNotificationSenderEndpointSpecification extends Specification {

    private final String ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSAQ__1";

    private final String ANDROID_VARIANT_NAME = "AndroidVariant__1";

    private final String ANDROID_VARIANT_DESC = "awesome variant__1";

    private final String AUTHORIZED_LOGIN_NAME = "admin";

    private final String AUTHORIZED_PASSWORD = "123";

    private final String PUSH_APPLICATION_NAME = "TestPushApplication__1";

    private final String PUSH_APPLICATION_DESC = "awesome app__1";

    private final String ANDROID_DEVICE_TOKEN = "gsmToken__1";

    private final String ANDROID_DEVICE_TOKEN_2 = "gsmToken__2";

    private final String ANDROID_DEVICE_OS = "ANDROID";

    private final String ANDROID_DEVICE_TYPE = "AndroidTablet";

    private final String ANDROID_DEVICE_TYPE_2 = "AndroidPhone";

    private final String ANDROID_DEVICE_OS_VERSION = "4.2.2";

    private final String ANDROID_CLIENT_ALIAS = "qa_1@aerogear";

    private final String ANDROID_CLIENT_ALIAS_2 = "qa_2@mobileteam";

    private final String SIMPLE_PUSH_VARIANT_NAME = "SimplePushVariant__1";

    private final String SIMPLE_PUSH_VARIANT_DESC = "awesome variant__1";

    private final String SIMPLE_PUSH_VARIANT_NETWORK_URL = "http://localhost:7777/endpoint/";

    private final String SIMPLE_PUSH_DEVICE_TOKEN = "simplePushToken__1";

    private final String SIMPLE_PUSH_DEVICE_TOKEN_2 = "simplePushToken__2";

    private final String SIMPLE_PUSH_DEVICE_TYPE = "Phone";
    
    private final String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    def @Shared static URL root = new URL("http://localhost:8080/ag-push/")

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        def unifiedPushServerPom = System.getProperty("unified.push.server.location", "pom.xml")

        WebArchive war = ShrinkWrap.create(MavenImporter.class).loadPomFromFile(unifiedPushServerPom).importBuildOutput()
                .as(WebArchive.class)

        war.delete("/WEB-INF/lib/gcm-server-1.0.2.jar")

        war.delete("/WEB-INF/classes/META-INF/persistence.xml")
        war.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
        
        war.addClasses(
            AuthenticationUtils.class,
            PushApplicationUtils.class,
            AndroidVariantUtils.class,
            SimplePushVariantUtils.class,
            InstallationUtils.class,
            PushNotificationSenderUtils.class,
            PushNotificationSenderEndpointSpecification.class
        );

        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "gcm-server-1.0.2.jar")
                .addClasses(
                    com.google.android.gcm.server.Result.class,
                    com.google.android.gcm.server.Message.class,
                    com.google.android.gcm.server.MulticastResult.class,
                    com.google.android.gcm.server.Message.Builder.class,
                    Sender.class
                );
        war.addAsLibraries(jar)
                
                
        File[] asm = Maven.resolver().resolve("org.ow2.asm:asm:4.1").withoutTransitivity().asFile()
        war = war.addAsLibraries(asm)

        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "org.mockito:mockito-core",
                "com.jayway.restassured:rest-assured",
                "com.jayway.awaitility:awaitility-groovy").withTransitivity().asFile()
        war = war.addAsLibraries(libs)

        return war
    }

    @Shared def authCookies

    @Shared def pushApplicationId

    @Shared def masterSecret

    @Shared def androidVariantId

    @Shared def androidSecret

    @Shared def simplePushVariantId

    @Shared def simplePushSecret

    @RunAsClient
    def "test method"() {
        when:
        authCookies = login("admin", "123").getCookies()
        
        then:
        authCookies != null
    }

    @RunAsClient
    def "Register a push application"() {
        given: "A Push Application"
        PushApplication pushApp = createPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DESC,
                null, null, null)

        when: "Application is registered"
        def response = registerPushApplication(pushApp, authCookies, null)
        def body = response.body().jsonPath()
        pushApplicationId = body.get("pushApplicationID")
        masterSecret = body.get("masterSecret")

        then: "Response code 201 is returned"
        response.statusCode() == Status.CREATED.getStatusCode()

        and: "Push App Id is not null"
        pushApplicationId != null

        and: "Master secret is not null"
        masterSecret != null

        and: "Push App Name is the expected one"
        body.get("name") == PUSH_APPLICATION_NAME
    }

    @RunAsClient
    def "Register an Android Variant"() {
        given: "An Android Variant"
        AndroidVariant variant = createAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC,
                null, null, null, ANDROID_VARIANT_GOOGLE_KEY)

        when: "Android Variant is registered"
        def response = registerAndroidVariant(pushApplicationId, variant, authCookies)
        def body = response.body().jsonPath()
        androidVariantId = body.get("variantID")
        androidSecret = body.get("secret")

        then: "Push Application id is not empty"
        pushApplicationId != null

        and: "Response status code is 201"
        response != null && response.statusCode() == Status.CREATED.getStatusCode()

        and: "Android Variant id is not null"
        androidVariantId != null

        and: "Secret is not empty"
        androidSecret != null
    }

    @RunAsClient
    def "Register a Simple Push Variant"() {
        given: "A SimplePush Variant"
        SimplePushVariant variant = createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME, SIMPLE_PUSH_VARIANT_DESC,
                null, null, null, SIMPLE_PUSH_VARIANT_NETWORK_URL)

        when: "Simple Push Variant is registered"
        def response = registerSimplePushVariant(pushApplicationId, variant, authCookies)
        def body = response.body().jsonPath()
        simplePushVariantId = body.get("variantID")
        simplePushSecret = body.get("secret")

        then: "Push Application id is not empty"
        pushApplicationId != null

        and: "Response status code is 201"
        response != null && response.statusCode() == Status.CREATED.getStatusCode()

        and: "Simple Push Variant id is not null"
        simplePushVariantId != null

        and: "Secret is not empty"
        simplePushSecret != null
    }

    @RunAsClient
    def "Register an installation for an Android device"() {

        given: "An installation for an Android device"
        InstallationImpl androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS, null)

        when: "Installation is registered"
        def response = registerInstallation(androidVariantId, androidSecret, androidInstallation)

        then: "Variant id and secret is not empty"
        androidVariantId != null && androidSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register a second installation for an Android device"() {

        given: "An installation for an Android device"
        InstallationImpl androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN_2, ANDROID_DEVICE_TYPE_2,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS_2, null)

        when: "Installation is registered"
        def response = registerInstallation(androidVariantId, androidSecret, androidInstallation)

        then: "Variant id and secret is not empty"
        androidVariantId != null && androidSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register an installation for a Simple Push device"() {

        given: "An installation for a Simple Push device"
        InstallationImpl simplePushInstallation = createInstallation(SIMPLE_PUSH_DEVICE_TOKEN, SIMPLE_PUSH_DEVICE_TYPE,
                "", "", "", "")

        when: "Installation is registered"
        def response = registerInstallation(simplePushVariantId, simplePushSecret, simplePushInstallation)

        then: "Variant id and secret is not empty"
        simplePushVariantId != null && simplePushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register a second installation for a Simple Push device"() {

        given: "An installation for a Simple Push device"
        InstallationImpl simplePushInstallation = createInstallation(SIMPLE_PUSH_DEVICE_TOKEN_2, SIMPLE_PUSH_DEVICE_TYPE,
                "", "", "", "")

        when: "Installation is registered"
        def response = registerInstallation(simplePushVariantId, simplePushSecret, simplePushInstallation)

        then: "Variant id and secret is not empty"
        simplePushVariantId != null && simplePushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Selective send to aliases"() {

        given: "A List of aliases"
        List<String> aliases = new ArrayList<String>();
        aliases.add(ANDROID_CLIENT_ALIAS);
        aliases.add(ANDROID_CLIENT_ALIAS_2);

        and: "A message"
        Map<String, Object> messages = new HashMap<String, Object>()
        messages.put("alert", NOTIFICATION_ALERT_MSG)

        when: "Selective send to aliases"
        def response = selectiveSend(pushApplicationId, masterSecret, aliases, null, messages, null)

        then: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "verify that GCM notifications were sent"() {
        
        expect: "Custom GCM Sender send is called with 2 token ids"
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return Sender.gcmRegIdsList != null && Sender.gcmRegIdsList.size() == 2; // The condition that must be fulfilled
                    }
                }
        );
        
        and: "The list contains 2 registration token ids"
        Sender.gcmRegIdsList.size() == 2
        
        and: "The list contains the correct token ids"
        Sender.gcmRegIdsList.contains(ANDROID_DEVICE_TOKEN) && Sender.gcmRegIdsList.contains(ANDROID_DEVICE_TOKEN_2)
        
        and: "The message sent is the correct one"
        Sender.gcmMessage != null && NOTIFICATION_ALERT_MSG.equals(Sender.gcmMessage.getData().get("alert"))
    }
}
