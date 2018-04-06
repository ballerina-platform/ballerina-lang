import ballerina/sql;

function testSelectData () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
    string firstName;
    try {
        var x = testDB -> select("SELECT Name from Customers where registrationID = 1", null, null);

        match x {
            table dt => {
                var j = check <json>dt;
                firstName = j.toString();
            }
            sql:SQLConnectorError err1 => {
               firstName = err1.message;
            }
        }

    } finally {
        _ = testDB -> close();
    }
    return firstName;
}


function testGeneratedKeyOnInsert () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    string id = "";
    try {
        string[] generatedID;
        int insertCount;
        var x = testDB -> updateWithGeneratedKeys("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",
                                                                  null, null);

        match x {
            (int, string[] ) =>{
                id = generatedID[0];
            }
                sql:SQLConnectorError err1 =>{
                id = err1.message;
            }
        }

    } finally {
        _ = testDB -> close();
    }
    return id;
}



function testCallProcedure () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
    string firstName;
    try {
        var x = testDB -> call("{call InsertPersonDataInfo(100,'James')}", null, null);
        match x {
            table[] dt  =>{
                var j = check <json>dt[0];
                firstName = j.toString();
            }
            sql:SQLConnectorError err1 =>{
                firstName = err1.message;
            }
        }

    } finally {
        _ = testDB -> close();
    }
    return firstName;
}

function testBatchUpdate () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    int[] updateCount;
    string returnVal;
    try {
        //Batch 1
        sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
        sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
        sql:Parameter para3 = {sqlType:sql:TYPE_INTEGER, value:20};
        sql:Parameter para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
        sql:Parameter para5 = {sqlType:sql:TYPE_VARCHAR, value:"Colombo"};
        sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

        //Batch 2
        para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
        para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
        para3 = {sqlType:sql:TYPE_INTEGER, value:20};
        para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
        para5 = {sqlType:sql:TYPE_VARCHAR, value:"Colombo"};
        sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];
        sql:Parameter[][] parameters = [parameters1, parameters2];

        var x = testDB -> batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
        match x {
            int[] data  =>{
                updateCount = data;
                returnVal = "success";
            }
            sql:SQLConnectorError err1 =>{
                returnVal = err1.message;
            }
        }
    } finally {
        _ = testDB -> close();
    }
    return returnVal;
}

function testInvalidArrayofQueryParameters () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
    string value;
    try {
        xml x1 = xml `<book>The Lost World</book>`;
        xml x2 = xml `<book>The Lost World2</book>`;
        xml[] xmlDataArray = [x1, x2];
        sql:Parameter para0 = {sqlType:sql:TYPE_INTEGER, value:xmlDataArray};
        sql:Parameter[] parameters = [para0];
        var x = testDB -> select("SELECT FirstName from Customers where registrationID in (?)", parameters, null);
        match x {
            table dt  =>{
                var j = check <json>dt;
                value = j.toString();
            }
            sql:SQLConnectorError err1 =>{
                value = err1.message;
            }
        }

    } finally {
        _ = testDB -> close();
    }
    return value;
}
