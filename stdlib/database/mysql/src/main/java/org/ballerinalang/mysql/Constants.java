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
        static final String HOST = "host";
        static final String PORT = "port";
        static final String USER = "user";
        static final String PASSWORD = "password";
        static final String DATABASE = "database";
        static final String OPTIONS = "options";
        static final String CONNECTION_POOL_OPTIONS = "connPool";
    }

    /**
     * Constants for database options.
     */
    public static final class Options {
        static final String SSL = "ssl";
        static final String USE_XA_DATASOURCE = "useXADatasource";
        static final String CONNECT_TIMEOUT_SECONDS = "connectTimeoutInSeconds";
        static final String SOCKET_TIMEOUT_SECONDS = "socketTimeoutInSeconds";
    }

    /**
     * Constants for ssl configuration.
     */
    static final class SSLConfig {
        static final String MODE = "mode";
        static final String VERIFY_CERT_MODE = "VERIFY_CERT";
        static final String CLIENT_CERT_KEYSTORE = "clientCertKeystore";
        static final String TRUST_CERT_KEYSTORE = "trustCertKeystore";
    }

    static final class DatabaseProps {
        static final String SSL_MODE = "sslMode";
        static final String SSL_MODE_DISABLED = "DISABLED";
        static final String SSL_MODE_VERIFY_CA = "VERIFY_CA";

        static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";
        static final String CLIENT_KEYSTORE_URL = "clientCertificateKeyStoreUrl";
        static final String CLIENT_KEYSTORE_PASSWORD = "clientCertificateKeyStorePassword";
        static final String CLIENT_KEYSTORE_TYPE = "clientCertificateKeyStoreType";
        static final String TRUST_KEYSTORE_URL = "trustCertificateKeyStoreUrl";
        static final String TRUST_KEYSTORE_PASSWORD = "trustCertificateKeyStorePassword";
        static final String TRUST_KEYSTORE_TYPE = "trustCertificateKeyStoreType";

        static final String CONNECT_TIMEOUT = "connectTimeout";
        static final String SOCKET_TIMEOUT = "socketTimeout";
    }

    static final String MYSQL_DATASOURCE_NAME = "com.mysql.cj.jdbc.MysqlDataSource";
    static final String MYSQL_XA_DATASOURCE_NAME = "com.mysql.cj.jdbc.MysqlXADataSource";
}
