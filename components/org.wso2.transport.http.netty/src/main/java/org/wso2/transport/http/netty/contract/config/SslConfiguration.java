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

package org.wso2.transport.http.netty.contract.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.transport.http.netty.contract.Constants.CLIENT_ENABLE_SESSION_CREATION;
import static org.wso2.transport.http.netty.contract.Constants.CLIENT_SUPPORT_CIPHERS;
import static org.wso2.transport.http.netty.contract.Constants.CLIENT_SUPPORT_SSL_PROTOCOLS;
import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.JKS;
import static org.wso2.transport.http.netty.contract.Constants.OPTIONAL;
import static org.wso2.transport.http.netty.contract.Constants.REQUIRE;
import static org.wso2.transport.http.netty.contract.Constants.SERVER_ENABLE_SESSION_CREATION;
import static org.wso2.transport.http.netty.contract.Constants.SERVER_SUPPORTED_SERVER_NAMES;
import static org.wso2.transport.http.netty.contract.Constants.SERVER_SUPPORTED_SNIMATCHERS;
import static org.wso2.transport.http.netty.contract.Constants.SERVER_SUPPORT_CIPHERS;
import static org.wso2.transport.http.netty.contract.Constants.SERVER_SUPPORT_SSL_PROTOCOLS;
import static org.wso2.transport.http.netty.contract.Constants.TLS_PROTOCOL;
/**
 * SSL configuration for HTTP connection.
 */
public class SslConfiguration {

    private String scheme = "http";
    private List<Parameter> parameters = new ArrayList<>();
    private SSLConfig sslConfig = new SSLConfig();
    private static final Logger LOG = LoggerFactory.getLogger(SslConfiguration.class);

    public void setKeyStoreFile(String keyStoreFile) {
        sslConfig.setKeyStore(new File(Util.substituteVariables(keyStoreFile)));
    }

    public void setKeyStorePass(String keyStorePassword) {
        sslConfig.setKeyStorePass(keyStorePassword);
    }

    public void setVerifyClient(String verifyClient) {
        if (REQUIRE.equalsIgnoreCase(verifyClient)) {
            sslConfig.setNeedClientAuth(true);
        } else if (OPTIONAL.equalsIgnoreCase(verifyClient)) {
            sslConfig.setWantClientAuth(true);
        } else if (!verifyClient.isEmpty()) {
            LOG.warn("Received an unidentified configuration for sslVerify client. "
                    + "Hence client verification will be disabled which is the default configuration.");
        }
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        sslConfig.setTrustStore(new File(Util.substituteVariables(trustStoreFile)));
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

    public String getKeyStoreFile() {
        return String.valueOf(sslConfig.getKeyStore());
    }

    public String getKeyStorePass() {
        return sslConfig.getKeyStorePass();
    }

    public void setServerKeyFile(String serverKeyFile) {
        sslConfig.setServerKeyFile(new File(Util.substituteVariables(serverKeyFile)));
    }

    public void setServerCertificates(String serverCertificates) {
        sslConfig.setServerCertificates(new File(Util.substituteVariables(serverCertificates)));
    }

    public void setClientKeyFile(String clientKeyFile) {
        sslConfig.setClientKeyFile(new File(Util.substituteVariables(clientKeyFile)));
    }

    public void setClientCertificates(String clientCertificates) {
        sslConfig.setClientCertificates(new File(Util.substituteVariables(clientCertificates)));
    }

    public void setServerTrustCertificates(String serverTrustCertificates) {
        sslConfig.setServerTrustCertificates(new File(Util.substituteVariables(serverTrustCertificates)));
    }

    public void setClientTrustCertificates(String clientTrustCertificates) {
        sslConfig.setClientTrustCertificates(new File(Util.substituteVariables(clientTrustCertificates)));
    }

    public void setClientKeyPassword(String clientKeyPassword) {
        sslConfig.setClientKeyPassword(clientKeyPassword);
    }

    public void setServerKeyPassword(String serverKeyPassword) {
        sslConfig.setServerKeyPassword(serverKeyPassword);
    }

    public void setSslSessionTimeOut(int sessionTimeOut) {
        sslConfig.setSessionTimeOut(sessionTimeOut);
    }

    public void setSslHandshakeTimeOut(long handshakeTimeOut) {
        sslConfig.setHandshakeTimeOut(handshakeTimeOut);
    }

    public void disableSsl() {
        sslConfig.disableSsl();
    }

    public void useJavaDefaults() {
        sslConfig.setUseJavaDefaults();
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
        if ((sslConfig.getKeyStore() == null || sslConfig.getKeyStorePass() == null) && (
                sslConfig.getServerKeyFile() == null || sslConfig.getServerCertificates() == null)) {
            throw new IllegalArgumentException("keyStoreFile or keyStorePassword not defined for HTTPS scheme");
        }
        if (sslConfig.getKeyStore() != null) {
            if (!sslConfig.getKeyStore().exists()) {
                throw new IllegalArgumentException("KeyStore File " + sslConfig.getKeyStore() + " not found");
            }
            sslConfig.setCertPass(sslConfig.getKeyStorePass());
        } else if ((!sslConfig.getServerKeyFile().exists() || !sslConfig.getServerCertificates().exists())) {
            throw new IllegalArgumentException("Key file or server certificates file not found");
        }
        for (Parameter parameter : parameters) {
            switch (parameter.getName()) {
                case SERVER_SUPPORT_CIPHERS:
                    sslConfig.setCipherSuites(parameter.getValue());
                    break;
                case SERVER_SUPPORT_SSL_PROTOCOLS:
                    sslConfig.setEnableProtocols(parameter.getValue());
                    break;
                case SERVER_SUPPORTED_SNIMATCHERS:
                    sslConfig.setSniMatchers(parameter.getValue());
                    break;
                case SERVER_SUPPORTED_SERVER_NAMES:
                    sslConfig.setServerNames(parameter.getValue());
                    break;
                case SERVER_ENABLE_SESSION_CREATION:
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
        if (sslConfig.isDisableSsl() || sslConfig.useJavaDefaults()) {
            return sslConfig;
        }
        if ((sslConfig.getTrustStore() == null || sslConfig.getTrustStorePass() == null) && (
                sslConfig.getClientTrustCertificates() == null)) {
            throw new IllegalArgumentException("TrustStoreFile or TrustStorePassword not defined for HTTPS/WS scheme");
        }
        if (sslConfig.getTrustStore() != null) {
            if (!sslConfig.getTrustStore().exists()) {
                throw new IllegalArgumentException("TrustStore File " + sslConfig.getTrustStore() + " not found");
            }
            sslConfig.setCertPass(sslConfig.getKeyStorePass());
        } else if (!sslConfig.getClientTrustCertificates().exists()) {
            throw new IllegalArgumentException("Key file or server certificates file not found");
        }
        sslConfig.setTrustStore(sslConfig.getTrustStore()).setTrustStorePass(sslConfig.getTrustStorePass());
        String sslProtocol = sslConfig.getSSLProtocol() != null ? sslConfig.getSSLProtocol() : TLS_PROTOCOL;
        sslConfig.setSSLProtocol(sslProtocol);
        String tlsStoreType = sslConfig.getTLSStoreType() != null ? sslConfig.getTLSStoreType() : JKS;
        sslConfig.setTLSStoreType(tlsStoreType);

        if (parameters != null) {
            for (Parameter parameter : parameters) {
                switch (parameter.getName()) {
                    case CLIENT_SUPPORT_CIPHERS:
                        sslConfig.setCipherSuites(parameter.getValue());
                        break;
                    case CLIENT_SUPPORT_SSL_PROTOCOLS:
                        sslConfig.setEnableProtocols(parameter.getValue());
                        break;
                    case CLIENT_ENABLE_SESSION_CREATION:
                        sslConfig.setEnableSessionCreation(Boolean.parseBoolean(parameter.getValue()));
                        break;
                    default:
                        //do nothing
                        break;
                }
            }
        }
        return sslConfig;
    }
}
