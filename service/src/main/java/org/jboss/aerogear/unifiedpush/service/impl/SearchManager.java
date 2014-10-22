package org.jboss.aerogear.unifiedpush.service.impl;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.jboss.aerogear.unifiedpush.service.PushSearchService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;
import org.jboss.aerogear.unifiedpush.service.annotations.SearchService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;


@RequestScoped
public class SearchManager {

    @Inject @DeltaSpike
    private HttpServletRequest httpServletRequest;

    @Produces
    @SearchService
    public PushSearchService getSearch(@New PushSearchServiceImpl searchAll,
                                              @New PushSearchByDeveloperServiceImpl searchByDeveloper) {

        boolean isAdmin = httpServletRequest.isUserInRole("admin");

        if (isAdmin) {
            return searchAll;
        }
        return searchByDeveloper;
    }

    @Produces
    @LoggedIn
    public String extractUsername() {
        KeycloakPrincipal p = (KeycloakPrincipal) httpServletRequest.getUserPrincipal();
        KeycloakSecurityContext kcSecurityContext = p.getKeycloakSecurityContext();
        return kcSecurityContext.getToken().getPreferredUsername();

    }


}
