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
package org.jboss.aerogear.connectivity;

import java.util.List;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import junit.framework.Assert;

import org.jboss.aerogear.connectivity.api.MobileVariant;
import org.jboss.aerogear.connectivity.jpa.PersistentObject;
import org.jboss.aerogear.connectivity.jpa.dao.PushApplicationDao;
import org.jboss.aerogear.connectivity.jpa.dao.impl.PushApplicationDaoImpl;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SimpleTest {

    @Deployment
    public static Archive<?> testArchive() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
            .addClasses(PushApplicationDao.class, PushApplicationDaoImpl.class) // tested classes
            .addPackage(PushApplication.class.getPackage()) // model
            .addPackage(PersistentObject.class.getPackage()) // jpa
            .addPackage(MobileVariant.class.getPackage()) // api
            .addAsManifestResource("META-INF/beans.xml", "beans.xml")
            .addAsResource("META-INF/persistence.xml");

        // System.out.println(jar.toString(true));

        return jar;

    }

    @Produces
    @PersistenceContext(unitName = "pushee-default", type = PersistenceContextType.EXTENDED)
    @Default
    private EntityManager entityManager;

    @Inject
    PushApplicationDao pushAppDao;

    @Test
    public void testPushAppDao() {
        Assert.assertNotNull("DAO was injected", pushAppDao);

        List<PushApplication> apps = pushAppDao.findAll();

        Assert.assertNotNull("Apps list in not null", apps);
        Assert.assertEquals("Zero apps were registered", 0, apps.size());
    }

    @UsingDataSet("pushapps.yml")
    @Test
    // FIXME if running in default (COMMIT) mode, APE fails to find the transaction to commit
    @Transactional(value = TransactionMode.DISABLED)
    public void testPushAppWithData() {
        Assert.assertNotNull("DAO was injected", pushAppDao);

        List<PushApplication> apps = pushAppDao.findAll();

        Assert.assertNotNull("Apps list in not null", apps);
        Assert.assertEquals("One app was registered", 1, apps.size());
    }
}
