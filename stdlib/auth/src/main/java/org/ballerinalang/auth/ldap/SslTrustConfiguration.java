/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.auth.ldap;

import org.ballerinalang.auth.ldap.util.LdapUtils;

import java.io.File;


/**
 * SSL configuration for HTTP connection.
 *
 * @since 0.983.0
 */
public class SslTrustConfiguration {

    private SslTrustConfig sslTrustConfig = new SslTrustConfig();

    public void setTrustStoreFile(File trustStoreFile) {
        sslTrustConfig.setTrustStore(trustStoreFile);
    }

    public void setTrustStorePass(String trustStorePass) {
        sslTrustConfig.setTrustStorePass(trustStorePass);
    }

    public void setSSLProtocol(String sslProtocol) {
        sslTrustConfig.setSSLProtocol(sslProtocol);
    }

    public void setTLSStoreType(String tlsStoreType) {
        sslTrustConfig.setTLSStoreType(tlsStoreType);
    }

    public void setClientTrustCertificates(String clientTrustCertificates) {
        sslTrustConfig.setClientTrustCertificates(new File(LdapUtils.substituteVariables(clientTrustCertificates)));
    }

    public SslTrustConfig getLdapSslConfiguration() {
        if ((sslTrustConfig.getTrustStore() == null || sslTrustConfig.getTrustStorePass() == null) && (
                sslTrustConfig.getClientTrustCertificates() == null)) {
            throw new IllegalArgumentException("TrustStoreFile or TrustStorePassword not defined for ldaps scheme");
        }
        if (sslTrustConfig.getTrustStore() != null) {
            if (!sslTrustConfig.getTrustStore().exists()) {
                throw new IllegalArgumentException("TrustStore File " + sslTrustConfig.getTrustStore() + " not found");
            }
            sslTrustConfig.setTrustStorePass(sslTrustConfig.getTrustStorePass());
        } else if (!sslTrustConfig.getClientTrustCertificates().exists()) {
            throw new IllegalArgumentException("Key file or server certificates file not found");
        }
        sslTrustConfig.setTrustStore(sslTrustConfig.getTrustStore()).setTrustStorePass(sslTrustConfig
                                                                                       .getTrustStorePass());
        String sslProtocol = sslTrustConfig.getSSLProtocol() != null ?
                                                            sslTrustConfig.getSSLProtocol() : LdapConstants.TLS;
        sslTrustConfig.setSSLProtocol(sslProtocol);
        String tlsStoreType = sslTrustConfig.getTLSStoreType() != null ? sslTrustConfig.getTLSStoreType() :
                LdapConstants.PKCS_STORE_TYPE;
        sslTrustConfig.setTLSStoreType(tlsStoreType);

        return sslTrustConfig;
    }
}
