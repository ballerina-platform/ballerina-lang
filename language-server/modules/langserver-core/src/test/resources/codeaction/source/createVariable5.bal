import ballerina/mysql;
import ballerina/sql;

type Foo record {|
    string s?;
|};

type Address record {|
    string city;
    string country;
|};

function queryBinaryType(mysql:Client mysqlClient) {
    Foo f = {};
    mysqlClient->query("Select * from Customers", Foo);
    <stream<Foo, sql:Error>>resultStream; 
    <int>1.1;
    Address address = {city: "Colombo", country: "Sri Lanka"};
    {
        name: "Anne",
        age: 18,
        grades: {
            maths: 70,
            physics: 80,
            chemistry: 55
        },
        ...address
    };
}
