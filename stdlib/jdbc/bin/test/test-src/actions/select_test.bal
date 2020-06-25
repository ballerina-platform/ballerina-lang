// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

string jdbcUserName = "SA";
string jdbcPassword = "";

type NumericData record {
    int int_type;
    int bigint_type;
    int smallint_type;
    int tinyint_type;
    int bit_type;
    decimal decimal_type;
    decimal numeric_type;
    float float_type;
    float real_type;
};

type ResultCount record {
    int COUNTVAL;
};

//TODO:Refactor record
type ResultCustomers record {
    string FIRSTNAME;
};

//TODO:Refactor record
type ResultBlob record {
    byte[] BLOB_TYPE;
};

function testSelectNumericData(string jdbcURL) returns [int, int, int, int, int, decimal, decimal, float, float] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type," +
                                "numeric_type, float_type, real_type from NumericTypes where id = 1", NumericData);

    int intType = -1;
    int bigintType = -1;
    int smallintType = -1;
    int tinyintType = -1;
    int bitType = -1;
    decimal decimalType = -1;
    decimal numericType = -1;
    float floatType = -1;
    float realType = -1;

    if (result is table<NumericData>) {
        foreach var x in result {
            intType = <@untainted>x.int_type;
            bigintType = <@untainted>x.bigint_type;
            smallintType = <@untainted>x.smallint_type;
            tinyintType = <@untainted>x.tinyint_type;
            bitType = <@untainted>x.bit_type;
            decimalType = <@untainted>x.decimal_type;
            numericType = <@untainted>x.numeric_type;
            floatType = <@untainted>x.float_type;
            realType = <@untainted>x.real_type;
        }
    }
    checkpanic testDB.stop();
    return [intType, bigintType, smallintType, tinyintType, bitType, decimalType, numericType, floatType, realType];
}

function testSelectNumericDataWithDBError(string jdbcURL) returns [boolean, boolean, boolean, boolean, string, string,
    string, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT * from InvalidTable where id = 1", ());
    boolean matchedToError = false;
    boolean matchedToJdbcError = false;
    boolean matchedToDBError = false;
    boolean matchedToApplicaitonError = false;
    string message = "";
    string state = "";
    string reason = "";
    int errorCode = -1;

    if (result is error) {
        matchedToError = true;
    }
    if (result is jdbc:Error) {
        matchedToJdbcError = true;
    }
    if (result is jdbc:DatabaseError) {
        matchedToDBError = true;
        message = <@untainted><string>result.detail()["message"];
        state = <@untainted>result.detail()["sqlState"];
        reason = <@untainted>result.reason();
        errorCode = <@untainted>result.detail()["sqlErrorCode"];
    }
    if (result is jdbc:ApplicationError) {
        matchedToApplicaitonError = true;
    }

    checkpanic testDB.stop();
    return [matchedToError, matchedToJdbcError, matchedToDBError, matchedToApplicaitonError, message, state, reason,
            errorCode];
}

function testSelectNumericDataWithApplicationError(string jdbcURL) returns [boolean, boolean, boolean, boolean,
    string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    xml x1 = xml `<fname>John</fname>`;
    xml x2 = xml `<fname>Jane</fname>`;
    xml[] xmlData = [x1, x2];
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: xmlData};
    var result = testDB->select("SELECT * from NumericTypes where id in (?)", (), para1);
    boolean matchedToError = false;
    boolean matchedToJdbcError = false;
    boolean matchedToDBError = false;
    boolean matchedToApplicaitonError = false;
    string reason = "";
    string message = "";

    if (result is error) {
        matchedToError = true;
    }
    if (result is jdbc:Error) {
        matchedToJdbcError = true;
    }
    if (result is jdbc:DatabaseError) {
        matchedToDBError = true;
    }
    if (result is jdbc:ApplicationError) {
        matchedToApplicaitonError = true;
        reason = <@untainted>result.reason();
        message = <@untainted><string>result.detail()["message"];
    }

    checkpanic testDB.stop();
    return [matchedToError, matchedToJdbcError, matchedToDBError, matchedToApplicaitonError, reason, message];
}

//TODO:Refactor with proper tables
function testArrayOfQueryParameters(string jdbcURL) returns string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int[] intDataArray = [1, 4343];
    string[] stringDataArray = ["A", "B"];
    float[] doubleArray = [233.4, 433.4];
    decimal[] decimalArray = [1233.4d, 1433.4d];
    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_VARCHAR, value: "Johhhn"};
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: intDataArray};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: stringDataArray};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_DOUBLE, value: doubleArray};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DOUBLE, value: decimalArray};

    var dt = testDB->select("SELECT  FirstName from Customers where FirstName = ? or lastName = 'A' or " +
        "lastName = '\"BB\"' or registrationID in(?) or lastName in(?) or creditLimit in(?) or creditLimit in (?)",
        ResultCustomers, para0, para1, para2, para3, para4);
    string firstName = "";
    if (dt is table<ResultCustomers>) {
        foreach var x in dt {
            firstName = <@untainted>x.FIRSTNAME;
        }
    }
    checkpanic testDB.stop();
    return firstName;
}

//TODO:Refactor with proper tables
function testBoolArrayOfQueryParameters(string jdbcURL) returns string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    boolean accepted1 = false;
    boolean accepted2 = false;
    boolean accepted3 = true;
    boolean[] boolDataArray = [accepted1, accepted2, accepted3];

    string[] stringDataArray = ["John", "Anne", "Peter"];

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_BOOLEAN, value: boolDataArray};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: stringDataArray};

    var dt = testDB->select("SELECT firstName from Customers where customerId = ? and visaAccepted in(?) and " +
        "firstName in (?)", ResultCustomers, 1, para1, para2);
    string firstName = "";
    if (dt is table<ResultCustomers>) {
        foreach var x in dt {
            firstName = <@untainted>x.FIRSTNAME;
        }
    }
    checkpanic testDB.stop();
    return firstName;
}

//TODO:Refactor with proper tables
function testBlobArrayOfQueryParameter(string jdbcURL) returns string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var dt1 = testDB->select("SELECT info from Customers where customerId = 1", ResultBlob);
    byte[] blobData = [];
    if (dt1 is table<ResultBlob>) {
        while (dt1.hasNext()) {
            var rs = dt1.getNext();
            if (rs is ResultBlob) {
                blobData = rs.BLOB_TYPE;
            }
        }
    }
    byte[][] blobDataArray = [blobData, blobData];

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_BLOB, value: blobDataArray};

    var dt = testDB->select("SELECT firstName from Customers where customerId = ? and info in (?)", ResultCustomers, 1,
        para1);
    string firstName = "";
    if (dt is table<ResultCustomers>) {
        foreach var x in dt {
            firstName = <@untainted>x.FIRSTNAME;
        }
    }
    checkpanic testDB.stop();
    return firstName;
}

function testCloseConnectionPool(string jdbcURL) returns @tainted (int) {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });
    var dt = testDB->select("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", ResultCount);
    int count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return count;
}

function getTableCountValColumn(table<ResultCount> | error result) returns int {
    int count = -1;
    if (result is table<ResultCount>) {
        foreach var x in result {
            count = <@untainted>x.COUNTVAL;
        }
        return count;
    }
    return -1;
}
