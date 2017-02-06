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
//import org.testng.annotations.Test;
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
        //initDatabase();
    }

    //@Test(description = "Check getByIndex methods for primitive types.")
    public void testGetXXXByIndex() {
        BValue[] returns = Functions.invoke(bFile, "getXXXByIndex");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BLong) returns[1]).longValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34f);
        Assert.assertEquals(((BDouble) returns[3]).doubleValue(), 9223372036854774807.75D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    //@Test(description = "Check getByName methods for primitive types.")
    public void testGetXXXByName() {
        BValue[] returns = Functions.invoke(bFile, "getXXXByName");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BLong) returns[1]).longValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34f);
        Assert.assertEquals(((BDouble) returns[3]).doubleValue(), 9223372036854774807.75D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    //@Test(description = "Check toJson methods.")
    public void toJson() {
        BValue[] returns = Functions.invoke(bFile, "toJson");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,"
                        + "\"DOUBLE_TYPE\":9.2233720368547748E18,\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\"}]");
    }

    //@Test(description = "Check toXml methods with wrapper element.")
    public void toXmlWithWrapper() {
        BValue[] returns = Functions.invoke(bFile, "toXmlWithWrapper");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>9.2233720368547748E18</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></type></types>");
    }

    //@Test(description = "Check getByName methods for complex types.")
    public void testGetByName() {
        BValue[] returns = Functions.invoke(bFile, "getByName");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals((returns[0]).stringValue(), "AHQAZQBzAHQAIAB2AGEAbAB1AGU=");
        //        Assert.assertEquals(( returns[1]).stringValue(), "very long text");
        Assert.assertEquals(((BLong) returns[2]).longValue(), 21945000);
        Assert.assertEquals(((BLong) returns[3]).longValue(), 1486060200000L);
        Assert.assertEquals(((BLong) returns[4]).longValue(), 1486102980000L);
    }

    //@Test(description = "Check getByName methods for complex types.")
    public void testGetByIndex() {
        BValue[] returns = Functions.invoke(bFile, "getByIndex");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals((returns[0]).stringValue(), "AHQAZQBzAHQAIAB2AGEAbAB1AGU=");
        //        Assert.assertEquals(( returns[1]).stringValue(), "very long text");
        Assert.assertEquals(((BLong) returns[2]).longValue(), 21945000);
        Assert.assertEquals(((BLong) returns[3]).longValue(), 1486060200000L);
        Assert.assertEquals(((BLong) returns[4]).longValue(), 1486102980000L);
    }

    //@Test(description = "Check getObjectAsStringByName methods for complex types.")
    public void getObjectAsStringByName() {
        BValue[] returns = Functions.invoke(bFile, "getObjectAsStringByName");

        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals((returns[0]).stringValue(), "AHQAZQBzAHQAIAB2AGEAbAB1AGU=");
        Assert.assertEquals(returns[1].stringValue(), "21945000");
        Assert.assertEquals(returns[2].stringValue(), "1486060200000");
        Assert.assertEquals(returns[3].stringValue(), "1486102980000");
    }

    //@Test(description = "Check getObjectAsStringByIndex methods for complex types.")
    public void getObjectAsStringByIndex() {
        BValue[] returns = Functions.invoke(bFile, "getObjectAsStringByIndex");

        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals((returns[0]).stringValue(), "AHQAZQBzAHQAIAB2AGEAbAB1AGU=");
        Assert.assertEquals(returns[1].stringValue(), "21945000");
        Assert.assertEquals(returns[2].stringValue(), "1486060200000");
        Assert.assertEquals(returns[3].stringValue(), "1486102980000");
    }

//    @Test(description = "Check getObjectAsStringByIndex methods for complex types.")
    public void getArrayByName() {
        BValue[] returns = Functions.invoke(bFile, "getArray");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BArray);
    }


    @AfterSuite
    public void cleanup() {
        //DeleteDbFiles.execute("./target/TEST_DATA_TABLE_DB2", null, true);
    }

    private void initDatabase() {
        Connection connection = null;
        Statement st = null;
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "root", "root");
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
