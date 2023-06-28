// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type ScoreEvent readonly & record {|
    string email;
    string problemId;
    float score;
|};

type Rec record {|
    string email;
    float score;
|};

function testClosuresInQueryInSelect() {
    ScoreEvent[] events = [
        {email: "jake@abc.com", problemId: "12", score: 80.0},
        {email: "anne@abc.com", problemId: "20", score: 95.0},
        {email: "peter@abc.com", problemId: "3", score: 72.0}
    ];

    Rec[][] res = from var {email, problemId, score} in events
        select from var i in ["20", "30", "40"]
            where problemId == i
            select {
                email: email,
                score: score
            };

    Rec[][] exp = [[], [{email: "anne@abc.com", score: 95.0}], []];
    assertEquality(exp, res);
}

function testClosuresInQueryInterpolatedInXmlInSelect() {
    int[] a = [1, 2];
    xml x = from int i in a
        select xml `<row>${from int j in a
            select xml `<num>${i}${j}</num>`}</row>`;
    assertEquality(true, xml `<row><num>11</num><num>12</num></row><row><num>21</num><num>22</num></row>` == x);
}

function testClosuresInQueryInLet() {
    ScoreEvent[] events = [
        {email: "jake@abc.com", problemId: "12", score: 80.0},
        {email: "anne@abc.com", problemId: "20", score: 95.0},
        {email: "peter@abc.com", problemId: "3", score: 72.0}
    ];

    ScoreEvent[] events2 = [
        {email: "jake@abc.com", problemId: "14", score: 90.0},
        {email: "anne@abc.com", problemId: "30", score: 95.0},
        {email: "peter@abc.com", problemId: "23", score: 50.0}
    ];

    Rec[][] res = from var {email, problemId, score} in events
        let string[] str = from ScoreEvent se in events2 where se.email == email select "problemId"
        select from var i in ["20", "30", "40"]
            where str[0] > i
            select {
                email: email,
                score: score
            };

    Rec[][] exp = [[{"email":"jake@abc.com","score":80.0},{"email":"jake@abc.com","score":80.0},{"email":"jake@abc.com","score":80.0}],[{"email":"anne@abc.com","score":95.0},{"email":"anne@abc.com","score":95.0},{"email":"anne@abc.com","score":95.0}],[{"email":"peter@abc.com","score":72.0},{"email":"peter@abc.com","score":72.0},{"email":"peter@abc.com","score":72.0}]];
    assertEquality(exp, res);
}

function testClosuresInNestedQueryInSelect() {
    ScoreEvent[] events = [
        {email: "jake@abc.com", problemId: "12", score: 80.0},
        {email: "anne@abc.com", problemId: "20", score: 95.0},
        {email: "peter@abc.com", problemId: "3", score: 72.0}
    ];

    Rec[][] res = from var {email, problemId, score} in events
        select from var i in (from float k in [20f, 30f] where score > k select k)
            select {
                email: email,
                score: score
            };

    Rec[][] exp = [[{"email":"jake@abc.com","score":80.0},{"email":"jake@abc.com","score":80.0}],[{"email":"anne@abc.com","score":95.0},{"email":"anne@abc.com","score":95.0}],[{"email":"peter@abc.com","score":72.0},{"email":"peter@abc.com","score":72.0}]];
    assertEquality(exp, res);
}

function testClosuresInNestedQueryInSelect2() {
    ScoreEvent[] events = [
        {email: "jake@abc.com", problemId: "12", score: 80.0},
        {email: "anne@abc.com", problemId: "20", score: 95.0},
        {email: "peter@abc.com", problemId: "3", score: 72.0}
    ];

    Rec[][] res = from var {email, problemId, score} in events
        let string str = "A"
        limit 2
        select from var i in ["20", "30", "40"]
            select {
                email: from var k in [1] select str,
                score: score
            };

    Rec[][] exp = [[{"email":"A","score":80.0},{"email":"A","score":80.0},{"email":"A","score":80.0}],[{"email":"A","score":95.0},{"email":"A","score":95.0},{"email":"A","score":95.0}]];
    assertEquality(exp, res);
}

type EmailRec readonly & record {|
    string email;
    string problemId;
|};

type ScoreRec record {|
    string pId;
    float score;
|};

function testClosuresInNestedQueryInSelect3() {
    EmailRec[] emailRec = [
        {email: "jake@abc.com", problemId: "12"},
        {email: "anne@abc.com", problemId: "20"},
        {email: "peter@abc.com", problemId: "3"}
    ];

    ScoreRec[] scoreRec = [
        {pId: "12", score: 80.0},
        {pId: "20", score: 95.0},
        {pId: "3", score: 72.0}
    ];

    var res = from var {email, problemId} in emailRec
        select from var {pId, score} in scoreRec
            where pId == problemId
            select {email, score};

    Rec[][] exp = [[{"email":"jake@abc.com","score":80.0}],[{"email":"anne@abc.com","score":95.0}],[{"email":"peter@abc.com","score":72.0}]];
    assertEquality(exp, res);
}

function testClosureInQueryActionInDo() {
    int[] arr = [];
    from var j in 1 ... 5
    do {
        from var k in 5 ... 7
        do {
            arr.push(k + j);
        };
    };
    assertEquality([6, 7, 8, 7, 8, 9, 8, 9, 10, 9, 10, 11, 10, 11, 12], arr);
}

function testClosureInQueryActionInDo2() {
    int[] arr = [];
    from var j in 1 ... 5
    do {
        from var k in 5 ... 7
        do {
            var x = from var i in 1 ... 2 select (from var t in 3 ... 4 where t == k select t);
        };
    };
    assertEquality([6, 7, 8, 7, 8, 9, 8, 9, 10, 9, 10, 11, 10, 11, 12], arr);
}

class EvenNumberGenerator {
    int i = 0;
    public isolated function next() returns record {|int value;|}? {
        self.i += 2;
        if (self.i >= 10) {
            return ();
        }
        return {value: self.i};
    }
}

string str1 = "string-1";

function testClosureInQueryActionInDo3() returns error? {
    string str2 = "string-2";
    EvenNumberGenerator evenGen = new;
    stream<int, error?> evenNumberStream = new (evenGen);
    int|string str3 = "string-3";

    if (str3 is int) {
    } else {
        check from var _ in evenNumberStream
        do {
            string _ = str1;
            string _ = str2;
            string _ = str3;
            int length1 = str1.length();
            int length2 = str2.length();
            int length3 = str3.length();

            assertEquality(str1, "string-1");
            assertEquality(str2, "string-2");
            assertEquality(str3, "string-3");
            assertEquality(length1, 8);
            assertEquality(length2, 8);
            assertEquality(length3, 8);
        };
  }
}

class A {
    public string? name;

    public function init(string? name) {
        self.name = name;
    }

    public function getName() returns string|error {
        if self.name is () {
            return error("Null value found for name attribute");
        }
        return <string> self.name;
    }
}

A object1 = new ("John");

function testClosureInQueryActionInDo4() returns error? {
    A object2 = new ("Jane");
    EvenNumberGenerator evenGen = new ();
    stream<int, error?> evenNumberStream = new (evenGen);
    A|error object3 = new ("Anne");

    if (object3 is error) {
    } else {
        check from var _ in evenNumberStream
        do {
            A _ = object1;
            A _ = object2;
            A _ = object3;

            string objectName1 = check object1.getName();
            string objectName2 = check object2.getName();
            string objectName3 = check object3.getName();

            assertEquality("John", objectName1);
            assertEquality("Jane", objectName2);
            assertEquality("Anne", objectName3);
        };
  }
}

function testClosureInQueryActionInDo5() returns error? {
    A object2 = new ("Jane");
    EvenNumberGenerator evenGen = new ();
    stream<int, error?> evenNumberStream = new (evenGen);
    A|error object3 = new ("Anne");

    if (object3 is error) {
    } else {
    check from var _ in evenNumberStream
    do {
        check from var _ in evenNumberStream
        do {
            A _ = object1;
            A _ = object2;
            A _ = object3;

            string objectName1 = check object1.getName();
            string objectName2 = check object2.getName();
            string objectName3 = check object3.getName();

            assertEquality("John", objectName1);
            assertEquality("Jane", objectName2);
            assertEquality("Anne", objectName3);
        };
    };
  }
}

function testClosureInQueryActionInDo6() returns error? {
    string str2 = "string-2";
    EvenNumberGenerator evenGen = new;
    stream<int, error?> evenNumberStream = new (evenGen);
    int|string str3 = "string-3";

    if (str3 is int) {
        assertTrue(false);
    } else {
        int[][] result = check from var _ in evenNumberStream
        let
            string _ = str1,
            string _ = str2,
            string _ = str3,
            int length1 = str1.length(),
            int length2 = str2.length(),
            int length3 = str3.length()
        select [length1, length2, length3];
        assertEquality([[8, 8, 8], [8, 8, 8], [8, 8, 8], [8, 8, 8]], result);
  }
}

function testClosureInQueryActionInDo7() returns error? {
    A object2 = new ("Jane");
    EvenNumberGenerator evenGen = new ();
    EvenNumberGenerator evenGen2 = new ();
    stream<int, error?> evenNumberStream = new (evenGen);
    stream<int, error?> evenNumberStream2 = new (evenGen2);
    A|error object3 = new ("Anne");

    if (object3 is error) {
        assertTrue(false);
    } else {
        string[][] result = check from var _ in evenNumberStream
                select check from var _ in evenNumberStream2
                let
                    A _ = object1,
                    A _ = object2,
                    A _ = object3,

                string objectName1 = check object1.getName(),
                string objectName2 = check object2.getName(),
                string objectName3 = check object3.getName()

                select "result";
                // select string `${objectName1}`; issue:- #40701
        assertEquality([["result", "result", "result", "result"],[],[],[]], result);
    }
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}

function assertTrue(anydata actual) {
    return assertEquality(true, actual);
}
