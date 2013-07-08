package org.jboss.aerogear.pushee.tests.json

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import spock.lang.Specification


class JsonParserSpecification extends Specification {

    def "JSON map and non-map syntax"() {
        given: "Two JSON builders"
        def json = new JsonBuilder();
        def json2 = new JsonBuilder();

        when: "Creating a JSON using map and non-map syntax"
        def string1 = JsonOutput.toJson(json.call() {
            foo: "bar"
            name: "namebar"
        });

        def string2 = JsonOutput.toJson(json {
            foo "bar"
            name "namebar"
        });

        then: "Map syntax yields empty JSON object"
        string1 == "{}"

        and: "Non-map syntax yields correct JSON object"
        string2 == '{"foo":"bar","name":"namebar"}'
    }
}
