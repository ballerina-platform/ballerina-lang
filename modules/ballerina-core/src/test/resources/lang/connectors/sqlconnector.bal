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
    @Path ("/actionInsertData")
    resource passthrough (message m) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./TEST_SERV_SAMP_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root"});

    int insertCount;
    message response;
    json payload;

    insertCount = sql:Connector.update(testDB, "insert into Customers (firstName,lastName,registrationID,creditLimit,country) values ('John', 'Thomas', 2, 5000.75, 'USA');");

    response = new message;
    message:setStringPayload(response,string:valueOf(insertCount));
    reply response;
    }

    @GET
    @Path ("/actionUpdateData")
    resource passthrough (message m) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./TEST_SERV_SAMP_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root"});

    int updateCount;
    message response;
    json payload;

    updateCount = sql:Connector.update(testDB, "Update Customers set country = 'UK' where registrationID = 1");

    response = new message;
    message:setStringPayload(response,string:valueOf(updateCount));
    reply response;
    }

    @GET
    @Path ("/actionInsertDataWithKeys")
    resource passthrough (message m) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./TEST_SERV_SAMP_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root"});

    int insertCount;
    string generatedID;
    message response;
    json payload;
    string[] keyColumns;

    insertCount,generatedID = sql:Connector.updateWithGeneratedKeys(testDB, "insert into Customers (firstName,lastName,registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",keyColumns);

    response = new message;
    message:setStringPayload(response,string:valueOf(generatedID));
    reply response;
    }
	
    @GET
    @Path ("/actionSelectData")
    resource passthrough (message m) {
	sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./TEST_SERV_SAMP_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root"});

    dataframe df;
	message response;
	string firstName;
	
	df = sql:Connector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1");
	while (dataframe:next(df)) {
	    firstName = dataframe:getString(df, 1);
	}

    response = new message;
    message:setStringPayload(response,firstName);
	reply response;
    }
}
