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

package org.ballerinalang.stdlib.ldap.nativeimpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.ldap.CommonLdapConfiguration;
import org.ballerinalang.stdlib.ldap.LdapConnectionContext;
import org.ballerinalang.stdlib.ldap.LdapConstants;
import org.ballerinalang.stdlib.ldap.UserStoreException;
import org.ballerinalang.stdlib.ldap.util.LdapUtils;

import java.nio.charset.Charset;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

/**
 * Ballerina function to authenticate users with LDAP user store.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "ldap",
        functionName = "doAuthenticate", isPublic = true)
public class Authenticate {

    private static final Log LOG = LogFactory.getLog(Authenticate.class);
    private static LdapConnectionContext connectionSource;

    public static Object doAuthenticate(Strand strand, MapValue<?, ?> ldapConnection, String userName,
                                        String password) {
        byte[] credential = password.getBytes(Charset.forName(LdapConstants.UTF_8_CHARSET));
        connectionSource = (LdapConnectionContext) ldapConnection.getNativeData(LdapConstants.LDAP_CONNECTION_SOURCE);
        DirContext ldapConnectionContext = (DirContext) ldapConnection.getNativeData(
                LdapConstants.LDAP_CONNECTION_CONTEXT);
        CommonLdapConfiguration ldapConfiguration = (CommonLdapConfiguration) ldapConnection.getNativeData(
                LdapConstants.LDAP_CONFIGURATION);
        LdapUtils.setServiceName((String) ldapConnection.getNativeData(LdapConstants.ENDPOINT_INSTANCE_ID));

        if (LdapUtils.isNullOrEmptyAfterTrim(userName)) {
            return LdapUtils.createError("Username or credential value is empty or null.");
        }

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authenticating user " + userName);
            }
            String name = LdapUtils.getNameInSpaceForUsernameFromLDAP(userName.trim(), ldapConfiguration,
                    ldapConnectionContext);
            if (name != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authenticating with " + name);
                }
                return bindAsUser(name, credential);
            }
            return false;
        } catch (NamingException e) {
            LOG.error("Cannot bind user: " + userName, e);
            return LdapUtils.createError(e.getMessage());
        } catch (UserStoreException e) {
            LOG.error(e.getMessage(), e);
            return LdapUtils.createError(e.getMessage());
        } finally {
            LdapUtils.removeServiceName();
        }
    }

    private static boolean bindAsUser(String dn, byte[] credentials) throws NamingException {
        boolean authenticated;
        LdapContext cxt = null;
        try {
            cxt = connectionSource.getContextWithCredentials(dn, credentials);
            authenticated = true;
        } finally {
            LdapUtils.closeContext(cxt);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("User: " + dn + " is authenticated: " + authenticated);
        }
        return authenticated;
    }
}
