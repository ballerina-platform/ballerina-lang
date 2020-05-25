/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.mysql;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.api.BString;

/**
 * Constants for JDBC client.
 *
 * @since 1.2.0
 */
public final class Constants {
    /**
     * Constants for Client Configs.
     */
    public static final class ClientConfiguration {
        static final BString HOST = StringUtils.fromString("host");
        static final BString PORT = StringUtils.fromString("port");
        static final BString USER = StringUtils.fromString("user");
        static final BString PASSWORD = StringUtils.fromString("password");
        static final BString DATABASE = StringUtils.fromString("database");
        static final BString OPTIONS = StringUtils.fromString("options");
        static final BString CONNECTION_POOL_OPTIONS = StringUtils.fromString("connectionPool");
    }

    /**
     * Constants for database options.
     */
    public static final class Options {
        static final BString SSL = StringUtils.fromString("ssl");
        static final BString USE_XA_DATASOURCE = StringUtils.fromString("useXADatasource");
        static final BString CONNECT_TIMEOUT_SECONDS = StringUtils.fromString("connectTimeoutInSeconds");
        static final BString SOCKET_TIMEOUT_SECONDS = StringUtils.fromString("socketTimeoutInSeconds");
    }

    /**
     * Constants for ssl configuration.
     */
    static final class SSLConfig {
        static final BString MODE = StringUtils.fromString("mode");
        static final String VERIFY_CERT_MODE = "VERIFY_CERT";
        static final BString CLIENT_CERT_KEYSTORE = StringUtils.fromString("clientCertKeystore");
        static final BString TRUST_CERT_KEYSTORE = StringUtils.fromString("trustCertKeystore");
    }

    static final class DatabaseProps {
        static final BString SSL_MODE = StringUtils.fromString("sslMode");
        static final BString SSL_MODE_DISABLED = StringUtils.fromString("DISABLED");
        static final BString SSL_MODE_VERIFY_CA = StringUtils.fromString("VERIFY_CA");

        static final BString KEYSTORE_TYPE_PKCS12 = StringUtils.fromString("PKCS12");
        static final BString CLIENT_KEYSTORE_URL = StringUtils.fromString("clientCertificateKeyStoreUrl");
        static final BString CLIENT_KEYSTORE_PASSWORD = StringUtils.fromString("clientCertificateKeyStorePassword");
        static final BString CLIENT_KEYSTORE_TYPE = StringUtils.fromString("clientCertificateKeyStoreType");
        static final BString TRUST_KEYSTORE_URL = StringUtils.fromString("trustCertificateKeyStoreUrl");
        static final BString TRUST_KEYSTORE_PASSWORD = StringUtils.fromString("trustCertificateKeyStorePassword");
        static final BString TRUST_KEYSTORE_TYPE = StringUtils.fromString("trustCertificateKeyStoreType");

        static final BString CONNECT_TIMEOUT = StringUtils.fromString("connectTimeout");
        static final BString SOCKET_TIMEOUT = StringUtils.fromString("socketTimeout");
    }

    static final String MYSQL_DATASOURCE_NAME = "com.mysql.cj.jdbc.MysqlDataSource";
    static final String MYSQL_XA_DATASOURCE_NAME = "com.mysql.cj.jdbc.MysqlXADataSource";
    static final String FILE = "file:";
    static final String POOL_CONNECT_TIMEOUT = "ConnectionTimeout";
}
