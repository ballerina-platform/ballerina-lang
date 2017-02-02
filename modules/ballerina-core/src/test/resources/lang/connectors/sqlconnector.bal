import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.lang.dataframe;
import ballerina.data.sql;

@BasePath ("/invoke")
service SQLConnectorTestService {

    @GET
    @Path ("/actionCreateTable")
    resource passthrough (message m) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./TEST_SERV_SAMP_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root"});

    int returnValue;
    message response;
    json payload;

    returnValue = sql:Connector.update(testDB, "CREATE TABLE IF NOT EXISTS Students(studentID int,LastName varchar(255))");

    response = new message;
    message:setStringPayload(response,string:valueOf(returnValue));
    reply response;
    }
	
    @GET
    @Path ("/actionSelectData")
    resource passthrough (message m) {
	sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./TEST_SERV_SAMP_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root"});

    dataframe df;
	message response;
	json payload;
	
	df = sql:Connector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1");
	payload=dataframe:toJson(df);

    response = new message;
    message:setJsonPayload(response,payload);
	reply response;
    }
}
