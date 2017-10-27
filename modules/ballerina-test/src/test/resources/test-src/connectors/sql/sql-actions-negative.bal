import ballerina.data.sql;

struct ResultCustomers {
    string FIRSTNAME;
}


function testSelectData () (string firstName) {
    sql:ClientConnector testDB;
    try {
        testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:5});

        datatable dt = testDB.select("SELECT  Name from Customers where registrationID = 1", null);
        TypeCastError err;
        ResultCustomers rs;
        while (dt.hasNext()) {
            any dataStruct = dt.getNext();
            rs, err = (ResultCustomers)dataStruct;
            firstName = rs.FIRSTNAME;
        }
    } finally {
        testDB.close();
    }
    return;
}


function testGeneratedKeyOnInsert () (string) {
    sql:ClientConnector testDB;
    string id = "";
    try {
        string[] generatedID;
        testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

        int insertCount;
        insertCount, generatedID = testDB.updateWithGeneratedKeys("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",
                                                                  null, null);
        id = generatedID[0];
    } finally {
        testDB.close();
    }
    return id;
}


function testCallProcedure () (string firstName) {
    sql:ClientConnector testDB;
    try {
        testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

        _ = testDB.call("{call InsertPersonDataInfo(100,'James')}", null);
        datatable dt = testDB.select("SELECT  FirstName from Customers where registrationID = 100", null);
        TypeCastError err;
        ResultCustomers rs;
        while (dt.hasNext()) {
            any dataStruct = dt.getNext();
            rs, err = (ResultCustomers)dataStruct;
            firstName = rs.FIRSTNAME;
        }
    } finally {
        testDB.close();
    }
    return;
}

function testBatchUpdate () (int[]) {
    sql:ClientConnector testDB;
    int[] updateCount;
    try {
        testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

        //Batch 1
        sql:Parameter para1 = {sqlType:"varchar", value:"Alex", direction:0};
        sql:Parameter para2 = {sqlType:"varchar", value:"Smith", direction:0};
        sql:Parameter para3 = {sqlType:"integer", value:20, direction:0};
        sql:Parameter para4 = {sqlType:"double", value:3400.5, direction:0};
        sql:Parameter para5 = {sqlType:"varchar", value:"Colombo", direction:0};
        sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

        //Batch 2
        para1 = {sqlType:"varchar", value:"Alex", direction:0};
        para2 = {sqlType:"varchar", value:"Smith", direction:0};
        para3 = {sqlType:"integer", value:20, direction:0};
        para4 = {sqlType:"double", value:3400.5, direction:0};
        para5 = {sqlType:"varchar", value:"Colombo", direction:0};
        sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];
        sql:Parameter[][] parameters = [parameters1, parameters2];

        updateCount = testDB.batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    } finally {
        testDB.close();
    }
    return updateCount;
}

function testInvalidArrayofQueryParameters () (string value ) {
    sql:ClientConnector testDB;
    try {
        testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
        xml x1 = xml `<book>The Lost World</book>`;
        xml x2 = xml `<book>The Lost World2</book>`;
        xml[] xmlDataArray = [x1, x2];
        sql:Parameter para0 = {sqlType:"integer", value:xmlDataArray, direction:0};
        sql:Parameter[] parameters = [para0];


        datatable dt = testDB.select("SELECT FirstName from Customers where registrationID in (?)", parameters);
        TypeCastError err;
        ResultCustomers rs;
        while (dt.hasNext()) {
            any dataStruct = dt.getNext();
            rs, err = (ResultCustomers)dataStruct;
            value = rs.FIRSTNAME;
        }
    } finally {
        testDB.close();
    }
    return;
}