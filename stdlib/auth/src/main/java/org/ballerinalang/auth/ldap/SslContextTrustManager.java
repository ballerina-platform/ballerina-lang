/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.auth.ldap;

import org.ballerinalang.auth.ldap.util.LdapUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;

/**
 * A class that encapsulates SSL Certificate Information.
 *
 * @since 0.983.0
 */
public class SslContextTrustManager {

    private static SslContextTrustManager sslContextTrustManager;
    private Map<String, SSLContext> sslContextMap = new ConcurrentHashMap<>();

    private SslContextTrustManager() {
    }

    public static SslContextTrustManager getInstance() {
        if (sslContextTrustManager != null) {
            return sslContextTrustManager;
        }
        synchronized (SslContextTrustManager.class) {
            if (sslContextTrustManager == null) {
                sslContextTrustManager = new SslContextTrustManager();
            }
        }
        return sslContextTrustManager;
    }

    public SSLContext getSSLContext(String contextId) {
        return SslContextTrustManager.getInstance().getSslContextMap().get(contextId);
    }

    public void addSSLContext(String key, SSLContext sslContext) {
        SslContextTrustManager.getInstance().getSslContextMap().put(key, sslContext);
    }

    public void removeSSLContext(String contextId) {
        if (LdapUtils.isNullOrEmptyAfterTrim(contextId)) {
            SslContextTrustManager.getInstance().getSslContextMap().remove(contextId);
        }
    }

    public Map<String, SSLContext> getSslContextMap() {
        return sslContextMap;
    }
}
