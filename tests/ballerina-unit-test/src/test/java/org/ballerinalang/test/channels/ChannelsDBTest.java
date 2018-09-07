/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.test.channels;

import com.zaxxer.hikari.HikariConfig;
import org.ballerinalang.channels.ChannelConstants;
import org.ballerinalang.channels.DatabaseUtils;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test channels with DB configurations.
 *
 * @since 0.982.0
 */
public class ChannelsDBTest {

    private SQLDBUtils.TestDatabase hSqlDatabase;
    private CompileResult result;
    private ConfigRegistry reg = ConfigRegistry.getInstance();
    private static final String DB_DIRECTORY = "./target/tempdb/";
    private static final String DB_NAME = "TEST_SQL_CHANNEL";
    private static final String CHANNEL_TEST = "ChannelsTest";

    @BeforeClass
    public void setup() {

        hSqlDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.HSQLDB, "datafiles/sql" +
                "/ChannelsSqlDataFile.sql", DB_DIRECTORY, DB_NAME);
        result = BCompileUtil.compile("test-src/channels/channel-worker-interactions.bal");
    }

    @Test(description = "Test channels with HSQL config", groups = CHANNEL_TEST)
    public void hSqlTest() {

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE,
                ChannelConstants.DBTypes.HSQLDB_FILE);
        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_HOST_OR_PATH, DB_DIRECTORY);
        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_NAME, DB_NAME);
        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PASSWORD,
                hSqlDatabase.getPassword());
        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_USERNAME,
                hSqlDatabase.getUsername());
        BValue[] returns = BRunUtil.invoke(result, "multipleInteractions");
        Assert.assertEquals(returns[0].stringValue(), "{\"payment\":50000}", "Channels with HSQL config failed");
    }

    @Test(description = "Test jdbc url creation")
    public void jdbcUrlGenerationTest() {

        DatabaseUtils.config = new HikariConfig();
        reg.resetRegistry();
        String url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:h2:mem:channels");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "MYSQL");
        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_HOST_OR_PATH, "localhost");
        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_NAME, "channels");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:mysql://localhost:3306/channels", "MYSQL JDBC url with default port");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT, "9000");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:mysql://localhost:9000/channels", "MYSQL JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "SQLSERVER");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:sqlserver://localhost:9000;databaseName=channels", "SQLSERVER JDBC url with " +
                "default port");

        reg.removeConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT);
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:sqlserver://localhost:1433;databaseName=channels", "SQLSERVER JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "ORACLE");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:oracle:thin:null/null@localhost:1521/channels", "ORALE JDBC url with default " +
                "port");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT, "9000");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:oracle:thin:null/null@localhost:9000/channels", "ORALE JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "SYBASE");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:sybase:Tds:localhost:9000/channels", "SyBase JDBC url");

        reg.removeConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT);
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:sybase:Tds:localhost:5000/channels", "SyBase JDBC url with default port");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "POSTGRESQL");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:postgresql://localhost:5432/channels", "Postgress JDBC url with default port");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT, "9000");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:postgresql://localhost:9000/channels", "Postgress JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "DB2");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:db2:localhost:9000/channels", "DB2 JDBC url");

        reg.removeConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT);
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:db2:localhost:50000/channels", "BB2 JDBC url with default port");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "HSQLDB_SERVER");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:hsqldb:hsql://localhost:9001/channels", "HSQLDB server JDBC url with default " +
                "port");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT, "9000");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:hsqldb:hsql://localhost:9000/channels", "HSQLDB server JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "HSQLDB_FILE");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:hsqldb:file:localhost" + File.separator + "channels", "HSQLDB file JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "H2_server");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:h2:tcp:localhost:9000/channels", "H2 server JDBC url");

        reg.removeConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT);
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:h2:tcp:localhost:9092/channels", "H2 server JDBC url with default port");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "H2_file");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:h2:file:localhost" + File.separator + "channels", "H2 file JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "H2_memory");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:h2:mem:channels", "H2 memory JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "DERBY_SERVER");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:derby:localhost:1527/channels", "Derby  server JDBC url with default port");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PORT, "9000");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:derby:localhost:9000/channels", "Derby server server JDBC url");

        reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "DERBY_FILE");
        url = DatabaseUtils.getJDBCURL();
        Assert.assertEquals(url, "jdbc:derby:localhost" + File.separator + "channels", "Derby server server JDBC url");

        try {
            reg.addConfiguration(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB_TYPE, "unknown");
            DatabaseUtils.getJDBCURL();
        } catch (BallerinaException balException) {
            Assert.assertTrue(balException.getMessage().contains("cannot generate url for unknown database type : " +
                    "UNKNOWN"), "Unsupported db type");
            return;
        }

        Assert.fail("Unknown Db type config test");
    }

    @AfterClass
    public void reset() {
        reg.resetRegistry();
    }
}

