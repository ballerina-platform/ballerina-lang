import ballerina/mysql;
import ballerina/sql;

type Foo record {|
    string s?;
|};


function queryBinaryType(mysql:Client mysqlClient) {
    Foo f = {};
    mysqlClient->query("Select * from Customers", Foo);
    <stream<Foo, sql:Error>>resultStream; 
    <int>1.1;
}
