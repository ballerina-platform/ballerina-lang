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

import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;


/**
 * JAXB representation of the Netty transport sender configuration.
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public class SenderConfiguration {

    public static final String DEFAULT_KEY = "netty";


    public static SenderConfiguration getDefault() {
        SenderConfiguration defaultConfig;
        defaultConfig = new SenderConfiguration(DEFAULT_KEY);
        return defaultConfig;
    }

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute
    private String senderWorkerThreads = "4";

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

    public SenderConfiguration() {
    }

    public SenderConfiguration(String id) {
        this.id = id;

    }

    public String getCertPass() {
        return certPass;
    }

    public void setCertPass(String certPass) {
        this.certPass = certPass;
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

    public String getSenderWorkerThreads() {
        return senderWorkerThreads;
    }

    public void setSenderWorkerThreads(String senderWorkerThreads) {
        this.senderWorkerThreads = senderWorkerThreads;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
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
        return Util.getSSLConfigForSender(certPass, keyStorePass, keyStoreFile, trustStoreFile, trustStorePass,
                parameters);
    }


}
