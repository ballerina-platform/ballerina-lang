import ballerina/h2;
import ballerina/privacy;

endpoint h2:Client testDB {
    path: "./target/H2PIIStore/",
    name: "TestDBH2",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 1 }
};

@final string TABLE_NAME = "PII_STORE";
@final string ID_CLOUMN = "id";
@final string PII_COLUMN = "pii";

function pseudonymizePii (string pii) returns string|error {
    privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:pseudonymize(piiStore, pii);
}

function depseudonymizePii (string id) returns string|error {
    privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:depseudonymize(piiStore, id);
}

function deletePii (string id) returns error? {
    privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:delete(piiStore, id);
}

function pseudonymizePiiWithInvalidFieldName (string pii) returns string|error {
    privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME + "`" + TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:pseudonymize(piiStore, pii);
}
