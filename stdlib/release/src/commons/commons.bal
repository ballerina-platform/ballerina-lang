import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/stringutils;

public function getModuleJsonArray() returns json[] {
    var result = readFileAndGetJson(CONFIG_FILE_PATH);
    if (result is error) {
        logAndPanicError("Error occurred while reading the config file", result);
    }
    json jsonFile = <json>result;
    return <json[]>jsonFile.modules;
}

public function getModuleArrayFromJson(json[] modulesJson) returns Module[] {
    Module[] modules = [];
    foreach json moduleJson in modulesJson {
        Module|error result = Module.constructFrom(moduleJson);
        if (result is error) {
            logAndPanicError("Error building the module record", result);
        }
        Module module = <Module>result;
        modules.push(module);
    }
    return modules;
}

function readFileAndGetJson(string path) returns json|error {
    io:ReadableByteChannel rbc = check <@untainted>io:openReadableFile(path);
    io:ReadableCharacterChannel rch = new (rbc, "UTF8");
    var result = <@untainted>rch.readJson();
    closeReadChannel(rch);
    return result;
}

public function printModules(Module[] modules) {
    string[] moduleStrings = modules.map(function (Module m) returns string {
        return m.name + " " + m.'version;
    });
    foreach string moduleString in moduleStrings {
        log:printInfo(moduleString);
    }
}

function closeReadChannel(io:ReadableCharacterChannel rc) {
    var result = rc.close();
    if (result is error) {
        log:printError("Error occurred while closing character stream", result);
    }
}

public function logAndPanicError(string message, error e) {
    log:printError(message, e);
    panic e;
}

public function removeSnapshots(Module[] modules) returns Module[] {
    foreach Module module in modules {
        string moduleVersion = module.'version;
        if (stringutils:contains(moduleVersion, SNAPSHOT)) {
            module.'version = stringutils:replace(moduleVersion, SNAPSHOT, "");
        }
    }
    return modules;
}

public function createRequest(string accessTokenHeaderValue) returns http:Request {
    http:Request request = new;
    request.setHeader(ACCEPT_HEADER_KEY, ACCEPT_HEADER_VALUE);
    request.setHeader(AUTH_HEADER_KEY, accessTokenHeaderValue);
    return request;
}

public function validateResponse(http:Response response, string moduleName) returns boolean {
    int statusCode = response.statusCode;
    if (statusCode != 200 && statusCode != 201 && statusCode != 202 && statusCode != 204) {
        return false;
    }
    return true;
}
