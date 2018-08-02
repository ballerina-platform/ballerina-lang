import ballerina/sql;
import ballerina/jdbc;
import ballerina/io;
import ballerina/http;

endpoint http:NonListener testEP {
    port:9090
};

@http:ServiceConfig { 
    basePath: "/foo" 
}
service<http:Service> MyService bind testEP {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/bar"
    }
    myResource (endpoint caller, http:Request req) {
		endpoint jdbc:Client testDB;
    	json params = check req.getJsonPayload();
    	io:println(params);

	    testDB.init({
	        url: check <string> params.jdbcUrl,
	        username: check <string> params.userName,
	        password: check <string> params.password,
	        poolOptions: { maximumPoolSize: 1 }
	    });

        table dt = check testDB->select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 1", ());
        json result = check <json>dt;

        http:Response res;
        res.setPayload(untaint result);
        caller->respond(res) but { error e => io:println("Error sending response") };
    }
}
