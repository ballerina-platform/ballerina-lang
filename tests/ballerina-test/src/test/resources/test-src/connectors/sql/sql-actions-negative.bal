import ballerina.data.sql;

function testSelectData () (string firstName) {
    endpoint<sql:ClientConnector> testDB {}
    try {
        sql:ClientConnector con = create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:5});
        bind con with testDB;

        table dt = testDB.selectQuery("SELECT Name from Customers where registrationID = 1", null, null);
        var j, _ = <json>dt;
        firstName = j.toString();
    } finally {
        testDB.close();
    }
    return;
}


function testGeneratedKeyOnInsert () (string) {
    endpoint<sql:ClientConnector> testDB {}
    string id = "";
    try {
        string[] generatedID;
        sql:ClientConnector con = create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
        bind con with testDB;

        int insertCount;
        insertCount, generatedID = testDB.updateWithGeneratedKeysQuery("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",
                                                                  null, null);
        id = generatedID[0];
    } finally {
        testDB.close();
    }
    return id;
}


function testCallProcedure () (string firstName) {
    endpoint<sql:ClientConnector> testDB {}
    try {
        sql:ClientConnector con = create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
        bind con with testDB;

        _ = testDB.callQuery("{call InsertPersonDataInfo(100,'James')}", null, null);
        table dt = testDB.selectQuery("SELECT  FirstName from Customers where registrationID = 100", null, null);
        var j, _ = <json>dt;
        firstName = j.toString();
    } finally {
        testDB.close();
    }
    return;
}

function testBatchUpdate () (int[]) {
    endpoint<sql:ClientConnector> testDB {}
    int[] updateCount;
    try {
        sql:ClientConnector con = create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
        bind con with testDB;

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

        updateCount = testDB.batchUpdateQuery("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    } finally {
        testDB.close();
    }
    return updateCount;
}

function testInvalidArrayofQueryParameters () (string value) {
    endpoint<sql:ClientConnector> testDB {}
    try {
        sql:ClientConnector con = create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
        bind con with testDB;
        xml x1 = xml `<book>The Lost World</book>`;
        xml x2 = xml `<book>The Lost World2</book>`;
        xml[] xmlDataArray = [x1, x2];
        sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:xmlDataArray};
        sql:Parameter[] parameters = [para0];
        table dt = testDB.selectQuery("SELECT FirstName from Customers where registrationID in (?)", parameters, null);
        var j, _ = <json>dt;
        value = j.toString();
    } finally {
        testDB.close();
    }
    return;
}
