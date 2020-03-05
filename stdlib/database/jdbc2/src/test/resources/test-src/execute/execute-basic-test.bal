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

type NumericType record {
    int id;
    int? int_type;
    int? bigint_type;
    int? smallint_type;
    int? tinyint_type;
    boolean? bit_type;
    decimal? decimal_type;
    decimal? numeric_type;
    float? float_type;
    float? real_type;
};

function testInsertAndSelectTableWithGeneratedKeys(string jdbcURL, string user, string password) returns [sql:ExecuteResult?,NumericType?]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("insert into NumericTypes (int_type) values (31)");

    NumericType? insertedData = ();
    if(result is sql:ExecuteResult){
        string|int? insertedId = result.lastInsertId;
        if(insertedId is int){
            string query = string `SELECT * from NumericTypes where id = ${insertedId}`;
            stream<record{}, error> queryResult = dbClient->query(query, NumericType);

            stream<NumericType, sql:Error> streamData = <stream<NumericType, sql:Error>> queryResult;
            record{| NumericType value; |}? data =  check streamData.next();
            check streamData.close();
            insertedData = data?.value;
        }
    }

    check dbClient.close();

    return [result,insertedData];
}

function testInsertWithAllNilAndSelectTableWithGeneratedKeys(string jdbcURL, string user, string password) returns [sql:ExecuteResult?,NumericType?]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("Insert into NumericTypes (int_type, bigint_type, "
        + "smallint_type, tinyint_type, bit_type, decimal_type, numeric_type, float_type, real_type) "
        +"values (null,null,null,null,null,null,null,null,null)");

    NumericType? insertedData = ();
    if(result is sql:ExecuteResult){
        string|int? insertedId = result.lastInsertId;
        if(insertedId is int){
            string query = string `SELECT * from NumericTypes where id = ${insertedId}`;
            stream<record{}, error> queryResult = dbClient->query(query, NumericType);

            stream<NumericType, sql:Error> streamData = <stream<NumericType, sql:Error>> queryResult;
            record{| NumericType value; |}? data =  check streamData.next();
            check streamData.close();
            insertedData = data?.value;
        }
    }

    check dbClient.close();

    return [result,insertedData];
}

type StringData record {
    int id;
    string varchar_type;
    string charmax_type;
    string char_type;
    string charactermax_type;
    string character_type;
    string nvarcharmax_type;
    string longvarchar_type;
    string clob_type;
};

function testInsertWithStringAndSelectTable(string jdbcURL, string user, string password) returns [sql:ExecuteResult?,StringData?]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int intIDVal = 25;
    string insertQuery = string `Insert into StringTypes (id, varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type, longvarchar_type, clob_type) values (${intIDVal},'str1','str2','str3','str4','str5','str6','str7','str8')`;
    sql:ExecuteResult? result = check dbClient->execute(insertQuery);

    StringData? insertedData = ();
    string query = string `SELECT * from StringTypes where id = ${intIDVal}`;
    stream<record{}, error> queryResult = dbClient->query(query, StringData);
    stream<StringData, sql:Error> streamData = <stream<StringData, sql:Error>> queryResult;
    record{| StringData value; |}? data =  check streamData.next();
    check streamData.close();
    insertedData = data?.value;

    check dbClient.close();

    return [result,insertedData];
}

function testInsertWithEmptyStringAndSelectTable(string jdbcURL, string user, string password) returns [sql:ExecuteResult?,StringData?]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int intIDVal = 35;
    string insertQuery = string `Insert into StringTypes (id, varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type, longvarchar_type, clob_type) values (${intIDVal},'','','','','','','','')`;
    sql:ExecuteResult? result = check dbClient->execute(insertQuery);

    StringData? insertedData = ();
    string query = string `SELECT * from StringTypes where id = ${intIDVal}`;
    stream<record{}, error> queryResult = dbClient->query(query, StringData);
    stream<StringData, sql:Error> streamData = <stream<StringData, sql:Error>> queryResult;
    record{| StringData value; |}? data =  check streamData.next();
    check streamData.close();
    insertedData = data?.value;

    check dbClient.close();

    return [result,insertedData];
}

type StringNilData record {
    int id;
    string? varchar_type;
    string? charmax_type;
    string? char_type;
    string? charactermax_type;
    string? character_type;
    string? nvarcharmax_type;
    string? longvarchar_type;
    string? clob_type;
};

function testInsertWithNilStringAndSelectTable(string jdbcURL, string user, string password) returns [sql:ExecuteResult?,StringNilData?]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int intIDVal = 45;
    string insertQuery = string `Insert into StringTypes (id, varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type, longvarchar_type, clob_type) values (${intIDVal},null,null,null,null,null,null,null,null)`;
    sql:ExecuteResult? result = check dbClient->execute(insertQuery);

    StringNilData? insertedData = ();
    string query = string `SELECT * from StringTypes where id = ${intIDVal}`;
    stream<record{}, error> queryResult = dbClient->query(query, StringNilData);
    stream<StringNilData, sql:Error> streamData = <stream<StringNilData, sql:Error>> queryResult;
    record{| StringNilData value; |}? data =  check streamData.next();
    check streamData.close();
    insertedData = data?.value;

    check dbClient.close();
    return [result,insertedData];
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


