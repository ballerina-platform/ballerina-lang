import ballerina.net.http as x;
import ballerina.data.sql as x;

function testFunc() {
    x:HttpClient httpConnector = create x:HttpClient("https://postman-echo.com", {});
    x:ClientConnector testDB = create x:ClientConnector(x:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
}
