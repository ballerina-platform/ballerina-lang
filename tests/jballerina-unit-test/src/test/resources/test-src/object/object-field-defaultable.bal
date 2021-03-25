// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public function testObjectFieldDefaultable () returns [int, string, int, string] {
    Person p = new Person();
    return [p.age, p.name, p.year, p.month];
}

class Person {
    public int age = 10;
    public string name = "sample name";

    int year = 50;
    string month = "february";

    function init (int age = 10, string name = "sample name") {
        self.age = age;
        self.name = name;
    }
}

class ErrorField {
    public error 'error;
    public int 'int;

    function init (error er, int value = 20) {
        self.'error = er;
        self.'int = value;
    }
}

public function testErrorAsObjectField() {
    error newError = error("bam", message = "new error");
    ErrorField p = new ErrorField(newError);
    assertEquality(p.'error.toString(), "error(\"bam\",message=\"new error\")");
    assertEquality(p.'int, 20);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
