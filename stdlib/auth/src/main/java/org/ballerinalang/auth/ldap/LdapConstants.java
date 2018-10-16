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

/**
 * Constants to be used in LDAP user stores.
 *
 * @since 0.983.0
 */
public class LdapConstants {

    public static final String DOMAIN_NAME = "domainName";
    // LDAP connection related constants
    public static final String CONNECTION_URL = "connectionURL";
    public static final String CONNECTION_NAME = "connectionName";
    public static final String CONNECTION_PASSWORD = "connectionPassword";
    public static final String CONNECTION_POOLING_ENABLED = "connectionPoolingEnabled";
    public static final String CONNECTION_TIME_OUT = "ldapConnectionTimeout";
    public static final String READ_TIME_OUT = "readTimeout";
    public static final String RETRY_ATTEMPTS = "retryAttempts";

    // Filter attribute in LDAP auth store configs that filters users by user name
    public static final String USER_NAME_SEARCH_FILTER = "userNameSearchFilter";
    // This property indicates which object class should be used for user entries in LDAP
    public static final String USER_ENTRY_OBJECT_CLASS = "userEntryObjectClass";
    public static final String USER_SEARCH_BASE = "userSearchBase";
    public static final String USER_NAME_LIST_FILTER = "userNameListFilter";
    public static final String USER_NAME_ATTRIBUTE = "userNameAttribute";

    // Groups Related Constants
    public static final String GROUP_SEARCH_BASE = "groupSearchBase";
    public static final String GROUP_NAME_LIST_FILTER = "groupNameListFilter";
    public static final String GROUP_NAME_SEARCH_FILTER = "groupNameSearchFilter";
    public static final String GROUP_NAME_ATTRIBUTE = "groupNameAttribute";
    public static final String GROUP_ENTRY_OBJECT_CLASS = "groupEntryObjectClass";

    public static final String MEMBER_UID = "memberUid";
    public static final String MEMBERSHIP_ATTRIBUTE = "membershipAttribute";
    public static final String USER_ROLE_CACHE_ENABLE = "userRolesCacheEnabled";
    public static final String LDAP_CONFIGURATION = "ldapConfiguration";
    public static final String LDAP_CONNECTION_SOURCE = "connectionSource";
    public static final String LDAP_CONNECTION_CONTEXT = "connectionContext";
    public static final String LDAP_AUTH_PROVIDER_CONFIG = "ldapAuthProviderConfig";
    public static final String ENDPOINT_INSTANCE_ID = "instanceId";
    public static final String SECURE_AUTH_STORE_CONFIG = "secureClientSocket";

    public static final String AUTH_STORE_CONFIG_TRUST_STORE = "trustStore";
    public static final String FILE_PATH = "path";
    public static final String PASSWORD = "password";
    public static final String AUTH_STORE_CONFIG_TRUST_CERTIFICATES = "trustedCertFile";
    public static final String TLS = "TLS";
    public static final String SSL = "ssl";
    public static final String PKCS_STORE_TYPE = "PKCS12";
    public static final String X_509 = "X.509";
    public static final String DEFAULT_CONNECTION_TIME_OUT = "5000";
    public static final String UTF_8_CHARSET = "UTF-8";
    public static final String CERTIFICATE_ALIAS = "ca";
}
