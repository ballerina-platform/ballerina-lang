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
import ballerina/io;

string jdbcURL = "jdbc:hsqldb:file:./target/tempdb/JDBC_ACTIONS_TEST_HSQLDB";
string jdbcUserName = "SA";
string jdbcPassword = "";

type ResultCount record {
    int COUNTVAL;
};

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

type StringData record {
    string varchar_type;
    string charmax_type;
    string char_type;
    string charactermax_type;
    string character_type;
    string nvarcharmax_type;
    string longvarchar_type;
    string clob_type;
};


//Update Remote Function Tests
function testCreateTable() returns [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->update("CREATE TABLE TestCreateTable(studentID int, LastName varchar(255))");
    int updatedRowCount = 0;
    int generatedKeysMapLength = 0;
    if (result is jdbc:UpdateResult) {
        updatedRowCount = result.updatedRowCount;
        generatedKeysMapLength =result.generatedKeys.length();
    }
    checkpanic testDB.stop();
    return [updatedRowCount, generatedKeysMapLength];
}

function testBasicInsertData() returns [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->update("Insert into NumericTypes (int_type) values (20)");
    int insertCount = 0;
    int generatedKey = -1;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
    }
    checkpanic testDB.stop();
    return [insertCount, generatedKey];
}

function testBasicInsertDataWithoutGeneratedKey() returns [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->update("Insert into StringTypes (id, varchar_type) values (20, 'test')");
    int insertCount = 0;
    int generatedKeyMapLength = -1;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKeyMapLength =result.generatedKeys.length();
    }
    checkpanic testDB.stop();
    return [insertCount, generatedKeyMapLength];
}

function testInsertDataWithGeneratedKey() returns [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int count = 0;
    int generatedKey = 0;
    var x = testDB->update("insert into NumericTypes (int_type) values (21)");
    if (x is jdbc:UpdateResult) {
        count = x.updatedRowCount;
        generatedKey = <int>x.generatedKeys["ID"];
    }
    checkpanic testDB.stop();
    return [count, generatedKey];
}

function testBasicInsertDataWithDatabaseError() returns [boolean, boolean, boolean, boolean, boolean, string, string,
    string, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->update("Insert into NumericTypesNonExistTable (int_type) values (20)");
    int insertCount = -1;
    int generatedKey = -1;
    boolean errorOccured = false;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
    } else {
        errorOccured = true;
    }

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
        message = <string>result.detail()["message"];
        errorCode = result.detail()["sqlErrorCode"];
        state = result.detail()["sqlState"];
        reason = result.reason();
    }
    if (result is jdbc:ApplicationError) {
        matchedToApplicaitonError = true;
    }

    checkpanic testDB.stop();
    return [errorOccured, matchedToError, matchedToJdbcError, matchedToDBError, matchedToApplicaitonError, message,
        state, reason, errorCode];
}


function testBasicInsertDataWithApplicationError() returns [boolean, boolean, boolean, boolean, boolean, string,
        string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    xml x1 = xml `<fname>John</fname>`;
    xml x2 = xml `<fname>Jane</fname>`;
    xml[] xmlData = [x1, x2];
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_VARCHAR, value: xmlData };
    var result = testDB->update("Insert into NumericTypes (int_type) values (?)", para1);
    int insertCount = -1;
    int generatedKey = -1;
    boolean errorOccured = false;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
    } else {
        errorOccured = true;
    }

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
        reason = result.reason();
        message = <string>result.detail()["message"];
    }

    checkpanic testDB.stop();
    return [errorOccured, matchedToError, matchedToJdbcError, matchedToDBError, matchedToApplicaitonError,
            reason, message];
}

function testUpdateTableData() returns [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int updateCount = 0;
    var result = testDB->update("Update NumericTypes set int_type = 11 where int_type = 10");
    if (result is jdbc:UpdateResult) {
        updateCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT count(*) as countval from NumericTypes where int_type = 11",
            ResultCount);
    int count = getTableCountValColumn(dt);

    checkpanic testDB.stop();
    return [updateCount, count];
}

function testInsertNumericDataWithParameters() returns [int, int, int, int, int, int, decimal, decimal, float, float] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int intTypeData = 2147483647;
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_INTEGER, value: intTypeData, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_BIGINT, value: 2147483650, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_SMALLINT, value: 3000, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_TINYINT, value: 255, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para5 = { sqlType: jdbc:TYPE_BIT, value: 1, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para6 = { sqlType: jdbc:TYPE_DECIMAL, value: 5000.75, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para7 = { sqlType: jdbc:TYPE_NUMERIC, value: 5000.76, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para8 = { sqlType: jdbc:TYPE_FLOAT, value: 5000.77, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para9 = { sqlType: jdbc:TYPE_REAL, value: 5000.78, direction: jdbc:DIRECTION_IN };

    var result = testDB->update("Insert into NumericTypes (int_type, bigint_type, smallint_type, tinyint_type,
                                                    bit_type, decimal_type, numeric_type, float_type, real_type)
                                                    values (?,?,?,?,?,?,?,?,?)", para1, para2, para3, para4, para5,
                                                    para6, para7, para8, para9);
    int insertCount = 0;
    int generatedKey = -1;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
    }

    var dt = testDB->select("SELECT int_type, bigint_type, smallint_type, tinyint_type,
                                  bit_type, decimal_type, numeric_type, float_type, real_type from
                                  NumericTypes where id = ?", NumericData, generatedKey);
    int intType = -1;
    int bigintType = -1;
    int smallintType = -1;
    int tinyintType = -1;
    int bitType = -1;
    decimal decimalType = -1;
    decimal numericType = -1;
    float floatType = -1;
    float realType = -1;
    if (dt is table<NumericData>) {
        foreach var x in dt {
            intType = <@untainted> x.int_type;
            bigintType = <@untainted> x.bigint_type;
            smallintType = <@untainted> x.smallint_type;
            tinyintType = <@untainted> x.tinyint_type;
            bitType = <@untainted> x.bit_type;
            decimalType = <@untainted> x.decimal_type;
            numericType = <@untainted> x.numeric_type;
            floatType = <@untainted> x.float_type;
            realType = <@untainted> x.real_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, intType, bigintType, smallintType, tinyintType, bitType, decimalType, numericType, floatType,
            realType];
}

function testInsertNumericDataWithDirectValues() returns [int, int, int, int, int, int, decimal, decimal, float, float] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->update("Insert into NumericTypes (int_type, bigint_type, smallint_type, tinyint_type,
                                                    bit_type, decimal_type, numeric_type, float_type, real_type)
                                                    values (?,?,?,?,?,?,?,?,?)", -2147483648, -2147483650, -32768, 0,
                                                    false, -5000.75, -5000.76, -5000.77, -5000.78);
    int insertCount = 0;
    int generatedKey = -1;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
    }

    var dt = testDB->select("SELECT int_type, bigint_type, smallint_type, tinyint_type,
                                  bit_type, decimal_type, numeric_type, float_type, real_type from
                                  NumericTypes where id = ?", NumericData, generatedKey);
    int intType = -1;
    int bigintType = -1;
    int smallintType = -1;
    int tinyintType = -1;
    int bitType = -1;
    decimal decimalType = -1;
    decimal numericType = -1;
    float floatType = -1;
    float realType = -1;
    if (dt is table<NumericData>) {
        foreach var x in dt {
            intType = <@untainted> x.int_type;
            bigintType = <@untainted> x.bigint_type;
            smallintType = <@untainted> x.smallint_type;
            tinyintType = <@untainted> x.tinyint_type;
            bitType = <@untainted> x.bit_type;
            decimalType = <@untainted> x.decimal_type;
            numericType = <@untainted> x.numeric_type;
            floatType = <@untainted> x.float_type;
            realType = <@untainted> x.real_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, intType, bigintType, smallintType, tinyintType, bitType, decimalType, numericType, floatType,
            realType];
}

function testInsertStringDataWithParameters() returns [int, string, string, string, string, string, string, string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int intIDVal = 20;
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_INTEGER, value: intIDVal, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "test1", direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_CHAR, value: "test2", direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_CHAR, value: "c", direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para5 = { sqlType: jdbc:TYPE_CHAR, value: "test3", direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para6 = { sqlType: jdbc:TYPE_CHAR, value: "d", direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para7 = { sqlType: jdbc:TYPE_NVARCHAR, value: "test4", direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para8 = { sqlType: jdbc:TYPE_LONGNVARCHAR, value: "test5", direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para9 = { sqlType: jdbc:TYPE_CLOB, value: "hello ballerina code", direction: jdbc:DIRECTION_IN };

    var result = testDB->update("Insert into StringTypes (id, varchar_type, charmax_type, char_type,
                                                    charactermax_type, character_type, nvarcharmax_type,
                                                    longvarchar_type, clob_type) values (?,?,?,?,?,?,?,?,?)",
                                                    para1, para2, para3, para4, para5, para6, para7, para8, para9);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type,
                             nvarcharmax_type, longvarchar_type, clob_type from
                             StringTypes where id = ?", StringData, intIDVal);

    string varcharType = "";
    string charmaxType = "";
    string charType = "";
    string charactermaxType = "";
    string characterType = "";
    string nvarcharmaxType = "";
    string longvarcharType = "";
    string clobType = "";
    if (dt is table<StringData>) {
        foreach var x in dt {
            varcharType = <@untainted> x.varchar_type;
            charmaxType = <@untainted> x.charmax_type;
            charType = <@untainted> x.char_type;
            charactermaxType = <@untainted> x.charactermax_type;
            characterType = <@untainted> x.character_type;
            nvarcharmaxType = <@untainted> x.nvarcharmax_type;
            longvarcharType = <@untainted> x.longvarchar_type;
            clobType = <@untainted> x.clob_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, varcharType, charmaxType, charType, charactermaxType, characterType, nvarcharmaxType,
            longvarcharType, clobType];
}

function getTableCountValColumn(table<ResultCount>|error result) returns int {
    int count = -1;
    if (result is table<ResultCount>) {
        foreach var x in result {
            count = <@untainted> x.COUNTVAL;
        }
        return count;
    }
    return -1;
}