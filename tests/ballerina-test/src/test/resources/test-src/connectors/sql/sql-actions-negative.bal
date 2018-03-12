import ballerina.data.sql;

function testSelectData () (string firstName) {
    endpoint<sql:ClientEndpoint> testDBEP {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    }
    var testDB = testDBEP.getConnector();

    try {
        table dt = testDB -> select("SELECT Name from Customers where registrationID = 1", null, null);
        var j, _ = <json>dt;
        firstName = j.toString();
    } finally {
        testDB -> close();
    }
    return;
}


function testGeneratedKeyOnInsert () (string) {
    endpoint<sql:ClientEndpoint> testDBEP {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    }
    var testDB = testDBEP.getConnector();

    string id = "";
    try {
        string[] generatedID;
        int insertCount;
        insertCount, generatedID = testDB -> updateWithGeneratedKeys("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",
                                                                  null, null);
        id = generatedID[0];
    } finally {
        testDB -> close();
    }
    return id;
}


function testCallProcedure () (string firstName) {
    endpoint<sql:ClientEndpoint> testDBEP {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    }
    var testDB = testDBEP.getConnector();

    try {
        _ = testDB -> call("{call InsertPersonDataInfo(100,'James')}", null, null);
        table dt = testDB -> select("SELECT  FirstName from Customers where registrationID = 100", null, null);
        var j, _ = <json>dt;
        firstName = j.toString();
    } finally {
        testDB -> close();
    }
    return;
}

function testBatchUpdate () (int[]) {
    endpoint<sql:ClientEndpoint> testDBEP {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    }
    var testDB = testDBEP.getConnector();

    int[] updateCount;
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

        updateCount = testDB -> batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    } finally {
        testDB -> close();
    }
    return updateCount;
}

function testInvalidArrayofQueryParameters () (string value) {
    endpoint<sql:ClientEndpoint> testDBEP {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    }
    var testDB = testDBEP.getConnector();

    try {
        xml x1 = xml `<book>The Lost World</book>`;
        xml x2 = xml `<book>The Lost World2</book>`;
        xml[] xmlDataArray = [x1, x2];
        sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:xmlDataArray};
        sql:Parameter[] parameters = [para0];
        table dt = testDB -> select("SELECT FirstName from Customers where registrationID in (?)", parameters, null);
        var j, _ = <json>dt;
        value = j.toString();
    } finally {
        testDB -> close();
    }
    return;
}
