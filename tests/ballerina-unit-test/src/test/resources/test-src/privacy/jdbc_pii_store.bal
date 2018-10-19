import ballerina/jdbc;
import ballerina/privacy;

@final string TABLE_NAME = "PII_STORE";
@final string ID_CLOUMN = "id";
@final string PII_COLUMN = "pii";

function pseudonymizePii (string jdbcUrl, string userName, string password, string pii) returns string|error {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };
    privacy:JdbcPiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:pseudonymize(piiStore, pii);
}

function depseudonymizePii (string jdbcUrl, string userName, string password, string id) returns string|error {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };
    privacy:JdbcPiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:depseudonymize(piiStore, id);
}

function deletePii (string jdbcUrl, string userName, string password, string id) returns error? {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };
    privacy:JdbcPiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:delete(piiStore, id);
}

function pseudonymizePiiWithEmptyTableName (string jdbcUrl, string userName, string password, string pii) returns string|error {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
        poolOptions: { maximumPoolSize: 1 }
    };
    privacy:JdbcPiiStore piiStore = new(testDB, "", ID_CLOUMN, PII_COLUMN);
    return privacy:pseudonymize(piiStore, pii);
}
