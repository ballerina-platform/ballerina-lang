import ballerina/mysql;

//It is possible to use @sensitive annotation with parameters
//of user-defined functions and actions. This will allow, users
//to restrict passing untrusted (tainted) data into a security
//sensitive parameter.
function userDefinedSecureOperation (@sensitive string secureParameter) {

}

type Student {
     string firstname,
};


public function main (string[] args) {
    endpoint mysql:Client customerDatabase {
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "root",
        password: "root",
        poolOptions: {maximumPoolSize:5}
    };

    //Sensitive parameters of operations built-in to Ballerina
    //carry @sensitive annotation, to make sure tainted data
    //cannot be passed into such parameters.
    //
    //For example, the taint checking mechanism will completely
    //prevent SQL injection vulnerabilities by disallowing any
    //tainted data in SQL query.
    //
    //This line will result in a compiler error since query has
    //been appended with a user provided argument.
    table dataTable = check customerDatabase ->
        select("SELECT firstname FROM student WHERE registration_id = " +
               args[0], null, null);

    //This line will result in a compiler error since a user provided
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

    while (dataTable.hasNext()) {
        var jsonData = check < Student > dataTable.getNext();
        //Return values of certain functions and actions built-in to Ballerina
        //will carry @tainted annotation to denote that the return value
        //should be considered as untrusted (tainted) data. One such example
        //is data read from a database.
        //
        //This line will result in a compiler error since a value derived
        //from a database read (tainted), is pass it to a sensitive parameter.
        userDefinedSecureOperation(jsonData.firstname);

        string sanitizedData1 = sanitizeAndReturnTainted(jsonData.firstname);
        //This line will result in a compiler error since the sanitize
        //function returns a value derived from tainted data. Therefore,
        //the return of sanitize function is also tainted.
        userDefinedSecureOperation(sanitizedData1);

        string sanitizedData2 = sanitizeAndReturnUntainted(jsonData.firstname);
        //This line will successfully compile. Even-though the sanitize
        //function returns a value derived from tainted data, the return
        //value is annotated with "@untainted" annotation, denoting that
        //the return value is safe and can be considered as trusted data.
        userDefinedSecureOperation(sanitizedData2);
    }
    var closeStatus = customerDatabase -> close();
    return;
}

function sanitizeAndReturnTainted (string input) returns string {
    Regex reg = {pattern:"[^a-zA-Z]"};
    return check input.replaceAllWithRegex(reg, "");
}

//The "@untainted" annotation denotes that the return value of
//the function should be considered as trusted (untainted) data
//even though the return value is derived from tainted data.
function sanitizeAndReturnUntainted (string input) returns @untainted string {
    Regex reg = {pattern:"[^a-zA-Z]"};
    return check input.replaceAllWithRegex(reg, "");
}

function isInteger (string input) returns boolean {
    Regex reg = {pattern:"\\d+"};
    boolean isInteger = check input.matchesWithRegex(reg);
    return isInteger;
}
