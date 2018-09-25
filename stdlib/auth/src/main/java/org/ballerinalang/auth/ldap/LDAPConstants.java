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
 */
public class LDAPConstants {

    public static final String DOMAIN_NAME = "domainName";
    public static final String CONNECTION_URL = "connectionURL";
    public static final String CONNECTION_NAME = "connectionName";
    public static final String CONNECTION_PASSWORD = "connectionPassword";
    public static final String USER_SEARCH_BASE = "userSearchBase";
    public static final String GROUP_SEARCH_BASE = "groupSearchBase";
    public static final String USER_NAME_LIST_FILTER = "userNameListFilter";
    public static final String USER_NAME_ATTRIBUTE = "userNameAttribute";
    //Property that defines the status of the referral to be used:
    public static final String RETRY_ATTEMPTS = "retryAttempts";
    public static final String MEMBER_UID = "memberUid";


    //filter attribute in user-mgt.xml that filters users by user name
    public static final String USER_NAME_SEARCH_FILTER = "userNameSearchFilter";
    //this property indicates which object class should be used for user entries in LDAP
    public static final String USER_ENTRY_OBJECT_CLASS = "userEntryObjectClass";
    // roles
    public static final String GROUP_NAME_LIST_FILTER = "groupNameListFilter";
    public static final String GROUP_NAME_SEARCH_FILTER = "groupNameSearchFilter";
    public static final String GROUP_NAME_ATTRIBUTE = "groupNameAttribute";
    @Deprecated
    public static final String READ_LDAP_GROUPS = "readLDAPGroups";
    @Deprecated
    public static final String WRITE_EXTERNAL_ROLES = "writeLDAPGroups";
    public static final String MEMBERSHIP_ATTRIBUTE = "membershipAttribute";
    public static final String USER_ROLE_CACHE_ENABLE = "userRolesCacheEnabled";
    //ldap glossary
    public static final String GROUP_ENTRY_OBJECT_CLASS = "groupEntryObjectClass";

    public static final String CONNECTION_POOLING_ENABLED = "connectionPoolingEnabled";

    public static final String CONNECTION_TIME_OUT = "ldapConnectionTimeout";
    public static final String READ_TIME_OUT = "readTimeout";

    public static final String LDAP_CONFIGURATION = "ldapConfiguration";
    public static final String LDAP_CONNECTION_SOURCE = "connectionSource";
    public static final String LDAP_AUTH_PROVIDER_CONFIG = "ldapAuthProviderConfig";

}
