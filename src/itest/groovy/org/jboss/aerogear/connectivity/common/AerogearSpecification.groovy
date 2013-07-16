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
package org.jboss.aerogear.connectivity.common

import groovy.json.JsonBuilder
import groovy.lang.Closure;

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.osgi.framework.internal.SystemBundleContext;
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification

import com.jayway.restassured.RestAssured
import com.jayway.restassured.config.DecoderConfig
import com.jayway.restassured.config.EncoderConfig
import com.jayway.restassured.config.RestAssuredConfig
import com.jayway.restassured.filter.log.RequestLoggingFilter
import com.jayway.restassured.filter.log.ResponseLoggingFilter
import com.jayway.restassured.response.Cookies;



class AerogearSpecification extends Specification {

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    // contains URL to the deployment application
    @ArquillianResource
    URL root

    // contains cookies with authorization
    @Shared
    def authCookies

    // enable direct invocation of json closure to all test writers
    // to construct Json objects in very simple DSL like way
    //
    // json {
    //   name "ddd"
    //   description "ddd"
    // }
    //
    def Closure json = new Closure(this, this) {
        def doCall(Object args) {
            new JsonBuilder().call(args)
        }
    }

    def setupSpec() {
        // enable logging
        def debugFlag = System.getProperty("debug", "true")
        if(debugFlag) {
            //RestAssured.filters(new RequestLoggingFilter(new LoggerPrintStream()), new ResponseLoggingFilter(new LoggerPrintStream()))
            RestAssured.filters(new RequestLoggingFilter(System.err), new ResponseLoggingFilter(System.err))
        }

        // RestAssured uses ISO-8859-1 by default to encode all the stuff, this is not the same as curl does
        // so we are changing RestAssuredConfiguration in order to change encoded/decoder config
        // see https://code.google.com/p/rest-assured/wiki/ReleaseNotes16
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
    }


    // authcookies setup happens in setup() instead of setupSpec(), as root is not available before deployment
    def setup() {
        authCookies = authCookies ? authCookies : login()
    }

    //    curl -v -b cookies.txt -c cookies.txt
    //    -H "Accept: application/json" -H "Content-type: application/json"
    //    -X POST -d '{"loginName": "admin", "password":"123"}'
    //    http://localhost:8080/ag-push/rest/auth/login
    def login() {
        assert root !=null
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .body( json() {
                    loginName "admin"
                    password "123"
                })
                .expect().statusCode(200)
                .when().post("${root}rest/auth/login")

        response.getDetailedCookies()
    }
}
