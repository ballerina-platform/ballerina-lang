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

import org.ballerinalang.auth.ldap.CommonLDAPConfiguration;
import org.ballerinalang.auth.ldap.LDAPConnectionContext;
import org.ballerinalang.auth.ldap.LDAPConstants;
import org.ballerinalang.auth.ldap.UserStoreException;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Initializes LDAP connection context.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "auth",
        functionName = "initLDAPConnectionContext",
        args = {@Argument(name = "ldapAuthStoreProvider",
                type = TypeKind.OBJECT,
                structType = "LDAPAuthStoreProvider")},
        isPublic = true)
public class InitLDAPConnectionContext extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        BMap<String, BValue> authStore = ((BMap<String, BValue>) context.getRefArgument(0));
        BMap<String, BValue> configBStruct =
                (BMap<String, BValue>) authStore.get(LDAPConstants.LDAP_AUTH_PROVIDER_CONFIG);
        Struct authProviderConfig = BLangConnectorSPIUtil.toStruct(configBStruct);
        CommonLDAPConfiguration commonLDAPConfiguration = new CommonLDAPConfiguration();

        commonLDAPConfiguration.setDomainName(authProviderConfig.getStringField(LDAPConstants.DOMAIN_NAME));
        commonLDAPConfiguration.setConnectionURL(authProviderConfig.getStringField(LDAPConstants.CONNECTION_URL));
        commonLDAPConfiguration.setConnectionName(authProviderConfig.getStringField(LDAPConstants.CONNECTION_NAME));
        commonLDAPConfiguration.setConnectionPassword(
                authProviderConfig.getStringField(LDAPConstants.CONNECTION_PASSWORD));

        commonLDAPConfiguration.setUserSearchBase(authProviderConfig.getStringField(LDAPConstants.USER_SEARCH_BASE));
        commonLDAPConfiguration.setUserEntryObjectClass(
                authProviderConfig.getStringField(LDAPConstants.USER_ENTRY_OBJECT_CLASS));
        commonLDAPConfiguration.setUserNameAttribute(
                authProviderConfig.getStringField(LDAPConstants.USER_NAME_ATTRIBUTE));
        commonLDAPConfiguration.setUserNameSearchFilter(
                authProviderConfig.getStringField(LDAPConstants.USER_NAME_SEARCH_FILTER));
        commonLDAPConfiguration.setUserNameListFilter(
                authProviderConfig.getStringField(LDAPConstants.USER_NAME_LIST_FILTER));

        commonLDAPConfiguration.setGroupSearchBase(authProviderConfig.getStringField(LDAPConstants.GROUP_SEARCH_BASE));
        commonLDAPConfiguration.setGroupEntryObjectClass(
                authProviderConfig.getStringField(LDAPConstants.GROUP_ENTRY_OBJECT_CLASS));
        commonLDAPConfiguration.setGroupNameAttribute(
                authProviderConfig.getStringField(LDAPConstants.GROUP_NAME_ATTRIBUTE));
        commonLDAPConfiguration.setGroupNameSearchFilter(
                authProviderConfig.getStringField(LDAPConstants.GROUP_NAME_SEARCH_FILTER));
        commonLDAPConfiguration.setGroupNameListFilter(
                authProviderConfig.getStringField(LDAPConstants.GROUP_NAME_LIST_FILTER));

        commonLDAPConfiguration.setMembershipAttribute(
                authProviderConfig.getStringField(LDAPConstants.MEMBERSHIP_ATTRIBUTE));
        commonLDAPConfiguration.setUserRolesCacheEnabled(
                authProviderConfig.getBooleanField(LDAPConstants.USER_ROLE_CACHE_ENABLE));
        commonLDAPConfiguration.setConnectionPoolingEnabled(
                authProviderConfig.getBooleanField(LDAPConstants.CONNECTION_POOLING_ENABLED));
        commonLDAPConfiguration.setLdapConnectionTimeout(
                (int) authProviderConfig.getIntField(LDAPConstants.CONNECTION_TIME_OUT));
        commonLDAPConfiguration.setReadTimeout(
                (int) authProviderConfig.getIntField(LDAPConstants.READ_TIME_OUT));
        commonLDAPConfiguration.setRetryAttempts((int) authProviderConfig.getIntField(LDAPConstants.RETRY_ATTEMPTS));
        authStore.addNativeData(LDAPConstants.LDAP_CONFIGURATION, commonLDAPConfiguration);

        try {
            LDAPConnectionContext connectionSource = new LDAPConnectionContext(commonLDAPConfiguration);
            authStore.addNativeData(LDAPConstants.LDAP_CONNECTION_SOURCE, connectionSource);
            context.setReturnValues();
        } catch (UserStoreException e) {
            context.setReturnValues(BLangVMErrors.createError(context, e.getMessage()));
        }
    }
}
