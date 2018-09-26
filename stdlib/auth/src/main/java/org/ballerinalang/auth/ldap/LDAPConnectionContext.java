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
package org.ballerinalang.auth.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.auth.ldap.nativeimpl.InitLDAPConnectionContext;
import org.ballerinalang.auth.ldap.util.LDAPUtils;
import org.ballerinalang.auth.ldap.util.Secret;

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * LDAP connection context representation.
 */
public class LDAPConnectionContext {

    private static final Log LOG = LogFactory.getLog(InitLDAPConnectionContext.class);
    private Hashtable environment;

    public LDAPConnectionContext(CommonLDAPConfiguration ldapConfiguration) throws UserStoreException {

        String connectionURL = ldapConfiguration.getConnectionURL();

        String connectionName = ldapConfiguration.getConnectionName();
        String connectionPassword = ldapConfiguration.getConnectionPassword();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Connection Name :: " + connectionName + ", Connection URL :: " + connectionURL);
        }

        environment = new Hashtable();

        environment.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");

        if (connectionName != null) {
            environment.put(javax.naming.Context.SECURITY_PRINCIPAL, connectionName);
        }

        if (connectionPassword != null) {
            environment.put(javax.naming.Context.SECURITY_CREDENTIALS, connectionPassword);
        }

        if (connectionURL != null) {
            environment.put(javax.naming.Context.PROVIDER_URL, connectionURL);
        }

        // Enable connection pooling if property is set in user-mgt.xml
        boolean isLDAPConnectionPoolingEnabled =
                ldapConfiguration.isConnectionPoolingEnabled();

        environment.put("com.sun.jndi.ldap.connect.pool", isLDAPConnectionPoolingEnabled ? "true" : "false");

        // Set connect timeout if provided in configuration. Otherwise set default value
        String connectTimeout = String.valueOf(ldapConfiguration.getLdapConnectionTimeout());
        String readTimeout = String.valueOf(ldapConfiguration.getReadTimeout());
        if (!connectTimeout.trim().isEmpty()) {
            environment.put("com.sun.jndi.ldap.connect.timeout", connectTimeout);
        } else {
            environment.put("com.sun.jndi.ldap.connect.timeout", "5000");
        }

        if (!LDAPUtils.isNullOrEmptyAfterTrim(readTimeout)) {
            environment.put("com.sun.jndi.ldap.read.timeout", readTimeout);
        }
    }

    public DirContext getContext() throws UserStoreException {
        try {
            DirContext context = new InitialDirContext(environment);
            return context;
        } catch (NamingException e) {
            throw new UserStoreException("Error obtaining connection. " + e.getMessage(), e);
        }
    }

    /**
     * Returns the LDAPContext for the given credentials.
     *
     * @param userDN   user DN
     * @param password user password
     * @return returns the LdapContext instance if credentials are valid
     * @throws UserStoreException if there is any exception occurs, while obtaining the secret
     * @throws NamingException    in case of an exception, when obtaining the LDAP context
     */
    public LdapContext getContextWithCredentials(String userDN, Object password)
            throws UserStoreException, NamingException {
        Secret credentialObj = null;
        try {
            credentialObj = Secret.getSecret(password);
            // Create a temp env for this particular authentication session by copying the original env
            Hashtable<String, Object> tempEnv = new Hashtable<>();
            tempEnv.putAll(environment);
            // Replace connection name and password with the passed credentials to this method
            tempEnv.put(javax.naming.Context.SECURITY_PRINCIPAL, userDN);
            tempEnv.put(javax.naming.Context.SECURITY_CREDENTIALS, credentialObj.getBytes());
            return getContextForEnvironmentVariables(tempEnv);
        } catch (UserStoreException e) {
            throw new UserStoreException("Unsupported credential type", e);
        } finally {
            if (credentialObj != null) {
                credentialObj.clear();
            }
        }
    }

    private LdapContext getContextForEnvironmentVariables(Hashtable<?, ?> environment) throws NamingException {
        Hashtable<Object, Object> tempEnv = new Hashtable<>();
        tempEnv.putAll(environment);
        LdapContext context = new InitialLdapContext(tempEnv, null);
        return context;
    }
}
