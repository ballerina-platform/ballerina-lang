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
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
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
        resultHelper = BCompileUtil.compile("test-src/types/table/table-literal-test.bal");
    }

    @Test
    public void testEmptyTableCreate() {
        BRunUtil.invoke(result, "testEmptyTableCreate");
        BValue[] args = new BValue[1];
        args[0] = new BString("TABLE_PERSON_%");
        BValue[] returns = BRunUtil.invoke(resultHelper, "getTableCount", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);

        args[0] = new BString("TABLE_COMPANY_%");
        returns = BRunUtil.invoke(resultHelper, "getTableCount", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
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

    @Test(priority = 1)
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
                    "{data: [{id:1, age:30, salary:300.5, name:\"jane\", married:true}, "
                            + "{id:2, age:20, salary:200.5, name:\"martin\", married:true}, "
                            + "{id:3, age:32, salary:100.5, name:\"john\", married:false}]}\n");
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
        Assert.assertEquals((returns[0]).stringValue(), "[{\"id\":1,\"age\":30,\"salary\":300.5,\"name\":\"jane\","
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

    @Test(priority = 1,
          description = "Test failed update with generated id action",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*message: table cannot be created without a constraint.*")
    public void testFailedGeneratedKeyOnInsert() {
        BRunUtil.invoke(result, "testEmptyTableCreateInvalid");
    }

    @Test(priority = 1,
          description = "Test failed update with generated id action",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*message: incompatible types: struct of type:Company cannot be added "
                  + "to a table with type:Person.*")
    public void testTableAddInvalid() {
        BRunUtil.invoke(result, "testTableAddInvalid");
    }

}
