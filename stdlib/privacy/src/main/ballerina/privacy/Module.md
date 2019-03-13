## Module overview

Privacy module acts as the foundation of making Ballerina programming
 language privacy aware. The module provides required utility functions
 that allow pseudonymization, depseudonymization and deletion of
 personally identifiable information (PII).

Pseudonymized identifier will be issued by the `pseudonymize` function
 for a provided PII. The actual PII value will be stored in the selected
 pluggable `PII Store`. The issued identifier can be used to represent
 the particular PII in other locations. The `depseudonymize` function can be
 used to get the actual PII value, give the pseudonymized identifier.
 PII can be deleted from the storage using the `delete` function.

## Samples

```ballerina
import ballerina/h2;
import ballerina/io;
import ballerina/privacy;
import ballerina/sql;

h2:Client testDB = new({
    path: "./H2PIIStore/",
    name: "TestDBH2",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 1 }
});

final string TABLE_NAME = "PII_STORE";
final string ID_CLOUMN = "id";
final string PII_COLUMN = "pii";

public function main(string pii) returns error? {
    // Create database structure in H2 PII store.
    var creationStatus = testDB->update("CREATE TABLE IF NOT EXISTS PII_STORE (ID VARCHAR(300) NOT NULL, PII VARCHAR(300) NOT NULL, PRIMARY KEY (ID))");
    if (creationStatus is sql:UpdateResult) {
        io:println("PII to be persisted: " + pii);

        // Create a storage that uses a H2 database to persist personally identifiable information (PII).
        privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);

        // Store PII information in the storage and take a pseudonymized identifier (UUID) representing stored data.
        var pseudonymizedPii = privacy:pseudonymize(piiStore, pii);
        if (pseudonymizedPii is string) {
            io:println("Pseudonymized identifier: " + pseudonymizedPii);

            // Read the PII by providing the pseudonymized identifier.
            var depseudonymizedPii = privacy:depseudonymize(piiStore, pseudonymizedPii);
            if (depseudonymizedPii is string) {
                io:println("Deseudonymized value: " + depseudonymizedPii);
            } else {
                return depseudonymizedPii;
            }

            // Delete the PII by providing the pseudonymized identifier.
            var deleteStatus = privacy:delete(piiStore, pseudonymizedPii);
            if (deleteStatus is error) {
                return deleteStatus;
            }
        } else {
            return pseudonymizedPii;
        }
    } else {
        return creationStatus;
    }
}
```
