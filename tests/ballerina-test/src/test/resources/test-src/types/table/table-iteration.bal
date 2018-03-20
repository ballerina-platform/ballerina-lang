import ballerina.data.sql;

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

function testForEachInTableWithStmt () (int id, int age, float salary, string name) {
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
    foreach x in dt {
        id = x.id;
        age = x.age;
        salary = x.salary;
        name = x.name;
    }
    testDB -> close();
    return;
}

function testForEachInTable () (int id, int age, float salary, string name) {
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
    id = idValue;
    age = ageValue;
    salary = salValue;
    name = nameValue;
    testDB -> close();
    return;
}

function testCountInTable () (int count) {
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
    count = dt.count();
    testDB -> close();
    return;
}

function testFilterTable () (int count, int id1, int id2) {
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
    count = lengthof personBelow35;
    id1 = personBelow35[0].id;
    id2 = personBelow35[1].id;
    testDB -> close();
    return;
}

function testFilterWithAnnonymousFuncOnTable () (int count, int id1, int id2) {
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
    Person[] personBelow35 = dt.filter(function (Person p) (boolean) {
                                           return p.age < 35;
                                       });
    count = lengthof personBelow35;
    id1 = personBelow35[0].id;
    id2 = personBelow35[1].id;
    testDB -> close();
    return;
}

function testFilterTableWithCount () (int count) {
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
    count = dt.filter(isBellow35).count();
    testDB -> close();
    return;
}

function testMapTable () (string[] names) {
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
    names = dt.map(getName);
    testDB -> close();
    return;
}

function testMapWithFilterTable () (string[] names) {
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
    names = dt.map(getName).filter(isGeraterThan4String);
    testDB -> close();
    return;
}

function testFilterWithMapTable () (string[] names) {
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
    names = dt.filter(isGeraterThan4).map(getName);
    testDB -> close();
    return;
}

function testFilterWithMapAndCountTable () (int count) {
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
    count = dt.filter(isGeraterThan4).map(getName).count();
    testDB -> close();
    return;
}

function testAverageWithTable () (float avgSal) {
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
    avgSal = dt.map(getSalary).average();
    testDB -> close();
    return;
}

function testMinWithTable () (float avgSal) {
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
    avgSal = dt.map(getSalary).min();
    testDB -> close();
    return;
}

function testMaxWithTable () (float avgSal) {
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
    avgSal = dt.map(getSalary).max();
    testDB -> close();
    return;
}

function testSumWithTable () (float avgSal) {
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
    avgSal = dt.map(getSalary).sum();
    testDB -> close();
    return;
}

function testCloseConnectionPool () (int count) {
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
    while (dt.hasNext()) {
        var rs, _ = (ResultCount) dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function isBellow35(Person p)(boolean){
    return p.age < 35;
}

function getName(Person p)(string s){
    return p.name;
}

function getSalary(Person p)(float f){
    return p.salary;
}

function isGeraterThan4(Person p) (boolean) {
    return lengthof p.name > 4;
}

function isGeraterThan4String(string s) (boolean) {
    return lengthof s > 4;
}
