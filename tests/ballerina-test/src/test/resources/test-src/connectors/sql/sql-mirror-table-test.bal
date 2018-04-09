import ballerina/sql;

type Employee {
    int id,
    string name,
    string address,
};

function testIterateMirrorTable () returns (Employee[], Employee[]) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    var temp = testDB -> mirror("employeeItr", typeof Employee);
    table dt = check temp;

    Employee [] employeeArray1;
    Employee [] employeeArray2;
    int i = 0;
    while (dt.hasNext()) {
        var rs = check <Employee>dt.getNext();
        Employee e = {id: rs.id, name: rs.name, address:rs.address};
        employeeArray1[i] = e;
        i++;
    }

    i = 0;
    while (dt.hasNext()) {
        var rs = check <Employee>dt.getNext();
        Employee e = {id: rs.id, name: rs.name, address:rs.address};
        employeeArray2[i] = e;
        i++;
    }

    _ = testDB -> close();
    return (employeeArray1, employeeArray2);
}

function testAddToMirrorTable () returns (Employee[]) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    var temp = testDB -> mirror("employeeAdd", typeof Employee);
    table dt = check temp;

    Employee e1 = {id: 1, name:"Manuri", address:"Sri Lanka"};
    Employee e2 = {id: 2, name:"Devni", address:"Sri Lanka"};

    var result1 = dt.add(e1);
    var result2 = dt.add(e2);

    var temp2 = testDB -> select("SELECT  * from employeeAdd", (), typeof Employee);
    table dt2 = check temp2;

    Employee [] employeeArray;
    int i = 0;
    while (dt2.hasNext()) {
        var rs = check <Employee>dt2.getNext();
        Employee e = {id: rs.id, name: rs.name, address:rs.address};
        employeeArray[i] = e;
        i++;
    }

    _ = testDB -> close();

    return employeeArray;
}

function testAddToMirrorTableNegative () returns (any) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    var temp = testDB -> mirror("employeeAddNegative", typeof Employee);
    table dt = check temp;

    Employee e1 = {id: 1, name:"Manuri", address:"Sri Lanka"};

    var result = dt.add(e1);

    _ = testDB -> close();

    return result;
}


function testDeleteFromMirrorTable () returns (boolean, int) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    var temp = testDB -> mirror("employeeDel", typeof Employee);
    table dt = check temp;

    var val = dt.remove(idMatches);
    int removedCount;
    match val {
        int count => removedCount = count;
        TableOperationError e => removedCount = -1;
    }

    var temp2 = testDB -> select("SELECT  * from employeeDel", (), typeof Employee);
    table dt2 = check temp2;
    boolean hasNext = dt2.hasNext();

    _ = testDB -> close();

    return (hasNext, removedCount);
}

function idMatches (Employee p) returns (boolean) {
    return p.id > 0;
}




