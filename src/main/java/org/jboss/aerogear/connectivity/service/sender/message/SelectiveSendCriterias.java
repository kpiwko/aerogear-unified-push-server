/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.aerogear.connectivity.service.sender.message;

import java.util.List;
import java.util.Map;

/**
 * Simple class, containing all "query criteria" options for a message,
 * that has been sent to the "Selective Send" HTTP endpoint.
 */
public class SelectiveSendCriterias {

    private final List<String> aliases;
    private final List<String> deviceTypes;

    @SuppressWarnings("unchecked")
    public SelectiveSendCriterias(Map<String, Object> data) {
        this.aliases = (List<String>) data.remove("alias");
        this.deviceTypes = (List<String>) data.remove("deviceType");
    }

    public List<String> getAliases() {
        return aliases;
    }
    
    public List<String> getDeviceTypes() {
        return deviceTypes;
    }
}
