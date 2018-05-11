import ballerina/sql;
import ballerina/jdbc;
import ballerina/io;
import ballerina/time;

type ResultCustomers {
    string FIRSTNAME,
};

type Person {
    int id,
    string name,
};

type ResultCustomers2 {
    string FIRSTNAME,
    string LASTNAME,
};

type NonNillableInt {
    int val;
};

type NonNillableLong {
    int val;
};

type NonNillableFloat {
    float val;
};

type NonNillableDouble {
    float val;
};

type NonNillableBoolean {
    boolean val;
};

type NonNillableString {
    string val;
};

type NonNillableNumeric {
    float val;
};

type NonNillableTinyInt {
    int val;
};

type NonNillableSmallInt {
    int val;
};

type NonNillableDecimal {
    float val;
};

type NonNillableReal {
    float val;
};

type NonNillableClob {
    string val;
};

type NonNillableBlob {
    blob val;
};

type NonNillableBinary {
    blob val;
};

type NonNillableDate {
    time:Time val;
};

type NonNillableTime {
    time:Time val;
};

type NonNillableDateTime {
    time:Time val;
};

type NonNillableTimeStamp {
    time:Time val;
};

type InvalidUnion1 {
    (int | string) val;
};

function testSelectData() returns (string) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };
    string returnData;
    try {
        var x = testDB->select("SELECT Name from Customers where registrationID = 1", ());

        match x {
            table dt => {
                var j = check <json>dt;
                returnData = io:sprintf("%j", j);
            }
            error err1 => {
                returnData = err1.message;
            }
        }

    } finally {
        testDB.stop();
    }
    return returnData;
}

function testGeneratedKeyOnInsert() returns (string) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    string id = "";
    try {
        string[] generatedID;
        int insertCount;
        var x = testDB->updateWithGeneratedKeys("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')", ());

        match x {
            (int, string[]) => {
                id = generatedID[0];
            }
            error err1 => {
                id = err1.message;
            }
        }

    } finally {
        testDB.stop();
    }
    return id;
}

function testCallProcedure() returns (string) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };
    string returnData;
    try {
        var x = testDB->call("{call InsertPersonDataInfo(100,'James')}", ());
        match x {
            table[] dt => {
                var j = check <json>dt[0];
                returnData = io:sprintf("%j", j);
            }
            () => returnData = "";
            error err1 => {
                returnData = err1.message;
            }
        }
    } finally {
        testDB.stop();
    }
    return returnData;
}

function testBatchUpdate() returns (string) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    int[] updateCount;
    string returnVal;
    try {
        //Batch 1
        sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
        sql:Parameter para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
        sql:Parameter para3 = { sqlType: sql:TYPE_INTEGER, value: 20 };
        sql:Parameter para4 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
        sql:Parameter para5 = { sqlType: sql:TYPE_VARCHAR, value: "Colombo" };
        sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

        //Batch 2
        para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
        para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
        para3 = { sqlType: sql:TYPE_INTEGER, value: 20 };
        para4 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
        para5 = { sqlType: sql:TYPE_VARCHAR, value: "Colombo" };
        sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];

        var x = testDB->batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters1, parameters2);
        match x {
            int[] data => {
                updateCount = data;
                returnVal = "success";
            }
            error err1 => {
                returnVal = err1.message;
            }
        }
    } finally {
        testDB.stop();
    }
    return returnVal;
}

function testInvalidArrayofQueryParameters() returns (string) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    string returnData;
    try {
        xml x1 = xml `<book>The Lost World</book>`;
        xml x2 = xml `<book>The Lost World2</book>`;
        xml[] xmlDataArray = [x1, x2];
        sql:Parameter para0 = { sqlType: sql:TYPE_INTEGER, value: xmlDataArray };
        var x = testDB->select("SELECT FirstName from Customers where registrationID in (?)", (), para0);

        match x {
            table dt => {
                var j = check <json>dt;
                returnData = io:sprintf("%j", j);
            }
            error err1 => {
                returnData = err1.message;
            }
        }

    } finally {
        testDB.stop();
    }
    return returnData;
}

function testCallProcedureWithMultipleResultSetsAndLowerConstraintCount() returns ((string, string)|error) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    var dtsRet = testDB->call("{call SelectPersonDataMultiple()}", [ResultCustomers]);

    match dtsRet {
        table[] dts => {
            string firstName1;
            string firstName2;

            while (dts[0].hasNext()) {
                ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
                firstName1 = rs.FIRSTNAME;
            }

            while (dts[1].hasNext()) {
                ResultCustomers rs = check <ResultCustomers>dts[1].getNext();
                firstName2 = rs.FIRSTNAME;
            }

            testDB.stop();
            return (firstName1, firstName2);
        }
        () => return ("", "");
        error e => {
            testDB.stop();
            return e;
        }
    }
}

function testCallProcedureWithMultipleResultSetsAndHigherConstraintCount() returns ((string, string)|error) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    var dtsRet = testDB->call("{call SelectPersonDataMultiple()}", [ResultCustomers, ResultCustomers2, Person]);

    match dtsRet {
        table[] dts => {
            string firstName1;
            string firstName2;

            while (dts[0].hasNext()) {
                ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
                firstName1 = rs.FIRSTNAME;
            }

            while (dts[1].hasNext()) {
                ResultCustomers rs = check <ResultCustomers>dts[1].getNext();
                firstName2 = rs.FIRSTNAME;
            }

            testDB.stop();
            return (firstName1, firstName2);
        }
        () => return ("", "");
        error e => {
            testDB.stop();
            return e;
        }
    }
}

function testCallProcedureWithMultipleResultSetsAndNilConstraintCount() returns ((string, string)|error) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    var dtsRet = testDB->call("{call SelectPersonDataMultiple()}", ());

    match dtsRet {
        table[] dts => {
            string firstName1;
            string firstName2;

            while (dts[0].hasNext()) {
                ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
                firstName1 = rs.FIRSTNAME;
            }

            while (dts[1].hasNext()) {
                ResultCustomers rs = check <ResultCustomers>dts[1].getNext();
                firstName2 = rs.FIRSTNAME;
            }

            return (firstName1, firstName2);
        }
        () => return ("", "");
        error e => {
            testDB.stop();
            return e;
        }
    }
}

function testAssignNilToNonNillableInt() {
    testAssignNilToNonNillableField("int_type");
}

function testAssignNilToNonNillableLong() {
    testAssignNilToNonNillableField("long_type");
}

function testAssignNilToNonNillableFloat() {
    testAssignNilToNonNillableField("float_type");
}

function testAssignNilToNonNillableDouble() {
    testAssignNilToNonNillableField("double_type");
}

function testAssignNilToNonNillableBoolean() {
    testAssignNilToNonNillableField("boolean_type");
}

function testAssignNilToNonNillableString() {
    testAssignNilToNonNillableField("string_type");
}

function testAssignNilToNonNillableNumeric() {
    testAssignNilToNonNillableField("numeric_type");
}

function testAssignNilToNonNillableTinyInt() {
    testAssignNilToNonNillableField("tinyint_type");
}

function testAssignNilToNonNillableSmallint() {
    testAssignNilToNonNillableField("smallint_type");
}

function testAssignNilToNonNillableDecimal() {
    testAssignNilToNonNillableField("decimal_type");
}

function testAssignNilToNonNillableReal() {
    testAssignNilToNonNillableField("real_type");
}

function testAssignNilToNonNillableClob() {
    testAssignNilToNonNillableField("clob_type");
}

function testAssignNilToNonNillableBlob() {
    testAssignNilToNonNillableField("blob_type");
}

function testAssignNilToNonNillableBinary() {
    testAssignNilToNonNillableField("binary_type");
}

function testAssignNilToNonNillableDate() {
    testAssignNilToNonNillableField("date_type");
}

function testAssignNilToNonNillableTime() {
    testAssignNilToNonNillableField("time_type");
}

function testAssignNilToNonNillableDateTime() {
    testAssignNilToNonNillableField("datetime_type");
}

function testAssignNilToNonNillableTimeStamp() {
    testAssignNilToNonNillableField("timestamp_type");
}

function testAssignNilToNonNillableField(string field) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT " + field + " from DataTypeTableNillable where row_id=2", NonNillableInt);

    try {
        while (dt.hasNext()) {
            var rs = <NonNillableInt>dt.getNext();
        }
    } finally {
        testDB.stop();
    }
}

function testAssignToInvalidUnionInt() {
    testAssignToInvalidUnionField("int_type");
}

function testAssignToInvalidUnionLong() {
    testAssignToInvalidUnionField("long_type");
}

function testAssignToInvalidUnionFloat() {
    testAssignToInvalidUnionField("float_type");
}

function testAssignToInvalidUnionDouble() {
    testAssignToInvalidUnionField("double_type");
}

function testAssignToInvalidUnionBoolean() {
    testAssignToInvalidUnionField("boolean_type");
}

function testAssignToInvalidUnionString() {
    testAssignToInvalidUnionField("string_type");
}

function testAssignToInvalidUnionNumeric() {
    testAssignToInvalidUnionField("numeric_type");
}

function testAssignToInvalidUnionTinyInt() {
    testAssignToInvalidUnionField("tinyint_type");
}

function testAssignToInvalidUnionSmallint() {
    testAssignToInvalidUnionField("smallint_type");
}

function testAssignToInvalidUnionDecimal() {
    testAssignToInvalidUnionField("decimal_type");
}

function testAssignToInvalidUnionReal() {
    testAssignToInvalidUnionField("real_type");
}

function testAssignToInvalidUnionClob() {
    testAssignToInvalidUnionField("clob_type");
}

function testAssignToInvalidUnionBlob() {
    testAssignToInvalidUnionField("blob_type");
}

function testAssignToInvalidUnionBinary() {
    testAssignToInvalidUnionField("binary_type");
}

function testAssignToInvalidUnionDate() {
    testAssignToInvalidUnionField("date_type");
}

function testAssignToInvalidUnionTime() {
    testAssignToInvalidUnionField("time_type");
}

function testAssignToInvalidUnionDateTime() {
    testAssignToInvalidUnionField("datetime_type");
}

function testAssignToInvalidUnionTimeStamp() {
    testAssignToInvalidUnionField("timestamp_type");
}

function testAssignToInvalidUnionField(string field) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT " + field + " from DataTypeTableNillable where row_id=1", InvalidUnion1);

    try {
        while (dt.hasNext()) {
            var rs = <InvalidUnion1>dt.getNext();
        }
    } finally {
        testDB.stop();
    }
}
