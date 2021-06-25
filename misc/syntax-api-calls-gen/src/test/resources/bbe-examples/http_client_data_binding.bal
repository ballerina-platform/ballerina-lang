import ballerina/http;
import ballerina/log;

type MapJson map<json>;
type Person record {|
    string name;
    int age;
|};

http:Client backendClient = new("http://localhost:9092");

service call on new http:Listener(9090) {

    @http:ResourceConfig {
        path: "/all"
    }
    resource function bindCheck(http:Caller caller, http:Request request)
                                                    returns @tainted error? {
        // The `string` typedesc is being passed to the `get` client remote function as the `targetType`
        // expecting the payload to be bound to a string value.
        var result = backendClient->
                        get("/backend/getString", targetType = string);

        // The return type of the client remote action is the union of `string`|`xml`|`json`|`map<json>`|`byte[]`|
        // `record`|`record[]`|`http:Response` or `http:ClientError`.
        if (result is error) {
            log:printError("Error: " + result.message());
            return result;
        }

        // If the type test of the error becomes false, it implies that the payload type is string.
        log:printInfo("String payload: " + <string>result);

        // Binding the payload to a JSON type. If an error returned, it will be responded back to the caller.
        json jsonPayload = <json> check backendClient->
                                    post("/backend/getJson", "foo", json);

        log:printInfo("Json payload: " + jsonPayload.toJsonString());

        // Since only the `named types` or `built-in types` can be passed as function parameters,  `Map` or `Array`
        // types have to be defined beforehand. Here, the type `MapJson` is passed instead of `map<json>`.
        // Same can be done for `byte[]` or `Person[]` as well.
        map<json> value = <map<json>> check backendClient->
                                post("/backend/getJson", "foo", MapJson);
        log:printInfo(check value.id);

        // A `record` and `record[]` are also possible types for data binding.
        Person person = <Person> check backendClient->
                            get("/backend/getPerson", targetType = Person);
        log:printInfo("Person name: " + person.name);

        // When the complete response is expected, the default value of the `targetType` will be applied.
        http:Response res =  <http:Response> check backendClient->
                                            get("/backend/getResponse");
        check caller->respond(<@untainted>res);
    }

    @http:ResourceConfig {
        path: "/5xx"
    }
    resource function handle500(http:Caller caller, http:Request request) {
        var res = backendClient->post("/backend/get5XX", "want 500", json);
        // When the data binding is expected to happen and if the `post` remote function gets a 5XX response from the
        // backend, the response will be returned as an `http:RemoteServerError` including the error message and
        // status code.
        if (res is http:RemoteServerError) {
            http:Response resp = new;
            resp.statusCode = res.detail()?.statusCode ?: 500;
            resp.setPayload(<@untainted>res.message());
            var responseToCaller = caller->respond(<@untainted>resp);
        } else {
            var responseToCaller = caller->respond(<@untainted json>res);
        }
    }

    @http:ResourceConfig {
        path: "/4xx"
    }
    resource function handle404(http:Caller caller, http:Request request) {
        // When the data binding is expected to happen and if the client remote function gets a 4XX response from the
        // backend, the response will be returned as an `http:ClientRequestError` including the error message and
        // status code.
        var res = backendClient->post("/backend/err", "want 400", json);
        if (res is http:ClientRequestError) {
            http:Response resp = new;
            resp.statusCode = res.detail()?.statusCode ?: 400;
            resp.setPayload(<@untainted>res.message());
            var responseToCaller = caller->respond(<@untainted>resp);
        } else {
            var responseToCaller = caller->respond(<@untainted json>res);
        }
    }
}

@http:ServiceConfig {
    basePath: "/backend"
}
service mockService on new http:Listener(9092) {

    resource function getString(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setTextPayload("Hello ballerina!!!!");
        checkpanic caller->respond(response);
    }

    resource function getJson(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setJsonPayload({id: "Ballerina", values: {a: {x: "b"}}});
        checkpanic caller->respond(response);
    }

    resource function getPerson(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setJsonPayload({name: "Smith", age: 15});
        checkpanic caller->respond(response);
    }

    resource function getResponse(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setJsonPayload({id: "data-binding-done"});
        response.setHeader("x-fact", "backend-payload");
        checkpanic caller->respond(response);
    }

    resource function get5XX(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.statusCode = 501;
        response.setTextPayload("data-binding-failed-with-501");
        checkpanic caller->respond(response);
    }
}
