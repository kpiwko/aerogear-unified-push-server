package org.jboss.aerogear.pushee.tests

import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter

class Deployments {

    def static WebArchive unifiedPushServer() {

        def unifiedPushServerPom = System.getProperty("unified.push.server.location", "pom.xml")

        return ShrinkWrap.create(MavenImporter.class).loadPomFromFile(unifiedPushServerPom).importBuildOutput()
        .as(WebArchive.class);
    }
}

