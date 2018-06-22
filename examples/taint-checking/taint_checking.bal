import ballerina/mysql;

// The `@sensitive` annotation can be used with parameters of user-defined functions. This allow users to restrict
// passing untrusted (tainted) data into a security sensitive parameter.
function userDefinedSecureOperation(@sensitive string secureParameter) {

}

type Student record {
    string firstname,
};


function main(string... args) {
    endpoint mysql:Client customerDBEP {
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "root",
        password: "root",
        poolOptions: { maximumPoolSize: 5 }
    };

    // Sensitive parameters of functions built-in to Ballerina are decorated with the `@sensitive` annotation. This
    // ensures that tainted data cannot pass into the security sensitive parameter.
    //
    // For example, the taint checking mechanism of Ballerina completely prevents SQL injection vulnerabilities by
    // disallowing tainted data in the SQL query.
    //
    // This line results in a compiler error because the query is appended with a user-provided argument.
    table dataTable = check customerDBEP->
    select("SELECT firstname FROM student WHERE registration_id = " +
            args[0], null);

    // This line results in a compiler error because a user-provided argument is passed to a sensitive parameter.
    userDefinedSecureOperation(args[0]);

    if (isInteger(args[0])) {
        // After performing necessary validations and/or escaping, the 'untaint' unary expression can be used to mark
        // the proceeding value as 'trusted' and pass it to a sensitive parameter.
        userDefinedSecureOperation(untaint args[0]);
    } else {
        error err = { message: "Validation error: ID should be an integer" };
        throw err;
    }

    while (dataTable.hasNext()) {
        var jsonData = check <Student>dataTable.getNext();
        // The return values of certain functions built-in to Ballerina are decorated with the `@tainted` annotation to
        // denote that the return value should be untrusted (tainted). One such example is the data read from a
        // database.
        //
        // This line results in a compiler error because a value derived from a database read (tainted) is passed to a
        // sensitive parameter.
        userDefinedSecureOperation(jsonData.firstname);

        string sanitizedData1 = sanitizeAndReturnTainted(jsonData.firstname);
        // This line results in a compiler error because the `sanitize` function returns a value derived from tainted
        // data. Therefore, the return of the `sanitize` function is also tainted.
        userDefinedSecureOperation(sanitizedData1);

        string sanitizedData2 = sanitizeAndReturnUntainted(jsonData.firstname);
        // This line successfully compiles. Although the `sanitize` function returns a value derived from tainted data,
        // the return value is annotated with the `@untainted` annotation. This means that the return value is safe and can be
        // trusted.
        userDefinedSecureOperation(sanitizedData2);
    }
    customerDBEP.stop();
    return;
}

function sanitizeAndReturnTainted(string input) returns string {
    string regEx = "[^a-zA-Z]";
    return input.replace(regEx, "");
}

// The `@untainted` annotation denotes that the return value of the function should be trusted (untainted) even though
// the return value is derived from tainted data.
function sanitizeAndReturnUntainted(string input) returns @untainted string {
    string regEx = "[^a-zA-Z]";
    return input.replace(regEx, "");
}

function isInteger(string input) returns boolean {
    string regEx = "\\d+";
    boolean isInt = check input.matches(regEx);
    return isInt;
}
