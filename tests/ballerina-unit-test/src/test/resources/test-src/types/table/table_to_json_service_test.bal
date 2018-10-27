// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

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
        path: "/bar1"
    }
    myResource1 (endpoint caller, http:Request req) {
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

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/bar2"
    }
    myResource2 (endpoint caller, http:Request req) {
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
        json j = { status: "SUCCESS", resp: { value: result } };

        http:Response res;
        res.setPayload(untaint j);
        caller->respond(res) but { error e => io:println("Error sending response") };
    }
}
