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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.stdlib.ldap.CommonLdapConfiguration;
import org.ballerinalang.stdlib.ldap.LdapConnectionContext;
import org.ballerinalang.stdlib.ldap.LdapConstants;
import org.ballerinalang.stdlib.ldap.SslContextTrustManager;
import org.ballerinalang.stdlib.ldap.util.LdapUtils;
import org.ballerinalang.stdlib.ldap.util.SslUtils;

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
public class InitLdapConnectionContext {

    public static Object initLdapConnectionContext(MapValue<BString, Object> authProviderConfig, BString instanceId) {
        CommonLdapConfiguration commonLdapConfiguration = new CommonLdapConfiguration();

        commonLdapConfiguration.setDomainName(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.DOMAIN_NAME)).getValue());
        commonLdapConfiguration.setConnectionURL(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.CONNECTION_URL)).getValue());
        commonLdapConfiguration.setConnectionName(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.CONNECTION_NAME)).getValue());
        commonLdapConfiguration.setConnectionPassword(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.CONNECTION_PASSWORD)).getValue());

        commonLdapConfiguration.setUserSearchBase(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.USER_SEARCH_BASE)).getValue());
        commonLdapConfiguration.setUserEntryObjectClass(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.USER_ENTRY_OBJECT_CLASS)).getValue());
        commonLdapConfiguration.setUserNameAttribute(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.USER_NAME_ATTRIBUTE)).getValue());
        commonLdapConfiguration.setUserNameSearchFilter(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.USER_NAME_SEARCH_FILTER)).getValue());
        commonLdapConfiguration.setUserNameListFilter(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.USER_NAME_LIST_FILTER)).getValue());

        commonLdapConfiguration.setGroupSearchBase(getAsStringList(authProviderConfig.getArrayValue(
                StringUtils.fromString(LdapConstants.GROUP_SEARCH_BASE)).getStringArray()));
        commonLdapConfiguration.setGroupEntryObjectClass(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.GROUP_ENTRY_OBJECT_CLASS)).getValue());
        commonLdapConfiguration.setGroupNameAttribute(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.GROUP_NAME_ATTRIBUTE)).getValue());
        commonLdapConfiguration.setGroupNameSearchFilter(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.GROUP_NAME_SEARCH_FILTER)).getValue());
        commonLdapConfiguration.setGroupNameListFilter(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.GROUP_NAME_LIST_FILTER)).getValue());

        commonLdapConfiguration.setMembershipAttribute(authProviderConfig.getStringValue(
                StringUtils.fromString(LdapConstants.MEMBERSHIP_ATTRIBUTE)).getValue());
        commonLdapConfiguration.setUserRolesCacheEnabled(authProviderConfig.getBooleanValue(
                StringUtils.fromString(LdapConstants.USER_ROLE_CACHE_ENABLE)));
        commonLdapConfiguration.setConnectionPoolingEnabled(authProviderConfig.getBooleanValue(
                StringUtils.fromString(LdapConstants.CONNECTION_POOLING_ENABLED)));
        commonLdapConfiguration.setLdapConnectionTimeout(authProviderConfig.getIntValue(
                StringUtils.fromString(LdapConstants.CONNECTION_TIME_OUT_IN_MILLIS)).intValue());
        commonLdapConfiguration.setReadTimeoutInMillis(authProviderConfig.getIntValue(
                StringUtils.fromString(LdapConstants.READ_TIME_OUT_IN_MILLIS)).intValue());
        commonLdapConfiguration.setRetryAttempts(authProviderConfig.getIntValue(
                StringUtils.fromString(LdapConstants.RETRY_ATTEMPTS)).intValue());

        MapValue<BString, Object> sslConfig = authProviderConfig.containsKey(
                StringUtils.fromString(LdapConstants.SECURE_AUTH_STORE_CONFIG)) ?
                (MapValue<BString, Object>) authProviderConfig.getMapValue(
                        StringUtils.fromString(LdapConstants.SECURE_AUTH_STORE_CONFIG)) : null;
        try {
            if (sslConfig != null) {
                setSslConfig(sslConfig, commonLdapConfiguration, instanceId.getValue());
                LdapUtils.setServiceName(instanceId.getValue());
            }
            LdapConnectionContext connectionSource = new LdapConnectionContext(commonLdapConfiguration);
            DirContext dirContext = connectionSource.getContext();

            MapValue<BString, Object> ldapConnectionRecord = BallerinaValues.
                    createRecordValue(LdapConstants.LDAP_PACKAGE_ID, LdapConstants.LDAP_CONNECTION);
            ldapConnectionRecord.addNativeData(LdapConstants.LDAP_CONFIGURATION, commonLdapConfiguration);
            ldapConnectionRecord.addNativeData(LdapConstants.LDAP_CONNECTION_SOURCE, connectionSource);
            ldapConnectionRecord.addNativeData(LdapConstants.LDAP_CONNECTION_CONTEXT, dirContext);
            ldapConnectionRecord.addNativeData(LdapConstants.ENDPOINT_INSTANCE_ID, instanceId.getValue());
            ldapConnectionRecord.put(StringUtils.fromString(LdapConstants.ENDPOINT_INSTANCE_ID),
                                     StringUtils.fromString(instanceId.getValue()));
            return ldapConnectionRecord;
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException
                | CertificateException | NamingException | IOException | IllegalArgumentException e) {
            if (e.getCause() == null) {
                return LdapUtils.createError(e.getMessage());
            }
            return LdapUtils.createError(e.getCause().getMessage());
        } finally {
            if (sslConfig != null) {
                LdapUtils.removeServiceName();
            }
        }
    }

    private static void setSslConfig(MapValue<BString, Object> sslConfig,
                                     CommonLdapConfiguration commonLdapConfiguration, String instanceId)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
            CertificateException {
        MapValue<BString, BString> trustStore = (MapValue<BString, BString>) sslConfig.getMapValue(
                StringUtils.fromString(LdapConstants.AUTH_STORE_CONFIG_TRUST_STORE));
        String trustCerts = sslConfig.containsKey(LdapConstants.AUTH_STORE_CONFIG_TRUST_CERTIFICATES) ?
                sslConfig.getStringValue(
                        StringUtils.fromString(LdapConstants.AUTH_STORE_CONFIG_TRUST_CERTIFICATES)).getValue() : null;

        if (trustStore != null) {
            String trustStoreFilePath = trustStore.getStringValue(
                    StringUtils.fromString(LdapConstants.FILE_PATH)).getValue();
            String trustStorePassword = trustStore.getStringValue(
                    StringUtils.fromString(LdapConstants.PASSWORD)).getValue();
            File trustStoreFile = new File(LdapUtils.substituteVariables(trustStoreFilePath));
            if (!trustStoreFile.exists()) {
                throw new IllegalArgumentException("trustStore File " + trustStoreFilePath + " not found");
            }
            commonLdapConfiguration.setTrustStoreFile(trustStoreFile);
            commonLdapConfiguration.setTrustStorePass(trustStorePassword);
            SSLContext sslContext = SslUtils.createClientSslContext(trustStoreFilePath, trustStorePassword);
            SslContextTrustManager.getInstance().addSSLContext(instanceId, sslContext);
        } else if (trustCerts != null) {
            commonLdapConfiguration.setClientTrustCertificates(trustCerts);
            SSLContext sslContext = SslUtils.getSslContextForCertificateFile(trustCerts);
            SslContextTrustManager.getInstance().addSSLContext(instanceId, sslContext);
        }
    }

    private static List<String> getAsStringList(Object[] values) {
        if (values == null) {
            return null;
        }
        List<String> valuesList = new ArrayList<>();
        for (Object val : values) {
            valuesList.add(val.toString().trim());
        }
        return !valuesList.isEmpty() ? valuesList : null;
    }
}
