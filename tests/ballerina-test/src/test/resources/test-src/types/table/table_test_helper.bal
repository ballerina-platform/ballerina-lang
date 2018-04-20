import ballerina/sql;
import ballerina/jdbc;

type ResultCount {
    int COUNTVAL,
};

function getTableCount(string tablePrefix) returns (int) {
    endpoint jdbc:Client testDB {
        url:"h2:mem:TABLEDB",
        username:"sa",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter p1 = {sqlType:sql:TYPE_VARCHAR, value:tablePrefix};

    int count;
    try {
        table dt = check  testDB->select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME
            like ?", ResultCount, p1);
        while (dt.hasNext()) {
            ResultCount rs = check <ResultCount>dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        testDB.stop();
    }
    return count;
}

function getSessionCount() returns (int) {

    endpoint jdbc:Client testDB {
        url:"jdbc:h2:mem:TABLEDB",
        username:"sa",
        poolOptions:{maximumPoolSize:1}
    };

    int count;
    try {
        table dt = check testDB->select("SELECT count(*) as count FROM information_schema.sessions", ResultCount);
        while (dt.hasNext()) {
            ResultCount rs = check <ResultCount>dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        testDB.stop();
    }
    return count;
}

