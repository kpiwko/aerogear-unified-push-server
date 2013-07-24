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
package org.jboss.aerogear.connectivity.service;

import java.util.List;
import java.util.Set;

import org.jboss.aerogear.connectivity.model.InstallationImpl;

/**
 * Service class used by the Server to work with Installations
 * for the different Variants. 
 */
public interface ClientInstallationService {

    /**
     * Store a new Installation object on the database.
     */
    InstallationImpl addInstallation(InstallationImpl installation);

    /**
     * Performs an update/merge on the given entity.
     */
    InstallationImpl updateInstallation(InstallationImpl installation);
    
    /**
     * Updates the first argument with the values from the second, and returns the updated entity.
     * @param toUpdate entity to be updated
     * @param postedInstallation entity where we read the "updateable" values from.
     * @return updated entity
     */
    InstallationImpl updateInstallation(InstallationImpl toUpdate, InstallationImpl postedInstallation);

    /**
     * Returns the Installation entity, matching the given primaryKey.
     */
    InstallationImpl findById(String primaryKey);

    /**
     * Removes the given installation entity.
     */
    void removeInstallation(InstallationImpl installation);

    /**
     * Removes all the installation entities in the {@link List}.
     */
    void removeInstallations(List<InstallationImpl> installations);

    /**
     * Used for "feedback service": Collects the invalid Installations for a Variant, based on the identifier tokens:
     */
    void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens);

    /**
     * Used for "Device Registration": loads all installations for one variant, containing the same token
     */
    List<InstallationImpl> findInstallationsForVariantByDeviceToken(String variantID, String deviceToken);

    // ===================   SENDER API   ===================

    /**
     * Used for (Android/iOS) Broadcast. Query for all IDs on a certain variant
     */
    List<String> findAllDeviceTokenForVariantID(String variantID);

    /**
     * Used for (SimplePush) Broadcast. Query for all "Tokens", that are in the (SimplePush) BROADCAST category.
     */
    List<String> findAllSimplePushBroadcastDeviceTokenForVariantID(String variantID);

    /**
     * Used for (Android/iOS) Selective Sender API. Queries the available device-tokens for a given variant, based on provided criteria
     */
    List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, String category, List<String> aliases, List<String> deviceTypes);

    /**
     * Used for (SimplePush) Selective Sender API. Queries the available SimplePush device-tokens(device-type : web) for a given variant, based on provided criteria
     */
    List<String> findAllSimplePushDeviceTokenForVariantIDByCriteria(String variantID, String category, List<String> aliases);
}
