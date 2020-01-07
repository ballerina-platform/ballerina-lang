import ballerina/lang.'int as ints;
import ballerinax/java.jdbc;

// The `@untainted` annotation can be used with the parameters of user-defined functions. This allow users to restrict
// passing untrusted (tainted) data into a security sensitive parameter.
function userDefinedSecureOperation(@untainted string secureParameter) {

}

type Student record {
    string firstname;
};


public function main(string... args) {
    jdbc:Client customerDBEP = new ({
        url: "jdbc:mysql://localhost:3306/testdb",
        username: "root",
        password: "root",
        poolOptions: { maximumPoolSize: 5 },
        dbOptions: { useSSL: false }
    });

    // Sensitive parameters of functions that are built-in to Ballerina are decorated with the `@untainted` annotation.
    // This ensures that tainted data cannot pass into the security sensitive parameter.
    //
    // For example, the taint checking mechanism of Ballerina completely prevents SQL injection vulnerabilities by
    // disallowing tainted data in the SQL query.
    //
    // This line results in a compile error because the query is appended with a user-provided argument.
    var result = customerDBEP->
    select("SELECT firstname FROM student WHERE registration_id = " +
            args[0], ());
    table<record { string firstname; }> dataTable;
    if (result is error) {
        error e = <error> result;
        panic e;
    } else {
        dataTable = result;
    }

    // This line results in a compiler error because a user-provided argument is passed to a sensitive parameter.
    userDefinedSecureOperation(args[0]);

    if (isInteger(args[0])) {
        // After performing necessary validations and/or escaping, we can use type cast expression with @untainted annotation
        // to mark the proceeding value as `trusted` and pass it to a sensitive parameter.
        userDefinedSecureOperation(<@untainted> args[0]);
    } else {
        error err = error("Validation error: ID should be an integer");
        panic err;
    }

    while (dataTable.hasNext()) {
        Student jsonData = dataTable.getNext();
        // The return values of certain functions built-in to Ballerina are decorated with the `@tainted` annotation to
        // denote that the return value should be untrusted (tainted). One such example is the data read from a
        // database.
        //
        // This line results in a compile error because a value derived from a database read (tainted) is passed to a
        // sensitive parameter.
        userDefinedSecureOperation(jsonData.firstname);

        string sanitizedData1 = sanitizeAndReturnTainted(jsonData.firstname);
        // This line results in a compile error because the `sanitize` function returns a value derived from the tainted
        // data. Therefore, the return of the `sanitize` function is also tainted.
        userDefinedSecureOperation(sanitizedData1);

        string sanitizedData2 = sanitizeAndReturnUntainted(jsonData.firstname);
        // This line successfully compiles. Although the `sanitize` function returns a value derived from tainted data,
        // the return value is annotated with the `@untainted` annotation. This means that the return value is safe and can be
        // trusted.
        userDefinedSecureOperation(sanitizedData2);
    }
    checkpanic customerDBEP.stop();
    return;
}

function sanitizeAndReturnTainted(string input) returns string {
    // transform and sanitize the string here.
    return input;
}

// The `@untainted` annotation denotes that the return value of the function should be trusted (untainted) even though
// the return value is derived from tainted data.
function sanitizeAndReturnUntainted(string input) returns @untainted string {
    // transform and sanitize the string here.
    return input;
}

function isInteger(string input) returns boolean {
    var intVal = ints:fromString(input);
    if (intVal is error) {
        return false;
    } else {
        return true;
    }
}
