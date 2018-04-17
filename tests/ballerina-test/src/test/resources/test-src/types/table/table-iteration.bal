import ballerina/sql;

type Person {
    int id;
    int age;
    float salary;
    string name;
};

type ResultCount {
    int COUNTVAL;
};

type Employee {
    int id;
    string name;
    float salary;
};

type EmployeeCompatible {
    int id;
    string name;
    float salary;
};

type EmployeeSalary {
    int id;
    float salary;
};

type EmployeeSalaryCompatible {
    int id;
    float salary;
};

int idValue = -1;
int ageValue = -1;
float salValue = -1;
string nameValue = "";

function testForEachInTableWithStmt() returns (int, int, float, string) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person where id = 1", Person);
    table<Person> dt = check temp;

    int id;
    int age;
    float salary;
    string name;

    foreach x in dt {
        id = x.id;
        age = x.age;
        salary = x.salary;
        name = x.name;
    }
    _ = testDB -> close();
    return (id, age, salary, name);
}

function testForEachInTable() returns (int, int, float, string) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person where id = 1", Person);
    table<Person> dt = check temp;

    dt.foreach((Person p) => {
            idValue = p.id;
            ageValue = p.age;
            salValue = p.salary;
            nameValue = p.name;
        }
    );
    int id = idValue;
    int age = ageValue;
    float salary = salValue;
    string name = nameValue;
    _ = testDB -> close();
    return (id, age, salary, name);
}

function testCountInTable() returns (int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person where id < 10", Person);
    table<Person> dt = check temp;
    int count = dt.count();
    _ = testDB -> close();
    return count;
}

function testFilterTable() returns (int, int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person", Person);
    table<Person> dt = check temp;
    Person[] personBelow35 = dt.filter(isBellow35);
    int count = lengthof personBelow35;
    int id1 = personBelow35[0].id;
    int id2 = personBelow35[1].id;
    _ = testDB -> close();
    return (count, id1, id2);
}

function testFilterWithAnnonymousFuncOnTable() returns (int, int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person", Person);
    table<Person> dt = check temp;
    Person[] personBelow35 = dt.filter((Person p) => (boolean) {
            return p.age < 35;
        });
    int count = lengthof personBelow35;
    int id1 = personBelow35[0].id;
    int id2 = personBelow35[1].id;
    _ = testDB -> close();
    return (count, id1, id2);
}

function testFilterTableWithCount() returns (int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person", Person);
    table<Person> dt = check temp;
    int count = dt.filter(isBellow35).count();
    _ = testDB -> close();
    return count;
}

function testMapTable() returns (string[]) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person order by id", Person);
    table<Person> dt = check temp;
    string[] names = dt.map(getName);
    _ = testDB -> close();
    return names;
}

function testMapWithFilterTable() returns (string[]) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person order by id", Person);
    table<Person> dt = check temp;
    string[] names = dt.map(getName).filter(isGeraterThan4String);
    _ = testDB -> close();
    return names;
}

function testFilterWithMapTable() returns (string[]) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person order by id", Person);
    table<Person> dt = check temp;
    string[] names = dt.filter(isGeraterThan4).map(getName);
    _ = testDB -> close();
    return names;
}

function testFilterWithMapAndCountTable() returns (int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person order by id", Person);
    table<Person> dt = check temp;
    int count = dt.filter(isGeraterThan4).map(getName).count();
    _ = testDB -> close();
    return count;
}

function testAverageWithTable() returns (float) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person order by id", Person);
    table<Person> dt = check temp;
    float avgSal = dt.map(getSalary).average();
    _ = testDB -> close();
    return avgSal;
}

function testMinWithTable() returns (float) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person order by id", Person);
    table<Person> dt = check temp;
    float avgSal = dt.map(getSalary).min();
    _ = testDB -> close();
    return avgSal;
}

function testMaxWithTable() returns (float) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person order by id", Person);
    table<Person> dt = check temp;
    float avgSal = dt.map(getSalary).max();
    _ = testDB -> close();
    return avgSal;
}

function testSumWithTable() returns (float) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT * from Person order by id", Person);
    table<Person> dt = check temp;
    float avgSal = dt.map(getSalary).sum();
    _ = testDB -> close();
    return avgSal;
}

function testCloseConnectionPool() returns (int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_DATA_TABLE__ITR_DB",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB -> select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", ResultCount);
    table<Person> dt = check temp;
    int count;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB -> close();
    return count;
}

function testSelect() returns (table) {

    table<Employee> dt = createTable();

    table<EmployeeSalary> salaryTable = dt.select(getEmployeeSalary);
    return salaryTable;
}

function testSelectCompatibleLambdaInput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> salaryTable = dt.select(getEmployeeSalaryCompatibleInput);
    return salaryTable;
}

function testSelectCompatibleLambdaOutput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> salaryTable = dt.select(getEmployeeSalaryCompatibleOutput);
    return salaryTable;
}

function testSelectCompatibleLambdaInputOutput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> salaryTable = dt.select(getEmployeeSalaryCompatibleInputOutput);
    return salaryTable;
}

function getEmployeeSalary(Employee e) returns (EmployeeSalary) {
    EmployeeSalary s = {id:e.id, salary:e.salary};
    return s;
}

function getEmployeeSalaryCompatibleInput(EmployeeCompatible e) returns (EmployeeSalary) {
    EmployeeSalary s = {id:e.id, salary:e.salary};
    return s;
}

function getEmployeeSalaryCompatibleOutput(Employee e) returns (EmployeeSalaryCompatible) {
    EmployeeSalaryCompatible s = {id:e.id, salary:e.salary};
    return s;
}

function getEmployeeSalaryCompatibleInputOutput(EmployeeCompatible e) returns (EmployeeSalaryCompatible) {
    EmployeeSalaryCompatible s = {id:e.id, salary:e.salary};
    return s;
}

function createTable() returns (table<Employee>) {
    table<Employee> dt = table{};

    Employee e1 = {id:1, name:"A", salary:100};
    Employee e2 = {id:2, name:"B", salary:200};
    Employee e3 = {id:3, name:"C", salary:300};

    _ = dt.add(e1);
    _ = dt.add(e2);
    _ = dt.add(e3);

    return dt;
}

function isBellow35(Person p) returns (boolean) {
    return p.age < 35;
}

function getName(Person p) returns (string) {
    return p.name;
}

function getSalary(Person p) returns (float) {
    return p.salary;
}

function isGeraterThan4(Person p) returns (boolean) {
    return lengthof p.name > 4;
}

function isGeraterThan4String(string s) returns (boolean) {
    return lengthof s > 4;
}
