/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
 *
 */
package org.wso2.transport.http.netty.contractimpl.common.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A class that encapsulates SSLContext configuration.
 */

public class SSLConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SSLConfig.class);

    private static final String SEPARATOR = ",";

    private File keyStore;
    private String keyStorePass;
    private String certPass;
    private File trustStore;
    private String trustStorePass;
    private String sslProtocol;
    private String tlsStoreType;
    private String[] cipherSuites;
    private String[] enableProtocols;
    private boolean enableSessionCreation = true;
    private boolean needClientAuth;
    private boolean wantClientAuth;
    private String[] serverNames;
    private String[] sniMatchers;
    private boolean validateCertEnabled;
    private int cacheSize = 50;
    private int cacheValidityPeriod = 15;
    private boolean ocspStaplingEnabled = false;
    private boolean hostNameVerificationEnabled = true;
    private File serverKeyFile;
    private File serverCertificates;
    private File clientKeyFile;
    private File clientCertificates;
    private File serverTrustCertificates;
    private File clientTrustCertificates;
    private String serverKeyPassword;
    private String clientKeyPassword;
    private int sessionTimeOut;
    private long handshakeTimeOut;
    private boolean disableSsl = false;
    private boolean useJavaDefaults = false;

    public SSLConfig() {}

    public String getCertPass() {
        return certPass;
    }

    public SSLConfig setCertPass(String certPass) {
        this.certPass = certPass;
        return this;
    }

    public File getTrustStore() {
        return trustStore;
    }

    public SSLConfig setTrustStore(File trustStore) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using trust store {}", trustStore);
        }
        this.trustStore = trustStore;
        return this;
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public SSLConfig setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass;
        return this;
    }

    public File getKeyStore() {
        return keyStore;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public String[] getSniMatchers() {
        return sniMatchers == null ? null : sniMatchers.clone();
    }

    public void setSniMatchers(String sniMatchers) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using sniMatchers {}", sniMatchers);
        }
        this.sniMatchers = sniMatchers.split(SEPARATOR);
    }

    public String[] getServerNames() {
        return serverNames == null ? null : serverNames.clone();
    }

    public void setServerNames(String serverNames) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using serverNames {}", serverNames);
        }
        this.serverNames = serverNames.replaceAll("\\s+", "").split(SEPARATOR);
    }

    public boolean isWantClientAuth() {
        return wantClientAuth;
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set WantClientAuth {}", wantClientAuth);
        }
        this.wantClientAuth = wantClientAuth;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set NeedClientAuth {}", needClientAuth);
        }
        this.needClientAuth = needClientAuth;
    }

    public void setSSLProtocol(String sslProtocol) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set SSLProtocol {}", sslProtocol);
        }
        this.sslProtocol = sslProtocol;
    }

    public String getSSLProtocol() {
        return sslProtocol;
    }

    public String getTLSStoreType() {
        return tlsStoreType;
    }

    public void setTLSStoreType(String tlsStoreType) {
        this.tlsStoreType = tlsStoreType;
    }

    public boolean isEnableSessionCreation() {
        return enableSessionCreation;
    }

    public void setEnableSessionCreation(boolean enableSessionCreation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Enable Session Creation {}", enableSessionCreation);
        }
        this.enableSessionCreation = enableSessionCreation;
    }

    public String[] getEnableProtocols() {
        return enableProtocols == null ? null : enableProtocols.clone();
    }

    public void setEnableProtocols(String enableProtocols) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set enable protocols {}", enableProtocols);
        }
        this.enableProtocols = enableProtocols.replaceAll("\\s+", "").split(SEPARATOR);
    }

    public String[] getCipherSuites() {
        return cipherSuites == null ? null : cipherSuites.clone();
    }

    public void setCipherSuites(String cipherSuites) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set supported cipherSuites {}", cipherSuites);
        }
        this.cipherSuites = cipherSuites.replaceAll("\\s+", "").split(SEPARATOR);
    }

    public void setKeyStore(File keyStore) {
        this.keyStore = keyStore;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public boolean isValidateCertEnabled() {
        return validateCertEnabled;
    }

    public void setValidateCertEnabled(boolean validateCertEnabled) {
        this.validateCertEnabled = validateCertEnabled;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public int getCacheValidityPeriod() {
        return cacheValidityPeriod;
    }

    public void setCacheValidityPeriod(int cacheValidityPeriod) {
        this.cacheValidityPeriod = cacheValidityPeriod;
    }

    public boolean isOcspStaplingEnabled() {
        return ocspStaplingEnabled;
    }

    public void setOcspStaplingEnabled(boolean ocspStaplingEnabled) {
        this.ocspStaplingEnabled = ocspStaplingEnabled;
    }

    public boolean isHostNameVerificationEnabled() {
        return hostNameVerificationEnabled;
    }

    public void setHostNameVerificationEnabled(boolean hostNameVerificationEnabled) {
        this.hostNameVerificationEnabled = hostNameVerificationEnabled;
    }

    public File getServerKeyFile() {
        return serverKeyFile;
    }

    public File getServerCertificates() {
        return serverCertificates;
    }

    public File getClientKeyFile() {
        return clientKeyFile;
    }

    public File getClientCertificates() {
        return clientCertificates;
    }

    public File getServerTrustCertificates() {
        return serverTrustCertificates;
    }

    public File getClientTrustCertificates() {
        return clientTrustCertificates;
    }

    public void setServerKeyFile(File serverKeyFile) {
        this.serverKeyFile = serverKeyFile;
    }

    public void setServerCertificates(File serverCertificates) {
        this.serverCertificates = serverCertificates;
    }

    public void setClientKeyFile(File clientKeyFile) {
        this.clientKeyFile = clientKeyFile;
    }

    public void setClientCertificates(File clientCertificates) {
        this.clientCertificates = clientCertificates;
    }

    public void setServerTrustCertificates(File serverTrustCertificates) {
        this.serverTrustCertificates = serverTrustCertificates;
    }

    public void setClientTrustCertificates(File clientTrustCertificates) {
        this.clientTrustCertificates = clientTrustCertificates;
    }

    public String getServerKeyPassword() {
        return serverKeyPassword;
    }

    public void setServerKeyPassword(String serverKeyPassword) {
        this.serverKeyPassword = serverKeyPassword;
    }

    public String getClientKeyPassword() {
        return clientKeyPassword;
    }

    public void setClientKeyPassword(String clientKeyPassword) {
        this.clientKeyPassword = clientKeyPassword;
    }

    public int getSessionTimeOut() {
        return sessionTimeOut;
    }

    public void setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    public long getHandshakeTimeOut() {
        return handshakeTimeOut;
    }

    public void setHandshakeTimeOut(long handshakeTimeOut) {
        this.handshakeTimeOut = handshakeTimeOut;
    }

    public boolean isDisableSsl() {
        return disableSsl;
    }

    public void disableSsl() {
        this.disableSsl = true;
    }

    public boolean useJavaDefaults() {
        return useJavaDefaults;
    }

    public void setUseJavaDefaults() {
        this.useJavaDefaults = true;
    }
}
