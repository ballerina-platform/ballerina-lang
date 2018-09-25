/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.auth.ldap.nativeimpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.auth.ldap.CommonLDAPConfiguration;
import org.ballerinalang.auth.ldap.LDAPConnectionContext;
import org.ballerinalang.auth.ldap.LDAPConstants;
import org.ballerinalang.auth.ldap.UserStoreException;
import org.ballerinalang.auth.ldap.util.LDAPUtils;
import org.ballerinalang.auth.ldap.util.Secret;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * Ballerina function to authenticate users with LDAP user store.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "auth",
        functionName = "LDAPAuthStoreProvider.doAuthenticate",
        args = {@Argument(name = "username", type = TypeKind.STRING),
                @Argument(name = "password", type = TypeKind.STRING)
        },
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true)
public class Authenticate extends BlockingNativeCallableUnit {

    private static final Log LOG = LogFactory.getLog(Authenticate.class);
    private LDAPConnectionContext connectionSource;
    private CommonLDAPConfiguration ldapConfiguration;

    @Override
    public void execute(Context context) {

        BMap<String, BValue> authStore = ((BMap<String, BValue>) context.getRefArgument(0));
        connectionSource = (LDAPConnectionContext) authStore.getNativeData(LDAPConstants.LDAP_CONNECTION_SOURCE);
        ldapConfiguration = (CommonLDAPConfiguration) authStore.getNativeData(LDAPConstants.LDAP_CONFIGURATION);

        String userName = context.getStringArgument(0);
        String credential = context.getStringArgument(1);

        if (userName == null || credential == null) {
            context.setReturnValues(new BBoolean(false));
            throw new BallerinaException("username or credential value is null.");
        }

        userName = userName.trim();
        Secret credentialObj = null;

        try {
            credentialObj = Secret.getSecret(credential);
            if (userName.isEmpty() || credentialObj.isEmpty()) {
                throw new BallerinaException("username or credential value is empty.");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Authenticating user " + userName);
            }
            boolean bValue = false;
            String name = null;

            name = LDAPUtils.getNameInSpaceForUsernameFromLDAP(userName, ldapConfiguration, this.connectionSource);
            if (name != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authenticating with " + name);
                }
                bValue = this.bindAsUser(userName, name, credentialObj);
            }
            context.setReturnValues(new BBoolean(bValue));
        } catch (NamingException e) {
            String errorMessage = "Cannot bind user : " + userName;
            if (LOG.isDebugEnabled()) {
                LOG.debug(errorMessage, e);
            }
            //TODO: Return errors when auth framework support returning errors.
            throw new BallerinaException(errorMessage, e);
        } catch (UserStoreException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
            throw new BallerinaException(e.getMessage(), e);
        } finally {
            if (credentialObj != null) {
                credentialObj.clear();
            }
        }
    }

    private boolean bindAsUser(String userName, String dn, Object credentials)
            throws NamingException, UserStoreException {

        boolean isAuthed = false;
        boolean debug = LOG.isDebugEnabled();
        LdapContext cxt = null;
        try {
            cxt = this.connectionSource.getContextWithCredentials(dn, credentials);
            isAuthed = true;
        } catch (AuthenticationException e) {
            if (debug) {
                LOG.debug("Authentication failed " + e);
                LOG.debug("Clearing cache for DN: " + dn);
            }
        } finally {
            LDAPUtils.closeContext(cxt);
        }

        if (debug) {
            LOG.debug("User: " + dn + " is authnticated: " + isAuthed);
        }
        return isAuthed;
    }
}
