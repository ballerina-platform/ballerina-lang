import ballerina/data.sql;

struct Employee {
    int id;
    string name;
    string address;
}

function testIterateMirrorTable () returns (Employee[], Employee[]) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    table dt =? testDB -> mirror("employeeItr", typeof Employee);

    Employee [] employeeArray1;
    Employee [] employeeArray2;
    int i = 0;
    while (dt.hasNext()) {
        var rs =? <Employee>dt.getNext();
        Employee e = {id: rs.id, name: rs.name, address:rs.address};
        employeeArray1[i] = e;
        i++;
    }

    i = 0;
    while (dt.hasNext()) {
        var rs =? <Employee>dt.getNext();
        Employee e = {id: rs.id, name: rs.name, address:rs.address};
        employeeArray2[i] = e;
        i++;
    }

    _ = testDB -> close();
    return (employeeArray1, employeeArray2);
}

function testAddToMirrorTable () returns (Employee[]) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    table dt =? testDB -> mirror("employeeAdd", typeof Employee);

    Employee e1 = {id: 1, name:"Manuri", address:"Sri Lanka"};
    Employee e2 = {id: 2, name:"Devni", address:"Sri Lanka"};

    var result1 = dt.add(e1);
    var result2 = dt.add(e2);

    table dt2 =? testDB -> select("SELECT  * from employeeAdd", null, typeof Employee);

    Employee [] employeeArray;
    int i = 0;
    while (dt2.hasNext()) {
        var rs =? <Employee>dt2.getNext();
        Employee e = {id: rs.id, name: rs.name, address:rs.address};
        employeeArray[i] = e;
        i++;
    }

    _ = testDB -> close();

    return employeeArray;
}

function testAddToMirrorTableNegative () returns (any) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    table dt =? testDB -> mirror("employeeAddNegative", typeof Employee);

    Employee e1 = {id: 1, name:"Manuri", address:"Sri Lanka"};

    var result = dt.add(e1);

    _ = testDB -> close();

    return result;
}


function testDeleteFromMirrorTable () returns (boolean, int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    table dt =? testDB -> mirror("employeeDel", typeof Employee);

    var val = dt.remove(idMatches);
    int removedCount;
    match val {
        int count => removedCount = count;
        TableOperationError e => removedCount = -1;
    }

    table dt2 =? testDB -> select("SELECT  * from employeeDel", null, typeof Employee);

    boolean hasNext = dt2.hasNext();

    _ = testDB -> close();

    return (hasNext, removedCount);
}

function idMatches (Employee p) returns (boolean) {
    return p.id > 0;
}




