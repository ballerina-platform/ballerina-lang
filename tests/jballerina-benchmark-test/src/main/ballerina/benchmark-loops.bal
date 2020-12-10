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

type Person record {|
   int id;
   string fname;
   string lname;
|};

type Department record {|
   int id;
   string name;
|};

type DeptPerson record {|
   string fname;
   string lname;
   string? dept;
|};

isolated function getPersonArray(int size) returns Person[] {
    Person[] personList = [];
    int i = 0;
    while (i < size) {
        personList.push({id: i, fname: "FName", lname: "LName"});
        i += 1;
    }
    return personList;
}

isolated function getDepartmentArray(int size) returns Department[] {
    Department[] deptList = [];
    int i = 0;
    while (i < size) {
        deptList.push({id: i, name: "DeptName"});
        i += 1;
    }
    return deptList;
}

public function benchmarkEmptyWhile(int warmupCount, int benchmarkCount) returns int {
    int i = 0;
    while (i < warmupCount) {
        i += 1;
    }

    i = 0;
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkWhileWithMapAssignment(int warmupCount, int benchmarkCount) returns int {
    Person p = {id: 1, fname: "FName", lname: "LName"};
    int i = 0;
    map<[string, any]> e = {};
    while (i < warmupCount) {
        e = {};
        i += 1;
    }

    i = 0;
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        e = {};
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkWhileWithEntries(int warmupCount, int benchmarkCount) returns int {
    Person p = {id: 1, fname: "FName", lname: "LName"};
    int i = 0;
    map<[string, any]> e = {};
    while (i < warmupCount) {
        e = p.entries();
        i += 1;
    }

    i = 0;
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        e = p.entries();
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkWithoutCasting(int warmupCount, int benchmarkCount) returns int {
    Person p = {id: 1, fname: "FName", lname: "LName"};
    int i = 0;
    while (i < warmupCount) {
        Person psn = p;
        i += 1;
    }

    i = 0;
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        Person psn = p;
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkWithCastingFromNilable(int warmupCount, int benchmarkCount) returns int {
    Person? p = {id: 1, fname: "FName", lname: "LName"};
    int i = 0;
    while (i < warmupCount) {
        Person psn = <Person>p;
        i += 1;
    }

    i = 0;
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        Person psn = <Person>p;
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkWithCastingFromAny(int warmupCount, int benchmarkCount) returns int {
    Person p = {id: 1, fname: "FName", lname: "LName"};
    any p2 = p;
    int i = 0;
    while (i < warmupCount) {
        Person psn = <Person>p2;
        i += 1;
    }

    i = 0;
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        Person psn = <Person>p2;
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkLoopWithQuery(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    Person[] outputList =
        from var person in pl
        select person;

    pl = getPersonArray(benchmarkCount);
    int startTime = nanoTime();
    outputList =
            from var person in pl
            select person;
    return (nanoTime() - startTime);
}

public function benchmarkLoopWithStreamNextAndInvertedTypeGuard(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    stream<Person> personStream = pl.toStream();
    Person[] outputList = [];

    record {| Person value; |}|error? v = personStream.next();
    while (!(v is ()) && !(v is error)) {
        outputList.push(v.value);
        v = personStream.next();
    }

    pl = getPersonArray(benchmarkCount);
    personStream = pl.toStream();
    outputList = [];
    int startTime = nanoTime();
    v = personStream.next();
    while (!(v is ()) && !(v is error)) {
        outputList.push(v.value);
        v = personStream.next();
    }
    return (nanoTime() - startTime);
}

public function benchmarkLoopWithStreamNextAndTypeGuard(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    stream<Person> personStream = pl.toStream();
    Person[] outputList = [];

    record {| Person value; |}|error? v = personStream.next();
    while (v is record {| Person value; |}) {
        outputList.push(v.value);
        v = personStream.next();
    }

    pl = getPersonArray(benchmarkCount);
    personStream = pl.toStream();
    outputList = [];
    int startTime = nanoTime();
    v = personStream.next();
    while (v is record {| Person value; |}) {
        outputList.push(v.value);
        v = personStream.next();
    }
    return (nanoTime() - startTime);
}

public function benchmarkLoopWithWhile(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    Person[] outputList = [];
    int i = 0;
    while (i < warmupCount) {
        outputList.push(pl[i]);
        i += 1;
    }

    pl = getPersonArray(benchmarkCount);
    outputList = [];
    i = 0;
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        outputList.push(pl[i]);
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkLoopWithForeach(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    Person[] outputList = [];

    foreach var v in pl {
        outputList.push(v);
    }

    pl = getPersonArray(benchmarkCount);
    outputList = [];
    int startTime = nanoTime();
    foreach var v in pl {
        outputList.push(v);
    }
    return (nanoTime() - startTime);
}

type _Frame record {|
    (any|error|())...;
|};

public function benchmarkLoopWithFramesForeach(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    Person[] outputList = [];
    _Frame[] frames = [];

    foreach var v in pl {
        _Frame _frame = {};
        foreach var [k, val] in v.entries() {
            _frame[k] = val;
        }
        frames.push(_frame);
    }
    foreach _Frame f in frames {
        Person p = {id: <int>f["id"], fname: <string>f["fname"], lname: <string>f["lname"]};
        outputList.push(p);
    }

    pl = getPersonArray(benchmarkCount);
    outputList = [];
    frames = [];
    int startTime = nanoTime();
    foreach var v in pl {
        _Frame _frame = {};
        foreach var [k, val] in v.entries() {
            _frame[k] = val;
        }
        frames.push(_frame);
    }
    foreach _Frame f in frames {
        Person p = {id: <int>f["id"], fname: <string>f["fname"], lname: <string>f["lname"]};
        outputList.push(p);
    }
    return (nanoTime() - startTime);
}

public function benchmarkLoopWithFramesWOEntries(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    Person[] outputList = [];
    _Frame[] frames = [];
    _Frame[] frames2 = [];

    foreach var v in pl {
        _Frame _frame = {};
        //foreach var [k, val] in v.entries() {
        //    _frame[k] = val;
        //}
        frames.push(_frame);
    }
    foreach _Frame f in frames {
        //Person p = <Person> f;
        //outputList.push(p);
        frames2.push(f);
    }

    pl = getPersonArray(benchmarkCount);
    outputList = [];
    frames = [];
    int startTime = nanoTime();
    foreach var v in pl {
        _Frame _frame = {};
        //foreach var [k, val] in v.entries() {
        //    _frame[k] = val;
        //}
        frames.push(_frame);
    }
    foreach _Frame f in frames {
        //Person p = <Person> f;
        //outputList.push(p);
        frames2.push(f);
    }
    return (nanoTime() - startTime);
}

public function benchmarkNestedLoopWithQuery(int warmupCount, int benchmarkCount) returns int {
    int c = round(ceil(sqrt(warmupCount * 1.0f)));
    Person[] pl = getPersonArray(c);
    Department[] dl = getDepartmentArray(c);
    DeptPerson[] outputList =
        from var person in pl
        from var dept in dl
        limit warmupCount
        select {
            fname: person.fname,
            lname: person.lname,
            dept: dept.name
        };

    c = round(ceil(sqrt(benchmarkCount * 1.0f)));
    pl = getPersonArray(c);
    dl = getDepartmentArray(c);

    int startTime = nanoTime();
    outputList =
        from var person in pl
        from var dept in dl
        limit benchmarkCount
        select {
            fname: person.fname,
            lname: person.lname,
            dept: dept.name
        };
    return (nanoTime() - startTime);
}

public function benchmarkNestedLoopWithWhile(int warmupCount, int benchmarkCount) returns int {
    int c = round(ceil(sqrt(warmupCount * 1.0f)));
    Person[] pl = getPersonArray(c);
    Department[] dl = getDepartmentArray(c);
    DeptPerson[] outputList = [];
    int i = 0;
    int j = 0;

    while (i < c && outputList.length() < warmupCount) {
        Person p = pl[i];
        j = 0;
        while (j < c && outputList.length() < warmupCount) {
            Department d = dl[j];
            outputList.push({fname: p.fname, lname: p.lname, dept: d.name});
            j += 1;
        }
        i += 1;
    }

    c = round(ceil(sqrt(benchmarkCount * 1.0f)));
    pl = getPersonArray(c);
    dl = getDepartmentArray(c);
    outputList = [];
    i = 0;
    j = 0;

    int startTime = nanoTime();
    while (i < c && outputList.length() < benchmarkCount) {
        Person p = pl[i];
        j = 0;
        while (j < c && outputList.length() < benchmarkCount) {
            Department d = dl[j];
            outputList.push({fname: p.fname, lname: p.lname, dept: d.name});
            j += 1;
        }
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkNestedLoopWithForeach(int warmupCount, int benchmarkCount) returns int {
    int c = round(ceil(sqrt(warmupCount * 1.0f)));
    Person[] pl = getPersonArray(c);
    Department[] dl = getDepartmentArray(c);
    DeptPerson[] outputList = [];

    foreach var p in pl {
        if (outputList.length() < warmupCount) {
            foreach var d in dl {
                if (outputList.length() < warmupCount) {
                    outputList.push({fname: p.fname, lname: p.lname, dept: d.name});
                }
            }
        }
    }

    c = round(ceil(sqrt(benchmarkCount * 1.0f)));
    pl = getPersonArray(c);
    dl = getDepartmentArray(c);
    outputList = [];
    int startTime = nanoTime();
    foreach var p in pl {
        if (outputList.length() < benchmarkCount) {
            foreach var d in dl {
                if (outputList.length() < benchmarkCount) {
                    outputList.push({fname: p.fname, lname: p.lname, dept: d.name});
                }
            }
        }
    }
    return (nanoTime() - startTime);
}

public function benchmarkJoinWithQuery(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    Department[] dl = getDepartmentArray(warmupCount);
    DeptPerson[] outputList =
        from var person in pl
        join var dept in dl
        on person.id equals dept.id
        limit warmupCount
        select {
            fname: person.fname,
            lname: person.lname,
            dept: dept.name
        };

    pl = getPersonArray(benchmarkCount);
    dl = getDepartmentArray(benchmarkCount);
    int startTime = nanoTime();
    outputList =
        from var person in pl
        join var dept in dl
        on person.id equals dept.id
        limit benchmarkCount
        select {
            fname: person.fname,
            lname: person.lname,
            dept: dept.name
        };
    return (nanoTime() - startTime);
}

public function benchmarkJoinWithWhile(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    Department[] dl = getDepartmentArray(warmupCount);
    map<Department> deptMap = {};
    DeptPerson[] outputList = [];
    int i = 0;
    // warmup exec
    while (i < warmupCount) {
        Department d = dl[i];
        deptMap[d.id.toString()] = d;
        i += 1;
    }
    i = 0;
    while (i < warmupCount) {
        Person p = pl[i];
        if (deptMap.hasKey(p.id.toString())) {
            Department d = <Department>deptMap[p.id.toString()];
            outputList.push({fname: p.fname, lname: p.lname, dept: d.name});
        }
        i += 1;
    }

    pl = getPersonArray(benchmarkCount);
    dl = getDepartmentArray(benchmarkCount);
    deptMap = {};
    outputList = [];
    i = 0;

    // benchmark exec
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        Department d = dl[i];
        deptMap[d.id.toString()] = d;
        i += 1;
    }
    i = 0;
    while (i < benchmarkCount) {
        Person p = pl[i];
        if (deptMap.hasKey(p.id.toString())) {
            Department d = <Department>deptMap[p.id.toString()];
            outputList.push({fname: p.fname, lname: p.lname, dept: d.name});
        }
        i += 1;
    }
    return (nanoTime() - startTime);
}

public function benchmarkJoinWithForeach(int warmupCount, int benchmarkCount) returns int {
    Person[] pl = getPersonArray(warmupCount);
    Department[] dl = getDepartmentArray(warmupCount);
    map<Department> deptMap = {};
    DeptPerson[] outputList = [];

    // warmup exec
    foreach var d in dl {
        deptMap[d.id.toString()] = d;
    }
    foreach var p in pl {
        if (deptMap.hasKey(p.id.toString())) {
            Department d = <Department>deptMap[p.id.toString()];
            outputList.push({fname: p.fname, lname: p.lname, dept: d.name});
        }
    }

    pl = getPersonArray(benchmarkCount);
    dl = getDepartmentArray(benchmarkCount);
    deptMap = {};
    outputList = [];

    // benchmark exec
    int startTime = nanoTime();
    foreach var d in dl {
        deptMap[d.id.toString()] = d;
    }
    foreach var p in pl {
        if (deptMap.hasKey(p.id.toString())) {
            Department d = <Department>deptMap[p.id.toString()];
            outputList.push({fname: p.fname, lname: p.lname, dept: d.name});
        }
    }
    return (nanoTime() - startTime);
}
