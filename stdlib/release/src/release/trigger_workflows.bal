import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/runtime;

http:Client httpClient = new (API_PATH);
string accessToken = config:getAsString(ACCESS_TOKEN_ENV);
string accessTokenHeaderValue = "Bearer " + accessToken;

public function main() {
    json[] modulesJson = getModuleJsonArray();
    Module[] modules = getModuleArray(modulesJson);
    handleRelease(modules);
}

function getModuleJsonArray() returns json[] {
    var result = readFileAndGetJson(CONFIG_FILE_PATH);
    if (result is error) {
        logAndPanicError("Error occurred while reading the config file", result);
    }
    json jsonFile = <json>result;
    return <json[]>jsonFile.modules;
}

function getModuleArray(json[] modulesJson) returns Module[] {
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

function handleRelease(Module[] modules) {
    int currentLevel = -1;
    Module[] currentModules = [];
    foreach Module module in modules {
        int nextLevel = module.level;
        if (nextLevel > currentLevel && currentModules.length() > 0) {
            waitForCurrentModuleReleases(currentModules);
            currentModules.removeAll();
        }
        if (module.release) {
            boolean releaseStarted = releaseModule(module);
            if (releaseStarted) {
                module.releaseStarted = releaseStarted;
                currentModules.push(module);
                log:printInfo("Module " + module.name + " release triggerred successfully.");
            } else {
                log:printWarn("Module " + module.name + " release did not triggerred successfully.");
            }
        }
        currentLevel = nextLevel;
    }
    waitForCurrentModuleReleases(currentModules);
}

function releaseModule(Module module) returns boolean {
    log:printInfo("------------------------------");
    log:printInfo("Releasing " + module.name + " Version " + module.'version);
    http:Request request = createRequest(accessTokenHeaderValue);
    string moduleName = module.name.toString();
    string 'version = module.'version.toString();
    string apiPath = "/" + moduleName + DISPATCHES;

    json payload = {
        event_type: EVENT_TYPE,
        client_payload: {
            'version: 'version
        }
    };
    request.setJsonPayload(payload);
    var result = httpClient->post(apiPath, request);
    if (result is error) {
        logAndPanicError("Error occurred while releasing the module: " + moduleName, result);
    }
    http:Response response = <http:Response>result;
    return validateResponse(response, moduleName);
}

function waitForCurrentModuleReleases(Module[] modules) {
    if (modules.length() == 0) {
        return;
    }
    log:printInfo("Waiting for previous level builds");
    Module[] unreleasedModules = modules;
    Module[] releasedModules = [];

    boolean allModulesReleased = false;
    int waitCycles = 0;
    while (!allModulesReleased) {
        foreach Module module in modules {
            boolean releaseCompleted = checkModuleRelease(module);
            if (releaseCompleted) {
                int moduleIndex = <int>unreleasedModules.indexOf(module);
                Module releasedModule = unreleasedModules.remove(moduleIndex);
                releasedModules.push(releasedModule);
                log:printInfo(releasedModule.name + " " + releasedModule.'version + " is released");
            }
        }
        if (releasedModules.length() == modules.length()) {
            allModulesReleased = true;
        } else if (waitCycles < MAX_WAIT_CYCLES) {
            runtime:sleep(SLEEP_INTERVAL);
            waitCycles += 1;
        } else {
            break;
        }
    }
    if (unreleasedModules.length() > 0) {
        log:printWarn("Following modules not released after the max wait time");
        printModules(unreleasedModules);
        error err = error("Unreleased", message = "There are modules not released after max wait time");
        logAndPanicError("Release Failed.", err);
    }
}

function checkModuleRelease(Module module) returns boolean {
    if (!module.releaseStarted) {
        return true;
    }
    log:printInfo("Validating " + module.name + " release");
    http:Request request = createRequest(accessTokenHeaderValue);
    string moduleName = module.name.toString();
    string 'version = module.'version.toString();
    string expectedReleaseTag = "v" + 'version;

    string modulePath = "/" + moduleName + RELEASES + TAGS + "/v" + 'version;
    var result = httpClient->get(modulePath, request);
    if (result is error) {
        logAndPanicError("Error occurred while checking the release status for module: " + moduleName, result);
    }
    http:Response response = <http:Response>result;

    if(!validateResponse(response, moduleName)) {
        return false;
    } else {
        map<json> payload = <map<json>>response.getJsonPayload();
        string releaseTag = payload[FIELD_TAG_NAME].toString();
        return <@untainted>releaseTag == expectedReleaseTag;
    }
}

function validateResponse(http:Response response, string moduleName) returns boolean {
    int statusCode = response.statusCode;
    if (statusCode != 200 && statusCode != 201 && statusCode != 202 && statusCode != 204) {
        return false;
    }
    return true;
}

function createRequest(string accessTokenHeaderValue) returns http:Request {
    http:Request request = new;
    request.setHeader(ACCEPT_HEADER_KEY, ACCEPT_HEADER_VALUE);
    request.setHeader(AUTH_HEADER_KEY, accessTokenHeaderValue);
    return request;
}

function readFileAndGetJson(string path) returns json|error {
    io:ReadableByteChannel rbc = check <@untainted>io:openReadableFile(path);
    io:ReadableCharacterChannel rch = new (rbc, "UTF8");
    var result = <@untainted>rch.readJson();
    closeReadChannel(rch);
    return result;
}

function closeReadChannel(io:ReadableCharacterChannel rc) {
    var result = rc.close();
    if (result is error) {
        log:printError("Error occurred while closing character stream", result);
    }
}

function printModules(Module[] modules) {
    string[] moduleStrings = modules.map(function (Module m) returns string {
        return m.name + " " + m.'version;
    });
    foreach string moduleString in moduleStrings {
        log:printInfo(moduleString);
    }
}

function logAndPanicError(string message, error e) {
    log:printError(message, e);
    panic e;
}
