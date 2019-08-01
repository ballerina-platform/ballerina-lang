// Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerinax/java.jdbc;

jdbc:PoolOptions properties = { maximumPoolSize: 1,
    idleTimeoutInMillis: 600000, connectionTimeoutInMillis: 30000, autoCommit: true, maxLifetimeInMillis: 1800000,
    minimumIdle: 1, validationTimeoutInMillis: 5000,
    connectionInitSql: "SELECT 1" };

map<any> propertiesMap = { "loginTimeout": "1000" };
jdbc:PoolOptions properties3 = { dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" };

map<any> propertiesMap2 = { "loginTimeout": "1000" };
jdbc:PoolOptions properties4 = { dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" };

jdbc:PoolOptions properties5 = { dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" };

map<any> propertiesMap3 = { "loginTimeout": "1000" };
jdbc:PoolOptions properties6 = { dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" };

function testConnectionPoolProperties1() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}

function testConnectionPoolProperties2() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}

function testConnectionPoolProperties3() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: ""
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}


function testConnectorWithDefaultPropertiesForListedDB() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: { }
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}

function testConnectorWithWorkers() returns (json) {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: { }
    });

    worker w1 returns json {
        int x = 0;
        json y;

        var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

        json j = getJsonConversionResult(dt);
        checkpanic testDB.stop();
        return j;
    }
    worker w2 {
        int x = 10;
    }
    return wait w1;
}

function testConnectorWithDataSourceClass() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties3,
        dbOptions: propertiesMap
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}

function testConnectorWithDataSourceClassAndProps() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties4,
        dbOptions: propertiesMap2
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}

function testConnectorWithDataSourceClassWithoutURL() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties5
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}

function testConnectorWithDataSourceClassURLPriority() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties6,
        dbOptions: propertiesMap3
    });


    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}


function testPropertiesGetUsedOnlyIfDataSourceGiven() returns @tainted json {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 },
        dbOptions: { "invalidProperty": 109 }
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());

    json j = getJsonConversionResult(dt);
    checkpanic testDB.stop();
    return j;
}

function testConnectionFailure() {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/NON_EXISTING_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 },
        dbOptions: { "IFEXISTS": true }
    });

}

function getJsonConversionResult(table<record {}>|error tableOrError) returns json {
    json retVal;
    if (tableOrError is table<record {}>) {
        var jsonConversionResult = typedesc<json>.constructFrom(tableOrError);
        if (jsonConversionResult is json) {
            retVal = jsonConversionResult;
        } else {
            retVal = { "Error": <string> jsonConversionResult.detail().message };
        }
    } else {
        retVal = { "Error": <string> tableOrError.detail()["message"] };
    }
    return retVal;
}
