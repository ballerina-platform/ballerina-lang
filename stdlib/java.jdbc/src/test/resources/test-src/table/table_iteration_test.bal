// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java.jdbc;

type Person record {
    int id;
    int age;
    float salary;
    string name;
};

type ResultCount record {
    int COUNTVAL;
};

function testCountInTable(string jdbcURL) returns @tainted int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person where id < 10", Person);
    int count = -1;
    if (dt is table<Person>) {
        count = dt.count();
    }
    checkpanic testDB.stop();
    return count;
}

function testFilterTable(string jdbcURL) returns @tainted [int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person", Person);
    Person[] personBelow35 = [];
    int id1 = -1;
    int id2 = -1;
    int count = -1;
    if (dt is table<Person>) {
        personBelow35 = dt.filter(isBelow35);
        count = personBelow35.length();
        id1 = personBelow35[0].id;
        id2 = personBelow35[1].id;
    }
    checkpanic testDB.stop();
    return [count, id1, id2];
}

function testFilterWithAnonymousFuncOnTable(string jdbcURL) returns @tainted [int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person", Person);
    Person[] personBelow35;
    int count = -1;
    int id1 = -1;
    int id2 = -1;
    if (dt is table<Person>) {
        personBelow35 = dt.filter(function (Person p) returns (boolean) {
            return p.age < 35;
        });
        count = personBelow35.length();
        id1 = personBelow35[0].id;
        id2 = personBelow35[1].id;
    }
    checkpanic testDB.stop();
    return [count, id1, id2];
}

function testFilterTableWithCount(string jdbcURL) returns @tainted int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person", Person);
    int count = -1;
    if (dt is table<Person>) {
        count = dt.filter(isBelow35).count();
    }
    checkpanic testDB.stop();
    return count;
}

function testMapTable(string jdbcURL) returns @tainted string[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    string[] names = [];
    if (dt is table<Person>) {
        names = dt.map(getName);
    }
    checkpanic testDB.stop();
    return names;
}

function testMapWithFilterTable(string jdbcURL) returns @tainted string[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var dt = testDB->select("SELECT * from Person order by id", Person);
    string[] names = [];
    if (dt is table<Person>) {
        names = dt.map(getName).filter(isGeraterThan4String);
    }
    checkpanic testDB.stop();
    return names;
}

function testFilterWithMapTable(string jdbcURL) returns @tainted string[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    string[] names = [];
    if (dt is table<Person>) {
        names = dt.filter(isGeraterThan4).map(getName);
    }
    checkpanic testDB.stop();
    return names;
}

function testFilterWithMapAndCountTable(string jdbcURL) returns @tainted int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    int count = -1;
    if (dt is table<Person>) {
        count = dt.filter(isGeraterThan4).map(getName).count();
    }
    checkpanic testDB.stop();
    return count;
}

function testAverageWithTable(string jdbcURL) returns @tainted float {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    float avgSal = -1;
    if (dt is table<Person>) {
        avgSal = dt.map(getSalary).average();
    }
    checkpanic testDB.stop();
    return avgSal;
}

function testMinWithTable(string jdbcURL) returns @tainted float {
    jdbc:Client testDB = new ({
        url: jdbcURL
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    float avgSal = -1;
    if (dt is table<Person>) {
        avgSal = dt.map(getSalary).min();
    }
    checkpanic testDB.stop();
    return avgSal;
}

function testMaxWithTable(string jdbcURL) returns @tainted float {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    float avgSal = -1;
    if (dt is table<Person>) {
        avgSal = dt.map(getSalary).max();
    }
    checkpanic testDB.stop();
    return avgSal;
}

function testSumWithTable(string jdbcURL) returns @tainted float {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    float avgSal = -1;
    if (dt is table<Person>) {
        avgSal = dt.map(getSalary).sum();
    }
    checkpanic testDB.stop();
    return avgSal;
}

function testCloseConnectionPool(string jdbcURL) returns @tainted int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SESSIONS",
    ResultCount);
    int count = -1;
    if (dt is table<ResultCount>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultCount) {
                count = rs.COUNTVAL;
            }
        }
    }
    checkpanic testDB.stop();
    return count;
}

function isBelow35(Person p) returns (boolean) {
    return p.age < 35;
}

function getName(Person p) returns (string) {
    return p.name;
}

function getSalary(Person p) returns (float) {
    return p.salary;
}

function isGeraterThan4(Person p) returns (boolean) {
    return p.name.length() > 4;
}

function isGeraterThan4String(string s) returns (boolean) {
    return s.length() > 4;
}
