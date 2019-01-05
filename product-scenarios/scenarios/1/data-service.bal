import ballerina/http;
import ballerina/log;
import ballerina/mysql;
import ballerina/io;
import ballerina/config;
import ballerinax/kubernetes;

mysql:Client testDB = new({
        host: config:getAsString("DATABASE_HOST", default = "localhost"),
        port: config:getAsInt("DATABASE_PORT", default = 3306),
        name: config:getAsString("DATABASE_NAME", default = "EMPLOYEE_RECORDS"),
        username: config:getAsString("DATABASE_USERNAME", default = "root"),
        password: config:getAsString("DATABASE_PASSWORD", default = ""),
        poolOptions: { maximumPoolSize: 5 },
        dbOptions: { useSSL: false }
    });

type Employee record {
    int id;
    string name;
};

@kubernetes:Ingress {
    hostname: "ballerina.scenariotests",
    name: "ballerina-data-service",
    path: "/"
}

@kubernetes:Service {
    serviceType:"LoadBalancer",
    port:80,
    name: "ballerina-data-service"
}

@kubernetes:Deployment {
    image: "ballerina.scenariotests/employee_database_service:v1.0",
    baseImage: "ballerina/ballerina:0.990.2",
    name: "ballerina-employee-database-service",
    copyFiles: [{ target: "/ballerina/runtime/bre/lib",
                source: "/home/manurip/Documents/Work/Tasks/Support/Tools/mysql-connector-java-5.1.46.jar" }]
}
listener http:Listener httpListener = new(9090);

// By default, Ballerina assumes that the service is to be exposed via HTTP/1.1.
service hello on httpListener {

    resource function sayHello(http:Caller caller, http:Request req) {
        // Send a response back to the caller.
        var result = caller->respond("Hello, World!");
        // Log the `error` in case of a failure.
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
    // curl -v http://localhost:9090/hello/select
    resource function select(http:Caller caller, http:Request req) {
        http:Response res = new;

        var dt = testDB->select("select * from selecttest where id = 1", Employee);

        if (dt is table<Employee>) {
            var employees = json.convert(dt);
            if (employees is json) {
                res.setPayload(untaint employees);
                var ret = caller->respond(res);
                if (ret is error) {
                    log:printError("Error sending response", err = ret);
                }
            } else {
                log:printError("Error converting table to json", err = employees);
                panic employees;
            }
        } else if (dt is error) {
            panic dt;
        }
    }

    // curl -v http://localhost:9090/hello/insert -d'{"id":4, "name":"C"}' -H'Content-Type:application/json'
    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function insert(http:Caller caller, http:Request req) {
        http:Response res = new;
        var employee = req.getJsonPayload();

        if (employee is json) {
            var x = testDB->update("insert into inserttest values(?, ?)", employee.id.toString(), employee.name.
                toString());

            if (x is int) {
                var ret = caller->respond(res);
                if (ret is error) {
                    log:printError("Error sending response", err = ret);
                }
            } else {
                panic x;
            }
        } else {
            panic employee;
        }
    }
}
