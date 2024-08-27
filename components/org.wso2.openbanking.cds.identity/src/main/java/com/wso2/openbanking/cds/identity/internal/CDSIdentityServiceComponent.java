/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.openbanking.cds.identity.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.oauth.event.OAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthenticator;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.openbanking.cds.identity.authenticator.CDSArrangementPrivateKeyJWTClientAuthenticator;
import org.wso2.openbanking.cds.identity.authenticator.CDSIntrospectionPrivateKeyJWTClientAuthenticator;
import org.wso2.openbanking.cds.identity.authenticator.CDSPARPrivateKeyJWTClientAuthenticator;
import org.wso2.openbanking.cds.identity.authenticator.CDSRevocationPrivateKeyJWTClientAuthenticator;
import org.wso2.openbanking.cds.identity.authenticator.CDSTokenPrivateKeyJWTClientAuthenticator;
import org.wso2.openbanking.cds.identity.listener.CDSTokenIntrospectionListener;
import org.wso2.openbanking.cds.identity.listener.CDSTokenIssueListener;


/**
 * Identity open banking common data holder.
 */
@Component(
        name = "org.wso2.openbanking.cds.identity.internal.CDSIdentityServiceComponent",
        immediate = true
)
public class CDSIdentityServiceComponent {

    private static final Log log = LogFactory.getLog(CDSIdentityServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        log.debug("Registering CDS related Identity services.");
        bundleContext.registerService(OAuthClientAuthenticator.class.getName(),
                new CDSRevocationPrivateKeyJWTClientAuthenticator(), null);
        bundleContext.registerService(OAuthClientAuthenticator.class.getName(),
                new CDSIntrospectionPrivateKeyJWTClientAuthenticator(), null);
        bundleContext.registerService(OAuthClientAuthenticator.class.getName(),
                new CDSPARPrivateKeyJWTClientAuthenticator(), null);
        bundleContext.registerService(OAuthClientAuthenticator.class.getName(),
                new CDSArrangementPrivateKeyJWTClientAuthenticator(), null);
        bundleContext.registerService(OAuthClientAuthenticator.class.getName(),
                new CDSTokenPrivateKeyJWTClientAuthenticator(), null);
        bundleContext.registerService(OAuthEventInterceptor.class.getName(),
                new CDSTokenIssueListener(), null);
        bundleContext.registerService(OAuthEventInterceptor.class.getName(),
                new CDSTokenIntrospectionListener(), null);
    }

    @Reference(
            name = "realm.service",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service");
        CDSIdentityDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("UnSetting the Realm Service");
        CDSIdentityDataHolder.getInstance().setRealmService(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Open banking CDS Identity Service Component is deactivated");
    }

    @Reference(name = "oauth.client.authn.service",
            service = OAuthClientAuthnService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuthClientAuthnService"
    )
    protected void setOAuthClientAuthnService(OAuthClientAuthnService oAuthClientAuthnService) {
        CDSIdentityDataHolder.getInstance().setOAuthClientAuthnService(oAuthClientAuthnService);
    }

    protected void unsetOAuthClientAuthnService(OAuthClientAuthnService oAuthClientAuthnService) {
        CDSIdentityDataHolder.getInstance().setOAuthClientAuthnService(null);
    }
}
