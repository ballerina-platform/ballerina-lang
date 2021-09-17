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

import org.ballerinalang.core.model.types.BErrorType;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        BValue[] results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampRecordToXML");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Employee' value cannot be converted to 'xml<(lang.xml:Element" +
                                    "|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>'");
    }

    @Test
    public void stampOpenRecordToClosedRecord() {
        BValue[] results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampOpenRecordToClosedRecord");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Teacher' value cannot be converted to 'Employee': " +
                                    "\n\t\tmissing required field 'salary' of type 'float' in record 'Employee'");
    }

    @Test
    public void stampClosedRecordToClosedRecord() {
        BValue[] results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampClosedRecordToClosedRecord");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Person' value cannot be converted to 'Student': " +
                                    "\n\t\tfield 'school' cannot be added to the closed record 'Student'");
    }

    @Test
    public void stampClosedRecordToMap() {
        BValue[] results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampClosedRecordToMap");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Person' value cannot be converted to 'map<string>'");
    }

    @Test
    public void stampRecordToArray() {
        BValue[] results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampRecordToArray");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Employee' value cannot be converted to 'string[]'");
    }

    @Test
    public void stampRecordToTuple() {
        BValue[] results = BRunUtil.invoke(recordNegativeTestCompileResult, "stampRecordToTuple");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Employee' value cannot be converted to '[string,string]'");
    }

    //----------------------------- JSON NegativeTest cases ------------------------------------------------------

    @Test
    public void stampJSONToXML() {
        BValue[] results = BRunUtil.invoke(jsonNegativeTestCompileResult, "stampJSONToXML");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'map<json>' value cannot be converted to 'xml<(lang.xml:Element|" +
                                    "lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>'");
    }

    @Test
    public void stampJSONToTuple() {
        BValue[] results = BRunUtil.invoke(jsonNegativeTestCompileResult, "stampJSONToTuple");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'map<json>' value cannot be converted to '[string,string]'");
    }

    //----------------------------- XML NegativeTest cases ------------------------------------------------------

    @Test
    public void stampXMLToRecord() {
        BValue[] results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToRecord");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'lang.xml:Element' value cannot be converted to 'BookRecord'");
    }

    @Test
    public void stampXMLToJson() {
        BValue[] results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToJson");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'lang.xml:Element' value cannot be converted to 'json'");
    }

    @Test
    public void stampXMLToMap() {
        BValue[] results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToMap");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'lang.xml:Element' value cannot be converted to 'map<anydata>'");
    }

    @Test
    public void stampXMLToArray() {
        BValue[] results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToArray");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'lang.xml:Element' value cannot be converted to 'BookRecord[]'");
    }

    @Test
    public void stampXMLToTuple() {
        BValue[] results = BRunUtil.invoke(xmlNegativeTestCompileResult, "stampXMLToTuple");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'lang.xml:Element' value cannot be converted to '[string,string]'");
    }

    //----------------------------- Map NegativeTest cases ------------------------------------------------------

    @Test
    public void stampMapToXML() {
        BValue[] results = BRunUtil.invoke(mapNegativeTestCompileResult, "stampMapToXML");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'map<anydata>' value cannot be converted to 'xml<(lang.xml:Element|" +
                                    "lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>'");
    }

    @Test
    public void stampMapToArray() {
        BValue[] results = BRunUtil.invoke(mapNegativeTestCompileResult, "stampMapToArray");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'map<anydata>' value cannot be converted to 'string[]'");
    }

    @Test
    public void stampMapToTuple() {
        BValue[] results = BRunUtil.invoke(mapNegativeTestCompileResult, "stampMapToTuple");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'map<anydata>' value cannot be converted to '[string,string]'");
    }
    
    //----------------------------- Array NegativeTest cases ------------------------------------------------------
    
    @Test
    public void stampAnyArrayToRecord() {
        BValue[] results = BRunUtil.invoke(arrayNegativeTestCompileResult, "stampAnyArrayToRecord");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'anydata[]' value cannot be converted to 'Employee'");
    }

    @Test
    public void stampAnyArrayToXML() {
        BValue[] results = BRunUtil.invoke(arrayNegativeTestCompileResult, "stampAnyArrayToXML");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'anydata[]' value cannot be converted to 'xml<(lang.xml:Element|" +
                                    "lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>'");
    }

    //----------------------------- Tuple NegativeTest cases ------------------------------------------------------

    @Test
    public void stampTupleToRecord() {
        BValue[] results = BRunUtil.invoke(tupleNegativeTestCompileResult, "stampTupleToRecord");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'[string,string,string]' value cannot be converted to 'Employee'");
    }
    
    @Test
    public void stampTupleToXML() {
        BValue[] results = BRunUtil.invoke(tupleNegativeTestCompileResult, "stampTupleToXML");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'[string,string,string]' value cannot be converted to 'xml<(lang.xml:Element" +
                                    "|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>'");
    }

    @Test
    public void stampTupleToMap() {
        BValue[] results = BRunUtil.invoke(tupleNegativeTestCompileResult, "stampTupleToMap");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'[string,string,string]' value cannot be converted to 'map<anydata>'");
    }
    
    //----------------------------- Union NegativeTest cases ------------------------------------------------------

    @Test
    public void stampUnionToXML() {
        BValue[] results = BRunUtil.invoke(unionNegativeTestCompileResult, "stampUnionToXML");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'lang.xml:Element' value cannot be converted to 'Employee'");
    }

    @Test
    public void stampUnionToConstraintMapToUnionNegative() {
        BValue[] results = BRunUtil.invoke(unionNegativeTestCompileResult, "stampUnionToConstraintMapToUnionNegative");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                "'int' value cannot be converted to '(float|decimal|[string,int])': ambiguous target type");
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
