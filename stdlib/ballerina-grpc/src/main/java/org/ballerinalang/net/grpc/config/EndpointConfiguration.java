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
package org.ballerinalang.net.grpc.config;

import org.ballerinalang.net.grpc.ssl.SSLConfig;

/**
 * Endpoint configuration of gRPC Service.
 */
public class EndpointConfiguration {
    private String host = "0.0.0.0";
    private int port = 9090;
    private String scheme = "http";
    private String tlsStoreType;
    private String keyStoreFile;
    private String keyStorePass;
    private String certPass;
    private String verifyClient;
    private String trustStoreFile;
    private String trustStorePass;
    private boolean validateCertEnabled;
    private int cacheSize;
    private int cacheValidityPeriod;
    private String sslProtocol;
    private SSLConfig sslConfig;
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getTlsStoreType() {
        return tlsStoreType;
    }

    public void setTlsStoreType(String tlsStoreType) {
        this.tlsStoreType = tlsStoreType;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public String getCertPass() {
        return certPass;
    }

    public void setCertPass(String certPass) {
        this.certPass = certPass;
    }

    public String getVerifyClient() {
        return verifyClient;
    }

    public void setVerifyClient(String verifyClient) {
        this.verifyClient = verifyClient;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public void setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass;
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

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    
    public SSLConfig getSslConfig() {
        return sslConfig;
    }
    
    public void setSslConfig(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
    }
}
