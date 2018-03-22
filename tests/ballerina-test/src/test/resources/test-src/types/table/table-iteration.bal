import ballerina/data.sql;

struct Person {
    int id;
    int age;
    float salary;
    string name;
}

struct ResultCount {
    int COUNTVAL;
}

int idValue = -1;
int ageValue = -1;
float salValue = -1;
string nameValue = "";

function testForEachInTableWithStmt () returns (int, int, float, string) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table<Person> dt = testDB -> select("SELECT * from Person where id = 1", null, typeof Person);
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
    testDB -> close();
    return (id, age, salary, name);
}

function testForEachInTable () returns (int, int, float, string) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB-> select("SELECT * from Person where id = 1", null, typeof Person);
    dt.foreach (function (Person p) {
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
    testDB -> close();
    return (id, age, salary, name);
}

function testCountInTable () returns (int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table<Person> dt = testDB -> select("SELECT * from Person where id < 10", null, typeof Person);
    int count = dt.count();
    testDB -> close();
    return count;
}

function testFilterTable () returns (int, int, int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person", null, typeof Person);
    Person[] personBelow35 = dt.filter(isBellow35);
    int count = lengthof personBelow35;
    int id1 = personBelow35[0].id;
    int id2 = personBelow35[1].id;
    testDB -> close();
    return (count, id1, id2);
}

function testFilterWithAnnonymousFuncOnTable () returns (int, int, int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person", null, typeof Person);
    Person[] personBelow35 = dt.filter(function (Person p) returns (boolean) {
                                           return p.age < 35;
                                       });
    int count = lengthof personBelow35;
    int id1 = personBelow35[0].id;
    int id2 = personBelow35[1].id;
    testDB -> close();
    return (count, id1, id2);
}

function testFilterTableWithCount () returns (int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person", null, typeof Person);
    int count = dt.filter(isBellow35).count();
    testDB -> close();
    return count;
}

function testMapTable () returns (string[]) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person order by id", null, typeof Person);
    string[] names = dt.map(getName);
    testDB -> close();
    return names;
}

function testMapWithFilterTable () returns (string[]) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person order by id", null, typeof Person);
    string[] names = dt.map(getName).filter(isGeraterThan4String);
    testDB -> close();
    return names;
}

function testFilterWithMapTable () returns (string[]) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person order by id", null, typeof Person);
    string[] names = dt.filter(isGeraterThan4).map(getName);
    testDB -> close();
    return names;
}

function testFilterWithMapAndCountTable () returns (int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person order by id", null, typeof Person);
    int count = dt.filter(isGeraterThan4).map(getName).count();
    testDB -> close();
    return count;
}

function testAverageWithTable () returns (float) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person order by id", null, typeof Person);
    float avgSal = dt.map(getSalary).average();
    testDB -> close();
    return avgSal;
}

function testMinWithTable () returns (float) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person order by id", null, typeof Person);
    float avgSal = dt.map(getSalary).min();
    testDB -> close();
    return avgSal;
}

function testMaxWithTable () returns (float) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table<Person> dt = testDB -> select("SELECT * from Person order by id", null, typeof Person);
    float avgSal = dt.map(getSalary).max();
    testDB -> close();
    return avgSal;
}

function testSumWithTable () returns (float) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table<Person> dt = testDB -> select("SELECT * from Person order by id", null, typeof Person);
    float avgSal = dt.map(getSalary).sum();
    testDB -> close();
    return avgSal;
}

function testCloseConnectionPool () returns (int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table dt = testDB -> select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", null,
                              typeof ResultCount);
    int count;
    while (dt.hasNext()) {
        var rs, _ = (ResultCount) dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return count;
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
