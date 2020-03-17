import ballerina/java.jdbc;

// The `@untainted` annotation can be used with parameters of user-defined functions. This allow users to restrict
// passing untrusted (tainted) data into a security sensitive parameter.
function userDefinedSecureOperation(@untainted string secureParameter) {

}

public function main(string... args) {
    jdbc:Client customerDBEP = new ({
        url: "jdbc:mysql://localhost:3306/testdb",
        username: "root",
        password: "root",
        poolOptions: { maximumPoolSize: 5 }
    });

   // Untainted parameters of functions built-in to Ballerina are decorated with the `@untainted` annotation. This
   // ensures that tainted data cannot pass into the security sensitive parameter.
   //
   // For example, the taint checking mechanism of Ballerina completely prevents SQL injection vulnerabilities by
   // disallowing tainted data in the SQL query.
   //
   // This line results in a compiler error because the query is appended with a user-provided argument.
   var result = customerDBEP->
   select("SELECT firstname FROM student WHERE registration_id = " +
         args[0], ());

   // This line results in a compiler error because a user-provided argument is passed to a untainted parameter.
   userDefinedSecureOperation(args[0]);
   return;
}