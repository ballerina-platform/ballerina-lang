//  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/lang.value;

type UserDefinedError error <BasicErrorDetail>;
type BasicErrorDetail record {|
    int basicErrorNo;
    anydata...;
|};

type UserDefinedOpenError error <OpenErrorDetail>;
type OpenErrorDetail record {|
    value:Cloneable...;
|};

UserDefinedError error(message1, basicErrorNo = detail1) = error UserDefinedError("error message one", basicErrorNo = 1);

function testBasic() {
    assertEquality("error message one", message1);
    assertEquality(1, detail1);
}

error cause = error("error message one", basicErrorNo = 1);
UserDefinedError error(message2, errorCause2, basicErrorNo = detail2) = error UserDefinedError("error message two",
                                                                                            cause, basicErrorNo = 2);
function testWithErrorCause() {
    assertEquality("error message one", error:message(<error> cause));
    assertEquality("error message two", message2);
    assertEquality(2, detail2);
}

type UserDefinedErrorWithTuple error <errorWithTupleDetail>;
type errorWithTupleDetail record {
    [int, string] basicErrorNo;
};

UserDefinedErrorWithTuple error(message3, basicErrorNo = [detail3, otherDetails]) =
                            error UserDefinedErrorWithTuple("error message three", basicErrorNo = [3, "myErrorList"]);

function testTupleVarInsideErrorVar() {
    assertEquality("error message three", message3);
    assertEquality(3, detail3);
    assertEquality("myErrorList", otherDetails);
}

type MyRecord record {
    int firstValue;
    string secondValue;
};

type UserDefinedError2 error<UserDefinedErrorDetail2>;
type UserDefinedErrorDetail2 record {
    MyRecord recordVar?;
    UserDefinedError errorVar?;
    int errorNo;
};

type UserDefinedError3 error<UserDefinedErrorDetail3>;
type UserDefinedErrorDetail3 record {
    MyRecord recordVar;
    UserDefinedError errorVar?;
    int errorNo?;
};

type UserDefinedError4 error<UserDefinedErrorDetail4>;
type UserDefinedErrorDetail4 record {
    MyRecord recordVar?;
    UserDefinedError errorVar;
    int errorNo?;
};

UserDefinedError3 error(message4, recordVar = {firstValue, secondValue}) = error UserDefinedError3(
                                        "error message four", recordVar = {firstValue: 5, secondValue: "Second value"});

function testRecordVarInsideErrorVar() {
    assertEquality("error message four", message4);
    assertEquality(5, firstValue);
    assertEquality("Second value", secondValue);
}

UserDefinedError4 error(message5, errorVar = error (message6, basicErrorNo = detail6)) =
                            error UserDefinedError4("error message five", errorVar = error UserDefinedError("error message six",
                            basicErrorNo = 7));

function testErrorVarInsideErrorVar() {
    assertEquality("error message five", message5);
    assertEquality("error message six", message6);
    assertEquality(7, detail6);
}

const annotation annot on source var;
@annot
UserDefinedError2 error(message7) = error UserDefinedError2("error message seven", errorNo = 2);

function testErrorVarWithAnnotations() {
    assertEquality("error message seven", message7);
}

UserDefinedError2 error(message9, errorNo = errorNo2) = error UserDefinedError2(message8, errorNo = <int> errorNo1);
UserDefinedError2 error(message8, errorNo = errorNo1) = error UserDefinedError2("error message nine", errorNo = 1);

function testVariableForwardReferencing() {
    assertEquality("error message nine", message9);
    assertEquality(1, errorNo2);
}

UserDefinedOpenError error(message10, ...otherDetails2) = error UserDefinedOpenError("error message ten", time = 2.21, riskLevel = "High");

function testErrorVarWithRestVariable() {
    assertEquality(2.21, otherDetails2["time"]);
    assertEquality("High", otherDetails2["riskLevel"]);
}

var error(message11, ...otherDetails3) = error UserDefinedOpenError(
                        "error message eleven", basicErrorNo = 8, lineNo = 342, fileName = "myfile", recoverable = true);

function testErrorVarDeclaredWithVar() {
    assertEquality("error message eleven", message11);
    assertEquality(342, otherDetails3["lineNo"]);
    assertEquality("myfile", otherDetails3["fileName"]);
    assertTrue(otherDetails3["recoverable"]);
}

type VarTestErrorDetail record {
    [int] fieldA;
    boolean fieldB;
    error fieldC;
};

type VarTestError error<VarTestErrorDetail>;

var error(m, error(c), fieldA = [varA], fieldB = varB, fieldC = error(msgVar)) = foo();

function foo() returns VarTestError =>
        error VarTestError("message", error("cause"), fieldA = [3], fieldB =true, fieldC = error("fieldC message"));

function testErrorVarDeclaredWithVar2() {
    assertEquality("message", m);
    assertEquality("cause", c);
    assertEquality(3, varA);
    assertTrue(varB);
    assertEquality("fieldC message", msgVar);
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(string `expected '${expectedValAsString}', found '${actualValAsString}'`);
}
