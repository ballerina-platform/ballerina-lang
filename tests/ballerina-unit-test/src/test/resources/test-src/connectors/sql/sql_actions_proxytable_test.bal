import ballerina/sql;
import ballerina/jdbc;
import ballerina/io;

type Employee record {
    int id,
    string name,
    string address,
};

type Salary record {
    float salaryValue,
};


function testIterateMirrorTable(string jdbcUrl, string userName, string password) returns (Employee[], Employee[]) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };

    table<Employee> dt = check testDB->getProxyTable("employeeItr", Employee);

    Employee[] employeeArray1;
    Employee[] employeeArray2;
    int i = 0;
    foreach x in dt {
        employeeArray1[i] = x;
        i++;
    }

    i = 0;
    foreach x in dt {
        employeeArray2[i] = x;
        i++;
    }

    testDB.stop();
    return (employeeArray1, employeeArray2);
}

function testIterateMirrorTableAfterClose(string jdbcUrl, string userName, string password) returns (Employee[],
            Employee[], error) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };

    table<Employee> dt = check testDB->getProxyTable("employeeItr", Employee);

    Employee[] employeeArray1;
    Employee[] employeeArray2;
    Employee[] employeeArray3;

    int i = 0;
    foreach x in dt {
        employeeArray1[i] = x;
        i++;
    }

    i = 0;
    foreach x in dt {
        employeeArray2[i] = x;
        i++;
    }

    dt.close();
    i = 0;
    error e;
    try {
        foreach x in dt {
            employeeArray3[i] = x;
            i++;
        }
    } catch (error err) {
        e = err;
    }
    testDB.stop();
    return (employeeArray1, employeeArray2, e);
}

function testAddToMirrorTable(string jdbcUrl, string userName, string password) returns (Employee[]) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->getProxyTable("employeeAdd", Employee);

    Employee e1 = { id: 1, name: "Manuri", address: "Sri Lanka" };
    Employee e2 = { id: 2, name: "Devni", address: "Sri Lanka" };

    var result1 = dt.add(e1);
    var result2 = dt.add(e2);

    table dt2 = check testDB->select("SELECT  * from employeeAdd", Employee);

    Employee[] employeeArray;
    int i = 0;
    while (dt2.hasNext()) {
        employeeArray[i] = check <Employee>dt2.getNext();
        i++;
    }

    testDB.stop();
    return employeeArray;
}

function testAddToMirrorTableNegative(string jdbcUrl, string userName, string password) returns (any) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->getProxyTable("employeeAddNegative", Employee);
    Employee e1 = { id: 1, name: "Manuri", address: "Sri Lanka" };
    var result = dt.add(e1);
    testDB.stop();
    return result;
}

function testDeleteFromMirrorTable(string jdbcUrl, string userName, string password) returns (boolean, int) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 2 }
    };

    table dt = check testDB->getProxyTable("employeeDel", Employee);
    var val = dt.remove(idMatches);
    int removedCount;
    match val {
        int count => removedCount = count;
        error e => removedCount = -1;
    }

    table dt2 = check testDB->select("SELECT  * from employeeDel", Employee);
    boolean hasNext = dt2.hasNext();

    testDB.stop();
    return (hasNext, removedCount);
}

function testDeleteFromMirrorTableinTransaction(string jdbcUrl, string userName, string password)
             returns (boolean, int) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 2 }
    };

    int removedCount;
    transaction {
        table dt = check testDB->getProxyTable("employeeDeleteInTrans", Employee);
        var val = dt.remove(idMatches);
        match val {
            int count => removedCount = count;
            error e => removedCount = -1;
        }
        abort;
    }

    table dt2 = check testDB->select("SELECT  * from employeeDeleteInTrans", Employee);
    boolean hasNext = dt2.hasNext();

    testDB.stop();
    return (hasNext, removedCount);
}

function testProxyTablePrintln(string jdbcUrl, string userName, string password) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->getProxyTable("employeeItr", Employee);

    io:println(dt);
    testDB.stop();
}

function idMatches(Employee p) returns (boolean) {
    return p.id > 0;
}

