/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.transport.http.netty.internal.config;

import org.wso2.carbon.transport.http.netty.listener.ssl.SSLConfig;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    public static final String DEFAULT_KEY = "netty";

    public static ListenerConfiguration getDefault() {
        ListenerConfiguration defaultConfig;
        try {
            defaultConfig = new ListenerConfiguration(DEFAULT_KEY, InetAddress.getLocalHost().getHostAddress(), 8080);
        } catch (UnknownHostException e) {
            defaultConfig = new ListenerConfiguration(DEFAULT_KEY, "127.0.0.1", 8080);
        }
        return defaultConfig;
    }

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute
    private String host;

    @XmlAttribute(required = true)
    private int port;

    @XmlAttribute
    private int bossThreadPoolSize = Runtime.getRuntime().availableProcessors();

    @XmlAttribute
    private int workerThreadPoolSize = Runtime.getRuntime().availableProcessors() * 2;

    @XmlAttribute
    private int execHandlerThreadPoolSize = 60;

    @XmlAttribute
    private String scheme = "http";

    @XmlAttribute
    private String keyStoreFile;

    @XmlAttribute
    private String keyStorePass;

    @XmlAttribute
    private String trustStoreFile;

    @XmlAttribute
    private String trustStorePass;

    @XmlAttribute
    private String certPass;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private List<Parameter> parameters;

    public ListenerConfiguration() {
    }

    public ListenerConfiguration(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public int getBossThreadPoolSize() {
        return bossThreadPoolSize;
    }

    public void setBossThreadPoolSize(int bossThreadPoolSize) {
        this.bossThreadPoolSize = bossThreadPoolSize;
    }

    public String getCertPass() {
        return certPass;
    }

    public void setCertPass(String certPass) {
        this.certPass = certPass;
    }

    public int getExecHandlerThreadPoolSize() {
        return execHandlerThreadPoolSize;
    }

    public void setExecHandlerThreadPoolSize(int execHandlerThreadPoolSize) {
        this.execHandlerThreadPoolSize = execHandlerThreadPoolSize;
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

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
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

    public int getWorkerThreadPoolSize() {
        return workerThreadPoolSize;
    }

    public void setWorkerThreadPoolSize(int workerThreadPoolSize) {
        this.workerThreadPoolSize = workerThreadPoolSize;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public SSLConfig getSslConfig() {
        if (scheme == null || !scheme.equalsIgnoreCase("https")) {
            return null;
        }
        if (certPass == null) {
            certPass = keyStorePass;
        }
        if (keyStoreFile == null || keyStorePass == null) {
            throw new IllegalArgumentException("keyStoreFile or keyStorePass not defined for " +
                    "HTTPS scheme");
        }
        File keyStore = new File(keyStoreFile);
        if (!keyStore.exists()) {
            throw new IllegalArgumentException("KeyStore File " + keyStoreFile + " not found");
        }
        SSLConfig sslConfig =
                new SSLConfig(keyStore, keyStorePass).setCertPass(certPass);
        if (trustStoreFile != null) {
            File trustStore = new File(trustStoreFile);
            if (!trustStore.exists()) {
                throw new IllegalArgumentException("trustStore File " + trustStoreFile + " not found");
            }
            if (trustStorePass == null) {
                throw new IllegalArgumentException("trustStorePass is not defined for HTTPS scheme");
            }
            sslConfig.setTrustStore(trustStore).setTrustStorePass(trustStorePass);
        }
        return sslConfig;
    }
}
