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
import java.util.Collections;
import java.util.List;

import com.google.android.gcm.server.Message;

public class GCMTestCounter {

    public static final GCMTestCounter INSTANCE = newInstance();

    private List<String> gcmRegIdsList;

    private Message gcmMessage;

    private GCMTestCounter() {
        this.gcmRegIdsList = new ArrayList<String>();
    }

    private static GCMTestCounter newInstance() {
        return new GCMTestCounter();
    }

    public synchronized void count(Message message, List<String> regIds) {
        if (regIds != null && !regIds.isEmpty()) {
            gcmRegIdsList = new ArrayList<String>();
            gcmRegIdsList.addAll(regIds);
        }
        if (message != null) {
            gcmMessage = message;
        }
    }

    public List<String> getRegIdsList() {
        return Collections.unmodifiableList(gcmRegIdsList);
    }

    public Message getMessage() {
        return gcmMessage;
    }
}
