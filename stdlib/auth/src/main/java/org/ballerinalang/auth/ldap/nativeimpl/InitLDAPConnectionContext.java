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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.auth.ldap.CommonLDAPConfiguration;
import org.ballerinalang.auth.ldap.LDAPConnectionContext;
import org.ballerinalang.auth.ldap.LDAPConstants;
import org.ballerinalang.auth.ldap.UserStoreException;
import org.ballerinalang.auth.ldap.util.LDAPUtils;
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
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;

import javax.naming.directory.DirContext;

import static org.ballerinalang.auth.ldap.LDAPConstants.AUTH_STORE_CONFIG_TRUST_CERTIFICATES;
import static org.ballerinalang.auth.ldap.LDAPConstants.AUTH_STORE_CONFIG_TRUST_STORE;
import static org.ballerinalang.auth.ldap.LDAPConstants.FILE_PATH;
import static org.ballerinalang.auth.ldap.LDAPConstants.PASSWORD;

/**
 * Initializes LDAP connection context.
 *
 * @since 0.983.0
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

        Struct sslConfig = authProviderConfig.getStructField(LDAPConstants.SECURE_AUTH_STORE_CONFIG);

        if (sslConfig != null) {
            setSslConfig(sslConfig, commonLDAPConfiguration);
        }

        try {
            LDAPConnectionContext connectionSource = new LDAPConnectionContext(commonLDAPConfiguration);
            DirContext dirContext = connectionSource.getContext();
            authStore.addNativeData(LDAPConstants.LDAP_CONFIGURATION, commonLDAPConfiguration);
            authStore.addNativeData(LDAPConstants.LDAP_CONNECTION_SOURCE, connectionSource);
            authStore.addNativeData(LDAPConstants.LDAP_CONNECTION_CONTEXT, dirContext);
            context.setReturnValues();
        } catch (UserStoreException e) {
            context.setReturnValues(BLangVMErrors.createError(context, e.getMessage()));
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    private void setSslConfig(Struct sslConfig, CommonLDAPConfiguration commonLDAPConfiguration) {
        Struct trustStore = sslConfig.getStructField(AUTH_STORE_CONFIG_TRUST_STORE);
        String trustCerts = sslConfig.getStringField(AUTH_STORE_CONFIG_TRUST_CERTIFICATES);

        if (trustStore != null) {
            String trustStoreFilePath = trustStore.getStringField(FILE_PATH);
            String trustStorePassword = trustStore.getStringField(PASSWORD);
            commonLDAPConfiguration.setTrustedCertFile(trustStoreFilePath);
            commonLDAPConfiguration.setTrustStorePassword(trustStorePassword);

            if (trustStoreFilePath != null) {
                File trustStoreFile = new File(LDAPUtils.substituteVariables(trustStoreFilePath));
                if (!trustStoreFile.exists()) {
                    throw new IllegalArgumentException("trustStore File " + trustStoreFilePath + " not found");
                }
                if (trustStorePassword == null) {
                    throw new IllegalArgumentException("trustStorePass is not defined for HTTPS scheme");
                }
                commonLDAPConfiguration.setTrustStore(trustStoreFile);
                System.setProperty(LDAPConstants.LDAP_TRUST_STORE_FILE_PATH, trustStoreFilePath);
                System.setProperty(LDAPConstants.LDAP_TRUST_STORE_PASSWORD, trustStorePassword);
            }
        } else if (StringUtils.isNotBlank(trustCerts)) {
            commonLDAPConfiguration.setTrustedCertFile(trustCerts);
            System.setProperty(LDAPConstants.LDAP_TRUST_STORE_TRUST_CERTIFICATES, trustCerts);
        }
    }
}
