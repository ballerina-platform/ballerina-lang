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
package org.wso2.carbon.transport.http.netty.listener.ssl;

import java.io.File;

/**
 * A class that encapsulates SSLContext configuration.
 */

public class SSLConfig {
    private File keyStore;
    private String keyStorePass;
    private String certPass;
    private File trustStore;
    private String trustStorePass;

    public SSLConfig(File keyStore, String keyStorePass) {
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
}
