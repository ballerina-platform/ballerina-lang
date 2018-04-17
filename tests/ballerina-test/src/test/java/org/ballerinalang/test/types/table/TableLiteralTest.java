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
package org.ballerinalang.test.types.table;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * Class to test table literal.
 */
public class TableLiteralTest {

    private CompileResult result;
    private CompileResult resultHelper;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table-literal.bal");
        resultHelper = BCompileUtil.compile("test-src/types/table/table-test-helper.bal");
    }

    @Test(enabled = false) //Issue #5106
    public void testEmptyTableCreate() {
        BValue[] returns = BRunUtil.invoke(result, "testEmptyTableCreate");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(priority = 1)
    public void testAddData() {
        BValue[] returns = BRunUtil.invoke(result, "testAddData");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BIntArray) returns[3]).get(0), 1);
        Assert.assertEquals(((BIntArray) returns[3]).get(1), 2);
        Assert.assertEquals(((BIntArray) returns[4]).get(0), 3);
        Assert.assertEquals(((BIntArray) returns[5]).get(0), 100);
    }

    @Test(priority = 2)
    public void testTableDrop() {
        BRunUtil.invoke(result, "testTableDrop");
        //Table count before garbage collection happens.
        BValue[] args = new BValue[1];
        args[0] = new BString("TABLE_PERSON_%");
        BValue[] returns = BRunUtil.invoke(resultHelper, "getTableCount", args);
        long beforeCount = ((BInteger) returns[0]).intValue();
        //Request for garbage collection process.
        System.gc();
        //Check whether table has drop
        await().atMost(30, SECONDS).until(() -> {
            BValue[] returnVal = BRunUtil.invoke(resultHelper, "getTableCount", args);
            long afterCount = ((BInteger) returnVal[0]).intValue();
            return afterCount < beforeCount;
        });

        returns = BRunUtil.invoke(resultHelper, "getTableCount", args);
        long afterCount = ((BInteger) returns[0]).intValue();
        Assert.assertTrue(beforeCount > afterCount);
    }

    @Test(priority = 1)
    public void testPrintData() throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BRunUtil.invoke(result, "testPrintData");
            Assert.assertEquals(outContent.toString(),
                    "table<Person> {index: [\"id\", \"age\"], primaryKey: [\"id\", \"age\"], data: [{id:1, age:30, "
                            + "salary:300.5, name:\"jane\", married:true}, {id:2, age:20, salary:200.5, "
                            + "name:\"martin\", married:true}, {id:3, age:32, salary:100.5, name:\"john\", "
                            + "married:false}]}\n");
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(priority = 1)
    public void testPrintDataEmptyTable() throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BRunUtil.invoke(result, "testPrintDataEmptyTable");
            Assert.assertEquals(outContent.toString(),
                    "table<Person> {index: [], primaryKey: [], data: []}\n");
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(priority = 1)
    public void testMultipleAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleAccess");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BIntArray) returns[2]).get(0), 1);
        Assert.assertEquals(((BIntArray) returns[2]).get(1), 2);
        Assert.assertEquals(((BIntArray) returns[2]).get(2), 3);
        Assert.assertEquals(((BIntArray) returns[3]).get(0), 1);
        Assert.assertEquals(((BIntArray) returns[3]).get(1), 2);
        Assert.assertEquals(((BIntArray) returns[3]).get(2), 3);
    }

    @Test(priority = 1)
    public void testLoopingTable() {
        BValue[] returns = BRunUtil.invoke(result, "testLoopingTable");
        Assert.assertEquals((returns[0]).stringValue(), "jane_martin_john_");
    }

    @Test(priority = 1)
    public void testToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testToJson");
        Assert.assertEquals((returns[0]).stringValue(),
                "[{\"id\":1,\"age\":30,\"salary\":300.5,\"name\":\"jane\","
                + "\"married\":true},{\"id\":2,\"age\":20,\"salary\":200.5,\"name\":\"martin\",\"married\":true},"
                + "{\"id\":3,\"age\":32,\"salary\":100.5,\"name\":\"john\",\"married\":false}]");
    }

    @Test(priority = 1)
    public void testToXML() {
        BValue[] returns = BRunUtil.invoke(result, "testToXML");
        Assert.assertEquals((returns[0]).stringValue(), "<results><result><id>1</id><age>30</age>"
                + "<salary>300.5</salary><name>jane</name><married>true</married></result><result>"
                + "<id>2</id><age>20</age><salary>200.5</salary><name>martin</name><married>true</married></result>"
                + "<result><id>3</id><age>32</age><salary>100.5</salary><name>john</name><married>false</married>"
                + "</result></results>");
    }

    @Test(priority = 1)
    public void testTableWithAllDataToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithAllDataToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"id\":1,\"jsonData\":{\"name\":\"apple\",\"color\":\"red\","
                + "\"price\":30.3},\"xmlData\":\"<book>The Lost World</book>\"},{\"id\":2,\""
                + "jsonData\":{\"name\":\"apple\",\"color\":\"red\",\"price\":30.3},"
                + "\"xmlData\":\"<book>The Lost World</book>\"}]");
    }

    @Test(priority = 1)
    public void testTableWithAllDataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithAllDataToXml");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<results><result><id>1</id><jsonData>{\"name\":\"apple\","
                + "\"color\":\"red\",\"price\":30.3}</jsonData><xmlData>&lt;book>The Lost World&lt;"
                + "/book></xmlData></result><result><id>2</id><jsonData>{\"name\":\"apple\",\"color\":\"red\","
                + "\"price\":30.3}</jsonData><xmlData>&lt;book>The Lost World&lt;/book></xmlData></result></results>");
    }

    @Test(priority = 1)
    public void testTableWithAllDataToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithAllDataToStruct");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"apple\",\"color\":\"red\",\"price\":30.3}");
        Assert.assertEquals(returns[1].stringValue(), "<book>The Lost World</book>");
    }

    @Test(priority = 1)
    public void testTableWithBlobDataToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithBlobDataToStruct");
        Assert.assertEquals((returns[0]).stringValue(), "Sample Text");
    }

    @Test(priority = 1)
    public void testTableWithBlobDataToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithBlobDataToJson");
        Assert.assertEquals((returns[0]).stringValue(), "[{\"id\":1,\"blobData\":\"Sample Text\"}]");
    }

    @Test(priority = 1)
    public void testTableWithBlobDataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithBlobDataToXml");
        Assert.assertEquals((returns[0]).stringValue(), "<results><result><id>1</id><blobData>Sample Text"
                + "</blobData></result></results>");
    }

    @Test(priority = 1)
    public void testStructWithDefaultDataToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testStructWithDefaultDataToJson");
        Assert.assertEquals((returns[0]).stringValue(), "[{\"id\":1,\"age\":0,\"salary\":0.0,\"name\":\"\","
                + "\"married\":false}]");
    }

    @Test(priority = 1)
    public void testStructWithDefaultDataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testStructWithDefaultDataToXml");
        Assert.assertEquals((returns[0]).stringValue(),
                "<results><result><id>1</id><age>0</age><salary>0.0</salary><name></name>" +
                        "<married>false</married></result></results>");
    }

    @Test(priority = 1)
    public void testStructWithDefaultDataToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testStructWithDefaultDataToStruct");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 0.0);
        Assert.assertEquals(returns[2].stringValue(), "");
        Assert.assertEquals(((BBoolean) returns[3]).booleanValue(), false);
    }

    @Test(priority = 1)
    public void testTableWithArrayDataToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithArrayDataToJson");
        Assert.assertEquals((returns[0]).stringValue(), "[{\"id\":1,\"intArrData\":[1,2,3],"
                + "\"floatArrData\":[11.1,22.2,33.3],\"stringArrData\":[\"Hello\",\"World\"],"
                + "\"booleanArrData\":[true,false,true]},"
                + "{\"id\":2,\"intArrData\":[10,20,30],\"floatArrData\":[111.1,222.2,333.3],"
                + "\"stringArrData\":[\"Hello\",\"World\",\"test\"],\"booleanArrData\":[false,false,true]}]");
    }

    @Test(priority = 1)
    public void testTableWithArrayDataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithArrayDataToXml");
        Assert.assertEquals((returns[0]).stringValue(),
                "<results><result><id>1</id><intArrData><element>1</element>"
                + "<element>2</element><element>3</element></intArrData>"
                + "<floatArrData><element>11.1</element><element>22.2</element><element>33.3</element></floatArrData>"
                + "<stringArrData><element>Hello</element><element>World</element></stringArrData>"
                + "<booleanArrData><element>true</element><element>false</element><element>true</element>"
                + "</booleanArrData></result>"
                + "<result><id>2</id><intArrData><element>10</element><element>20</element><element>30</element>"
                + "</intArrData><floatArrData><element>111.1</element><element>222.2</element><element>333.3</element>"
                + "</floatArrData><stringArrData><element>Hello</element><element>World</element><element>test"
                + "</element></stringArrData><booleanArrData><element>false</element><element>false</element>"
                + "<element>true</element></booleanArrData></result></results>");
    }

    @Test(priority = 1)
    public void testTableWithArrayDataToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithArrayDataToStruct");
        Assert.assertEquals(((BIntArray) returns[0]).get(0), 1);
        Assert.assertEquals(((BIntArray) returns[0]).get(1), 2);
        Assert.assertEquals(((BIntArray) returns[0]).get(2), 3);
        Assert.assertEquals(((BFloatArray) returns[1]).get(0), 11.1);
        Assert.assertEquals(((BFloatArray) returns[1]).get(1), 22.2);
        Assert.assertEquals(((BFloatArray) returns[1]).get(2), 33.3);
        Assert.assertEquals(((BStringArray) returns[2]).get(0), "Hello");
        Assert.assertEquals(((BStringArray) returns[2]).get(1), "World");
        Assert.assertEquals(((BBooleanArray) returns[3]).get(0), 1);
        Assert.assertEquals(((BBooleanArray) returns[3]).get(1), 0);
        Assert.assertEquals(((BBooleanArray) returns[3]).get(2), 1);
    }

    @Test(priority = 1)
    public void testTableRemoveSuccess() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveSuccess");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals((returns[1]).stringValue(), "[{\"id\":1,\"age\":35,\"salary\":300.5,"
                + "\"name\":\"jane\",\"married\":true}]");
    }

    @Test(priority = 1)
    public void testTableRemoveSuccessMultipleMatch() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveSuccessMultipleMatch");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals((returns[1]).stringValue(), "[{\"id\":2,\"age\":20,\"salary\":200.5,"
                + "\"name\":\"martin\",\"married\":true}]");
    }

    @Test(priority = 1)
    public void testTableRemoveFailed() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveFailed");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals((returns[1]).stringValue(), "[{\"id\":1,\"age\":35,\"salary\":300.5,"
                + "\"name\":\"jane\",\"married\":true},{\"id\":2,\"age\":40,\"salary\":200.5,"
                + "\"name\":\"martin\",\"married\":true},{\"id\":3,\"age\":42,\"salary\":100.5,"
                + "\"name\":\"john\",\"married\":false}]");
    }

    @Test(priority = 1)
    public void testTableAddAndAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddAndAccess");
        Assert.assertEquals((returns[0]).stringValue(),
                "[{\"id\":1,\"age\":35,\"salary\":300.5,\"name\":\"jane\","
                + "\"married\":true},{\"id\":2,\"age\":40,\"salary\":200.5,\"name\":\"martin\",\"married\":true}]");
        Assert.assertEquals((returns[1]).stringValue(),
                "[{\"id\":1,\"age\":35,\"salary\":300.5,\"name\":\"jane\","
                + "\"married\":true},{\"id\":2,\"age\":40,\"salary\":200.5,\"name\":\"martin\",\"married\":true},"
                + "{\"id\":3,\"age\":42,\"salary\":100.5,\"name\":\"john\",\"married\":false}]");
    }

    @Test(priority = 1,
          description = "Test struct with any typed field",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*message: unsupported column type for table : any.*")
    public void testTableWithAnyDataToJson() {
       BRunUtil.invoke(result, "testTableWithAnyDataToJson");
    }

    @Test(priority = 1,
          description = "Test invalid empty table create",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*message: table cannot be created without a constraint.*",
          enabled = false)
    public void testEmptyTableCreateInvalid() {
        BRunUtil.invoke(result, "testEmptyTableCreateInvalid");
    }

    @Test(priority = 1,
          description = "Test add data with  mismatched types",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*message: incompatible types: struct of type:Company cannot be added "
                  + "to a table with type:Person.*")
    public void testTableAddInvalid() {
        BRunUtil.invoke(result, "testTableAddInvalid");
    }

    @Test(priority = 3, enabled = false) //Issue #5106
    public void testSessionCount() {
        BValue[] returns = BRunUtil.invoke(resultHelper, "getSessionCount");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }
}
