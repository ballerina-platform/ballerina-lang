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
            var x = from var i in 1 ... 2
                select (from var t in 5 ... 7
                    where t == k
                    select t);
            arr.push(...x[0]);
            arr.push(...x[1]);
        };
    };
    assertEquality([5, 5, 6, 6, 7, 7, 5, 5, 6, 6, 7, 7, 5, 5, 6, 6, 7, 7, 5, 5, 6, 6, 7, 7, 5, 5, 6, 6, 7, 7], arr);
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

class QueryArrowInit {
    function () returns int[] f;

    function init() {
        int[] ys = [1, 2, 3, 4, 5, 6, 7];
        self.f = () => from int y in ys
            select y;
    }
}

function testQueriesWithinArrowFunctionsAndWithLet() {
    int[] ys = [1, 2, 3, 4, 5, 6, 7];
    int[] xs = [8, 9, 10, 1, 2, 3, 4];
    int x = 21;

    function () returns int[] f1 = () => from var y in ys
        select y;
    assertEquality([1, 2, 3, 4, 5, 6, 7], f1());

    function () returns int[] f2 = () => from var y in ys
        select x + y;
    assertEquality([22, 23, 24, 25, 26, 27, 28], f2());

    function () returns function () returns int[] f3 = () => () => from var y in ys
        select y + x;
    function () returns int[] f4 = f3();
    assertEquality([22, 23, 24, 25, 26, 27, 28], f4());

    function () returns int[] f5 = () => from int y in ys
        let int sum = y + x
        where sum % 2 == 0
        select sum + y + x;
    assertEquality([44, 48, 52, 56], f5());

    function () returns int[] f6 = () => let int two = 2, int one = 1 in from int y in ys
            select y + one + two;
    assertEquality([4, 5, 6, 7, 8, 9, 10], f6());

    function () returns int[] f7 = let int two = 2, int one = 1 in () => from int y in ys
            select y + one + two;
    assertEquality([4, 5, 6, 7, 8, 9, 10], f7());

    QueryArrowInit class1 = new QueryArrowInit();
    assertEquality([1, 2, 3, 4, 5, 6, 7], class1.f());

    int[] q1 = let int two = 2, int one = 1 in from int y in ys
            select y + one + two;
    assertEquality([4, 5, 6, 7, 8, 9, 10], q1);

    record {|int r;|}[] q2 = let int two = 2, int one = 1 in from int y in ys
            select {
                r: y + one + two
            };
    assertEquality([{"r": 4}, {"r": 5}, {"r": 6}, {"r": 7}, {"r": 8}, {"r": 9}, {"r": 10}], q2);

    record {|int r; int k; int j;|}[] q3 = let int two = 2, int one = 1 in from int y in ys
            select {
                r: y + one + two,
                k: one,
                j: y
            };
    assertEquality([
                {"r": 4, "k": 1, "j": 1},
                {"r": 5, "k": 1, "j": 2},
                {"r": 6, "k": 1, "j": 3},
                {"r": 7, "k": 1, "j": 4},
                {"r": 8, "k": 1, "j": 5},
                {"r": 9, "k": 1, "j": 6},
                {"r": 10, "k": 1, "j": 7}
            ], q3);

    record {|int r; int k; function () returns int[] j;|}[] q4 = let int two = 2, int one = 1 in from int y in ys
            where y % 2 == 0
            select {
                r: y + one + two,
                k: one,
                j: () => from int q in xs
                    select one + q
            };
    var item = q4[0];
    assertEquality(item.r, 5);
    assertEquality(item.k, 1);
    function () returns int[] f8 = item.j;
    assertEquality(f8(), [9, 10, 11, 2, 3, 4, 5]);

    // Uncomment once #43415 is fixed
    // record {|function () returns int[] f; int r;|}[] q5 = from int y in ys
    //     select {
    //         r : y,
    //         f: function() returns int[] {
    //             return from int k in xs
    //                 select k + y;
    //         }
    //     };
    // function () returns int[] f9 = q5[0].f;
}

type Grade record {|
    string course;
    string grade;
    int[] sections;
|};

type Student record {|
    string name;
    Grade[] grades;
|};

type GradeSection record {|
    string course;
    int[] sections;
|};

Student[] students = [
    {
        name: "john",
        grades: [
            {course: "Ma", grade: "A+", sections: [10, 9, 6, 5]},
            {course: "Bal", grade: "A+", sections: [8, 8, 2, 5]}
        ]
    },
    {
        name: "bob",
        grades: [
            {course: "Sci", grade: "A", sections: [8, 3, 4, 5]},
            {course: "Com", grade: "B", sections: [2, 6, 5, 7]}
        ]
    }
];

function testNestedQueryAndClosureFieldInQuerySelect() {
    record {|string name; string[] courses;|}[] studentCourses = from Student student in students
        select {
            courses: from Grade grade in student.grades
                select grade.course,
            name: student.name
        };
    assertEquality([{name: "john", courses: ["Ma", "Bal"]}, {name: "bob", courses: ["Sci", "Com"]}], studentCourses);

    record {|string name; function () returns Grade[] getGrades;|}[] studentCourse2 = from Student student in students
        select {
            getGrades: function() returns Grade[] {
                return student.grades;
            },
            name: student.name
        };

    assertEquality(studentCourse2[0].name, "john");
    function () returns Grade[] getGrades = studentCourse2[0].getGrades;
    assertEquality([
                {course: "Ma", grade: "A+", sections: [10, 9, 6, 5]},
                {course: "Bal", grade: "A+", sections: [8, 8, 2, 5]}
            ], getGrades());

    assertEquality(studentCourse2[1].name, "bob");
    getGrades = studentCourse2[1].getGrades;
    assertEquality([
                {course: "Sci", grade: "A", sections: [8, 3, 4, 5]},
                {course: "Com", grade: "B", sections: [2, 6, 5, 7]}
            ], getGrades());

    GradeSection[][] studentGradeSections = from Student student in students
        let GradeSection[] gradeSections = from Grade ge in student.grades
            select {
                sections: from int section in ge.sections
                    where section > 5
                    select section,
                course: ge.course
            }
        select gradeSections;
    assertEquality([
                [{course: "Ma", sections: [10, 9, 6]}, {course: "Bal", sections: [8, 8]}],
                [{course: "Sci", sections: [8]}, {course: "Com", sections: [6, 7]}]
            ], studentGradeSections);

    record {|int[] total; string name;|}[] totals = from Student s in students
        select {
            total: from Grade g in s.grades
                select from int mark in g.sections
                    collect sum(mark),
            name: s.name
        };

    assertEquality([
                {total: [30, 23], name: "john"},
                {total: [20, 20], name: "bob"}
            ], totals);

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
