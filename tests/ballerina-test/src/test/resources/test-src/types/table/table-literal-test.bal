import ballerina.data.sql;

struct ResultCount {
    int COUNTVAL;
}

function getTableCount (string tablePrefix) (int count) {

    endpoint<sql:ClientConnector> testDB {}
    sql:ConnectionProperties Properties = {url:"jdbc:h2:mem:TABLEDB"};
    bind create sql:ClientConnector(sql:DB.GENERIC, "", 0, "", "sa", "", Properties) with testDB;

    sql:Parameter  p1 = {value:tablePrefix, sqlType:sql:Type.VARCHAR};
    sql:Parameter[] parameters = [p1];

    try {
        table dt = testDB.select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME like ?",
                                 parameters, typeof ResultCount);
        if (dt.hasNext()) {
            var rs, err = (ResultCount) dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        testDB.close();
    }
    return;
}
