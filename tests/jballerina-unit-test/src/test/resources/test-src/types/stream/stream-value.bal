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

type ResultValue record {|
    int value;
|};

type NumberGenerator object {
    int i = 0;
    public function next() returns record {| int value; |}? {
        self.i += 1;
        return { value: self.i };
    }
};

type EvenNumberGenerator object {
    int i = 0;
    public function next() returns record {| int value; |}|error? {
        self.i += 2;
        return { value: self.i };
    }
};

type OddNumberGenerator object {
    int i = 1;
    public function next() returns record {| int value; |}|error? {
        self.i += 2;
        return { value: self.i };
    }
};

function getRecordValue((record {| int value; |}|error?)|(record {| int value; |}?) returnedVal) returns ResultValue? {
    if (returnedVal is ResultValue) {
        return <ResultValue> returnedVal;
    } else {
        return ();
    }
}

EvenNumberGenerator evenGen = new();
stream<int,error> evenNumberStream = new(evenGen);

function testGlobalStreamConstruct() returns boolean {
    boolean testPassed = true;

    record {| int value; |}? evenNumber = getRecordValue(evenNumberStream.next());
    testPassed = testPassed && (<int>evenNumber["value"] % 2 == 0);

    evenNumber = getRecordValue(evenNumberStream.next());
    testPassed = testPassed && (<int>evenNumber["value"] % 2 == 0);

    evenNumber = getRecordValue(evenNumberStream.next());
    testPassed = testPassed && (<int>evenNumber["value"] % 2 == 0);

    evenNumber = getRecordValue(evenNumberStream.next());
    testPassed = testPassed && (<int>evenNumber["value"] % 2 == 0);

    return testPassed;
}

function testStreamConstruct() returns boolean {
    boolean testPassed = true;

    OddNumberGenerator oddGen = new();
    var oddNumberStream = new stream<int,error>(oddGen);

    var oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    return testPassed;
}

function testStreamConstructWithFilter() returns boolean {
    boolean testPassed = true;
    // TODO: enable after filter typeParam fix
    //NumberGenerator numGen = new NumberGenerator();
    //var intStream = new stream<int,error>(numGen);
    //
    //stream<int,error> oddNumberStream = intStream.filter(function (int intVal) returns boolean {
    //    return intVal % 2 == 1;
    //});
    //
    //var oddNumber = getRecordValue(oddNumberStream.next());
    //testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);
    //
    //oddNumber = getRecordValue(oddNumberStream.next());
    //testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);
    //
    //oddNumber = getRecordValue(oddNumberStream.next());
    //testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);
    //
    //oddNumber = getRecordValue(oddNumberStream.next());
    //testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    return testPassed;
}

function getIntStream() returns stream<int> {
    NumberGenerator numGen = new();
    stream<int> intStream = new(numGen);
    return intStream;
}

function testStreamReturnTypeExplicit() returns boolean {
    boolean testPassed = true;
    stream<int> intStream = getIntStream();

    var intNumber = getRecordValue(intStream.next());
    testPassed = testPassed && (<int>intNumber["value"] == 1);

    intNumber = getRecordValue(intStream.next());
    testPassed = testPassed && (<int>intNumber["value"] == 2);

    intNumber = getRecordValue(intStream.next());
    testPassed = testPassed && (<int>intNumber["value"] == 3);

    intNumber = getRecordValue(intStream.next());
    testPassed = testPassed && (<int>intNumber["value"] == 4);

    return testPassed;
}

function testStreamReturnTypeImplicit() returns boolean {
    boolean testPassed = true;
    var intStream = getIntStream();

    var intNumber = getRecordValue(intStream.next());
    testPassed = testPassed && (<int>intNumber["value"] == 1);

    intNumber = getRecordValue(intStream.next());
    testPassed = testPassed && (<int>intNumber["value"] == 2);

    intNumber = getRecordValue(intStream.next());
    testPassed = testPassed && (<int>intNumber["value"] == 3);

    intNumber = getRecordValue(intStream.next());
    testPassed = testPassed && (<int>intNumber["value"] == 4);

    return testPassed;
}

// ------------------- Error Related Tests -------------------

type CustomErrorData record {|
    string message?;
    error cause?;
    int accountID?;
|};

type CustomError error<string, CustomErrorData>;

type IteratorWithCustomError object {
    int i = 0;

    public function next() returns record {| int value; |}|CustomError? {
        self.i += 1;
        if (self.i == 2) {
            CustomError e = error("CustomError", message = "custom error occured", accountID = 1);
            return e;
        } else {
            return { value: self.i };
        }
    }
};

function testIteratorWithCustomError() returns boolean {
    boolean testPassed = true;

    IteratorWithCustomError numGen = new();
    var intStreamA = new stream<int, CustomError>(numGen);
    stream<int, CustomError> intStreamB = new(numGen);

    var returnedVal = getRecordValue(intStreamA.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 1);

    returnedVal = getRecordValue(intStreamB.next());
    testPassed = testPassed && (returnedVal is ());

    returnedVal = getRecordValue(intStreamA.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 3);

    returnedVal = getRecordValue(intStreamB.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 4);

    return testPassed;
}

type IteratorWithGenericError object {
    int i = 0;

    public function next() returns record {| int value; |}|error? {
        self.i += 1;
        if (self.i == 2) {
            return error("GenericError", message = "generic error occured");
        } else {
            return { value: self.i };
        }
    }
};

function testIteratorWithGenericError() returns boolean {
    boolean testPassed = true;

    IteratorWithGenericError numGen = new();
    var intStreamA = new stream<int, error>(numGen);
    stream<int, error> intStreamB = new(numGen);

    var returnedVal = getRecordValue(intStreamA.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 1);

    returnedVal = getRecordValue(intStreamB.next());
    testPassed = testPassed && (returnedVal is ());

    returnedVal = getRecordValue(intStreamA.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 3);

    returnedVal = getRecordValue(intStreamB.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 4);

    return testPassed;
}

type IteratorWithOutError object {
    int i = 0;

    public function next() returns record {| int value; |}? {
        self.i += 1;
        return { value: self.i };
    }
};

function testIteratorWithOutError() returns boolean {
    boolean testPassed = true;

    IteratorWithOutError numGen = new();
    var intStreamA = new stream<int>(numGen);
    stream<int> intStreamB = new(numGen);

    var returnedVal = getRecordValue(intStreamA.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 1);

    returnedVal = getRecordValue(intStreamB.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 2);

    returnedVal = getRecordValue(intStreamA.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 3);

    returnedVal = getRecordValue(intStreamB.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 4);

    return testPassed;
}

type CustomError1 error<string, CustomErrorData>;
type Error CustomError | CustomError1;

type IteratorWithErrorUnion object {
    int i = 0;

    public function next() returns record {| int value; |}|Error? {
        self.i += 1;
        if (self.i == 2) {
            CustomError e = error("CustomError", message = "custom error occured", accountID = 2);
            return e;
        } else if (self.i == 3) {
            CustomError1 e = error("CustomError1", message = "custom error occured", accountID = 3);
            return e;
        } else {
            return { value: self.i };
        }
    }
};

function testIteratorWithErrorUnion() returns boolean {
    boolean testPassed = true;

    IteratorWithErrorUnion numGen = new();
    var intStreamA = new stream<int, Error>(numGen);
    stream<int, Error> intStreamB = new(numGen);

    var returnedVal = getRecordValue(intStreamA.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 1);

    returnedVal = getRecordValue(intStreamB.next());
    testPassed = testPassed && (returnedVal is ());

    returnedVal = getRecordValue(intStreamA.next());
    testPassed = testPassed && (returnedVal is ());

    returnedVal = getRecordValue(intStreamB.next());
    testPassed = testPassed && (<int>returnedVal["value"] == 4);

    return testPassed;
}