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
package org.wso2.transport.http.netty.common.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A class that encapsulates SSLContext configuration.
 */

public class SSLConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSLConfig.class);

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

    public SSLConfig(File keyStore, String keyStorePass) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using key store {}", keyStore);
        }
        this.keyStore = keyStore;
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using trust store {}", trustStore);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using sniMatchers {}", sniMatchers);
        }
        this.sniMatchers = sniMatchers.split(SEPARATOR);
    }

    public String[] getServerNames() {
        return serverNames == null ? null : serverNames.clone();
    }

    public void setServerNames(String serverNames) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using serverNames {}", serverNames);
        }
        this.serverNames = serverNames.replaceAll("\\s+", "").split(SEPARATOR);
    }

    public boolean isWantClientAuth() {
        return wantClientAuth;
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set WantClientAuth {}", wantClientAuth);
        }
        this.wantClientAuth = wantClientAuth;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set NeedClientAuth {}", needClientAuth);
        }
        this.needClientAuth = needClientAuth;
    }

    public void setSSLProtocol(String sslProtocol) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set SSLProtocol {}", sslProtocol);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Enable Session Creation {}", enableSessionCreation);
        }
        this.enableSessionCreation = enableSessionCreation;
    }

    public String[] getEnableProtocols() {
        return enableProtocols == null ? null : enableProtocols.clone();
    }

    public void setEnableProtocols(String enableProtocols) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set enable protocols {}", enableProtocols);
        }
        this.enableProtocols = enableProtocols.replaceAll("\\s+", "").split(SEPARATOR);
    }

    public String[] getCipherSuites() {
        return cipherSuites == null ? null : cipherSuites.clone();
    }

    public void setCipherSuites(String cipherSuites) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set supported cipherSuites {}", cipherSuites);
        }
        this.cipherSuites = cipherSuites.replaceAll("\\s+", "").split(SEPARATOR);
    }
}
