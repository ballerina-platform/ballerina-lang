// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
