import ballerina/sql;

//It is possible to use @sensitive annotation with parameters
//of user defined functions and actions. This will allow, users
//to restrict passing tainted data into a security sensitive parameter.
public function userDefinedSecureOperation (@sensitive string secureParameter) {

}

function main (string[] args) {
    endpoint sql:Client customerDatabase {
        database: sql:DB.MYSQL,
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "root",
        password: "root",
        options: {maximumPoolSize:5}
    };

    //Sensitive parameters of operations builtin to Ballerina
    //carry @sensitive annotation, to make sure tainted data
    //cannot be passed into such parameters. For example, the
    //taint checking mechanism will completely prevent SQL injection
    //vulnerabilities by disallowing any tainted data in SQL query.
    //This line will result in a compiler error, since query has
    //been appended with a user provided argument.
    var dataTable = customerDatabase -> select("SELECT firstname from customers where registrationID = " + args[0], null, null);

    //This line will result in a compiler error, since a user provided
    //argument has been passed to a sensitive parameter.
    userDefinedSecureOperation(args[0]);

    if (isInteger(args[0])) {
        //After performing necessary validations and/or escaping, it
        //is possible to use the "untaint" unary expression, to mark
        //the proceeding value untainted and pass it to a sensitive parameter.
        userDefinedSecureOperation(untaint args[0]);
    } else {
        error err = {message:"Validation error: ID should be an integer"};
        throw err;
    }

    var closeStatus = customerDatabase -> close();
    return;
}

public function isInteger (string input) returns boolean{
    Regex reg = {pattern:"\\d+"};
    boolean isInteger =? input.matchesWithRegex(reg);
    return isInteger;
}
