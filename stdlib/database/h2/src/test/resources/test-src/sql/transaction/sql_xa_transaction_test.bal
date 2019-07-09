// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/h2;

type ResultCount record {
    int COUNTVAL;
};

function testXATransactonSuccess() returns @tainted (int, int) {
    h2:Client testDB1 = new({
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    });

    h2:Client testDB2 = new({
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    });

    transaction {
        _ = checkpanic testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                values (1, 'Anne', 1000, 'UK')");
        _ = checkpanic testDB2->update("insert into Salary (id, value ) values (1, 1000)");
    }

    int count1;
    int count2;
    //check whether update action is performed
    var dt1 = testDB1->select("Select COUNT(*) as countval from Customers where customerId = 1 ", ResultCount);
    count1 = getTableCountValColumn(dt1);

    var dt2 = testDB2->select("Select COUNT(*) as countval from Salary where id = 1", ResultCount);
    count2 = getTableCountValColumn(dt2);

    checkpanic testDB1.stop();
    checkpanic testDB2.stop();
    return (count1, count2);
}

function testXATransactonSuccessWithDataSource() returns @tainted (int, int) {
    h2:Client testDB1 = new({
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" }
    });

    h2:Client testDB2 = new({
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" }
    });

    transaction {
        _ = checkpanic testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                values (10, 'Anne', 1000, 'UK')");
        _ = checkpanic testDB2->update("insert into Salary (id, value ) values (10, 1000)");
    }

    int count1;
    int count2;
    //check whether update action is performed
    var dt1 = testDB1->select("Select COUNT(*) as countval from Customers where customerId = 10 ", ResultCount);
    count1 = getTableCountValColumn(dt1);

    var dt2 = testDB2->select("Select COUNT(*) as countval from Salary where id = 10", ResultCount);
    count2 = getTableCountValColumn(dt2);
    checkpanic testDB1.stop();
    checkpanic testDB2.stop();
    return (count1, count2);
}

function testXATransactonSuccessWithH2Client() returns @tainted (int, int) {
    h2:Client testDB1 = new({
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" }
    });

    h2:Client testDB2 = new({
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" }
    });

    transaction {
        _ = checkpanic testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                values (11, 'Anne', 1000, 'UK')");
        _ = checkpanic testDB2->update("insert into Salary (id, value ) values (11, 1000)");
    }

    int count1;
    int count2;
    //check whether update action is performed
    var dt1 = testDB1->select("Select COUNT(*) as countval from Customers where customerId = 11", ResultCount);
    count1 = getTableCountValColumn(dt1);

    var dt2 = testDB2->select("Select COUNT(*) as countval from Salary where id = 11", ResultCount);
    count2 = getTableCountValColumn(dt2);
    checkpanic testDB1.stop();
    checkpanic testDB2.stop();
    return (count1, count2);
}

function testXATransactonFailed1() returns @tainted (int, int) {

    h2:Client testDB1 = new({
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    });

    h2:Client testDB2 = new({
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    });

    var e = trap testXATransactonFailed1Helper(testDB1, testDB2);

    int count1;
    int count2;
    //check whether update action is performed
    var dt1 = testDB1->select("Select COUNT(*) as countval from Customers where customerId = 2", ResultCount);
    count1 = getTableCountValColumn(dt1);

    var dt2 = testDB2->select("Select COUNT(*) as countval from Salary where id = 2 ", ResultCount);
    count2 = getTableCountValColumn(dt2);

    checkpanic testDB1.stop();
    checkpanic testDB2.stop();
    return (count1, count2);
}

function testXATransactonFailed1Helper(h2:Client testDB1, h2:Client testDB2) {
    transaction {
        _ = checkpanic testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                    values (2, 'John', 1000, 'UK')");
        _ = checkpanic testDB2->update("insert into Salary (id, invalidColumn ) values (2, 1000)");
    }
}

function testXATransactonFailed2() returns @tainted (int, int) {

    h2:Client testDB1 = new({
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    });

    h2:Client testDB2 = new({
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    });
    var e = trap testXATransactonFailed2Helper(testDB1, testDB2);
    //check whether update action is performed
    var dt1 = testDB1->select("Select COUNT(*) as countval from Customers where customerId = 2", ResultCount);
    int count1 = getTableCountValColumn(dt1);

    var dt2 = testDB2->select("Select COUNT(*) as countval from Salary where id = 2 ", ResultCount);
    int count2 = getTableCountValColumn(dt2);

    checkpanic testDB1.stop();
    checkpanic testDB2.stop();
    return (count1, count2);
}

function testXATransactonFailed2Helper(h2:Client testDB1, h2:Client testDB2) {
    transaction {
        _ = checkpanic testDB1->update("insert into Customers (customerId, name, creditLimit, invalidColumn)
                                    values (2, 'John', 1000, 'UK')");
        _ = checkpanic testDB2->update("insert into Salary (id, value ) values (2, 1000)");
    }
}

function testXATransactonRetry() returns @tainted (int, int) {

    h2:Client testDB1 = new({
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    });

    h2:Client testDB2 = new({
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    });

    testXATransactonRetryHelper(testDB1, testDB2);
    //check whether update action is performed
    var dt1 = testDB1->select("Select COUNT(*) as countval from Customers where customerId = 4", ResultCount);
    int count1 = getTableCountValColumn(dt1);

    var dt2 = testDB2->select("Select COUNT(*) as countval from Salary where id = 4", ResultCount);
    int count2 = getTableCountValColumn(dt2);

    checkpanic testDB1.stop();
    checkpanic testDB2.stop();
    return (count1, count2);
}

function testXATransactonRetryHelper(h2:Client testDB1, h2:Client testDB2) {
    int i = 0;
    transaction {
        if (i == 2) {
            _ = checkpanic testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                        values (4, 'John', 1000, 'UK')");
        } else {
            _ = checkpanic testDB1->update("insert into Customers (customerId, name, creditLimit, invalidColumn)
                        values (4, 'John', 1000, 'UK')");
        }
        _ = checkpanic testDB2->update("insert into Salary (id, value ) values (4, 1000)");
    } onretry {
        i = i + 1;
    }
}

function getTableCountValColumn(table<ResultCount>|error result) returns int {
    int count = -1;
    if (result is table<ResultCount>) {
        while (result.hasNext()) {
            var rs = result.getNext();
            if (rs is ResultCount) {
                count = rs.COUNTVAL;
            }
        }
        return count;
    }
    return -1;
}
