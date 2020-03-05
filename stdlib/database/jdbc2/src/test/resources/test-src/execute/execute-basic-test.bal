import ballerina/java.jdbc;
import ballerina/sql;

function testCreateTable(string jdbcURL, string user, string password) returns sql:ExecuteResult|sql:Error? {
     jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
     sql:ExecuteResult? result = check dbClient->execute("CREATE TABLE TestCreateTable(studentID int, LastName varchar(255))");
    check dbClient.close();
    return result;
}

function testInsertTable(string jdbcURL, string user, string password) returns sql:ExecuteResult|sql:Error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("Insert into NumericTypes (int_type) values (20)");
    check dbClient.close();
    return result;
}

function testInsertTableWithoutGeneratedKeys(string jdbcURL, string user, string password) returns sql:ExecuteResult|sql:Error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("Insert into StringTypes (id, varchar_type) values (20, 'test')");
    check dbClient.close();
    return result;
}

function testInsertTableWithGeneratedKeys(string jdbcURL, string user, string password) returns sql:ExecuteResult|sql:Error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("insert into NumericTypes (int_type) values (21)");
    check dbClient.close();
    return result;
}

function testInsertTableWithDatabaseError(string jdbcURL, string user, string password) returns sql:ExecuteResult|sql:Error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("Insert into NumericTypesNonExistTable (int_type) values (20)");
    check dbClient.close();
    return result;
}

function testInsertTableWithDataTypeError(string jdbcURL, string user, string password) returns sql:ExecuteResult|sql:Error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("Insert into NumericTypes (int_type) values ('This is wrong type')");
    check dbClient.close();
    return result;
}

type ResultCount record {
    int countVal;
};

function testUdateData(string jdbcURL, string user, string password) returns [sql:ExecuteResult?, ResultCount?]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("Update NumericTypes set int_type = 11 where int_type = 10");
    ResultCount? resultCount = ();

    stream<record{}, error> queryResult = dbClient->query("SELECT count(*) as countval from NumericTypes where int_type = 11", ResultCount);
    stream<ResultCount, sql:Error> streamData = <stream<ResultCount, sql:Error>> queryResult;
    record{| ResultCount value; |}? data =  check streamData.next();
    check streamData.close();
    resultCount = data?.value;

    check dbClient.close();
    return [result, resultCount];
}


