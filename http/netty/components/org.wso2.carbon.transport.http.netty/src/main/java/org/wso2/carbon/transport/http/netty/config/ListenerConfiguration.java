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
package org.wso2.carbon.transport.http.netty.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    private static final Logger logger = LoggerFactory.getLogger(ListenerConfiguration.class);

    public static final String DEFAULT_KEY = "netty";
    private int execHandlerThreadPoolSize = 60;

    private ExecutorService executorService;

    public static ListenerConfiguration getDefault() {
        ListenerConfiguration defaultConfig;
        defaultConfig = new ListenerConfiguration(DEFAULT_KEY, "0.0.0.0", 8080);

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
    private String enableDisruptor;

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
        this.executorService = Executors.newFixedThreadPool(execHandlerThreadPoolSize);

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

        return Util.getSSLConfigForListener(certPass, keyStorePass, keyStoreFile, trustStoreFile, trustStorePass);
    }


    public String getEnableDisruptor() {
        return enableDisruptor;
    }

    public void setEnableDisruptor(String enableDisruptor) {
        this.enableDisruptor = enableDisruptor;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
