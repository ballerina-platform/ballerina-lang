import ballerina/config;
import ballerina/http;
import ballerina/log;
import ballerina/runtime;
import ballerina_stdlib/commons;

http:Client httpClient = new (commons:API_PATH);
string accessToken = config:getAsString(commons:ACCESS_TOKEN_ENV);
string accessTokenHeaderValue = "Bearer " + accessToken;

public function main() {
    json[] modulesJson = commons:getModuleJsonArray();
    commons:Module[] modules = commons:getModuleArrayFromJson(modulesJson);
    modules = commons:removeSnapshots(modules);
    handleRelease(modules);
}

function handleRelease(commons:Module[] modules) {
    int currentLevel = -1;
    commons:Module[] currentModules = [];
    foreach commons:Module module in modules {
        int nextLevel = module.level;
        if (nextLevel > currentLevel && currentModules.length() > 0) {
            waitForCurrentModuleReleases(currentModules);
            currentModules.removeAll();
            currentLevel = nextLevel;
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
    }
    waitForCurrentModuleReleases(currentModules);
}

function releaseModule(commons:Module module) returns boolean {
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
        commons:logAndPanicError("Error occurred while releasing the module: " + moduleName, result);
    }
    http:Response response = <http:Response>result;
    return validateResponse(response, moduleName);
}

function waitForCurrentModuleReleases(commons:Module[] modules) {
    if (modules.length() == 0) {
        return;
    }
    log:printInfo("Waiting for previous level builds");
    commons:Module[] unreleasedModules = modules.filter(function (commons:Module m) returns boolean {
        return m.releaseStarted;
    });

    boolean allModulesReleased = false;
    int waitCycles = 0;
    while (!allModulesReleased) {
        foreach commons:Module module in unreleasedModules {
            boolean releaseCompleted = checkModuleRelease(module);
            if (releaseCompleted) {
                int moduleIndex = <int>unreleasedModules.indexOf(module);
                commons:Module releasedModule = unreleasedModules.remove(moduleIndex);
                log:printInfo(releasedModule.name + " " + releasedModule.'version + " is released");
            }
        }
        if (unreleasedModules.length() == 0) {
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
        commons:printModules(unreleasedModules);
        error err = error("Unreleased", message = "There are modules not released after max wait time");
        commons:logAndPanicError("Release Failed.", err);
    }
}

function checkModuleRelease(commons:Module module) returns boolean {
    log:printInfo("Validating " + module.name + " release");
    http:Request request = createRequest(accessTokenHeaderValue);
    string moduleName = module.name.toString();
    string 'version = module.'version.toString();
    string expectedReleaseTag = "v" + 'version;

    string modulePath = "/" + moduleName + RELEASES + TAGS + "/v" + 'version;
    var result = httpClient->get(modulePath, request);
    if (result is error) {
        commons:logAndPanicError("Error occurred while checking the release status for module: " + moduleName, result);
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
