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
package org.wso2.transport.http.netty.config;

import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;


/**
 * JAXB representation of a transport listener.
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListenerConfiguration {

    public static final String DEFAULT_KEY = "default";

    /**
     * @deprecated
     * @return the default listener configuration.
     */
    @Deprecated
    public static ListenerConfiguration getDefault() {
        ListenerConfiguration defaultConfig;
        defaultConfig = new ListenerConfiguration(DEFAULT_KEY, "0.0.0.0", 8080);
        return defaultConfig;
    }

    @XmlAttribute(required = true)
    private String id = DEFAULT_KEY;

    @XmlAttribute
    private String host = "0.0.0.0";

    @XmlAttribute(required = true)
    private int port = 9090;

    private ChunkConfig chunkingConfig = ChunkConfig.AUTO;

    private KeepAliveConfig keepAliveConfig = KeepAliveConfig.AUTO;

    @XmlAttribute
    private boolean bindOnStartup = false;

    @XmlAttribute
    private String scheme = "http";

    @XmlAttribute
    private String version = "1.1";

    @XmlAttribute
    private String keyStoreFile;

    @XmlAttribute
    private String keyStorePassword;

    @XmlAttribute
    private String trustStoreFile;

    @XmlAttribute
    private String trustStorePass;

    @XmlAttribute
    private String certPass;

    @XmlAttribute
    private long socketIdleTimeout;

    @XmlAttribute
    private String messageProcessorId;

    @XmlAttribute
    private boolean httpTraceLogEnabled;

    @XmlAttribute
    private boolean httpAccessLogEnabled;

    @XmlAttribute
    private String verifyClient;

    @XmlAttribute
    private String sslProtocol;

    @XmlAttribute
    private String tlsStoreType;

    @XmlAttribute
    private String serverHeader = "wso2-http-transport";

    @XmlAttribute
    private boolean validateCertEnabled;

    @XmlAttribute
    private int cacheSize = 50;

    @XmlAttribute
    private int cacheValidityPeriod = 15;

    @XmlAttribute
    private boolean ocspStaplingEnabled = false;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private List<Parameter> parameters = getDefaultParameters();

    private RequestSizeValidationConfig requestSizeValidationConfig = new RequestSizeValidationConfig();

    public ListenerConfiguration() {
    }

    public ListenerConfiguration(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public String getTLSStoreType() {
        return tlsStoreType;
    }

    public void setTLSStoreType(String tlsStoreType) {
        this.tlsStoreType = tlsStoreType;
    }

    public String getCertPass() {
        return certPass;
    }

    public void setCertPass(String certPass) {
        this.certPass = certPass;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePass() {
        return keyStorePassword;
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public void setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass;
    }

    public void setKeyStorePass(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setVerifyClient(String verifyClient) {
        this.verifyClient = verifyClient;
    }

    public String getVerifyClient() {
        return verifyClient;
    }

    public void setSSLProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public String getSSLProtocol() {
        return sslProtocol;
    }

    public boolean validateCertEnabled() {
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isBindOnStartup() {
        return bindOnStartup;
    }

    public void setBindOnStartup(boolean bindOnStartup) {
        this.bindOnStartup = bindOnStartup;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public SSLConfig getSSLConfig() {
        if (scheme == null || !scheme.equalsIgnoreCase("https")) {
            return null;
        }

        return Util.getSSLConfigForListener(certPass, keyStorePassword, keyStoreFile, trustStoreFile, trustStorePass,
                                            parameters, verifyClient, sslProtocol, tlsStoreType);
    }

    private List<Parameter> getDefaultParameters() {
        return new ArrayList<>();

    }

    public long getSocketIdleTimeout() {
        return socketIdleTimeout;
    }

    public String getMessageProcessorId() {
        return messageProcessorId;
    }

    public void setMessageProcessorId(String messageProcessorId) {
        this.messageProcessorId = messageProcessorId;
    }

    public void setSocketIdleTimeout(int socketIdleTimeout) {
        this.socketIdleTimeout = socketIdleTimeout;
    }

    public boolean isHttpTraceLogEnabled() {
        return httpTraceLogEnabled;
    }

    public void setHttpTraceLogEnabled(boolean httpTraceLogEnabled) {
        this.httpTraceLogEnabled = httpTraceLogEnabled;
    }

    public boolean isHttpAccessLogEnabled() {
        return httpAccessLogEnabled;
    }

    public void setHttpAccessLogEnabled(boolean httpAccessLogEnabled) {
        this.httpAccessLogEnabled = httpAccessLogEnabled;
    }

    public RequestSizeValidationConfig getRequestSizeValidationConfig() {
        return requestSizeValidationConfig;
    }

    public void setRequestSizeValidationConfig(RequestSizeValidationConfig requestSizeValidationConfig) {
        this.requestSizeValidationConfig = requestSizeValidationConfig;
    }

    public ChunkConfig getChunkConfig() {
        return chunkingConfig;
    }

    public void setChunkConfig(ChunkConfig chunkConfig) {
        this.chunkingConfig = chunkConfig;
    }

    public KeepAliveConfig getKeepAliveConfig() {
        return keepAliveConfig;
    }

    public void setKeepAliveConfig(KeepAliveConfig keepAliveConfig) {
        this.keepAliveConfig = keepAliveConfig;
    }

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    public void setOcspStaplingEnabled(boolean ocspStaplingEnabled) {
        this.ocspStaplingEnabled = ocspStaplingEnabled;
    }

    public boolean isOcspStaplingEnabled () {
        return ocspStaplingEnabled;
    }
}
