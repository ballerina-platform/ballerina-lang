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

class NumberGenerator {
    int i = 0;
    public isolated function next() returns record {| int value; |}? {
        self.i += 1;
        return { value: self.i };
    }
}

class EvenNumberGenerator {
    int i = 0;
    public isolated function next() returns record {| int value; |}|error? {
        self.i += 2;
        return { value: self.i };
    }
}

class OddNumberGenerator {
    int i = 1;
    public isolated function next() returns record {| int value; |}|error? {
        self.i += 2;
        return { value: self.i };
    }
}

function getRecordValue((record {| int value; |}|error?)|(record {| int value; |}?) returnedVal) returns ResultValue? {
    if (returnedVal is ResultValue) {
        return returnedVal;
    } else {
        return ();
    }
}

EvenNumberGenerator evenGen = new();
stream<int,error> evenNumberStream = new(evenGen);

function testGlobalStreamConstruct() returns boolean {
    boolean testPassed = true;

    record {| int value; |}? evenNumber = getRecordValue(evenNumberStream.next());
    testPassed = testPassed && (evenNumber?.value == 2);

    evenNumber = getRecordValue(evenNumberStream.next());
    testPassed = testPassed && (evenNumber?.value == 4);

    evenNumber = getRecordValue(evenNumberStream.next());
    testPassed = testPassed && (evenNumber?.value == 6);

    evenNumber = getRecordValue(evenNumberStream.next());
    testPassed = testPassed && (evenNumber?.value == 8);

    return testPassed;
}

function testStreamConstruct() returns boolean {
    boolean testPassed = true;

    OddNumberGenerator oddGen = new();
    var oddNumberStream = new stream<int,error>(oddGen);

    record {| int value; |}? oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (oddNumber?.value == 3);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (oddNumber?.value == 5);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (oddNumber?.value == 7);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (oddNumber?.value == 9);

    return testPassed;
}

function testStreamConstructWithFilter() returns boolean {
    boolean testPassed = true;
    NumberGenerator numGen = new NumberGenerator();
    var intStream = new stream<int>(numGen);

    stream<int,error> oddNumberStream = intStream.filter(function (int intVal) returns boolean {
        return intVal % 2 == 1;
    });

    ResultValue? oddNumber = <ResultValue?> oddNumberStream.next();
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

    oddNumber = getRecordValue(oddNumberStream.next());
    testPassed = testPassed && (<int>oddNumber["value"] % 2 == 1);

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

    record {| int value; |}? intNumber = getRecordValue(intStream.next());
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

    record {| int value; |}? intNumber = getRecordValue(intStream.next());
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

type CustomError distinct error<CustomErrorData>;
boolean closed = false;

class IteratorWithCustomError {
    int i = 0;

    public isolated function next() returns record {| int value; |}|CustomError? {
        self.i += 1;
        if (self.i == 2) {
            CustomError e = CustomError("CustomError", message = "custom error occured", accountID = 1);
            return e;
        } else {
            return { value: self.i };
        }
    }

    public function close() returns CustomError? {
        closed = true;
    }
}

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
    error? err = intStreamB.close();
    testPassed = testPassed && closed;
    return testPassed;
}

class IteratorWithGenericError {
    int i = 0;

    public isolated function next() returns record {| int value; |}|error? {
        self.i += 1;
        if (self.i == 2) {
            return error("GenericError", message = "generic error occured");
        } else {
            return { value: self.i };
        }
    }
}

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

class IteratorWithOutError {
    int i = 0;

    public isolated function next() returns record {| int value; |}? {
        self.i += 1;
        return { value: self.i };
    }
}

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

type CustomError1 distinct error<CustomErrorData>;
type Error CustomError | CustomError1;

class IteratorWithErrorUnion {
    int i = 0;

    public isolated function next() returns record {| int value; |}|Error? {
        self.i += 1;
        if (self.i == 2) {
            CustomError e = CustomError("CustomError", message = "custom error occured", accountID = 2);
            return e;
        } else if (self.i == 3) {
            CustomError1 e = CustomError1("CustomError1", message = "custom error occured", accountID = 3);
            return e;
        } else {
            return { value: self.i };
        }
    }
}

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

class NeverNumberGenerator {
    int i = 0;
    public isolated function next() returns record {| int value; |}?|never {
        self.i += 1;
        if (self.i < 3) {
            return { value: self.i };
        }
    }
}

function testStreamConstructWithNever() returns boolean {
    boolean testPassed = true;

    NumberGenerator numGen = new();
    stream<int> numberStream1 = new stream<int,never>(numGen);
    stream<int, never> numberStream2 = new(numGen);

    record {| int value; |}? number1 = getRecordValue(numberStream1.next());
    testPassed = testPassed && (number1?.value == 1);

    record {| int value; |}? number2 = getRecordValue(numberStream2.next());
    testPassed = testPassed && (number2?.value == 2);

    NeverNumberGenerator neverNumGen = new();
    stream<int|never> neverNumStream = new(neverNumGen);

    record {| int value; |}? neverNum1 = neverNumStream.next();
    record {| int value; |}? neverNum2 = neverNumStream.next();
    record {| int value; |}? neverNum3 = neverNumStream.next();
    testPassed = testPassed && (neverNum3 == ());

    return testPassed;
}

class NumberStreamGenerator {
    public isolated function next() returns record {| stream<int> value; |}? {
         NumberGenerator numGen = new();
         stream<int> numberStream = new (numGen);
         return { value: numberStream};
    }
}

function testStreamOfStreams() returns boolean {
    boolean testPassed = false;
    NumberStreamGenerator numStreamGen = new();
    stream<stream<int>> numberStream = new (numStreamGen);
    record {| stream<int> value; |}? nextStream1 = numberStream.next();
    stream<int>? str1 = nextStream1?.value;
    if(str1 is stream) {
        record {| int value; |}? val = str1.next();
        int? num = val?.value;
        testPassed = (num == 1);
    }
    record {| stream<int> value; |}? nextStream2 = numberStream.next();
    stream<int>? str2 = nextStream2?.value;
    if(str2 is stream) {
       record {| int value; |}? val = str2.next();
       int? num = val?.value;
       testPassed = testPassed && (num == 1);
    }
    return testPassed;
}

function testEmptyStreamConstructs() returns boolean {
    boolean testPassed = true;
    stream<int> emptyStream1 = new;
    stream<int> emptyStream2 = new stream<int>();
    stream<int, never> emptyStream3 = new;
    stream<int, error> emptyStream4 = new;
    stream<int, never> emptyStream5 = new stream<int, never>();
    stream<int, error> emptyStream6 = new stream<int, error>();
    var emptyStream7 = new stream<int>();
    var emptyStream8 = new stream<int, never>();
    var emptyStream9 = new stream<int, error>();

    testPassed = testPassed && (emptyStream1.next() == ());
    testPassed = testPassed && (emptyStream2.next() == ());
    testPassed = testPassed && (emptyStream3.next() == ());
    testPassed = testPassed && (emptyStream4.next() == ());
    testPassed = testPassed && (emptyStream5.next() == ());
    testPassed = testPassed && (emptyStream6.next() == ());
    testPassed = testPassed && (emptyStream7.next() == ());
    testPassed = testPassed && (emptyStream8.next() == ());
    testPassed = testPassed && (emptyStream9.next() == ());
    return testPassed;
}
