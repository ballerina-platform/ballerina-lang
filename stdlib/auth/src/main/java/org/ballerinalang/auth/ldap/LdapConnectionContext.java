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

import org.ballerinalang.auth.ldap.util.LdapUtils;

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import static javax.naming.Context.INITIAL_CONTEXT_FACTORY;
import static javax.naming.Context.SECURITY_AUTHENTICATION;
import static javax.naming.Context.SECURITY_CREDENTIALS;
import static javax.naming.Context.SECURITY_PRINCIPAL;
import static javax.naming.Context.SECURITY_PROTOCOL;

/**
 * LDAP connection context representation.
 *
 * @since 0.983.0
 */
public class LdapConnectionContext {

    private Hashtable environment;

    public LdapConnectionContext(CommonLdapConfiguration ldapConfiguration) {

        String connectionURL = ldapConfiguration.getConnectionURL();
        String connectionName = ldapConfiguration.getConnectionName();
        String connectionPassword = ldapConfiguration.getConnectionPassword();
        environment = new Hashtable();
        environment.put(INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(SECURITY_AUTHENTICATION, "simple");

        if (connectionName != null) {
            environment.put(SECURITY_PRINCIPAL, connectionName);
        }

        if (connectionPassword != null) {
            environment.put(SECURITY_CREDENTIALS, connectionPassword);
        }

        if (connectionURL != null) {
            environment.put(javax.naming.Context.PROVIDER_URL, connectionURL);
        }

        boolean isLDAPConnectionPoolingEnabled = ldapConfiguration.isConnectionPoolingEnabled();
        environment.put("com.sun.jndi.ldap.connect.pool", isLDAPConnectionPoolingEnabled ? "true" : "false");

        if (LdapUtils.isLdapsUrl(connectionURL)) {
            environment.put(SECURITY_PROTOCOL, LdapConstants.SSL);
            environment.put("java.naming.ldap.factory.socket", LdapSslSocketFactory.class.getName());
        }

        // Set connect timeout if provided in configuration. Otherwise set default value
        String connectTimeout = String.valueOf(ldapConfiguration.getLdapConnectionTimeout());
        String readTimeout = String.valueOf(ldapConfiguration.getReadTimeout());
        if (!connectTimeout.trim().isEmpty()) {
            environment.put("com.sun.jndi.ldap.connect.timeout", connectTimeout);
        } else {
            environment.put("com.sun.jndi.ldap.connect.timeout", LdapConstants.DEFAULT_CONNECTION_TIME_OUT);
        }

        if (!LdapUtils.isNullOrEmptyAfterTrim(readTimeout)) {
            environment.put("com.sun.jndi.ldap.read.timeout", readTimeout);
        }
    }

    /**
     * Returns the LDAPContext.
     *
     * @return returns the LdapContext instance
     * @throws NamingException in case of an exception, when obtaining the LDAP context
     */
    public DirContext getContext() throws NamingException {
        return new InitialDirContext(environment);
    }

    /**
     * Returns the LDAPContext for the given credentials.
     *
     * @param userDN   user DN
     * @param password user password
     * @return returns the LdapContext instance if credentials are valid
     * @throws NamingException    in case of an exception, when obtaining the LDAP context
     */
    public LdapContext getContextWithCredentials(String userDN, byte[] password) throws NamingException {
        // Create a temp env for this particular authentication session by copying the original env
        Hashtable<String, Object> tempEnv = new Hashtable<>();
        tempEnv.putAll(environment);
        // Replace connection name and password with the passed credentials to this method
        tempEnv.put(SECURITY_PRINCIPAL, userDN);
        tempEnv.put(SECURITY_CREDENTIALS, password);
        return getContextForEnvironmentVariables(tempEnv);
    }

    private LdapContext getContextForEnvironmentVariables(Hashtable<?, ?> environment) throws NamingException {
        Hashtable<Object, Object> tempEnv = new Hashtable<>();
        tempEnv.putAll(environment);
        LdapContext context = new InitialLdapContext(tempEnv, null);
        return context;
    }
}
