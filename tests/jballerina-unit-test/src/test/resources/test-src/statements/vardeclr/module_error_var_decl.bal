//  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type userDefinedError error <basicErrorDetail>;
type basicErrorDetail record {|
    int basicErrorNo?;
    anydata...;
|};

userDefinedError error (message1, basicErrorNo = detail1) = error userDefinedError("error message one", basicErrorNo = 1);

function testBasic() {
    assertEquality("error message one", message1);
    assertEquality(1, detail1);
}

error cause = error ("error message one", basicErrorNo = 1);
userDefinedError error (message2, errorCause2, basicErrorNo = detail2) = error userDefinedError("error message two",
                                                                                            cause, basicErrorNo = 2);
function testWithErrorCause() {
    assertEquality("error message one", error:message(<error> cause));
    assertEquality("error message two", message2);
    assertEquality(2, detail2);
}

type userDefinedErrorWithTuple error <errorWithTupleDetail>;
type errorWithTupleDetail record {
    [int, string] basicErrorNo?;
};

userDefinedErrorWithTuple error (message3, basicErrorNo = [detail3, otherDetails]) =
                            error userDefinedErrorWithTuple("error message three", basicErrorNo = [3, "myErrorList"]);

function testTupleVarInsideErrorVar() {
    assertEquality("error message three", message3);
    assertEquality(3, detail3);
    assertEquality("myErrorList", otherDetails);
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
