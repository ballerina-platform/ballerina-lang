import ballerina/java.jdbc;
import ballerina/sql;

type Person record {|
    int id;
    string name;
    int age;
|};

public function main(string url, string user, string password, string nickname1, string nickname2) returns error? {
    Person p1 = {id: 17, name: "Melina", age: 23};
    Person p2 = {id: 29, name: "Tobi", age: 46};

    Person[] personList = [p1, p2];
    string[] nicknameList = [nickname1, nickname2];

    jdbc:Client dbClient = check new (url = url, user = user, password = password);

    sql:ParameterizedQuery[] sqlQuery1 = from var nickname in nicknameList
                                         select `INSERT INTO Nickname VALUES (nickname)`;
    sql:ExecutionResult[]? result1 = check dbClient->batchExecute(sqlQuery1);

    sql:ParameterizedQuery[] sqlQuery2 = from var person in personList
                                         from var nickname in nicknameList
                                         where person.name == nickname
                                         select `INSERT INTO Person VALUES (${person.id}, ${nickname},
                                         ${person.age})`;
    sql:ExecutionResult[]? result2 = check dbClient->batchExecute(sqlQuery2);

    sql:ParameterizedQuery[] sqlQuery3 = from var person in personList
                                         join var nickname in nicknameList
                                         on person.name equals nickname
                                         select `INSERT INTO Person VALUES (${person.id}, ${nickname},
                                         ${person.age})`;
    sql:ExecutionResult[]? result3 = check dbClient->batchExecute(sqlQuery3);

    sql:ParameterizedQuery[] sqlQuery4 = from var person in personList
                                         outer join var nickname in nicknameList
                                         on person.name equals nickname
                                         select `INSERT INTO Person VALUES (${person.id}, ${nickname},
                                         ${person.age})`;
    sql:ExecutionResult[]? result4 = check dbClient->batchExecute(sqlQuery4);

    sql:ParameterizedQuery[] sqlQuery5 = from var nickname in (from var nm in nicknameList select nm)
                                         select `INSERT INTO Nickname VALUES (nickname)`;
    sql:ExecutionResult[]? result5 = check dbClient->batchExecute(sqlQuery5);

    string[] opList = from var nickname in nicknameList
                      select nickname;

    var nm = getFirstNickname(opList);
}

function getFirstNickname(@untainted string[] op) returns string {
    return op[0];
}
