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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.privacy;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.ballerinalang.test.utils.SQLDBUtils.DBType;
import org.ballerinalang.test.utils.SQLDBUtils.TestDatabase;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.stream.Stream;

/**
 * Test class for Privacy API.
 */
public class JdbcDatabasePIIStoreTest {

    private DBType dbType;
    private TestDatabase testDatabase;
    private BValue[] connectionArgs = new BValue[3];

    private CompileResult result;
    private static final String DB_NAME_H2 = "TestDB_H2";
    private static final String DB_NAME_HSQLDB = "TestDB_HSQL";
    private static final String DB_SCRIPT_FILE_PATH = "datafiles/privacy/PII_Store_Table_Create.sql";

    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static final String SAMPLE_PII_VALUE = "Personally Identifiable Information";

    @Parameters({ "dataClientTestDBType" })
    public JdbcDatabasePIIStoreTest(@Optional("HSQLDB") DBType dataClientTestDBType) {
        this.dbType = dataClientTestDBType;
    }

    @BeforeClass
    public void setup() {
        switch (dbType) {
            case MYSQL:
            case POSTGRES:
                testDatabase = new SQLDBUtils.ContainerizedTestDatabase(dbType, DB_SCRIPT_FILE_PATH);
                break;
            case H2:
                testDatabase = new SQLDBUtils.FileBasedTestDatabase(dbType, DB_SCRIPT_FILE_PATH,
                        SQLDBUtils.DB_DIRECTORY_H2_1, DB_NAME_H2);
                break;
            case HSQLDB:
                testDatabase = new SQLDBUtils.FileBasedTestDatabase(dbType, DB_SCRIPT_FILE_PATH,
                        SQLDBUtils.DB_DIRECTORY, DB_NAME_HSQLDB);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }

        connectionArgs[0] = new BString(testDatabase.getJDBCUrl());
        connectionArgs[1] = new BString(testDatabase.getUsername());
        connectionArgs[2] = new BString(testDatabase.getPassword());

        result = BCompileUtil.compile("test-src/privacy/jdbc_pii_store.bal");
    }

    @Test
    public void testPseudonymizeValidPii() {
        BValue[] args = { new BString(SAMPLE_PII_VALUE) };
        args = Stream.of(connectionArgs, args).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] returns = BRunUtil.invokeFunction(result, "pseudonymizePii", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(((BString) returns[0]).value().matches(UUID_REGEX));
    }

    @Test
    public void testDepseudonymizeValidId() {
        BValue[] args = { new BString(SAMPLE_PII_VALUE) };
        args = Stream.of(connectionArgs, args).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] returns = BRunUtil.invokeFunction(result, "pseudonymizePii", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(((BString) returns[0]).value().matches(UUID_REGEX));

        BValue[] depseudonymizeArgs = { returns[0] };
        depseudonymizeArgs = Stream.of(connectionArgs, depseudonymizeArgs).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] depseudonymizeReturns = BRunUtil.invokeFunction(result, "depseudonymizePii", depseudonymizeArgs);
        Assert.assertEquals(depseudonymizeReturns.length, 1);
        Assert.assertTrue(depseudonymizeReturns[0] instanceof BString);
        Assert.assertTrue(((BString) depseudonymizeReturns[0]).value().equals(SAMPLE_PII_VALUE));
    }

    @Test
    public void testDepseudonymizeInvalidId() {
        String invalidId = "12345";
        BValue[] args = { new BString(invalidId) };
        args = Stream.of(connectionArgs, args).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] returns = BRunUtil.invokeFunction(result, "depseudonymizePii", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(((BMap) returns[0]).get("message").stringValue(),
                "Identifier " + invalidId + " is not found in PII store");
    }

    @Test
    public void testDeleteValidId() {
        BValue[] args = { new BString(SAMPLE_PII_VALUE) };
        args = Stream.of(connectionArgs, args).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] returns = BRunUtil.invokeFunction(result, "pseudonymizePii", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(((BString) returns[0]).value().matches(UUID_REGEX));

        BValue[] deleteArgs = Stream.of(connectionArgs, returns).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] deleteReturns = BRunUtil.invokeFunction(result, "deletePii", deleteArgs);
        Assert.assertEquals(deleteReturns.length, 1);
        Assert.assertNull(deleteReturns[0]);

        BValue[] depseudonymizeArgs = deleteArgs;
        BValue[] depseudonymizeReturns = BRunUtil.invokeFunction(result, "depseudonymizePii", depseudonymizeArgs);
        Assert.assertEquals(depseudonymizeReturns.length, 1);
        Assert.assertTrue(depseudonymizeReturns[0] instanceof BMap);
        Assert.assertEquals(((BMap) depseudonymizeReturns[0]).get("message").stringValue(),
                "Identifier " + ((BString) returns[0]).value() + " is not found in PII store");
    }

    @Test
    public void testDeleteInvalidId() {
        String invalidId = "12345";
        BValue[] args = { new BString(invalidId) };
        args = Stream.of(connectionArgs, args).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] returns = BRunUtil.invokeFunction(result, "deletePii", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(((BMap) returns[0]).get("message").stringValue(),
                "Identifier " + invalidId + " is not found in PII store");
    }

    @Test
    public void testPseudonymizeSamePiiTwice() {
        BValue[] args1 = { new BString(SAMPLE_PII_VALUE) };
        args1 = Stream.of(connectionArgs, args1).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] returns1 = BRunUtil.invokeFunction(result, "pseudonymizePii", args1);
        Assert.assertEquals(returns1.length, 1);
        Assert.assertTrue(returns1[0] instanceof BString);
        Assert.assertTrue(((BString) returns1[0]).value().matches(UUID_REGEX));

        BValue[] args2 = { new BString(SAMPLE_PII_VALUE) };
        args2 = Stream.of(connectionArgs, args2).flatMap(Stream::of).toArray(BValue[]::new);
        BValue[] returns2 = BRunUtil.invokeFunction(result, "pseudonymizePii", args2);
        Assert.assertEquals(returns2.length, 1);
        Assert.assertTrue(returns2[0] instanceof BString);
        Assert.assertTrue(((BString) returns2[0]).value().matches(UUID_REGEX));

        Assert.assertNotEquals(((BString) returns1[0]).value(), ((BString) returns2[0]).value());
    }

    @Test (expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error:.*caused by error, message: table name is required.*")
    public void testEmptyTableName() {
        BValue[] args = { new BString(SAMPLE_PII_VALUE) };
        args = Stream.of(connectionArgs, args).flatMap(Stream::of).toArray(BValue[]::new);
        BRunUtil.invokeFunction(result, "pseudonymizePiiWithEmptyTableName", args);
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
