import ballerina/io;

endpoint mysql:Client testDB {
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { useSSL: false }
};

public function main(string... args) {
    DatabasePIIStore piiStore = new(testDB, "table-name", "id-column-name", "pii-column-name");
    var pseudoResult = pseudonymize(piiStore, "Private Data");
    match pseudoResult {
        string id => {
            io:println("Pseudo ID: " + id);
        }
        error e => {
            throw e;
        }
    }
}
