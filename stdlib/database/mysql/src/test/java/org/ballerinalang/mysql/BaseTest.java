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

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.ScriptResolver;
import com.wix.mysql.config.DownloadConfig;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;
import org.ballerinalang.mysql.utils.SQLDBUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This test class includes base test case that initializes the mysql database.
 *
 * @since 1.2.0
 */
public class BaseTest {

    private static final Map<String, String> schemas = new HashMap<>();
    private EmbeddedMysql mysql;

    @BeforeSuite
    public void setup() {
        DownloadConfig downloadConfig = DownloadConfig.aDownloadConfig()
                .withCacheDir(System.getProperty("java.io.tmpdir"))
                .build();

        MysqldConfig config = MysqldConfig.aMysqldConfig(Version.v8_latest)
                .withPort(SQLDBUtils.DB_PORT)
                .withUser(SQLDBUtils.DB_USER_NAME, SQLDBUtils.DB_USER_PW)
                .withTimeZone("Europe/Vilnius")
                .withTimeout(2, TimeUnit.MINUTES)
                .withServerVariable("ssl_ca", new File("src/test/resources/keystore/server/ca-cert.pem")
                        .getAbsolutePath())
                .withServerVariable("ssl_cert", new File("src/test/resources/keystore/server/server-cert.pem")
                        .getAbsolutePath())
                .withServerVariable("ssl_key", new File("src/test/resources/keystore/server/server-key.pem")
                        .getAbsolutePath())
                .build();
        final EmbeddedMysql.Builder mysqlBuilder = EmbeddedMysql.anEmbeddedMysql(config, downloadConfig);
        schemas.forEach(((k, v) -> {
            mysqlBuilder.addSchema(k, ScriptResolver.classPathScript(v));
        }));
        mysql = mysqlBuilder.start();
    }

    public static void addDBSchema(String dbName, String sqlScript) {
        schemas.put(dbName, sqlScript);
    }

    @AfterSuite
    public void stop() {
        mysql.stop();
    }

}
