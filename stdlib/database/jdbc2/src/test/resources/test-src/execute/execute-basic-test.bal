import ballerina/java.jdbc;
import ballerina/sql;

function testCreateTable(string jdbcURL, string user, string password) returns sql:ExecuteResult|error? {
     jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
     sql:ExecuteResult? result = check dbClient->execute("CREATE TABLE TestCreateTable(studentID int, LastName varchar(255))");
    check dbClient.close();
    return result;
}


