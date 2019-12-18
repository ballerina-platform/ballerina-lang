import ballerina/io;
import ballerinax/java.jdbc;

string jdbcUserName = "SA";
string jdbcPassword = "";

type Customer record {
    string firstName;
};

public function main(string jdbcURL) {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT firstName FROM Customers", Customer);

    string firstName = "";

    if (result is table<Customer>) {
        foreach var x in result {
            firstName = <@untainted>x.firstName;
        }
    }
    checkpanic testDB.stop();
    io:println("Customer name is :" + firstName);
}
