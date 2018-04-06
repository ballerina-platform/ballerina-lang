import ballerina/sql;

type ResultCount {
    int COUNTVAL,
};

function getTableCount (string tablePrefix) returns (int) {
    endpoint sql:Client testDB {
        database: sql:DB_H2_MEM,
        host: "",
        port: 0,
        name: "TABLEDB",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1}
    };

    sql:Parameter  p1 = {value:tablePrefix, sqlType:sql:TYPE_VARCHAR};
    sql:Parameter[] parameters = [p1];

    int count;
    try {
        var temp = testDB -> select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME
            like ?", parameters, typeof ResultCount);
        table dt = check temp;
        while (dt.hasNext()) {
            ResultCount rs = check <ResultCount> dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        _ = testDB -> close();
    }
    return count;
}

function getSessionCount () returns (int) {

    endpoint sql:Client testDB {
        database: sql:DB_H2_MEM,
        host: "",
        port: 0,
        name: "TABLEDB",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1}
    };

    int count;
    try {
        var temp = testDB -> select("SELECT count(*) as count FROM information_schema.sessions",
            (), typeof ResultCount);
        table dt = check temp;
        while (dt.hasNext()) {
            ResultCount rs = check <ResultCount> dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        _ = testDB -> close();
    }
    return count;
}

