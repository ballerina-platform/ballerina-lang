import ballerina.data.sql;

struct ResultCount {
    int COUNTVAL;
}

function getTableCount (string tablePrefix) (int count) {

    endpoint<sql:ClientEndpoint> testDBEP {
        database: sql:DB.H2_MEM,
        host: "",
        port: 0,
        name: "TABLEDB",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1}
    }
    var testDB = testDBEP.getConnector();

    sql:Parameter  p1 = {value:tablePrefix, sqlType:sql:Type.VARCHAR};
    sql:Parameter[] parameters = [p1];

    try {
        table dt = testDB -> select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME like ?",
                                 parameters, typeof ResultCount);
        if (dt.hasNext()) {
            var rs, _ = (ResultCount) dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        testDB -> close();
    }
    return;
}

function getSessionCount () (int count) {

    endpoint<sql:ClientEndpoint> testDBEP {
        database: sql:DB.H2_MEM,
        host: "",
        port: 0,
        name: "TABLEDB",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1}
    }
    var testDB = testDBEP.getConnector();

    try {
        table dt = testDB -> select("SELECT count(*) as count FROM information_schema.sessions",
                                 null, typeof ResultCount);
        if (dt.hasNext()) {
            var rs, _ = (ResultCount) dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        testDB -> close();
    }
    return;
}

