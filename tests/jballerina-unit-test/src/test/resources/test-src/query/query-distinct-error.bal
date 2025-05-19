// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org)
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

type Album record {
    string id;
    string name;
    string artist;
    decimal price;
};

type TypeMismatchError distinct error;

class MockStream {
    private boolean hasNext = true;

    public isolated function next() returns record {| Album value; |}|error? {
        if self.hasNext {
            self.hasNext = false;
            return error TypeMismatchError("Error when iterating the SQL result. invalid value for record field 'artist': expected value of type 'string', found '()'");
        }
        return ();
    }
}

function createMockStream() returns stream<Album, error?> {
    MockStream mockStream = new MockStream();
    return new (mockStream);
}

public function testDistinctTypeMismatchError() {
    stream<Album, error?> albumStream = createMockStream();

    int count = 0;
    error? e = from Album _ in albumStream do {
        count = count + 1;
    };

    assertEquality(0, count);
    assertTrue(e is TypeMismatchError);
    if e is TypeMismatchError {
        assertEquality(e.message(), "Error when iterating the SQL result. invalid value for record field 'artist': expected value of type 'string', found '()'");
    }
}

class EvenNumberGenerator {
    int i = 0;
    public isolated function next() returns record {|int value;|}|error? {
        self.i += 2;
        if (self.i > 10) {
            return error("No more even numbers available.");
        }
        return {value: self.i};
    }
}

function testErrorFromQueryPipeline() returns string|error? {
    EvenNumberGenerator evenGen = new ();
    EvenNumberGenerator evenGen2 = new ();

    stream<int, error?> evenNumberStream = new (evenGen);
    stream<int, error?> evenNumberStream2 = new (evenGen2);

    error? err = from var i in evenNumberStream 
        do {
            record {| int value; |}? result = check evenNumberStream2.next();
            int? intResult = result?.value;
        };

    assertTrue(err is error);
    return "should reach here";
}

function testErrorFromQueryBody() returns string|error? {
    EvenNumberGenerator evenGen = new ();
    EvenNumberGenerator evenGen2 = new ();

    stream<int, error?> evenNumberStream = new (evenGen);
    stream<int, error?> evenNumberStream2 = new (evenGen2);

    error? err = from var i in 1...10
        do {
            record {| int value; |}? result = check evenNumberStream2.next();
            int? intResult = result?.value;
        };

    assertTrue(err is error);
    return "shouldn't reach here";
}

function testQueryDistinctError() {
    assertEquality("should reach here", testErrorFromQueryPipeline());
    assertTrue(testErrorFromQueryBody() is error);
}

// Assertions ---------------------------------------------------------------------------------------------------------

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
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
    if expectedValAsString == actualValAsString {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
