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
import ballerina/time;
import ballerina/io;

type ResultCustomers record {
    string FIRSTNAME;
};

type CustomerFullName record {
    string FIRSTNAME;
    string LASTNAME;
};

type ResultIntType record {
    int INT_TYPE;
};

type ResultBlob record {
    byte[] BLOB_TYPE;
};

type ResultRowIDBlob record {
    int row_id;
};

type ResultDataType record {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    decimal DECIMAL_TYPE;
};

type ResultCount record {
    int COUNTVAL;
};

type ResultArrayType record {
    int[] INT_ARRAY;
    int[] LONG_ARRAY;
    float[] DOUBLE_ARRAY;
    boolean[] BOOLEAN_ARRAY;
    string[] STRING_ARRAY;
    float[] FLOAT_ARRAY;
};

type ResultDates record {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
};

type ResultBalTypes record {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
    decimal NUMERIC_TYPE;
    decimal DECIMAL_TYPE;
    float REAL_TYPE;
};

type Employee record {
    int id;
    string name;
    string address;
};

function testInsertTableData() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    var result = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                         values ('James', 'Clerk', 3, 5000.75, 'USA')");
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testCreateTable() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    var result = testDB->update("CREATE TABLE IF NOT EXISTS Students(studentID int, LastName varchar(255))");
    int returnValue = 0;
    if (result is jdbc:UpdateResult) {
        returnValue = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return returnValue;
}

function testUpdateTableData() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    int updateCount = 0;
    var result = testDB->update("Update Customers set country = 'UK' where registrationID = 1");
    if (result is jdbc:UpdateResult) {
        updateCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return updateCount;
}

function testGeneratedKeyOnInsert() returns [int, int] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    int count = 0;
    int generatedKey = 0;
    var x = testDB->update("insert into Customers (firstName,lastName,
            registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')");
    if (x is jdbc:UpdateResult) {
        count = x.updatedRowCount;
        generatedKey = <int>x.generatedKeys["CUSTOMERID"];
    }
    checkpanic testDB.stop();
    return [count, generatedKey];
}

function testGeneratedKeyOnInsertEmptyResults() returns (int|string) {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int|string returnVal = "";

    var x = testDB->update("insert into CustomersNoKey (firstName,lastName,
            registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')");

    if (x is jdbc:UpdateResult) {
        returnVal = x.generatedKeys.length();
    } else {
        error e = x;
        returnVal = e.reason();
    }
    checkpanic testDB.stop();
    return returnVal;
}


function testGeneratedKeyWithColumn() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string firstName = "Kathy";
    string lastName = "Williams";
    string queryString = "insert into Customers (firstName,lastName,registrationID,creditLimit,country) values (?,
        ?, 4, 5000.75, 'USA')";
    var x = testDB->update(queryString, firstName, lastName);

    int generatedID = 0;
    if (x is jdbc:UpdateResult) {
        generatedID = <int>x.generatedKeys["CUSTOMERID"];
    }
    checkpanic testDB.stop();
    return generatedID;
}

function testSelectData() returns @tainted string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ResultCustomers);
    string firstName = getTableFirstNameColumn(dt);
    checkpanic testDB.stop();
    return firstName;
}

function testSelectIntFloatData() returns @tainted [int, int, float, float, decimal] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var dt = testDB->select("SELECT  int_type, long_type, float_type, double_type, decimal_type from DataTypeTable
        where row_id = 1", ResultDataType);
    int int_type = -1;
    int long_type = -1;
    float float_type = -1;
    float double_type = -1;
    decimal decimal_type = -1;
    if (dt is table<ResultDataType>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultDataType) {
                int_type = rs.INT_TYPE;
                long_type = rs.LONG_TYPE;
                float_type = rs.FLOAT_TYPE;
                double_type = rs.DOUBLE_TYPE;
                decimal_type = rs.DECIMAL_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [int_type, long_type, float_type, double_type, decimal_type];
}

function testQueryParameters() returns @tainted string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = ?", ResultCustomers, 1);
    string firstName = getTableFirstNameColumn(dt);
    checkpanic testDB.stop();
    return firstName;
}

function testQueryParameters2() returns @tainted string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    jdbc:Parameter p1 = { sqlType: jdbc:TYPE_INTEGER, value: 1 };
    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = ?", ResultCustomers, p1);
    string firstName = getTableFirstNameColumn(dt);
    checkpanic testDB.stop();
    return firstName;
}

function testInsertTableDataWithParameters() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string s1 = "Anne";
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_VARCHAR, value: s1, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "James", direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_INTEGER, value: 3, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 5000.75, direction: jdbc:DIRECTION_IN };
    jdbc:Parameter para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "UK", direction: jdbc:DIRECTION_IN };

    var result = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", para1, para2, para3, para4, para5);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testInsertTableDataWithParameters2() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var result = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", "Anne", "James", 3, 5000.75, "UK");
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testInsertTableDataWithParameters3() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string s1 = "Anne";
    var result = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", s1, "James", 3, 5000.75, "UK");
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testArrayofQueryParameters() returns @tainted string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int[] intDataArray = [1, 4343];
    string[] stringDataArray = ["A", "B"];
    float[] doubleArray = [233.4, 433.4];
    decimal[] decimalArray = [1233.4d, 1433.4d];
    jdbc:Parameter para0 = { sqlType: jdbc:TYPE_VARCHAR, value: "Johhhn" };
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_INTEGER, value: intDataArray };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_VARCHAR, value: stringDataArray };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_DOUBLE, value: doubleArray };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_DOUBLE, value: decimalArray };

    var dt = testDB->select("SELECT  FirstName from Customers where FirstName = ? or lastName = 'A' or
        lastName = '\"BB\"' or registrationID in(?) or lastName in(?) or creditLimit in(?) or creditLimit in (?)",
        ResultCustomers, para0, para1, para2, para3, para4);
    string firstName = getTableFirstNameColumn(dt);
    checkpanic testDB.stop();
    return firstName;
}

function testBoolArrayofQueryParameters() returns @tainted int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    boolean accepted1 = false;
    boolean accepted2 = false;
    boolean accepted3 = true;
    boolean[] boolDataArray = [accepted1, accepted2, accepted3];

    string[] stringDataArray = ["Hello", "World", "Test"];

    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_BOOLEAN, value: boolDataArray };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_VARCHAR, value: stringDataArray };

    var dt = testDB->select("SELECT int_type from DataTypeTable where row_id = ? and boolean_type in(?) and
        string_type in (?)", ResultIntType, 1, para1, para2);
    int value = -1;
    if (dt is table<ResultIntType>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultIntType) {
                value = rs.INT_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return value;
}

function testBlobArrayQueryParameter() returns @tainted int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var dt1 = testDB->select("SELECT blob_type from BlobTable where row_id = 7", ResultBlob);
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

    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_BLOB, value: blobDataArray };

    var dt = testDB->select("SELECT row_id from BlobTable where row_id = ? and blob_type in (?)", ResultRowIDBlob, 7,
        para1);
    int value = -1;
    if (dt is table<ResultRowIDBlob>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultRowIDBlob) {
                value = rs.row_id;
            }
        }
    }
    checkpanic testDB.stop();
    return value;
}

function testINParameters() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    jdbc:Parameter paraID = { sqlType: jdbc:TYPE_INTEGER, value: 3 };
    jdbc:Parameter paraInt = { sqlType: jdbc:TYPE_INTEGER, value: 1 };
    jdbc:Parameter paraLong = { sqlType: jdbc:TYPE_BIGINT, value: 9223372036854774807 };
    jdbc:Parameter paraFloat = { sqlType: jdbc:TYPE_FLOAT, value: 123.34 };
    jdbc:Parameter paraDouble = { sqlType: jdbc:TYPE_DOUBLE, value: 2139095039 };
    jdbc:Parameter paraBool = { sqlType: jdbc:TYPE_BOOLEAN, value: true };
    jdbc:Parameter paraString = { sqlType: jdbc:TYPE_VARCHAR, value: "Hello" };
    jdbc:Parameter paraNumeric = { sqlType: jdbc:TYPE_NUMERIC, value: 1234.567 };
    jdbc:Parameter paraDecimal = { sqlType: jdbc:TYPE_DECIMAL, value: 1234.567 };
    jdbc:Parameter paraReal = { sqlType: jdbc:TYPE_REAL, value: 1234.567 };
    jdbc:Parameter paraTinyInt = { sqlType: jdbc:TYPE_TINYINT, value: 1 };
    jdbc:Parameter paraSmallInt = { sqlType: jdbc:TYPE_SMALLINT, value: 5555 };
    jdbc:Parameter paraClob = { sqlType: jdbc:TYPE_CLOB, value: "very long text" };
    jdbc:Parameter paraBinary = { sqlType: jdbc:TYPE_BINARY, value: "d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu" };

    var result = testDB->update("INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBinary);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testBlobInParameter() returns @tainted [int, byte[]] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    jdbc:Parameter paraID = { sqlType: jdbc:TYPE_INTEGER, value: 3 };
    jdbc:Parameter paraBlob = { sqlType: jdbc:TYPE_BLOB, value: "YmxvYiBkYXRh" };

    byte[] blobVal = [];

    var result = testDB->update("INSERT INTO BlobTable (row_id,blob_type) VALUES (?,?)", paraID, paraBlob);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    var dt = testDB->select("SELECT blob_type from BlobTable where row_id=3", ResultBlob);
    if (dt is table<ResultBlob>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultBlob) {
                blobVal = rs.BLOB_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [insertCount, blobVal];
}

function testINParametersWithDirectValues() returns @tainted [int, int, float, float, boolean, string, decimal, decimal, float] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var result = testDB->update("INSERT INTO DataTypeTable (row_id, int_type, long_type, float_type,
        double_type, boolean_type, string_type, numeric_type, decimal_type, real_type,
        bit_type) VALUES (?,?,?,?,?,?,?,?,?,?,?)", 25, 1, 9223372036854774807, 123.34, 2139095039.1, true, "Hello",
        1234.567, 1234.567, 1234.567, [1, 2]);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    var dt = testDB->select("SELECT int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type from
            DataTypeTable where row_id = 25", ResultBalTypes);
    int i = -1;
    int l = -1;
    float f = -1;
    float d = -1;
    boolean b = false;
    string s = "";
    decimal n = -1;
    decimal dec = -1;
    float real = -1;
    if (dt is table<ResultBalTypes>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultBalTypes) {
                i = rs.INT_TYPE;
                l = rs.LONG_TYPE;
                f = rs.FLOAT_TYPE;
                d = rs.DOUBLE_TYPE;
                s = rs.STRING_TYPE;
                n = rs.NUMERIC_TYPE;
                dec = rs.DECIMAL_TYPE;
                real = rs.REAL_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [i, l, f, d, b, s, n, dec, real];
}

function testINParametersWithDirectVariables() returns @tainted [int, int, float,
        float, boolean, string, decimal, decimal, float] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int rowid = 26;
    int intType = 1;
    int longType = 9223372036854774807;
    float floatType = 123.34;
    float doubleType = 2139095039.1;
    boolean boolType = true;
    string stringType = "Hello";
    decimal numericType = 1234.567;
    decimal decimalType = 1234.567;
    float realType = 1234.567;
    byte[] byteArray = [1, 2];

    var result = testDB->update("INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, bit_type)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)", rowid, intType, longType, floatType, doubleType, boolType,
            stringType, numericType, decimalType, realType, byteArray);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    var dt = testDB->select("SELECT int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type from
            DataTypeTable where row_id = 26", ResultBalTypes);
    int i = -1;
    int l = -1;
    float f = -1;
    float d = -1;
    boolean b = false;
    string s = "";
    decimal n = -1;
    decimal dec = -1;
    float real = -1;

    if (dt is table<ResultBalTypes>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultBalTypes) {
                i = rs.INT_TYPE;
                l = rs.LONG_TYPE;
                f = rs.FLOAT_TYPE;
                d = rs.DOUBLE_TYPE;
                s = rs.STRING_TYPE;
                n = rs.NUMERIC_TYPE;
                dec = rs.DECIMAL_TYPE;
                real = rs.REAL_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [i, l, f, d, b, s, n, dec, real];
}

function testNullINParameterValues() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    jdbc:Parameter paraID = { sqlType: jdbc:TYPE_INTEGER, value: 4 };
    jdbc:Parameter paraInt = { sqlType: jdbc:TYPE_INTEGER, value: () };
    jdbc:Parameter paraLong = { sqlType: jdbc:TYPE_BIGINT, value: () };
    jdbc:Parameter paraFloat = { sqlType: jdbc:TYPE_FLOAT, value: () };
    jdbc:Parameter paraDouble = { sqlType: jdbc:TYPE_DOUBLE, value: () };
    jdbc:Parameter paraBool = { sqlType: jdbc:TYPE_BOOLEAN, value: () };
    jdbc:Parameter paraString = { sqlType: jdbc:TYPE_VARCHAR, value: () };
    jdbc:Parameter paraNumeric = { sqlType: jdbc:TYPE_NUMERIC, value: () };
    jdbc:Parameter paraDecimal = { sqlType: jdbc:TYPE_DECIMAL, value: () };
    jdbc:Parameter paraReal = { sqlType: jdbc:TYPE_REAL, value: () };
    jdbc:Parameter paraTinyInt = { sqlType: jdbc:TYPE_TINYINT, value: () };
    jdbc:Parameter paraSmallInt = { sqlType: jdbc:TYPE_SMALLINT, value: () };
    jdbc:Parameter paraClob = { sqlType: jdbc:TYPE_CLOB, value: () };
    jdbc:Parameter paraBinary = { sqlType: jdbc:TYPE_BINARY, value: () };

    var result = testDB->update("INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBinary);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testNullINParameterBlobValue() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    jdbc:Parameter paraID = { sqlType: jdbc:TYPE_INTEGER, value: 4 };
    jdbc:Parameter paraBlob = { sqlType: jdbc:TYPE_BLOB, value: () };

    var result = testDB->update("INSERT INTO BlobTable (row_id, blob_type) VALUES (?,?)", paraID, paraBlob);
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testEmptySQLType() returns int {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    var result = testDB->update("Insert into Customers (firstName) values (?)", "Anne");
    int insertCount = 0;
    if (result is jdbc:UpdateResult) {
        insertCount = result.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testBatchUpdate() returns [int[], jdbc:Error?, int, int] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    //Batch 1
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    jdbc:Parameter para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,
                                     creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
    anydata[]? generatedKeys = ret.generatedKeys["CUSTOMERID"];
    int key1 = -1;
    int key2 = -1;
    if (generatedKeys is int[]) {
        key1 = generatedKeys[0];
        key2 = generatedKeys[1];
    }
    checkpanic testDB.stop();
    return [ret.updatedRowCount, ret.returnedError, key1, key2];
}

function testBatchUpdateWithoutGeneratedKeys() returns [int[], jdbc:Error?, int] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    //Batch 1
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_INTEGER, value: 44 };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_INTEGER, value: 45 };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    jdbc:Parameter?[] parameters1 = [para1, para2, para3];

    //Batch 2
    para1 = { sqlType: jdbc:TYPE_INTEGER, value: 54 };
    para2 = { sqlType: jdbc:TYPE_INTEGER, value: 55 };
    para3 = { sqlType: jdbc:TYPE_VARCHAR, value: "Jane" };
    jdbc:Parameter?[] parameters2 = [para1, para2, para3];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into DataTypeTable (row_id,int_type,string_type)
                                     values (?,?,?)", false, parameters1, parameters2);
    int mapSize = ret.generatedKeys.length();
    checkpanic testDB.stop();
    return [ret.updatedRowCount, ret.returnedError, mapSize];
}

function testBatchUpdateSingleValParamArray() returns int[] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string[] parameters1 = ["Harry"];

    string[] parameters2 = ["Ron"];

    string[][] arrayofParamArrays = [parameters1, parameters2];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName) values (?)", false,
                                                    ...arrayofParamArrays);
    checkpanic testDB.stop();
    return ret.updatedRowCount;
}

type myBatchType string|int|float;

function testBatchUpdateWithValues() returns int[] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    //Batch 1
    myBatchType?[] parameters1 = ["Alex", "Smith", 20, 3400.5, "Colombo"];

    //Batch 2
    myBatchType?[] parameters2 = ["John", "Gates", 45, 2400.5, "NY"];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,
                            creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
    checkpanic testDB.stop();
    return ret.updatedRowCount;
}

function testBatchUpdateWithVariables() returns int[] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    //Batch 1
    string firstName1 = "Alex";
    string lastName1 = "Smith";
    int id = 20;
    float creditlimit = 3400.5;
    string city = "Colombo";

    myBatchType?[] parameters1 = [firstName1, lastName1, id, creditlimit, city];

    //Batch 2
    myBatchType?[] parameters2 = ["John", "Gates", 45, 2400.5, "NY"];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,
                            creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
    checkpanic testDB.stop();
    return ret.updatedRowCount;
}

function testBatchUpdateWithFailure() returns @tainted [int[], int] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    //Batch 1
    jdbc:Parameter para0 = { sqlType: jdbc:TYPE_INTEGER, value: 111 };
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    jdbc:Parameter para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters1 = [para0, para1, para2, para3, para4, para5];

    //Batch 2
    para0 = { sqlType: jdbc:TYPE_INTEGER, value: 222 };
    para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters2 = [para0, para1, para2, para3, para4, para5];

    //Batch 3
    para0 = { sqlType: jdbc:TYPE_INTEGER, value: 222 };
    para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters3 = [para0, para1, para2, para3, para4, para5];

    //Batch 4
    para0 = { sqlType: jdbc:TYPE_INTEGER, value: 333 };
    para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters4 = [para0, para1, para2, para3, para4, para5];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (customerId, firstName,lastName,registrationID,
        creditLimit, country) values (?,?,?,?,?,?)", false, parameters1, parameters2, parameters3, parameters4);
    int[] updateCount = ret.updatedRowCount;
    var dt = testDB->select("SELECT count(*) as countval from Customers where customerId in (111,222,333)",
        ResultCount);
    int count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [updateCount, count];
}

function testBatchUpdateWithNullParam() returns int[] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values ('Alex','Smith',20,3400.5,'Colombo')", false);
    int[] updateCount = ret.updatedRowCount;
    checkpanic testDB.stop();
    return updateCount;
}

function testDateTimeInParameters() returns int[] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string stmt =
    "Insert into DateTimeTypes(row_id,date_type,time_type,datetime_type,timestamp_type) values (?,?,?,?,?)";
    int[] returnValues = [];
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_INTEGER, value: 100 };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_DATE, value: "2017-01-30-08:01" };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_TIME, value: "13:27:01.999999+08:33" };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_TIMESTAMP, value: "2017-01-30T13:27:01.999-08:00" };
    jdbc:Parameter para5 = { sqlType: jdbc:TYPE_DATETIME, value: "2017-01-30T13:27:01.999999Z" };

    var result1 = testDB->update(stmt, para1, para2, para3, para4, para5);
    int insertCount1 = 0;
    if (result1 is jdbc:UpdateResult) {
        insertCount1 = result1.updatedRowCount;
    }

    returnValues[0] = insertCount1;

    para1 = { sqlType: jdbc:TYPE_INTEGER, value: 200 };
    para2 = { sqlType: jdbc:TYPE_DATE, value: "-2017-01-30Z" };
    para3 = { sqlType: jdbc:TYPE_TIME, value: "13:27:01+08:33" };
    para4 = { sqlType: jdbc:TYPE_TIMESTAMP, value: "2017-01-30T13:27:01.999" };
    para5 = { sqlType: jdbc:TYPE_DATETIME, value: "-2017-01-30T13:27:01.999999-08:30" };

    var result2 = testDB->update(stmt, para1, para2, para3, para4, para5);
    int insertCount2 = 0;
    if (result2 is jdbc:UpdateResult) {
        insertCount2 = result2.updatedRowCount;
    }

    returnValues[1] = insertCount2;

    time:Time timeNow = time:currentTime();
    para1 = { sqlType: jdbc:TYPE_INTEGER, value: 300 };
    para2 = { sqlType: jdbc:TYPE_DATE, value: timeNow };
    para3 = { sqlType: jdbc:TYPE_TIME, value: timeNow };
    para4 = { sqlType: jdbc:TYPE_TIMESTAMP, value: timeNow };
    para5 = { sqlType: jdbc:TYPE_DATETIME, value: timeNow };

    var result3 = testDB->update(stmt, para1, para2, para3, para4, para5);
    int insertCount3 = 0;
    if (result3 is jdbc:UpdateResult) {
        insertCount3 = result3.updatedRowCount;
    }
    returnValues[2] = insertCount3;

    checkpanic testDB.stop();
    return returnValues;
}

function testDateTimeNullInValues() returns string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    jdbc:Parameter para0 = { sqlType: jdbc:TYPE_INTEGER, value: 33 };
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_DATE, value: () };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_TIME, value: () };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_TIMESTAMP, value: () };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_DATETIME, value: () };
    jdbc:Parameter?[] parameters = [para0, para1, para2, para3, para4];

    _ = checkpanic testDB->update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);
    var dt = testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 33", ResultDates);
    json j = getJsonConversionResult(dt);
    string data = io:sprintf("%s", j);
    checkpanic testDB.stop();
    return data;
}

function testComplexTypeRetrieval() returns [string, string, string, string] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string s1;
    string s2;
    string s3;
    string s4;

    var dt1 = testDB->select("SELECT * from BlobTable where row_id = 1", ());
    xml x1 = getXMLConversionResult(dt1);
    s1 = io:sprintf("%s", x1);

    var dt2 = testDB->select("SELECT * from DateTimeTypes where row_id = 1", ());
    xml x2 = getXMLConversionResult(dt2);
    s2 = io:sprintf("%s", x2);

    var dt3 = testDB->select("SELECT * from BlobTable where row_id = 1", ());
    json j = getJsonConversionResult(dt3);
    s3 = io:sprintf("%s", j);

    var dt4 = testDB->select("SELECT * from DateTimeTypes where row_id = 1", ());
    j = getJsonConversionResult(dt4);
    s4 = io:sprintf("%s", j);

    checkpanic testDB.stop();
    return [s1, s2, s3, s4];
}

function iterateTableAndReturnResultArray(table<CustomerFullName> dt) returns CustomerFullName[] {
    CustomerFullName[] fullNameArray = [];
    int i = 0;
    foreach var x in dt {
        fullNameArray[i] = x;
        i += 1;
    }
    return fullNameArray;
}

function testCloseConnectionPool(string connectionCountQuery)
             returns @tainted (int) {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    var dt = testDB->select(connectionCountQuery, ResultCount);
    int count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return count;
}

function testStopClient() returns error? {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    return testDB.stop();
}

function getTableCountValColumn(table<record {}>|error result) returns int {
    int count = -1;
    if (result is table<record {}>) {
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

function getTableFirstNameColumn(table<record {}>|error result) returns string {
    if (result is table<record {}>) {
        string firstName= "";
        while (result.hasNext()) {
            var rs = result.getNext();
            if (rs is ResultCustomers) {
                firstName = rs.FIRSTNAME;
            }
        }
        return firstName;
    }
    return "";
}

function getJsonConversionResult(table<record {}>|error tableOrError) returns json {
    json retVal = {};
    if (tableOrError is table<record {}>) {
        var jsonConversionResult = typedesc<json>.constructFrom(tableOrError);
        if (jsonConversionResult is json) {
            retVal = jsonConversionResult;
        } else {
            retVal = { "Error": <string> jsonConversionResult.detail()["message"] };
        }
    } else {
        retVal = { "Error": <string> tableOrError.detail()["message"] };
    }
    return retVal;
}

function getXMLConversionResult(table<record {}>|error tableOrError) returns xml {
    xml retVal = xml `<Error/>`;
    if (tableOrError is table<record {}>) {
        var xmlConversionResult = typedesc<xml>.constructFrom(tableOrError);
        if (xmlConversionResult is xml) {
            retVal = xmlConversionResult;
        } else {
            string errorXML = <string> xmlConversionResult.detail()["message"];
            retVal = xml `<Error>{{errorXML}}</Error>`;
        }
    } else {
        string errorXML = <string> tableOrError.detail()["message"];
        retVal = xml `<Error>{{errorXML}}</Error>`;
    }
    return retVal;
}
