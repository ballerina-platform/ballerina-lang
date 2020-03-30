/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.mysql;

import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

/**
 * This class includes utility functions.
 *
 * @since 1.2.0
 */
public class Utils {

    static MapValue generateOptionsMap(MapValue mysqlOptions) {
        if (mysqlOptions != null) {
            MapValue<String, Object> options = new MapValueImpl<>();
            addSSLOptions(mysqlOptions.getMapValue(Constants.Options.SSL), options);

            long connectTimeout = getTimeout(mysqlOptions.get(Constants.Options.CONNECT_TIMEOUT_SECONDS));
            if (connectTimeout > 0) {
                options.put(Constants.DatabaseProps.CONNECT_TIMEOUT, connectTimeout);
            }

            long socketTimeout = getTimeout(mysqlOptions.get(Constants.Options.SOCKET_TIMEOUT_SECONDS));
            if (socketTimeout > 0) {
                options.put(Constants.DatabaseProps.SOCKET_TIMEOUT, socketTimeout);
            }
            return options;
        }
        return null;
    }

    public static long getTimeout(Object secondsDecimal) {
        if (secondsDecimal instanceof DecimalValue) {
            DecimalValue timeoutSec = (DecimalValue) secondsDecimal;
            if (timeoutSec.floatValue() > 0) {
                return Double.valueOf(timeoutSec.floatValue() * 1000).longValue();
            }
        }
        return -1;
    }

    private static void addSSLOptions(MapValue sslConfig, MapValue<String, Object> options) {
        if (sslConfig == null) {
            options.put(Constants.DatabaseProps.SSL_MODE, Constants.DatabaseProps.SSL_MODE_DISABLED);
        } else {
            String mode = sslConfig.getStringValue(Constants.SSLConfig.MODE);
            if (mode.equalsIgnoreCase(Constants.SSLConfig.VERIFY_CERT_MODE)) {
                mode = Constants.DatabaseProps.SSL_MODE_VERIFY_CA;
            }
            options.put(Constants.DatabaseProps.SSL_MODE, mode);

            MapValue clientCertKeystore = sslConfig.getMapValue(Constants.SSLConfig.CLIENT_CERT_KEYSTORE);
            if (clientCertKeystore != null) {
                options.put(Constants.DatabaseProps.CLIENT_KEYSTORE_URL, Constants.FILE + clientCertKeystore
                        .getStringValue(org.ballerinalang.stdlib.crypto.Constants.KEY_STORE_RECORD_PATH_FIELD));
                options.put(Constants.DatabaseProps.CLIENT_KEYSTORE_PASSWORD, clientCertKeystore
                        .getStringValue(org.ballerinalang.stdlib.crypto.Constants.KEY_STORE_RECORD_PASSWORD_FIELD));
                options.put(Constants.DatabaseProps.CLIENT_KEYSTORE_TYPE, Constants.DatabaseProps.KEYSTORE_TYPE_PKCS12);
            }

            MapValue trustCertKeystore = sslConfig.getMapValue(Constants.SSLConfig.TRUST_CERT_KEYSTORE);
            if (trustCertKeystore != null) {
                options.put(Constants.DatabaseProps.TRUST_KEYSTORE_URL, Constants.FILE + trustCertKeystore
                        .getStringValue(org.ballerinalang.stdlib.crypto.Constants.KEY_STORE_RECORD_PATH_FIELD));
                options.put(Constants.DatabaseProps.TRUST_KEYSTORE_PASSWORD, trustCertKeystore
                        .getStringValue(org.ballerinalang.stdlib.crypto.Constants.KEY_STORE_RECORD_PASSWORD_FIELD));
                options.put(Constants.DatabaseProps.TRUST_KEYSTORE_TYPE, Constants.DatabaseProps.KEYSTORE_TYPE_PKCS12);
            }
        }
    }
}
