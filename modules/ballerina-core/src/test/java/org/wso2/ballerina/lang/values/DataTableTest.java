/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.lang.values;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.core.utils.XMLUtils;
import org.wso2.ballerina.lang.util.Functions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test Native functions in ballerina.lang.datatable.
 */
public class DataTableTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.system:print_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/datatableTest.bal", symScope);
        initDatabase();
    }

    @Test(description = "Check getByIndex methods for primitive types.")
    public void testGetXXXByIndex() {
        BValue[] returns = Functions.invoke(bFile, "getXXXByIndex");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BLong) returns[1]).longValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34f);
        Assert.assertEquals(((BDouble) returns[3]).doubleValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(description = "Check getByName methods for primitive types.")
    public void testGetXXXByName() {
        BValue[] returns = Functions.invoke(bFile, "getXXXByName");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BLong) returns[1]).longValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34f);
        Assert.assertEquals(((BDouble) returns[3]).doubleValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(description = "Check toJson methods.")
    public void toJson() {
        BValue[] returns = Functions.invoke(bFile, "toJson");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,"
                        + "\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(description = "Check toXml methods with wrapper element.")
    public void toXmlWithWrapper() {
        BValue[] returns = Functions.invoke(bFile, "toXmlWithWrapper");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></type></types>");
    }

    @Test(description = "Check getByName methods for complex types.")
    public void testGetByName() {
        BValue[] returns = Functions.invoke(bFile, "getByName");

        Assert.assertEquals(returns.length, 5);
        // Create text file with some content. Generate Hex value of that. Insert to database.
        // Implementation will return base64encoded value of that text content. Verify that value.
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "dmVyeSBsb25nIHRleHQ=");
        Assert.assertEquals(((BLong) returns[2]).longValue(), 21945000);
        Assert.assertEquals(((BLong) returns[3]).longValue(), 1486060200000L);
        Assert.assertEquals(((BLong) returns[4]).longValue(), 1486102980000L);
    }

    @Test(description = "Check getByName methods for complex types.")
    public void testGetByIndex() {
        BValue[] returns = Functions.invoke(bFile, "getByIndex");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "dmVyeSBsb25nIHRleHQ=");
        Assert.assertEquals(((BLong) returns[2]).longValue(), 21945000);
        Assert.assertEquals(((BLong) returns[3]).longValue(), 1486060200000L);
        Assert.assertEquals(((BLong) returns[4]).longValue(), 1486102980000L);
    }

    @Test(description = "Check getObjectAsStringByName methods for complex types.")
    public void getObjectAsStringByName() {
        BValue[] returns = Functions.invoke(bFile, "getObjectAsStringByName");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "dmVyeSBsb25nIHRleHQ=");
        Assert.assertEquals(returns[2].stringValue(), "21945000");
        Assert.assertEquals(returns[3].stringValue(), "1486060200000");
        Assert.assertEquals(returns[4].stringValue(), "1486102980000");
    }

    @Test(description = "Check getObjectAsStringByIndex methods for complex types.")
    public void getObjectAsStringByIndex() {
        BValue[] returns = Functions.invoke(bFile, "getObjectAsStringByIndex");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "dmVyeSBsb25nIHRleHQ=");
        Assert.assertEquals(returns[2].stringValue(), "21945000");
        Assert.assertEquals(returns[3].stringValue(), "1486060200000");
        Assert.assertEquals(returns[4].stringValue(), "1486102980000");
    }

    @Test(description = "Check getXXXArray methods for complex types.")
    public void getArrayByName() {
        BValue[] returns = Functions.invoke(bFile, "getArrayByName");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0] instanceof BArray);
        BArray<BInteger> intArray = (BArray<BInteger>) returns[0];
        Assert.assertTrue(intArray.get(0) instanceof BInteger);
        Assert.assertEquals(intArray.get(0).intValue(), 1);
        Assert.assertEquals(intArray.get(1).intValue(), 2);
        Assert.assertEquals(intArray.get(2).intValue(), 3);

        Assert.assertTrue(returns[1] instanceof BArray);
        BArray<BLong> longArray = (BArray<BLong>) returns[1];
        Assert.assertTrue(longArray.get(0) instanceof BLong);
        Assert.assertEquals(longArray.get(0).longValue(), 100000000);
        Assert.assertEquals(longArray.get(1).longValue(), 200000000);
        Assert.assertEquals(longArray.get(2).longValue(), 300000000);

        Assert.assertTrue(returns[2] instanceof BArray);
        BArray<BDouble> doubleArray = (BArray<BDouble>) returns[2];
        Assert.assertTrue(doubleArray.get(0) instanceof BDouble);
        Assert.assertEquals(doubleArray.get(0).doubleValue(), 245.23);
        Assert.assertEquals(doubleArray.get(1).doubleValue(), 5559.49);
        Assert.assertEquals(doubleArray.get(2).doubleValue(), 8796.123);

        Assert.assertTrue(returns[3] instanceof BArray);
        BArray<BString> stringArray = (BArray<BString>) returns[3];
        Assert.assertTrue(stringArray.get(0) instanceof BString);
        Assert.assertEquals(stringArray.get(0).stringValue(), "Hello");
        Assert.assertEquals(stringArray.get(1).stringValue(), "Ballerina");


        Assert.assertTrue(returns[4] instanceof BArray);
        BArray<BBoolean> booleanArray = (BArray<BBoolean>) returns[4];
        Assert.assertTrue(booleanArray.get(0) instanceof BBoolean);
        Assert.assertEquals(booleanArray.get(0).booleanValue(), true);
        Assert.assertEquals(booleanArray.get(1).booleanValue(), false);
        Assert.assertEquals(booleanArray.get(2).booleanValue(), true);
    }

    @Test(description = "Check getXXXArray methods for complex types.")
    public void getArrayByIndex() {
        BValue[] returns = Functions.invoke(bFile, "getArrayByIndex");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0] instanceof BArray);
        BArray<BInteger> intArray = (BArray<BInteger>) returns[0];
        Assert.assertTrue(intArray.get(0) instanceof BInteger);
        Assert.assertEquals(intArray.get(0).intValue(), 1);
        Assert.assertEquals(intArray.get(1).intValue(), 2);
        Assert.assertEquals(intArray.get(2).intValue(), 3);

        Assert.assertTrue(returns[1] instanceof BArray);
        BArray<BLong> longArray = (BArray<BLong>) returns[1];
        Assert.assertTrue(longArray.get(0) instanceof BLong);
        Assert.assertEquals(longArray.get(0).longValue(), 100000000);
        Assert.assertEquals(longArray.get(1).longValue(), 200000000);
        Assert.assertEquals(longArray.get(2).longValue(), 300000000);

        Assert.assertTrue(returns[2] instanceof BArray);
        BArray<BDouble> doubleArray = (BArray<BDouble>) returns[2];
        Assert.assertTrue(doubleArray.get(0) instanceof BDouble);
        Assert.assertEquals(doubleArray.get(0).doubleValue(), 245.23);
        Assert.assertEquals(doubleArray.get(1).doubleValue(), 5559.49);
        Assert.assertEquals(doubleArray.get(2).doubleValue(), 8796.123);

        Assert.assertTrue(returns[3] instanceof BArray);
        BArray<BString> stringArray = (BArray<BString>) returns[3];
        Assert.assertTrue(stringArray.get(0) instanceof BString);
        Assert.assertEquals(stringArray.get(0).stringValue(), "Hello");
        Assert.assertEquals(stringArray.get(1).stringValue(), "Ballerina");


        Assert.assertTrue(returns[4] instanceof BArray);
        BArray<BBoolean> booleanArray = (BArray<BBoolean>) returns[4];
        Assert.assertTrue(booleanArray.get(0) instanceof BBoolean);
        Assert.assertEquals(booleanArray.get(0).booleanValue(), true);
        Assert.assertEquals(booleanArray.get(1).booleanValue(), false);
        Assert.assertEquals(booleanArray.get(2).booleanValue(), true);
    }

    @AfterSuite
    public void cleanup() {
        //DeleteDbFiles.execute("./target/TEST_DATA_TABLE_DB2", null, true);
    }

    private void initDatabase() {
        Connection connection = null;
        Statement st = null;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection("jdbc:hsqldb:file:./target/TEST_DATA_TABLE_DB2", "root", "root");
            String sql = XMLUtils.readFileToString("datafiles/DataTableDataFile.sql");
            String[] sqlQuery = sql.split(";");

            st = connection.createStatement();
            for (String query : sqlQuery) {
                st.executeUpdate(query.trim());
            }
        } catch (ClassNotFoundException | SQLException e) {
            //Do nothing
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                //Do nothing
            }
        }
    }
}
