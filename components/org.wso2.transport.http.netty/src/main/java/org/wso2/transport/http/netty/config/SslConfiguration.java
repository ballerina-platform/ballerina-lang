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

package org.wso2.transport.http.netty.config;

import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.transport.http.netty.common.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.common.Constants.JKS;
import static org.wso2.transport.http.netty.common.Constants.REQUIRE;
import static org.wso2.transport.http.netty.common.Constants.TLS_PROTOCOL;
import static org.wso2.transport.http.netty.common.Util.substituteVariables;

/**
 * SSL configuration for HTTP connection.
 */
public class SslConfiguration {

    private String scheme = "http";
    private List<Parameter> parameters = new ArrayList<>();
    private SSLConfig sslConfig = new SSLConfig();

    public void setKeyStoreFile(String keyStoreFile) {
        sslConfig.setKeyStore(new File(substituteVariables(keyStoreFile)));
    }

    public void setKeyStorePass(String keyStorePassword) {
        sslConfig.setKeyStorePass(keyStorePassword);
    }

    public void setVerifyClient(String verifyClient) {
        if (REQUIRE.equalsIgnoreCase(verifyClient)) {
            sslConfig.setNeedClientAuth(true);
        }
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        sslConfig.setTrustStore(new File(substituteVariables(trustStoreFile)));
    }

    public void setTrustStorePass(String trustStorePass) {
        sslConfig.setTrustStorePass(trustStorePass);
    }

    public void setSSLProtocol(String sslProtocol) {
        sslConfig.setSSLProtocol(sslProtocol);
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setTLSStoreType(String tlsStoreType) {
        sslConfig.setTLSStoreType(tlsStoreType);
    }

    public void setValidateCertEnabled(boolean validateCertEnabled) {
        sslConfig.setValidateCertEnabled(validateCertEnabled);
    }

    public void setHostNameVerificationEnabled(boolean hostNameVerificationEnabled) {
        sslConfig.setHostNameVerificationEnabled(hostNameVerificationEnabled);
    }

    public void setCacheValidityPeriod(int cacheValidityPeriod) {
        sslConfig.setCacheValidityPeriod(cacheValidityPeriod);
    }

    public void setCacheSize(int cacheSize) {
        sslConfig.setCacheSize(cacheSize);
    }

    public void setOcspStaplingEnabled(boolean ocspStaplingEnabled) {
        sslConfig.setOcspStaplingEnabled(ocspStaplingEnabled);
    }

    public SSLConfig getClientSSLConfig() {
        if (scheme == null || !scheme.equalsIgnoreCase(HTTPS_SCHEME)) {
            return null;
        }
        return getSSLConfigForSender();
    }

    public SSLConfig getListenerSSLConfig() {
        if (scheme == null || !scheme.equalsIgnoreCase(HTTPS_SCHEME)) {
            return null;
        }
        return getSSLConfigForListener();
    }

    private SSLConfig getSSLConfigForListener() {
        if (sslConfig.getKeyStore() == null || sslConfig.getKeyStorePass() == null) {
            throw new IllegalArgumentException("keyStoreFile or keyStorePassword not defined for HTTPS scheme");
        }
        if (!sslConfig.getKeyStore().exists()) {
            throw new IllegalArgumentException("KeyStore File " + sslConfig.getKeyStore() + " not found");
        }
        sslConfig.setCertPass(sslConfig.getKeyStorePass());
        for (Parameter parameter : parameters) {
            switch (parameter.getName()) {
            case Constants.SERVER_SUPPORT_CIPHERS:
                sslConfig.setCipherSuites(parameter.getValue());
                break;
            case Constants.SERVER_SUPPORT_SSL_PROTOCOLS:
                sslConfig.setEnableProtocols(parameter.getValue());
                break;
            case Constants.SERVER_SUPPORTED_SNIMATCHERS:
                sslConfig.setSniMatchers(parameter.getValue());
                break;
            case Constants.SERVER_SUPPORTED_SERVER_NAMES:
                sslConfig.setServerNames(parameter.getValue());
                break;
            case Constants.SERVER_ENABLE_SESSION_CREATION:
                sslConfig.setEnableSessionCreation(Boolean.parseBoolean(parameter.getValue()));
                break;
            default:
                //do nothing
                break;
            }
        }

        String sslProtocol = sslConfig.getSSLProtocol() != null ? sslConfig.getSSLProtocol() : TLS_PROTOCOL;
        sslConfig.setSSLProtocol(sslProtocol);
        String tlsStoreType = sslConfig.getTLSStoreType() != null ? sslConfig.getTLSStoreType() : JKS;
        sslConfig.setTLSStoreType(tlsStoreType);

        if (sslConfig.getTrustStore() != null) {
            if (!sslConfig.getTrustStore().exists()) {
                throw new IllegalArgumentException("TrustStore file " + sslConfig.getTrustStore() + " not found");
            }
            if (sslConfig.getTrustStorePass() == null) {
                throw new IllegalArgumentException("Truststore password is not defined for HTTPS scheme");
            }
        }
        return sslConfig;
    }

    private SSLConfig getSSLConfigForSender() {
        if (sslConfig.getTrustStore() == null || sslConfig.getTrustStorePass() == null) {
            throw new IllegalArgumentException("TrustStoreFile or trustStorePassword not defined for HTTPS/WSS scheme");
        }
        if (sslConfig.getKeyStore() != null) {
            if (!sslConfig.getKeyStore().exists()) {
                throw new IllegalArgumentException("KeyStore File " + sslConfig.getKeyStore() + " not found");
            }
            sslConfig.setCertPass(sslConfig.getKeyStorePass());
        }

        sslConfig.setTrustStore(sslConfig.getTrustStore()).setTrustStorePass(sslConfig.getTrustStorePass());
        String sslProtocol = sslConfig.getSSLProtocol() != null ? sslConfig.getSSLProtocol() : TLS_PROTOCOL;
        sslConfig.setSSLProtocol(sslProtocol);
        String tlsStoreType = sslConfig.getTLSStoreType() != null ? sslConfig.getTLSStoreType() : JKS;
        sslConfig.setTLSStoreType(tlsStoreType);
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                String paramName = parameter.getName();
                if (Constants.CLIENT_SUPPORT_CIPHERS.equals(paramName)) {
                    sslConfig.setCipherSuites(parameter.getValue());
                } else if (Constants.CLIENT_SUPPORT_SSL_PROTOCOLS.equals(paramName)) {
                    sslConfig.setEnableProtocols(parameter.getValue());
                } else if (Constants.CLIENT_ENABLE_SESSION_CREATION.equals(paramName)) {
                    sslConfig.setEnableSessionCreation(Boolean.parseBoolean(parameter.getValue()));
                }
            }
        }
        return sslConfig;
    }
}
