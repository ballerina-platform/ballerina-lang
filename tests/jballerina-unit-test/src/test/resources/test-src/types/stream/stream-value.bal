// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

int i = 0;

stream<int> evenNumberStream = stream {
    i += 2;
    return { value: i };
};

function testGlobalStreamConstruct() returns boolean {
    boolean testPassed = true;
    i = 0;

    record {| int value; |}? evenNumber = evenNumberStream.next();
    testPassed = testPassed && (<int>evenNumber["value"] % 2 == 0);

    evenNumber = evenNumberStream.next();
    testPassed = testPassed && (<int>evenNumber["value"] % 2 == 0);

    evenNumber = evenNumberStream.next();
    testPassed = testPassed && (<int>evenNumber["value"] % 2 == 0);

    evenNumber = evenNumberStream.next();
    testPassed = testPassed && (<int>evenNumber["value"] % 2 == 0);

    return testPassed;
}


function testStreamConstruct() returns boolean {
    boolean testPassed = true;
    int j = 1;

    stream<int> oddNumberStream = stream {
        j += 2;
        return { value: j };
    };

    var oddNumber = oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    return testPassed;
}

function testStreamConstructWithFilter() returns boolean {
    boolean testPassed = true;
    int j = 1;

    stream<int> intStream = stream {
        j += 1;
        return { value: j };
    };

    stream<int> oddNumberStream = intStream.filter(function (int intVal) returns boolean {
        return intVal % 2 == 1;
    });

    var oddNumber = oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    return testPassed;
}

int val = 0;
function getIntStream() returns stream<int> {
    return stream {
        val += 1;
        return { value: val };
    };
}

function testStreamReturnType() returns boolean {
    boolean testPassed = true;
    stream<int> intStream = getIntStream();

    var intNumber = intStream.next();
    testPassed = testPassed && (<int>intNumber["value"] == 1);

    intNumber = intStream.next();
    testPassed = testPassed && (<int>intNumber["value"] == 2);

    intNumber = intStream.next();
    testPassed = testPassed && (<int>intNumber["value"] == 3);

    intNumber = intStream.next();
    testPassed = testPassed && (<int>intNumber["value"] == 4);

    return testPassed;
}
