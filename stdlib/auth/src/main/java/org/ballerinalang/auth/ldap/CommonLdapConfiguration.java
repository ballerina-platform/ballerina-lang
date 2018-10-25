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

import java.util.List;

/**
 * Ballerina LDAP user store configurations.
 *
 * @since 0.983.0
 */
public class CommonLdapConfiguration extends SslTrustConfiguration {

    private String domainName;
    private String connectionURL;
    private String connectionName;
    private String connectionPassword;
    private String userSearchBase;
    private String userEntryObjectClass;
    private String userNameAttribute;
    private String userNameSearchFilter;
    private String userNameListFilter;
    private List<String> groupSearchBase;
    private String groupEntryObjectClass;
    private String groupNameAttribute;
    private String groupNameSearchFilter;
    private String groupNameListFilter;
    private String membershipAttribute;
    private boolean userRolesCacheEnabled;
    private boolean connectionPoolingEnabled;
    private int ldapConnectionTimeout;
    private int readTimeout;
    private int retryAttempts;

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public void setUserSearchBase(String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public String getUserEntryObjectClass() {
        return userEntryObjectClass;
    }

    public void setUserEntryObjectClass(String userEntryObjectClass) {
        this.userEntryObjectClass = userEntryObjectClass;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public String getUserNameSearchFilter() {
        return userNameSearchFilter;
    }

    public void setUserNameSearchFilter(String userNameSearchFilter) {
        this.userNameSearchFilter = userNameSearchFilter;
    }

    public String getUserNameListFilter() {
        return userNameListFilter;
    }

    public void setUserNameListFilter(String userNameListFilter) {
        this.userNameListFilter = userNameListFilter;
    }

    public List<String> getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(List<String> groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getGroupEntryObjectClass() {
        return groupEntryObjectClass;
    }

    public void setGroupEntryObjectClass(String groupEntryObjectClass) {
        this.groupEntryObjectClass = groupEntryObjectClass;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public void setGroupNameAttribute(String groupNameAttribute) {
        this.groupNameAttribute = groupNameAttribute;
    }

    public String getGroupNameSearchFilter() {
        return groupNameSearchFilter;
    }

    public void setGroupNameSearchFilter(String groupNameSearchFilter) {
        this.groupNameSearchFilter = groupNameSearchFilter;
    }

    public String getGroupNameListFilter() {
        return groupNameListFilter;
    }

    public void setGroupNameListFilter(String groupNameListFilter) {
        this.groupNameListFilter = groupNameListFilter;
    }

    public String getMembershipAttribute() {
        return membershipAttribute;
    }

    public void setMembershipAttribute(String membershipAttribute) {
        this.membershipAttribute = membershipAttribute;
    }

    public boolean isUserRolesCacheEnabled() {
        return userRolesCacheEnabled;
    }

    public void setUserRolesCacheEnabled(boolean userRolesCacheEnabled) {
        this.userRolesCacheEnabled = userRolesCacheEnabled;
    }

    public boolean isConnectionPoolingEnabled() {
        return connectionPoolingEnabled;
    }

    public void setConnectionPoolingEnabled(boolean connectionPoolingEnabled) {
        this.connectionPoolingEnabled = connectionPoolingEnabled;
    }

    public int getLdapConnectionTimeout() {
        return ldapConnectionTimeout;
    }

    public void setLdapConnectionTimeout(int ldapConnectionTimeout) {
        this.ldapConnectionTimeout = ldapConnectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
}
