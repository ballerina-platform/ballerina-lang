package org.ballerinalang.test.channels;

import org.ballerinalang.channels.ChannelConstants;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test channels with DB configurations.
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
}
