import ballerina/io;
import ballerina/java.jdbc;
import ballerina/sql;

type Person record {|
    int id;
    string name;
    int age;
|};

public function main(string url, string user, string password, string nickname1, string nickname2)
returns @tainted error? {
    boolean testPassed = true;
    string[] nicknameList = [nickname1, nickname2];
    Person p1 = {id: 17, name: "Matty", age: 23};
    Person p2 = {id: 29, name: "Tom", age: 46};

    Person[] personList = [p1, p2];

    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    sql:ParameterizedQuery[] sqlQuery1 = from var person in personList
                                         from var nickname in nicknameList
                                         where person.name == nickname
                                         select `INSERT INTO Person VALUES (${person.id}, ${nickname},
                                         ${person.age})`;
    sql:ExecutionResult[]? result1 = check dbClient->batchExecute(<@untainted>sqlQuery1);

    stream<record{}, error> streamData1 = dbClient->query("SELECT * FROM Person WHERE age=46");
    record {|record {} value;|}? data1 = check streamData1.next();
    check streamData1.close();
    record {}? value1 = data1?.value;

    sql:ParameterizedQuery[] sqlQuery2 = from var person in personList
                                         from var nickname in <@untainted>nicknameList
                                         where person.name == nickname
                                         select `INSERT INTO Person VALUES (${person.id}, ${nickname}, 59)`;
    sql:ExecutionResult[]? result2 = check dbClient->batchExecute(sqlQuery2);

    stream<record{}, error> streamData2 = dbClient->query("SELECT * FROM Person WHERE name='Matty' AND age=59");
    record {|record {} value;|}? data2 = check streamData2.next();
    check streamData2.close();
    record {}? value2 = data2?.value;

    sql:ParameterizedQuery[] sqlQuery3 = from var person in personList
                                         select `INSERT INTO Person VALUES (${person.id}, "Maththew",
                                         ${person.age})`;
    sql:ExecutionResult[]? result3 = check dbClient->batchExecute(sqlQuery3);

    stream<record{}, error> streamData3 = dbClient->query("SELECT * FROM Person WHERE name='Maththew'");
    record {|record {} value;|}? data3 = check streamData3.next();
    check streamData3.close();
    record {}? value3 = data3?.value;
    check dbClient.close();

    io:println(value1);
    io:println(value2);
    io:println(value3);
}
