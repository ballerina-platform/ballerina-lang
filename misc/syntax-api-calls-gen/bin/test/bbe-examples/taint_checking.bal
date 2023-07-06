import ballerina/lang.'string as stringlib;
import ballerinax/mysql;

const EXEC_MAGIC_NUMBER = "MZ";

// The `@untainted` annotation can be used with the parameters of user-defined functions. This allow users to restrict
// passing untrusted (tainted) data into a security sensitive parameter.
function userDefinedSecureOperation(@untainted string secureParameter) {

}

type Student record {
    string name;
    string favQuote;
};

public function main(string... args) returns error? {
    mysql:Client dbClient = check new ("localhost", "root", "root",
                                       "testdb", 3306);

    // Security sensitive parameters of Ballerina standard library functions
    // are decorated with the `@untainted` annotation. This ensures that
    // tainted data cannot pass into the security sensitive parameter.
    //
    // For example, the taint-checking mechanism of Ballerina warns the user to prevent
    // SQL injection vulnerabilities by disallowing tainted data in the SQL query.
    //
    // The call to `query` action results in a compile time error because untainted
    // data from `args[0]` is directly used in the SQL query string.
    // Users should rather use raw templates when providing SQL query parameters
    // to make sure SQL injection attacks are not possible. For more information, see [Parameterized Query](https://ballerina.io/swan-lake/learn/by-example/jdbc-parameterized-query.html)
    // support. 
    var result = dbClient->query("SELECT name, favQuote from Student " +
                                 "WHERE registrationId = " + args[0], ());

    // This line results in a compiler error because a user-provided argument is passed to a sensitive parameter.
    userDefinedSecureOperation(args[1]);

    if !stringlib:startsWith(args[1], EXEC_MAGIC_NUMBER) {
        // After performing the necessary validations and/or escaping,
        // use the type-cast expression with the @untainted annotation
        // to mark the proceeding value as `trusted` and pass it to a sensitive parameter.
        userDefinedSecureOperation(<@untainted> args[1]);
    } else {
        return error("Invalid input: executable code detected!");
    }

    var entry = check result.next();
    Student student = <Student> entry?.value;
    // The return values of certain functions in the Ballerina standard library are decorated with the `@tainted` annotation to
    // denote that the return value should be untrusted (tainted). One such example is the data read from a
    // database.
    //
    // This line results in a compile error because a value derived from a database read (tainted) is passed to a
    // sensitive parameter.
    userDefinedSecureOperation(student.favQuote);

    string sanitizedData1 = sanitizeAndReturnTainted(student.favQuote);
    // This line results in a compile error because the `sanitize` function returns a value derived from the tainted
    // data. Therefore, the return value of the `sanitize` function is also tainted.
    userDefinedSecureOperation(sanitizedData1);

    string sanitizedData2 = sanitizeAndReturnUntainted(student.favQuote);
    // This line compiles successfully. Although the `sanitize` function returns a value derived from tainted data,
    // the return value is annotated with the `@untainted` annotation. This means that the return value is safe and can be
    // trusted.
    userDefinedSecureOperation(sanitizedData2);

    check dbClient.close();
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
