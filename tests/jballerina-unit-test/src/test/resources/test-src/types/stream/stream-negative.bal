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

type CustomErrorData record {|
    string message?;
    error cause?;
    int accountID?;
|};

type CustomError error<string, CustomErrorData>;
type CustomError1 error<string, CustomErrorData>;

type Iterator object {
    int i = 0;
    public function next() returns record {| int value; |}? {
        self.i += 1;
        return { value: self.i };
    }
};

function testIterator() {
    Iterator itr = new();
    // correct
    var streamA = new stream<int>(itr);
    stream<int> streamB = new(itr);

    record {| int value; |}|error? returnedVal = streamA.next();
    returnedVal = streamB.next();
}

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

function testIteratorWithCustomError() {
    IteratorWithCustomError itr = new();

    // correct
    var streamA = new stream<int, CustomError>(itr);
    stream<int, CustomError> streamB = new(itr);

    var streamC = new stream<int, error>(itr);
    stream<int, error> streamD = new(itr);

    record {| int value; |}|CustomError? returnedValA = streamA.next();
    returnedValA = streamB.next();

    record {| int value; |}|error? returnedValB = streamC.next();
    returnedValB = streamD.next();

    // incorrect (next returns record {| int value; |}|CustomError?)
    record {| int value; |}? returnedValC = streamC.next();

    // incorrect (error type should be there in the stream<int, ?>)
    var streamE = new stream<int>(itr);
    stream<int> streamF = new(itr);
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

function testIteratorWithGenericError() {
    IteratorWithGenericError itr = new();

    // correct
    var streamA = new stream<int, error>(itr);
    stream<int, error> streamB = new(itr);

    record {| int value; |}|error? returnedValA = streamA.next();
    returnedValA = streamB.next();

    // incorrect (next returns record {| int value; |}|error?)
    record {| int value; |}|CustomError? returnedValB = streamA.next();
    record {| int value; |}? returnedValC = streamB.next();

    // incorrect (itr returns an `error` instead of `CustomError`)
    var streamC = new stream<int, CustomError>(itr);
    stream<int, CustomError> streamD = new(itr);

    // incorrect (error type should be there in the stream<int, ?>)
    var streamE = new stream<int>(itr);
    stream<int> streamF = new(itr);
}

type IteratorWithOutError object {
    int i = 0;

    public function next() returns record {| int value; |}? {
        self.i += 1;
        return { value: self.i };
    }
};

function testIteratorWithOutError() {
    IteratorWithOutError itr = new();

    // correct
    var streamA = new stream<int>(itr);
    stream<int> streamB = new(itr);

    record {| int value; |}? returnedValA = streamA.next();
    returnedValA = streamB.next();

    // incorrect (error type shouldn't be there)
    var streamC = new stream<int, error>(itr);
    stream<int, error> streamD = new(itr);
    var streamE = new stream<int, CustomError>(itr);
    stream<int, CustomError> streamF = new(itr);

    // incorrect (next returns record {| int value; |}?)
    record {| int value; |}|error? returnedValB = streamA.next();
    record {| int value; |}|CustomError? returnedValC = streamB.next();
}

type IteratorWithOutNext object {
    int i = 10;

    public function prev() returns record {| int value; |}? {
        self.i -= 1;
        return { value: self.i };
    }
};

function testIteratorWithOutNext() {
    IteratorWithOutNext itr = new();
    // incorrect (itr doesn't have a next() method implemented)
    var streamA = new stream<int>(itr);
    stream<int> streamB = new(itr);
    var streamC = new stream<int, error>(itr);
    stream<int, error> streamD = new(itr);
    var streamE = new stream<int, CustomError>(itr);
    stream<int, CustomError> streamF = new(itr);
}

type IteratorWithMismatchedNextA object {
    int i = 0;
    public function next(int i) returns record {| int value; |}? {
        self.i += 1;
        return { value: self.i };
    }
};

function testIteratorWithMismatchedNextA() {
    IteratorWithMismatchedNextA itr = new();
    // incorrect (`next(int i)` of itr, doesn't match with `next()`)
    var streamA = new stream<int>(itr);
    stream<int> streamB = new(itr);
    var streamC = new stream<int, error>(itr);
    stream<int, error> streamD = new(itr);
    var streamE = new stream<int, CustomError>(itr);
    stream<int, CustomError> streamF = new(itr);
}

type IteratorWithMismatchedNextC object {
    int i = 0;

    public function next() returns record {| int val; |}|CustomError? {
        self.i += 1;
        return { value: self.i };
    }
};

function testIteratorWithMismatchedNextC() {
    IteratorWithMismatchedNextC itr = new();
    // correct
    var streamA = new stream<int, error>(itr);
    stream<int, error> streamB = new(itr);

    // incorrect (`return type of `next()` of itr, doesn't match with value type)
    var streamC = new stream<string, error>(itr);
    stream<string, error> streamD = new(itr);
    var streamE = new stream<string, CustomError>(itr);
    stream<string, CustomError> streamF = new(itr);
}

type IteratorWithMismatchedError object {
    int i = 0;

    public function next() returns record {| int value; |}|CustomError? {
        self.i += 1;
        return { value: self.i };
    }
};

function testIteratorWithMismatchedError() {
    IteratorWithMismatchedError itr = new();
    // correct
    var streamA = new stream<int, CustomError>(itr);
    stream<int, CustomError> streamB = new(itr);

    var streamC = new stream<int, error>(itr);
    stream<int, error> streamD = new(itr);

    // incorrect (`CustomError` & `CustomError1` doesn't match)
    var streamE = new stream<string, CustomError1>(itr);
    stream<string, CustomError1> streamF = new(itr);
}
