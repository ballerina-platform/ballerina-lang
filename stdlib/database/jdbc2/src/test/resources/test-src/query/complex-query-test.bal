import ballerina/java.jdbc;

function testQuery(string jdbcURL, string user, string password) returns record{}|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT int_type, long_type, float_type, double_type, boolean_type, string_type from DataTable WHERE row_id = 1");
    record{| record{} value; |}? data =  check streamData.next();
    check streamData.close();
    record{}? value = data?.value;
    check dbClient.close();
    return value;
}
