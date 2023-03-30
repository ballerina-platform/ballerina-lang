/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.ballerinalang.test.expressions.stamp;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BErrorType;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Negative test cases for stamping variables.
 *
 * @since 0.985.0
 */
public class StampInbuiltFunctionNegativeTest {

    private CompileResult recordNegativeTestCompileResult;
    private CompileResult jsonNegativeTestCompileResult;
    private CompileResult xmlNegativeTestCompileResult;
    private CompileResult mapNegativeTestCompileResult;
    private CompileResult arrayNegativeTestCompileResult;
    private CompileResult tupleNegativeTestCompileResult;
    private CompileResult unionNegativeTestCompileResult;

    @BeforeClass
    public void setup() {

        recordNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/record-stamp-expr-negative-test.bal");
        jsonNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/json-stamp-expr-negative-test.bal");
        xmlNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/xml-stamp-expr-negative-test.bal");
        mapNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/map-stamp-expr-negative-test.bal");
        arrayNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/array-stamp-expr-negative-test.bal");
        tupleNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/tuple-stamp-expr-negative-test.bal");
        unionNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/union-stamp-expr-negative-test.bal");
    }

    //----------------------------- NegativeTest cases ------------------------------------------------------

    @Test
    public void testStampNegativeTest() {

        CompileResult compileResult =
                BCompileUtil.compile("test-src/expressions/stamp/negative/stamp-expr-negative-test.bal");

        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "too many arguments in call to 'cloneWithType()'", 50, 24);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc'", 64, 52);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'anydata', found 'any'",
                71, 33);
        BAssertUtil.validateError(compileResult, index++, "undefined symbol 'TestType'", 79, 54);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<EmployeeObject>'",
                87, 63);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<map>'", 95, 54);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'anydata', found 'ExtendedEmployee'", 103, 34);
        Assert.assertEquals(index, compileResult.getErrorCount());
    }

    //----------------------------- Object NegativeTest cases ------------------------------------------------------

    @Test
    public void testObjectNegativeTest() {

        CompileResult objectNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/object-stamp-expr-negative-test.bal");
        int index = 0;
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 85, 51);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 92, 48);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 99, 46);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 106, 51);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<map>'", 106, 54);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 113, 48);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<any[]>'", 113, 51);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 120, 58);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 127, 28);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<PersonObj>'", 128,
                60);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<EmployeeObject>'",
                136, 63);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<EmployeeObject>'",
                144, 63);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected '(EmployeeObj|error)', found '(EmployeeObject|error)'",
                145, 12);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<BookObject>'", 152,
                59);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<IntObject>'", 158,
                51);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<TeacherObj>'", 166,
                58);
        BAssertUtil.validateError(objectNegativeTestCompileResult, index++,
                "incompatible types: expected 'typedesc<anydata>', found 'typedesc<EmployeeObj>'",
                174, 62);
        Assert.assertEquals(index, objectNegativeTestCompileResult.getErrorCount());
    }

    //----------------------------- Record NegativeTest cases ------------------------------------------------------

    @Test
    public void stampRecordToXML() {

        Object results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampRecordToXML");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Employee' value cannot be converted to 'XmlType'");
    }

    @Test
    public void stampOpenRecordToClosedRecord() {

        Object results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampOpenRecordToClosedRecord");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Teacher' value cannot be converted to 'Employee': " +
                        "\n\t\tmissing required field 'salary' of type 'float' in record 'Employee'");
    }

    @Test
    public void stampClosedRecordToClosedRecord() {

        Object results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampClosedRecordToClosedRecord");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Person' value cannot be converted to 'Student': " +
                        "\n\t\tfield 'school' cannot be added to the closed record 'Student'");
    }

    @Test
    public void stampClosedRecordToMap() {

        Object results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampClosedRecordToMap");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Person' value cannot be converted to 'StringMap': " +
                        "\n\t\tmap field 'age' should be of type 'string', found '25'");
    }

    @Test
    public void stampRecordToArray() {

        Object results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampRecordToArray");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Employee' value cannot be converted to 'StringArray'");
    }

    @Test
    public void stampRecordToTuple() {

        Object results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampRecordToTuple");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Employee' value cannot be converted to 'StringString'");
    }

    //----------------------------- JSON NegativeTest cases ------------------------------------------------------

    @Test
    public void stampJSONToXML() {

        Object results = BRunUtil.invoke(jsonNegativeTestCompileResult, "stampJSONToXML");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<json>' value cannot be converted to 'XmlType'");
    }

    @Test
    public void stampJSONToTuple() {

        Object results = BRunUtil.invoke(jsonNegativeTestCompileResult, "stampJSONToTuple");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<json>' value cannot be converted to '[string,string]'");
    }

    //----------------------------- XML NegativeTest cases ------------------------------------------------------

    @Test
    public void stampXMLToRecord() {

        Object results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToRecord");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'lang.xml:Element' value cannot be converted to 'BookRecord'");
    }

    @Test
    public void stampXMLToJson() {

        Object results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToJson");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'lang.xml:Element' value cannot be converted to 'json'");
    }

    @Test
    public void stampXMLToMap() {

        Object results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToMap");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'lang.xml:Element' value cannot be converted to 'AnydataMap'");
    }

    @Test
    public void stampXMLToArray() {

        Object results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToArray");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'lang.xml:Element' value cannot be converted to 'BookRecordArray'");
    }

    @Test
    public void stampXMLToTuple() {

        Object results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToTuple");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'lang.xml:Element' value cannot be converted to '[string,string]'");
    }

    //----------------------------- Map NegativeTest cases ------------------------------------------------------

    @Test
    public void stampMapToXML() {

        Object results = BRunUtil.invoke(mapNegativeTestCompileResult, "stampMapToXML");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<anydata>' value cannot be converted to 'XmlType'");
    }

    @Test
    public void stampMapToArray() {

        Object results = BRunUtil.invoke(mapNegativeTestCompileResult, "stampMapToArray");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<anydata>' value cannot be converted to 'StringArray'");
    }

    @Test
    public void stampMapToTuple() {

        Object results = BRunUtil.invoke(mapNegativeTestCompileResult, "stampMapToTuple");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<anydata>' value cannot be converted to 'StringString'");
    }

    //----------------------------- Array NegativeTest cases ------------------------------------------------------

    @Test
    public void stampAnyArrayToRecord() {

        Object results = BRunUtil.invoke(arrayNegativeTestCompileResult, "stampAnyArrayToRecord");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results).getDetails()).get(
                        StringUtils.fromString("message")).toString(),
                "'anydata[]' value cannot be converted to 'Employee'");
    }

    @Test
    public void stampAnyArrayToXML() {

        Object results = BRunUtil.invoke(arrayNegativeTestCompileResult, "stampAnyArrayToXML");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'anydata[]' value cannot be converted to 'XmlType'");
    }

    //----------------------------- Tuple NegativeTest cases ------------------------------------------------------

    @Test
    public void stampTupleToRecord() {

        Object results = BRunUtil.invoke(tupleNegativeTestCompileResult, "stampTupleToRecord");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'[string,string,string]' value cannot be converted to 'Employee'");
    }

    @Test
    public void stampTupleToXML() {

        Object results = BRunUtil.invoke(tupleNegativeTestCompileResult, "stampTupleToXML");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'[string,string,string]' value cannot be converted to 'XmlType'");
    }

    @Test
    public void stampTupleToMap() {

        Object results = BRunUtil.invoke(tupleNegativeTestCompileResult, "stampTupleToMap");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'[string,string,string]' value cannot be converted to 'AnydataMap'");
    }

    //----------------------------- Union NegativeTest cases ------------------------------------------------------

    @Test
    public void stampUnionToXML() {

        Object results = BRunUtil.invoke(unionNegativeTestCompileResult, "stampUnionToXML");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'lang.xml:Element' value cannot be converted to 'Employee'");
    }

    @AfterClass
    public void tearDown() {

        recordNegativeTestCompileResult = null;
        jsonNegativeTestCompileResult = null;
        xmlNegativeTestCompileResult = null;
        mapNegativeTestCompileResult = null;
        arrayNegativeTestCompileResult = null;
        tupleNegativeTestCompileResult = null;
        unionNegativeTestCompileResult = null;
    }
}
