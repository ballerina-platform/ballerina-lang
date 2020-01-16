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

import ballerina/time;
import ballerinax/java.jdbc;

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

type NumericDataWithNil record {
    int? int_type;
    int? bigint_type;
    int? smallint_type;
    int? tinyint_type;
    int? bit_type;
    decimal? decimal_type;
    decimal? numeric_type;
    float? float_type;
    float? real_type;
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

type StringDataWithNil record {
    string? varchar_type;
    string? charmax_type;
    string? char_type;
    string? charactermax_type;
    string? character_type;
    string? nvarcharmax_type;
    string? longvarchar_type;
    string? clob_type;
};

type BoolDataAsInt record {
    int bool_type;
    int bit_type;
};

type BoolDataAsBool record {
    boolean bool_type;
    boolean bit_type;
};

type BoolDataWithNil record {
    boolean? bool_type;
    boolean? bit_type;
};

type BinaryData record {
    byte[] binary_type;
    byte[] varbinary_type;
    byte[] blob_type;
    byte[] longvarbinary_type;
    byte[] binaryvarying_type;
    byte[] binarylargetobj_type;
};

type BinaryDataWithNil record {
    byte[]? binary_type;
    byte[]? varbinary_type;
    byte[]? blob_type;
    byte[]? longvarbinary_type;
    byte[]? binaryvarying_type;
    byte[]? binarylargetobj_type;
};

type TimeDataAsTime record {
    time:Time date_type;
    time:Time timenz_type;
    time:Time timestampnz_type;
    time:Time datetime_type;
    time:Time timez_type;
    time:Time timestampz_type;
};

type TimeDataAsString record {
    string date_type;
    string timenz_type;
    string timestampnz_type;
    string datetime_type;
    string timez_type;
    string timestampz_type;
};

type TimeDataAsInt record {
    int timenz_type;
    int timestampnz_type;
    int datetime_type;
};

type TimeDataWithNil record {
    time:Time? date_type;
    time:Time? timenz_type;
    time:Time? timestampnz_type;
    time:Time? datetime_type;
    time:Time? timez_type;
    time:Time? timestampz_type;
};

//Update Remote Function Tests
function testCreateTable(string jdbcURL) returns [int, int] {
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

function testBasicInsertData(string jdbcURL) returns [int, int] {
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

function testBasicInsertDataWithoutGeneratedKey(string jdbcURL) returns [int, int] {
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

function testInsertDataWithGeneratedKey(string jdbcURL) returns [int, int] {
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

function testBasicInsertDataWithDatabaseError(string jdbcURL) returns [boolean, boolean, boolean, boolean, boolean,
    string, string, string, int] {
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

function testBasicInsertDataWithApplicationError(string jdbcURL) returns [boolean, boolean, boolean, boolean, boolean,
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
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_VARCHAR, value: xmlData};
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

function testUpdateTableData(string jdbcURL) returns [int, int] {
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

function testInsertNumericDataWithParameters(string jdbcURL) returns [int, int, int, int, int, int, decimal, decimal,
    float, float] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int intTypeData = 2147483647;
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: intTypeData, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_BIGINT, value: 2147483650, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_SMALLINT, value: 3000, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_TINYINT, value: 255, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_BIT, value: 1, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_DECIMAL, value: 5000.75, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_NUMERIC, value: 5000.76, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para8 = {sqlType: jdbc:TYPE_FLOAT, value: 5000.77, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para9 = {sqlType: jdbc:TYPE_REAL, value: 5000.78, direction: jdbc:DIRECTION_IN};

    var result = testDB->update("Insert into NumericTypes (int_type, bigint_type, smallint_type, tinyint_type," +
                                                    "bit_type, decimal_type, numeric_type, float_type, real_type) " +
                                                    "values (?,?,?,?,?,?,?,?,?)", para1, para2, para3, para4, para5,
                                                    para6, para7, para8, para9);
    int insertCount = 0;
    int generatedKey = -1;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
    }

    jdbc:Parameter p1 = {sqlType: jdbc:TYPE_INTEGER, value: generatedKey};
    var dt = testDB->select("SELECT int_type, bigint_type, smallint_type, tinyint_type," +
                                  "bit_type, decimal_type, numeric_type, float_type, real_type from " +
                                  "NumericTypes where id = ?", NumericData, generatedKey);
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
    return [insertCount, intType, bigintType, smallintType, tinyintType, bitType, decimalType, numericType, floatType,
    realType];
}

function testInsertNumericDataWithDirectValues(string jdbcURL) returns [int, int, int, int, int, int, decimal, decimal,
    float, float] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->update("Insert into NumericTypes (int_type, bigint_type, smallint_type, tinyint_type," +
                                                    "bit_type, decimal_type, numeric_type, float_type, real_type) " +
                                                    "values (?,?,?,?,?,?,?,?,?)", -2147483648, -2147483650, -32768, 0,
    false, -5000.75, -5000.76, -5000.77, -5000.78);
    int insertCount = 0;
    int generatedKey = -1;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
    }

    var dt = testDB->select("SELECT int_type, bigint_type, smallint_type, tinyint_type," +
                                  "bit_type, decimal_type, numeric_type, float_type, real_type from " +
                                  "NumericTypes where id = ?", NumericData, generatedKey);
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
    return [insertCount, intType, bigintType, smallintType, tinyintType, bitType, decimalType, numericType, floatType,
    realType];
}

function testInsertNumericDataWithNilValues(string jdbcURL) returns [int?, int?, int?, int?, int?, int?, decimal?, decimal?, float?,
    float?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_BIGINT, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_SMALLINT, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_TINYINT, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_BIT, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_DECIMAL, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_NUMERIC, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para8 = {sqlType: jdbc:TYPE_FLOAT, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para9 = {sqlType: jdbc:TYPE_REAL, value: (), direction: jdbc:DIRECTION_IN};

    var result = testDB->update("Insert into NumericTypes (int_type, bigint_type, smallint_type, tinyint_type," +
                                                    "bit_type, decimal_type, numeric_type, float_type, real_type) " +
                                                    "values (?,?,?,?,?,?,?,?,?)", para1, para2, para3, para4, para5,
    para6, para7, para8, para9);
    int insertCount = 0;
    int generatedKey = -1;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
    }

    var dt = testDB->select("SELECT int_type, bigint_type, smallint_type, tinyint_type," +
                                  "bit_type, decimal_type, numeric_type, float_type, real_type from " +
                                  "NumericTypes where id = ?", NumericDataWithNil, generatedKey);
    int? intType = -1;
    int? bigintType = -1;
    int? smallintType = -1;
    int? tinyintType = -1;
    int? bitType = -1;
    decimal? decimalType = -1;
    decimal? numericType = -1;
    float? floatType = -1;
    float? realType = -1;
    if (dt is table<NumericDataWithNil>) {
        foreach var x in dt {
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
    return [insertCount, intType, bigintType, smallintType, tinyintType, bitType, decimalType, numericType, floatType,
    realType];
}

function testInsertStringDataWithParameters(string jdbcURL) returns [int, string, string, string, string, string,
    string, string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int intIDVal = 24;
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: intIDVal, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "test1", direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_CHAR, value: "test2", direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_CHAR, value: "c", direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_CHAR, value: "test3", direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_CHAR, value: "d", direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_NVARCHAR, value: "test4", direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para8 = {sqlType: jdbc:TYPE_LONGNVARCHAR, value: "test5", direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para9 = {sqlType: jdbc:TYPE_CLOB, value: "hello ballerina code", direction: jdbc:DIRECTION_IN};

    var result = testDB->update("Insert into StringTypes (id, varchar_type, charmax_type, char_type," +
                                                    "charactermax_type, character_type, nvarcharmax_type," +
                                                    "longvarchar_type, clob_type) values (?,?,?,?,?,?,?,?,?)",
    para1, para2, para3, para4, para5, para6, para7, para8, para9);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type," +
                             "nvarcharmax_type, longvarchar_type, clob_type from " +
                             "StringTypes where id = ?", StringData, intIDVal);
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
            varcharType = <@untainted>x.varchar_type;
            charmaxType = <@untainted>x.charmax_type;
            charType = <@untainted>x.char_type;
            charactermaxType = <@untainted>x.charactermax_type;
            characterType = <@untainted>x.character_type;
            nvarcharmaxType = <@untainted>x.nvarcharmax_type;
            longvarcharType = <@untainted>x.longvarchar_type;
            clobType = <@untainted>x.clob_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, varcharType, charmaxType, charType, charactermaxType, characterType, nvarcharmaxType,
    longvarcharType, clobType];
}

function testInsertStringDataWithDirectParams(string jdbcURL) returns [int, string, string, string, string, string,
    string, string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int intIDVal = 25;

    var result = testDB->update("Insert into StringTypes (id, varchar_type, charmax_type, char_type," +
                                                    "charactermax_type, character_type, nvarcharmax_type," +
                                                    "longvarchar_type, clob_type) values (?,?,?,?,?,?,?,?,?)",
    intIDVal, "str1", "str2", "A", "str3", "B", "str4", "str5",
    "hello ballerina code");
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type," +
                             "nvarcharmax_type, longvarchar_type, clob_type from " +
                             "StringTypes where id = ?", StringData, intIDVal);
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
            varcharType = <@untainted>x.varchar_type;
            charmaxType = <@untainted>x.charmax_type;
            charType = <@untainted>x.char_type;
            charactermaxType = <@untainted>x.charactermax_type;
            characterType = <@untainted>x.character_type;
            nvarcharmaxType = <@untainted>x.nvarcharmax_type;
            longvarcharType = <@untainted>x.longvarchar_type;
            clobType = <@untainted>x.clob_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, varcharType, charmaxType, charType, charactermaxType, characterType, nvarcharmaxType,
    longvarcharType, clobType];
}

function testInsertStringDataWithNilValues(string jdbcURL) returns [int, string?, string?, string?, string?, string?,
    string?, string?, string?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 26, direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_CHAR, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_CHAR, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_CHAR, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_CHAR, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_NVARCHAR, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para8 = {sqlType: jdbc:TYPE_LONGNVARCHAR, value: (), direction: jdbc:DIRECTION_IN};
    jdbc:Parameter para9 = {sqlType: jdbc:TYPE_CLOB, value: (), direction: jdbc:DIRECTION_IN};

    var result = testDB->update("Insert into StringTypes (id, varchar_type, charmax_type, char_type," +
                                                    "charactermax_type, character_type, nvarcharmax_type," +
                                                    "longvarchar_type, clob_type) values (?,?,?,?,?,?,?,?,?)",
    para1, para2, para3, para4, para5, para6, para7, para8, para9);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type," +
                             "nvarcharmax_type, longvarchar_type, clob_type from " +
                             "StringTypes where id = ?", StringDataWithNil, 26);
    string? varcharType = "";
    string? charmaxType = "";
    string? charType = "";
    string? charactermaxType = "";
    string? characterType = "";
    string? nvarcharmaxType = "";
    string? longvarcharType = "";
    string? clobType = "";
    if (dt is table<StringDataWithNil>) {
        foreach var x in dt {
            varcharType = <@untainted>x.varchar_type;
            charmaxType = <@untainted>x.charmax_type;
            charType = <@untainted>x.char_type;
            charactermaxType = <@untainted>x.charactermax_type;
            characterType = <@untainted>x.character_type;
            nvarcharmaxType = <@untainted>x.nvarcharmax_type;
            longvarcharType = <@untainted>x.longvarchar_type;
            clobType = <@untainted>x.clob_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, varcharType, charmaxType, charType, charactermaxType, characterType, nvarcharmaxType,
    longvarcharType, clobType];
}

function testInsertStringDataWithEmptyValues(string jdbcURL) returns [int, string, string, string, string, string,
    string, string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int intIDVal = 27;

    var result = testDB->update("Insert into StringTypes (id, varchar_type, charmax_type, char_type," +
                                                    "charactermax_type, character_type, nvarcharmax_type," +
                                                    "longvarchar_type, clob_type) values (?,?,?,?,?,?,?,?,?)",
                        intIDVal, "", "", "", "", "", "", "", "");
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type," +
                             "nvarcharmax_type, longvarchar_type, clob_type from " +
                             "StringTypes where id = ?", StringData, intIDVal);
    string varcharType = "a";
    string charmaxType = "a";
    string charType = "a";
    string charactermaxType = "a";
    string characterType = "a";
    string nvarcharmaxType = "a";
    string longvarcharType = "a";
    string clobType = "a";
    if (dt is table<StringData>) {
        foreach var x in dt {
            varcharType = <@untainted>x.varchar_type;
            charmaxType = <@untainted>x.charmax_type;
            charType = <@untainted>x.char_type;
            charactermaxType = <@untainted>x.charactermax_type;
            characterType = <@untainted>x.character_type;
            nvarcharmaxType = <@untainted>x.nvarcharmax_type;
            longvarcharType = <@untainted>x.longvarchar_type;
            clobType = <@untainted>x.clob_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, varcharType, charmaxType, charType, charactermaxType, characterType, nvarcharmaxType,
    longvarcharType, clobType];
}

function testInsertBoolDataAsIntsAndReturnInts(string jdbcURL) returns [int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter paraID = {sqlType: jdbc:TYPE_INTEGER, value: 11};
    jdbc:Parameter paraBool = {sqlType: jdbc:TYPE_BOOLEAN, value: 1};
    jdbc:Parameter paraBit = {sqlType: jdbc:TYPE_BIT, value: 1};


    var result = testDB->update("INSERT INTO BooleanTypes (id, bool_type, bit_type) VALUES (?,?,?)",
    paraID, paraBool, paraBit);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT bool_type, bit_type from BooleanTypes where id = 11", BoolDataAsInt);
    int boolVal = -1;
    int bitVal = -1;
    if (dt is table<BoolDataAsInt>) {
        foreach var x in dt {
            boolVal = <@untainted>x.bool_type;
            bitVal = <@untainted>x.bit_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, boolVal, bitVal];
}

function testInsertBoolDataAsBoolAndReturnBool(string jdbcURL) returns [int, boolean, boolean] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter paraID = {sqlType: jdbc:TYPE_INTEGER, value: 12};
    jdbc:Parameter paraBool = {sqlType: jdbc:TYPE_BOOLEAN, value: true};
    jdbc:Parameter paraBit = {sqlType: jdbc:TYPE_BIT, value: true};

    var result = testDB->update("INSERT INTO BooleanTypes (id, bool_type, bit_type) VALUES (?,?,?)",
    paraID, paraBool, paraBit);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT bool_type, bit_type from BooleanTypes where id = 12", BoolDataAsBool);
    boolean boolVal = false;
    boolean bitVal = false;
    if (dt is table<BoolDataAsBool>) {
        foreach var x in dt {
            boolVal = <@untainted>x.bool_type;
            bitVal = <@untainted>x.bit_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, boolVal, bitVal];
}

function testInsertBoolDataAsBoolAndReturnBoolAsDirectParams(string jdbcURL) returns [int, boolean, boolean] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->update("INSERT INTO BooleanTypes (id, bool_type, bit_type) VALUES (?,?,?)",
    13, true, true);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT bool_type, bit_type from BooleanTypes where id = 13", BoolDataAsBool);
    boolean boolVal = false;
    boolean bitVal = false;
    if (dt is table<BoolDataAsBool>) {
        foreach var x in dt {
            boolVal = <@untainted>x.bool_type;
            bitVal = <@untainted>x.bit_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, boolVal, bitVal];
}

function testInsertBoolDataAsIntsInvalidParams(string jdbcURL) returns jdbc:UpdateResult | jdbc:Error {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter paraID = {sqlType: jdbc:TYPE_INTEGER, value: 14};
    jdbc:Parameter paraBool = {sqlType: jdbc:TYPE_BOOLEAN, value: 91};
    jdbc:Parameter paraBit = {sqlType: jdbc:TYPE_BIT, value: 1};

    var result = testDB->update("INSERT INTO BooleanTypes (id, bool_type, bit_type) VALUES (?,?,?)",
    paraID, paraBool, paraBit);
    checkpanic testDB.stop();
    return result;
}

function testInsertBoolDataWithNilValues(string jdbcURL) returns [int, boolean?, boolean?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter paraID = {sqlType: jdbc:TYPE_INTEGER, value: 15};
    jdbc:Parameter paraBool = {sqlType: jdbc:TYPE_BOOLEAN, value: ()};
    jdbc:Parameter paraBit = {sqlType: jdbc:TYPE_BIT, value: ()};


    var result = testDB->update("INSERT INTO BooleanTypes (id, bool_type, bit_type) VALUES (?,?,?)",
    paraID, paraBool, paraBit);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT bool_type, bit_type from BooleanTypes where id = 15", BoolDataWithNil);
    boolean? boolVal = false;
    boolean? bitVal = false;
    if (dt is table<BoolDataWithNil>) {
        foreach var x in dt {
            boolVal = <@untainted>x.bool_type;
            bitVal = <@untainted>x.bit_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, boolVal, bitVal];
}

function testInsertBinaryDataWithParameters(string jdbcURL) returns [int, byte[], byte[], byte[], byte[], byte[],
    byte[]] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 11};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_BINARY, value: "YmxvYiBkYXRh"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_VARBINARY, value: "YmxvYiBkYXRh"};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_BLOB, value: "YmxvYiBkYXRh"};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_LONGVARBINARY, value: "YmxvYiBkYXRh"};
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_BINARY, value: "YmxvYiBkYXRh"};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_BINARY, value: "YmxvYiBkYXRh"};

    var result = testDB->update("Insert into BinaryTypes (id, binary_type, varbinary_type, blob_type," +
                                                    "longvarbinary_type, binaryvarying_type, binarylargetobj_type) " +
                                                    "values (?,?,?,?,?,?,?)",
    para1, para2, para3, para4, para5, para6, para7);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT binary_type, varbinary_type, blob_type, longvarbinary_type, binaryvarying_type," +
                            "binarylargetobj_type from BinaryTypes where id = ?", BinaryData, 11);
    byte[] binaryType = [];
    byte[] varbinaryType = [];
    byte[] blobType = [];
    byte[] longvarbinaryType = [];
    byte[] binaryvaryingType = [];
    byte[] binarylargetobjType = [];

    if (dt is table<BinaryData>) {
        foreach var x in dt {
            binaryType = <@untainted>x.binary_type;
            varbinaryType = <@untainted>x.varbinary_type;
            blobType = <@untainted>x.blob_type;
            longvarbinaryType = <@untainted>x.longvarbinary_type;
            binaryvaryingType = <@untainted>x.binaryvarying_type;
            binarylargetobjType = <@untainted>x.binarylargetobj_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, binaryType, varbinaryType, blobType, longvarbinaryType, binaryvaryingType, binarylargetobjType];
}

function testInsertBinaryDataWithNilValues(string jdbcURL) returns [int, byte[]?, byte[]?, byte[]?, byte[]?, byte[]?,
    byte[]?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 12};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_BINARY, value: ()};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_VARBINARY, value: ()};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_BLOB, value: ()};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_LONGVARBINARY, value: ()};
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_BINARY, value: ()};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_BINARY, value: ()};

    var result = testDB->update("Insert into BinaryTypes (id, binary_type, varbinary_type, blob_type," +
                                                    "longvarbinary_type, binaryvarying_type, binarylargetobj_type) " +
                                                    "values (?,?,?,?,?,?,?)",
    para1, para2, para3, para4, para5, para6, para7);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }

    var dt = testDB->select("SELECT binary_type, varbinary_type, blob_type, longvarbinary_type, binaryvarying_type," +
                            "binarylargetobj_type from BinaryTypes where id = ?", BinaryDataWithNil, 12);
    byte[]? binaryType = [];
    byte[]? varbinaryType = [];
    byte[]? blobType = [];
    byte[]? longvarbinaryType = [];
    byte[]? binaryvaryingType = [];
    byte[]? binarylargetobjType = [];

    if (dt is table<BinaryDataWithNil>) {
        foreach var x in dt {
            binaryType = <@untainted>x.binary_type;
            varbinaryType = <@untainted>x.varbinary_type;
            blobType = <@untainted>x.blob_type;
            longvarbinaryType = <@untainted>x.longvarbinary_type;
            binaryvaryingType = <@untainted>x.binaryvarying_type;
            binarylargetobjType = <@untainted>x.binarylargetobj_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, binaryType, varbinaryType, blobType, longvarbinaryType, binaryvaryingType, binarylargetobjType];
}

function testInsertTimeDataAsString(string jdbcURL) returns [int, string, string, string, string, string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 11};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_DATE, value: "2019-08-09"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIME, value: "20:08:08"};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_TIMESTAMP, value: "2019-08-09T20:08:08"};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_DATETIME, value: "2019-08-09T20:08:08"};
    //TODO:#17546 - Create two new types as TYPE_TIME_WITH_ZONE and TYPE_TIMESTAMP_WITH_ZONE and handle them
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_TIME, value: "20:08:08.034900-08:00"};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_TIMESTAMP, value: "2019-08-09T20:08:08.034900-08:00"};

    var result = testDB->update("Insert into TimeTypes (id, date_type, timenz_type, timestampnz_type," +
                                                    "datetime_type, timez_type, timestampz_type) " +
                                                    "values (?,?,?,?,?,?,?)",
    para1, para2, para3, para4, para5, para6, para7);

    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    var dt = testDB->select("SELECT date_type, timenz_type, timestampnz_type, datetime_type, timez_type, timestampz_type " +
                             "from TimeTypes where id = ?", TimeDataAsString, 11);
    string dateType = "";
    string timenzType = "";
    string timestampnzType = "";
    string datetimeType = "";
    string timezType = "";
    string timestampzType = "";

    if (dt is table<TimeDataAsString>) {
        foreach var x in dt {
            dateType = <@untainted>x.date_type;
            timenzType = <@untainted>x.timenz_type;
            timestampnzType = <@untainted>x.timestampnz_type;
            datetimeType = <@untainted>x.datetime_type;
            timezType = <@untainted>x.timez_type;
            timestampzType = <@untainted>x.timestampz_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, dateType, timenzType, timestampnzType, datetimeType, timezType, timestampzType];
}

function testInsertTimeDataAsBallerinaTime(string jdbcURL) returns [int, boolean, boolean, boolean, boolean, boolean,
    boolean] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    time:Time dateVal = time:currentTime();
    time:Time | error dateValRet = time:parse("2017-06-26", "yyyy-MM-dd");
    if (dateValRet is time:Time) {
        dateVal = dateValRet;
    }

    time:Time timeVal = time:currentTime();
    time:Time | error timeValRet = time:parse("09:46:22", "HH:mm:ss");
    if (timeValRet is time:Time) {
        timeVal = timeValRet;
    }

    time:Time timeStampVal = time:currentTime();
    time:Time | error timeStampValRet = time:parse("2019-08-09T20:08:08", "yyyy-MM-dd'T'HH:mm:ss");
    if (timeStampValRet is time:Time) {
        timeStampVal = timeStampValRet;
    }

    time:Time timeValWithZoneVal = time:currentTime();
    time:Time | error timeValWithZoneRet = time:parse("09:46:22.444-0500", "HH:mm:ss.SSSZ");
    if (timeValWithZoneRet is time:Time) {
        timeValWithZoneVal = timeValWithZoneRet;
    }

    time:Time timeStampValWithZoneVal = time:currentTime();
    time:Time | error timeStampValWithZoneRet = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (timeStampValWithZoneRet is time:Time) {
        timeStampValWithZoneVal = timeStampValWithZoneRet;
    }

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 12};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_DATE, value: dateVal};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIME, value: timeVal};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_TIMESTAMP, value: timeStampVal};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_DATETIME, value: timeStampVal};
    //TODO:#17546 - Create two new types as TYPE_TIME_WITH_ZONE and TYPE_TIMESTAMP_WITH_ZONE and handle them
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_TIME, value: timeValWithZoneVal};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_TIMESTAMP, value: timeStampValWithZoneVal};

    var result = testDB->update("Insert into TimeTypes (id, date_type, timenz_type, timestampnz_type," +
                                                    "datetime_type, timez_type, timestampz_type)" +
                                                    "values (?,?,?,?,?,?,?)",
    para1, para2, para3, para4, para5, para6, para7);

    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    var dt = testDB->select("SELECT date_type, timenz_type, timestampnz_type, datetime_type, timez_type, timestampz_type " +
                             "from TimeTypes where id = ?", TimeDataAsTime, 12);

    boolean dateEquals = false;
    boolean timeEquals = false;
    boolean timestampEquals = false;
    boolean datetimeEquals = false;
    boolean timezEquals = false;
    boolean timestampzEquals = false;

    if (dt is table<TimeDataAsTime>) {
        foreach var x in dt {
            time:Time t1 = <@untainted>x.date_type;
            dateEquals = t1.time == dateVal.time ? true : false;

            t1 = <@untainted>x.timenz_type;
            timeEquals = t1.time == timeVal.time ? true : false;

            t1 = <@untainted>x.timestampnz_type;
            timestampEquals = t1.time == timeStampVal.time ? true : false;

            t1 = <@untainted>x.datetime_type;
            datetimeEquals = t1.time == timeStampVal.time ? true : false;

            t1 = <@untainted>x.timez_type;
            timezEquals = t1.time == timeValWithZoneVal.time ? true : false;

            t1 = <@untainted>x.timestampz_type;
            timestampzEquals = t1.time == timeStampValWithZoneVal.time ? true : false;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, dateEquals, timeEquals, timestampEquals, datetimeEquals, timezEquals, timestampzEquals];
}

function testInsertTimeDataAsInt(string jdbcURL) returns [int, int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 13};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_TIME, value: 72488000};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIMESTAMP, value: 1565381288};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DATETIME, value: 1565381288};

    var result = testDB->update("Insert into TimeTypes (id, timenz_type, timestampnz_type," +
                                                    "datetime_type) values (?,?,?,?)",
    para1, para2, para3, para4);

    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    var dt = testDB->select("SELECT timenz_type, timestampnz_type, datetime_type " +
                             "from TimeTypes where id = ?", TimeDataAsInt, 13);

    int timenzType = -1;
    int timestampnzType = -1;
    int datetimeType = -1;

    if (dt is table<TimeDataAsInt>) {
        foreach var x in dt {
            timenzType = <@untainted>x.timenz_type;
            timestampnzType = <@untainted>x.timestampnz_type;
            datetimeType = <@untainted>x.datetime_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, timenzType, timestampnzType, datetimeType];
}

function testInsertTimeDataAsBallerinaTimeWithNil(string jdbcURL) returns [int, time:Time?, time:Time?, time:Time?,
    time:Time?, time:Time?, time:Time?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 14};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_DATE, value: ()};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIME, value: ()};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_TIMESTAMP, value: ()};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_DATETIME, value: ()};
    //TODO:#17546 - Create two new types as TYPE_TIME_WITH_ZONE and TYPE_TIMESTAMP_WITH_ZONE and handle them
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_TIME, value: ()};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_TIMESTAMP, value: ()};

    var result = testDB->update("Insert into TimeTypes (id, date_type, timenz_type, timestampnz_type," +
                                                    "datetime_type, timez_type, timestampz_type) " +
                                                    "values (?,?,?,?,?,?,?)",
    para1, para2, para3, para4, para5, para6, para7);

    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    var dt = testDB->select("SELECT date_type, timenz_type, timestampnz_type, datetime_type, timez_type, timestampz_type " +
                             "from TimeTypes where id = ?", TimeDataWithNil, 14);

    time:Time currentTime = time:currentTime();

    time:Time? dateType = currentTime;
    time:Time? timenzType = currentTime;
    time:Time? timestampnzType = currentTime;
    time:Time? datetimeType = currentTime;
    time:Time? timezType = currentTime;
    time:Time? timestampzType = currentTime;

    if (dt is table<TimeDataWithNil>) {
        foreach var x in dt {
            dateType = <@untainted>x.date_type;
            timenzType = <@untainted>x.timenz_type;
            timestampnzType = <@untainted>x.timestampnz_type;
            datetimeType = <@untainted>x.datetime_type;
            timezType = <@untainted>x.timez_type;
            timestampzType = <@untainted>x.timestampz_type;
        }
    }

    checkpanic testDB.stop();
    return [insertCount, dateType, timenzType, timestampnzType, datetimeType, timezType, timestampzType];
}

function testInvalidUpdateOnUpdateResultRecord(string jdbcURL) returns error | () {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->update("Insert into NumericTypes (int_type) values (21)");
    int insertCount = 0;
    int generatedKey = -1;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
        generatedKey = <int>result.generatedKeys["ID"];
        error | () res = trap updateResultSet(result);
        if (res is error) {
            checkpanic testDB.stop();
            return res;
        }
    }
    checkpanic testDB.stop();
}

function testStopClient(string jdbcURL) returns error? {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });
    return testDB.stop();
}

function updateResultSet(jdbc:UpdateResult result) {
    result.updatedRowCount = 100;//Invalid update
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
