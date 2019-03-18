## Module overview

The privacy module acts as the foundation in making the Ballerina a privacy-aware programming
 language. The module provides the required utility functions
 that allow pseudonymization, de-pseudonymization, and deletion of
 Personally Identifiable Information (PII).

A pseudonymized identifier will be issued by the `pseudonymize` function
 for a provided PII. The actual PII value will be stored in the selected
 pluggable `PII Store`. The issued identifier can be used to represent
 the particular PII in other locations. The `depseudonymize` function
 can be used to obtain the actual PII value and give the pseudonymized
`` 
 identifier. PII can be deleted from the store using the `delete`
 function.

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
    // Creates the database structure in the H2 PII store.
    var creationStatus = testDB->update("CREATE TABLE IF NOT EXISTS PII_STORE (ID VARCHAR(300) NOT NULL, PII VARCHAR(300) NOT NULL, PRIMARY KEY (ID))");
    if (creationStatus is sql:UpdateResult) {
        io:println("PII to be persisted: " + pii);

        // Creates a storage that uses an H2 database to persist Personally Identifiable Information (PII).
        privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);

        // Store PII information in the storage and take a pseudonymized identifier (UUID) representing the stored data.
        var pseudonymizedPii = privacy:pseudonymize(piiStore, pii);
        if (pseudonymizedPii is string) {
            io:println("Pseudonymized identifier: " + pseudonymizedPii);

            // Reads the PII by providing the pseudonymized identifier.
            var depseudonymizedPii = privacy:depseudonymize(piiStore, pseudonymizedPii);
            if (depseudonymizedPii is string) {
                io:println("Deseudonymized value: " + depseudonymizedPii);
            } else {
                return depseudonymizedPii;
            }

            // Deletes the PII by providing the pseudonymized identifier.
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
