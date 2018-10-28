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
import org.ballerinalang.auth.ldap.CommonLdapConfiguration;
import org.ballerinalang.auth.ldap.LdapConnectionContext;
import org.ballerinalang.auth.ldap.LdapConstants;
import org.ballerinalang.auth.ldap.SslContextTrustManager;
import org.ballerinalang.auth.ldap.util.LdapUtils;
import org.ballerinalang.auth.ldap.util.SslUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.net.ssl.SSLContext;


/**
 * Initializes LDAP connection context.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "auth",
        functionName = "initLdapConnectionContext",
        args = {@Argument(name = "ldapAuthStoreProvider", type = TypeKind.OBJECT, structType = "LdapAuthStoreProvider"),
                @Argument(name = "instanceId", type = TypeKind.STRING)},
        isPublic = true)
public class InitLdapConnectionContext extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String instanceId = context.getStringArgument(0);
        BMap<String, BValue> authStore = (BMap) context.getRefArgument(0);
        BMap<String, BValue> configBStruct =
                (BMap<String, BValue>) authStore.get(LdapConstants.LDAP_AUTH_PROVIDER_CONFIG);
        Struct authProviderConfig = BLangConnectorSPIUtil.toStruct(configBStruct);

        CommonLdapConfiguration commonLdapConfiguration = new CommonLdapConfiguration();

        commonLdapConfiguration.setDomainName(authProviderConfig.getStringField(LdapConstants.DOMAIN_NAME));
        commonLdapConfiguration.setConnectionURL(authProviderConfig.getStringField(LdapConstants.CONNECTION_URL));
        commonLdapConfiguration.setConnectionName(authProviderConfig.getStringField(LdapConstants.CONNECTION_NAME));
        commonLdapConfiguration.setConnectionPassword(
                authProviderConfig.getStringField(LdapConstants.CONNECTION_PASSWORD));

        commonLdapConfiguration.setUserSearchBase(authProviderConfig.getStringField(LdapConstants.USER_SEARCH_BASE));
        commonLdapConfiguration.setUserEntryObjectClass(
                authProviderConfig.getStringField(LdapConstants.USER_ENTRY_OBJECT_CLASS));
        commonLdapConfiguration.setUserNameAttribute(
                authProviderConfig.getStringField(LdapConstants.USER_NAME_ATTRIBUTE));
        commonLdapConfiguration.setUserNameSearchFilter(
                authProviderConfig.getStringField(LdapConstants.USER_NAME_SEARCH_FILTER));
        commonLdapConfiguration.setUserNameListFilter(
                authProviderConfig.getStringField(LdapConstants.USER_NAME_LIST_FILTER));

        commonLdapConfiguration.setGroupSearchBase(getAsStringList(authProviderConfig.
                getArrayField(LdapConstants.GROUP_SEARCH_BASE)));
        commonLdapConfiguration.setGroupEntryObjectClass(
                authProviderConfig.getStringField(LdapConstants.GROUP_ENTRY_OBJECT_CLASS));
        commonLdapConfiguration.setGroupNameAttribute(
                authProviderConfig.getStringField(LdapConstants.GROUP_NAME_ATTRIBUTE));
        commonLdapConfiguration.setGroupNameSearchFilter(
                authProviderConfig.getStringField(LdapConstants.GROUP_NAME_SEARCH_FILTER));
        commonLdapConfiguration.setGroupNameListFilter(
                authProviderConfig.getStringField(LdapConstants.GROUP_NAME_LIST_FILTER));

        commonLdapConfiguration.setMembershipAttribute(
                authProviderConfig.getStringField(LdapConstants.MEMBERSHIP_ATTRIBUTE));
        commonLdapConfiguration.setUserRolesCacheEnabled(
                authProviderConfig.getBooleanField(LdapConstants.USER_ROLE_CACHE_ENABLE));
        commonLdapConfiguration.setConnectionPoolingEnabled(
                authProviderConfig.getBooleanField(LdapConstants.CONNECTION_POOLING_ENABLED));
        commonLdapConfiguration.setLdapConnectionTimeout(
                (int) authProviderConfig.getIntField(LdapConstants.CONNECTION_TIME_OUT));
        commonLdapConfiguration.setReadTimeout(
                (int) authProviderConfig.getIntField(LdapConstants.READ_TIME_OUT));
        commonLdapConfiguration.setRetryAttempts((int) authProviderConfig.getIntField(LdapConstants.RETRY_ATTEMPTS));

        Struct sslConfig = authProviderConfig.getStructField(LdapConstants.SECURE_AUTH_STORE_CONFIG);
        try {
            if (sslConfig != null) {
                setSslConfig(sslConfig, commonLdapConfiguration, instanceId);
                LdapUtils.setServiceName(instanceId);
            }
            LdapConnectionContext connectionSource = new LdapConnectionContext(commonLdapConfiguration);
            DirContext dirContext = connectionSource.getContext();
            authStore.addNativeData(LdapConstants.LDAP_CONFIGURATION, commonLdapConfiguration);
            authStore.addNativeData(LdapConstants.LDAP_CONNECTION_SOURCE, connectionSource);
            authStore.addNativeData(LdapConstants.LDAP_CONNECTION_CONTEXT, dirContext);
            authStore.addNativeData(LdapConstants.ENDPOINT_INSTANCE_ID, instanceId);
            context.setReturnValues();
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException
                | CertificateException | NamingException | IOException e) {
            throw new BallerinaException(e.getMessage(), e);
        } finally {
            if (sslConfig != null) {
                LdapUtils.removeServiceName();
            }
        }
    }

    private void setSslConfig(Struct sslConfig, CommonLdapConfiguration commonLdapConfiguration, String instanceId)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
            CertificateException {
        Struct trustStore = sslConfig.getStructField(LdapConstants.AUTH_STORE_CONFIG_TRUST_STORE);
        String trustCerts = sslConfig.getStringField(LdapConstants.AUTH_STORE_CONFIG_TRUST_CERTIFICATES);

        if (trustStore != null) {
            String trustStoreFilePath = trustStore.getStringField(LdapConstants.FILE_PATH);
            String trustStorePassword = trustStore.getStringField(LdapConstants.PASSWORD);

            if (trustStoreFilePath != null) {
                File trustStoreFile = new File(LdapUtils.substituteVariables(trustStoreFilePath));
                if (!trustStoreFile.exists()) {
                    throw new IllegalArgumentException("trustStore File " + trustStoreFilePath + " not found");
                }
                if (trustStorePassword == null) {
                    throw new IllegalArgumentException("trustStorePass is not defined for HTTPS scheme");
                }
                commonLdapConfiguration.setTrustStoreFile(trustStoreFile);
                commonLdapConfiguration.setTrustStorePass(trustStorePassword);
                SSLContext sslContext = SslUtils.createClientSslContext(trustStoreFilePath, trustStorePassword);
                SslContextTrustManager.getInstance().addSSLContext(instanceId, sslContext);

            }
        } else if (StringUtils.isNotBlank(trustCerts)) {
            commonLdapConfiguration.setClientTrustCertificates(trustCerts);
            SSLContext sslContext = SslUtils.getSslContextForCertificateFile(trustCerts);
            SslContextTrustManager.getInstance().addSSLContext(instanceId, sslContext);
        }
    }

    private static List<String> getAsStringList(Value[] values) {
        if (values == null) {
            return null;
        }
        List<String> valuesList = new ArrayList<>();
        for (Value val : values) {
            valuesList.add(val.getStringValue().trim());
        }
        return !valuesList.isEmpty() ? valuesList : null;
    }
}
