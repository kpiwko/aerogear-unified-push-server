package org.jboss.aerogear.pushee.tests

import groovy.json.JsonBuilder

import com.jayway.restassured.RestAssured

// FIXME Arquillian Spock testrunner does not support inherritance, investigate and/or report
class AdminLogin {

    def login() {
        assert root !=null

        def json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .body( json {
                    loginName "admin"
                    password "123"
                })
                .expect().statusCode(200)
                .when().post("${root}rest/auth/login")

        response.getDetailedCookies()
    }
}
