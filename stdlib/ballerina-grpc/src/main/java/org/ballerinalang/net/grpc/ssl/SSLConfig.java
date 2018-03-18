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
package org.ballerinalang.net.grpc.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A class that encapsulates SSLContext configuration.
 *
 * @since 1.0.0
 */
public class SSLConfig {
    
    private static final Logger LOG = LoggerFactory.getLogger(SSLConfig.class);
    
    private static final String separator = ",";
    
    private File keyStore;
    private String keyStorePass;
    private String certPass;
    private File trustStore;
    private String trustStorePass;
    private String sslProtocol;
    private String tlsStoreType;
    private String sslVerifyClient;
    private String[] cipherSuites;
    private int cacheSize;
    private int cacheValidityPeriod;
    
    private String[] enableProtocols;
    private boolean validateCertificateEnabled;
    private boolean enableSessionCreation;
    private boolean needClientAuth;
    private boolean wantClientAuth;
    private String[] serverNames;
    private String[] sniMatchers;
    
    public SSLConfig() {
    }
    
    public String getSslVerifyClient() {
        return sslVerifyClient;
    }
    
    public void setSslVerifyClient(String sslVerifyClient) {
        this.sslVerifyClient = sslVerifyClient;
    }
    
    public boolean isValidateCertificateEnabled() {
        return validateCertificateEnabled;
    }
    
    public String getSslProtocol() {
        return sslProtocol;
    }
    
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    
    public void setValidateCertificateEnabled(boolean validateCertificateEnabled) {
        this.validateCertificateEnabled = validateCertificateEnabled;
    }
    
    public SSLConfig(File keyStore, String keyStorePass) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using key store" + keyStore);
        }
        this.keyStore = keyStore;
        this.keyStorePass = keyStorePass;
    }
    
    public void setKeyStore(File keyStore) {
        this.keyStore = keyStore;
    }
    
    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }
    
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
            LOG.debug("Using trust store" + trustStore);
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
            LOG.debug("Using sniMatchers" + sniMatchers);
        }
        this.sniMatchers = sniMatchers.split(separator);
    }
    
    public String[] getServerNames() {
        return serverNames == null ? null : serverNames.clone();
    }
    
    public void setServerNames(String serverNames) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using serverNames" + serverNames);
        }
        this.serverNames = serverNames.replaceAll("\\s+", "").split(separator);
    }
    
    public boolean isWantClientAuth() {
        return wantClientAuth;
    }
    
    public void setWantClientAuth(boolean wantClientAuth) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set WantClientAuth" + wantClientAuth);
        }
        this.wantClientAuth = wantClientAuth;
    }
    
    public boolean isNeedClientAuth() {
        return needClientAuth;
    }
    
    public void setNeedClientAuth(boolean needClientAuth) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set NeedClientAuth" + needClientAuth);
        }
        this.needClientAuth = needClientAuth;
    }
    
    public void setSSLProtocol(String sslProtocol) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set SSLProtocol" + sslProtocol);
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
            LOG.debug("Enable Session Creation" + enableSessionCreation);
        }
        this.enableSessionCreation = enableSessionCreation;
    }
    
    public String[] getEnableProtocols() {
        return enableProtocols == null ? null : enableProtocols.clone();
    }
    
    public void setEnableProtocols(String enableProtocols) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set enable protocols" + enableProtocols);
        }
        this.enableProtocols = enableProtocols.replaceAll("\\s+", "").split(separator);
    }
    
    public String[] getCipherSuites() {
        return cipherSuites == null ? null : cipherSuites.clone();
    }
    
    public void setCipherSuites(String cipherSuites) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set supported cipherSuites" + cipherSuites);
        }
        this.cipherSuites = cipherSuites.replaceAll("\\s+", "").split(separator);
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
    
    
}
