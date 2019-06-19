import ballerina/mysql;

// The `@sensitive` annotation can be used with parameters of user-defined functions. This allow users to restrict
// passing untrusted (tainted) data into a security sensitive parameter.
function userDefinedSecureOperation(@sensitive string secureParameter) {

}

public function main(string... args) {
   mysql:Client customerDBEP = new ({
         host: "localhost",
         port: 3306,
         name: "testdb",
         username: "root",
         password: "root",
         poolOptions: { maximumPoolSize: 5 }
      });

   // Sensitive parameters of functions built-in to Ballerina are decorated with the `@sensitive` annotation. This
   // ensures that tainted data cannot pass into the security sensitive parameter.
   //
   // For example, the taint checking mechanism of Ballerina completely prevents SQL injection vulnerabilities by
   // disallowing tainted data in the SQL query.
   //
   // This line results in a compiler error because the query is appended with a user-provided argument.
   var result = customerDBEP->
   select("SELECT firstname FROM student WHERE registration_id = " +
         args[0], ());

   // This line results in a compiler error because a user-provided argument is passed to a sensitive parameter.
   userDefinedSecureOperation(args[0]);
   return;
}