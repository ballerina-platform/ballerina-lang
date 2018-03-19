import ballerina.data.sql;
import ballerina.io;

function testSelectData () (string firstName, error e) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt;
        dt, e = testDB -> select("SELECT Name from Customers where registrationID = 1", null, null);
                          io:print("ERROR:");
        if (e == null) {
            var j, _ = <json>dt;
            firstName = j.toString();
        }
    } finally {
        _ = testDB -> close();
    }
    return;
}


function testGeneratedKeyOnInsert () (string id, error e) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    id = "";
    try {
        string[] generatedID;
        int insertCount;
        insertCount, generatedID, e = testDB -> updateWithGeneratedKeys("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",
                                                                  null, null);
        if (e == null) {
            id = generatedID[0];
        }
    } finally {
        _ = testDB -> close();
    }
    return id, e;
}


function testCallProcedure () (string firstName, error e) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        _, e = testDB -> call("{call InsertPersonDataInfo(100,'James')}", null, null);
        if (e == null) {
            var dt, _ = testDB-> select("SELECT  FirstName from Customers where registrationID = 100", null, null);
            var j, _ = <json> dt;
            firstName = j.toString();
        }
    } finally {
        _ = testDB -> close();
    }
    return;
}

function testBatchUpdate () (int[]updateCount, error e) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        //Batch 1
        sql:Parameter para1 = {sqlType:sql:Type.VARCHAR, value:"Alex"};
        sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
        sql:Parameter para3 = {sqlType:sql:Type.INTEGER, value:20};
        sql:Parameter para4 = {sqlType:sql:Type.DOUBLE, value:3400.5};
        sql:Parameter para5 = {sqlType:sql:Type.VARCHAR, value:"Colombo"};
        sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

        //Batch 2
        para1 = {sqlType:sql:Type.VARCHAR, value:"Alex"};
        para2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
        para3 = {sqlType:sql:Type.INTEGER, value:20};
        para4 = {sqlType:sql:Type.DOUBLE, value:3400.5};
        para5 = {sqlType:sql:Type.VARCHAR, value:"Colombo"};
        sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];
        sql:Parameter[][] parameters = [parameters1, parameters2];

        updateCount, e = testDB -> batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
        io:print("error:");
        io:print(updateCount);
    } finally {
        _ = testDB -> close();
    }
    return updateCount, e;
}

function testInvalidArrayofQueryParameters () (string value, error e) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        xml x1 = xml `<book>The Lost World</book>`;
        xml x2 = xml `<book>The Lost World2</book>`;
        xml[] xmlDataArray = [x1, x2];
        sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:xmlDataArray};
        sql:Parameter[] parameters = [para0];
        table dt;
        dt, e = testDB -> select("SELECT FirstName from Customers where registrationID in (?)", parameters, null);
        if (e == null) {
            var j, _ = <json> dt;
            value = j.toString();
        }
    } finally {
        _ = testDB -> close();
    }
    return;
}
