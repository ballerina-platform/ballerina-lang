import ballerina/sql;

type ResultCount {
    int COUNTVAL,
};

function getTableCount (string tablePrefix) returns (int) {
    endpoint sql:Client testDB {
        url: "h2:mem:TABLEDB",
        username: "sa",
        poolOptions: {maximumPoolSize:1}
    };

    sql:Parameter  p1 = (sql:TYPE_VARCHAR ,tablePrefix );

    int count;
    try {
        var temp = testDB -> select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME
            like ?", ResultCount, p1);
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
        url: "h2:mem:TABLEDB",
        username: "sa",
        poolOptions: {maximumPoolSize:1}
    };

    int count;
    try {
        var temp = testDB -> select("SELECT count(*) as count FROM information_schema.sessions", ResultCount);
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

