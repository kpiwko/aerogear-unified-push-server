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
package org.jboss.aerogear.connectivity.rest.sender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class APNsTestCounter {

    public static final APNsTestCounter INSTANCE = newInstance();

    private Collection<String> tokensList;

    private String alert;

    private String sound;

    private int badge;

    private APNsTestCounter() {
        this.tokensList = new ArrayList<String>();
        this.badge = -1;
    }

    private static APNsTestCounter newInstance() {
        return new APNsTestCounter();
    }

    @SuppressWarnings("unchecked")
    public synchronized void count(Collection<String> tokens, String message) {
        if (tokens != null) {
            tokensList.addAll(tokens);
        }

        if (message != null) {
            Map<Object, Object> map;
            try {
                map = (Map<Object, Object>) new JSONParser().parse(message);
                Map<Object, Object> messageMap = (Map<Object, Object>) map.get("aps");

                alert = (String) messageMap.get("alert");
                sound = (String) messageMap.get("sound");
                badge = ((Long) messageMap.get("badge")).intValue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAlert() {
        return alert;
    }

    public int getBadge() {
        return badge;
    }

    public String getSound() {
        return sound;
    }

    public Collection<String> getTokensList() {
        return tokensList;
    }
}
