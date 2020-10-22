import ballerina/mime;
import ballerina/http;
import ballerina/io;

listener http:MockListener mockEP = new(9090);

@http:ServiceConfig {basePath:"/test"}
service echo on mockEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/largepayload"
    }
    resource function getPayloadFromFileChannel(http:Caller caller, http:Request request) {
        http:Response response = new;
        mime:Entity responseEntity = new;

        var result = request.getByteChannel();
        if (result is io:ReadableByteChannel) {
            responseEntity.setByteChannel(result);
        } else {
            io:print("Error in getting byte channel");
        }

        response.setEntity(responseEntity);
        checkpanic caller->respond(response);
    }

    resource function getPayloadFromEntity(http:Caller caller, http:Request request) {
        http:Response res = new;
        var entity = request.getEntity();
        if (entity is mime:Entity) {
            json|error jsonPayload = entity.getJson();
            if (jsonPayload is json) {
                mime:Entity ent = new;
                ent.setJson(<@untainted>{"payload" : jsonPayload, "header" : entity.getHeader("Content-type")});
                res.setEntity(ent);
                checkpanic caller->ok(res);
            } else {
                checkpanic caller->internalServerError("Error while retrieving from entity");
            }
        } else {
            checkpanic caller->internalServerError({ message: "Error while retrieving from request" });
        }
    }
}

function testHeaderWithRequest() returns @tainted string {
    mime:Entity entity = new;
    entity.setHeader("123Authorization", "123Basicxxxxxx");

    http:Request request = new;
    request.setEntity(entity);
    return (request.getHeader("123Authorization"));
}

function testPayloadInEntityOfRequest() returns @tainted json {
    mime:Entity entity = new;
    entity.setJson({"payload": "PayloadInEntityOfRequest"});

    http:Request request = new;
    request.setEntity(entity);
    var payload = request.getJsonPayload();
    if payload is json {
        return (payload);
    } else {
        json errorJson = {"payload":"error"};
        return errorJson;
    }
}

function testPayloadInRequest() returns @tainted json {
    http:Request request = new;
    request.setJsonPayload({"payload": "PayloadInTheRequest"});

    var entity = request.getEntity();
    if (entity is mime:Entity) {
        var payload = entity.getJson();
        if payload is json {
            return payload;
        } else {
            return {"error":payload.toString()};
        }
    } else {
        return {"error":entity.toString()};
    }
}

function testHeaderWithResponse() returns @tainted string {
    mime:Entity entity = new;
    entity.setHeader("123Authorization", "123Basicxxxxxx");

    http:Response response = new;
    response.setEntity(entity);
    return (response.getHeader("123Authorization"));
}

function testPayloadInEntityOfResponse() returns @tainted json {
    mime:Entity entity = new;
    entity.setJson({"payload": "PayloadInEntityOfResponse"});

    http:Response response = new;
    response.setEntity(entity);
    var payload = response.getJsonPayload();
    if payload is json {
        return payload;
    } else {
        return {"error":payload.toString()};
    }
}

function testPayloadInResponse() returns @tainted json {
    http:Response response = new;
    response.setJsonPayload({"payload": "PayloadInTheResponse"});

    var entity = response.getEntity();
    if (entity is mime:Entity) {
        var payload = entity.getJson();
        if payload is json {
            return payload;
        } else {
            return {"error":payload.toString()};
        }
    } else {
        return {"error":entity.toString()};
    }
}
