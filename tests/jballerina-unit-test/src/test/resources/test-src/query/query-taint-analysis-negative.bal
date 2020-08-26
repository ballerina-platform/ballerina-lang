import ballerina/java.jdbc;
import ballerina/sql;

type Person record {|
    int id;
    string name;
    int age;
|};

type Employee record {|
    int empId;
    float salary;
|};

type PartTimeEmployee record {|
    int partTimeEmpId;
    string name;
    float salary;
    int noOfHours;
|};

function testQueryExprWithTaintedValues(string url, string user, string password)
returns sql:ExecutionResult[]|sql:Error? {
    Person p1 = {id: 17, name: "Melina", age: 23};
    Person p2 = {id: 29, name: "Tobi", age: 46};

    Person[] personList = [p1, p2];

    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    sql:ParameterizedQuery[] sqlQuery = from var person in <@tainted>personList
                                        select `INSERT INTO Person VALUES (${person.id}, ${person.name},
                                        ${person.age})`;
    sql:ExecutionResult[]? result = check dbClient->batchExecute(sqlQuery);
    return result;
}

function testQueryExprWithMultipleFromHavingTaintedValues(string url, string user, string password)
returns sql:ExecutionResult[]|sql:Error? {
    Person p1 = {id: 17, name: "Melina", age: 23};
    Person p2 = {id: 29, name: "Tobi", age: 46};

    Employee e1 = {empId: 17, salary: 4000.50};
    Employee e2 = {empId: 29, salary: 5600.50};

    Person[] personList = [p1, p2];
    Employee[] employeeList = [e1, e2];

    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    sql:ParameterizedQuery[] sqlQuery = from var person in personList
                                        from var emp in <@tainted>employeeList
                                        where person.id == emp.empId
                                        select `INSERT INTO PartTimeEmployee VALUES (${person.id}, ${person.name},
                                        ${emp.salary}, 8)`;
    sql:ExecutionResult[]? result = check dbClient->batchExecute(sqlQuery);
    return result;
}

function testQueryExprWithInnerJoinHavingTaintedValues(string url, string user, string password)
returns sql:ExecutionResult[]|sql:Error? {
    Person p1 = {id: 17, name: "Melina", age: 23};
    Person p2 = {id: 29, name: "Tobi", age: 46};

    Employee e1 = {empId: 17, salary: 4000.50};
    Employee e2 = {empId: 29, salary: 5600.50};

    Person[] personList = [p1, p2];
    Employee[] employeeList = [e1, e2];

    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    sql:ParameterizedQuery[] sqlQuery = from var person in personList
                                        join var emp in <@tainted>employeeList
                                        on person.id equals emp.empId
                                        select `INSERT INTO PartTimeEmployee VALUES (${person.id}, ${person.name},
                                        ${emp.salary}, 8)`;
    sql:ExecutionResult[]? result = check dbClient->batchExecute(sqlQuery);
    return result;
}

function testQueryExprWithOuterJoinHavingTaintedValues(string url, string user, string password)
returns sql:ExecutionResult[]|sql:Error? {
    Person p1 = {id: 17, name: "Melina", age: 23};
    Person p2 = {id: 29, name: "Tobi", age: 46};

    Employee e1 = {empId: 17, salary: 4000.50};
    Employee e2 = {empId: 30, salary: 5600.50};

    Person[] personList = [p1, p2];
    Employee[] employeeList = [e1, e2];

    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    sql:ParameterizedQuery[] sqlQuery = from var person in personList
                                        outer join var emp in <@tainted>employeeList
                                        on person.id equals emp.empId
                                        select `INSERT INTO PartTimeEmployee VALUES (${person.id}, ${person.name},
                                        ${emp.salary}, 8)`;
    sql:ExecutionResult[]? result = check dbClient->batchExecute(sqlQuery);
    return result;
}

function testQueryExprWithInnerQueryHavingTaintedValues(string url, string user, string password)
returns sql:ExecutionResult[]|sql:Error? {
    Person p1 = {id: 17, name: "Melina", age: 23};
    Person p2 = {id: 29, name: "Tobi", age: 46};

    Person[] personList = [p1, p2];

    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    sql:ParameterizedQuery[] sqlQuery = from var person in (from var ps in <@tainted>personList select ps)
                                        select `INSERT INTO Person VALUES (${person.id}, ${person.name},
                                        ${person.age})`;
    sql:ExecutionResult[]? result = check dbClient->batchExecute(sqlQuery);
    return result;
}

function testSimpleQueryExprWithTaintedValues() returns PartTimeEmployee {
    Person p1 = {id: 17, name: "Melina", age: 23};
    Person p2 = {id: 29, name: "Tobi", age: 46};

    Employee e1 = {empId: 17, salary: 4000.50};
    Employee e2 = {empId: 30, salary: 5600.50};

    Person[] personList = [p1, p2];
    Employee[] employeeList = [e1, e2];

    PartTimeEmployee[] op = from var person in personList
                            from var emp in <@tainted>employeeList
                            select {
                                partTimeEmpId: person.id,
                                name: person.name,
                                salary: emp.salary,
                                noOfHours: 12
                            };
    return getFirstEmployee(op);
}

function getFirstEmployee(@untainted PartTimeEmployee[] op) returns PartTimeEmployee {
    return op[0];
}
