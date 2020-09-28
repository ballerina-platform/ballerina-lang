import ballerina/config;
import ballerina/http;
import ballerina/log;
import ballerina/runtime;
import ballerina_stdlib/commons;

http:Client httpClient = new (commons:API_PATH);
string accessToken = config:getAsString(commons:ACCESS_TOKEN_ENV);
string accessTokenHeaderValue = "Bearer " + accessToken;

public function main() {
    string eventType = config:getAsString(CONFIG_EVENT_TYPE);
    json[] modulesJson = commons:getModuleJsonArray();
    commons:Module[] modules = commons:getModuleArray(modulesJson);
    modules = commons:sortModules(modules);
    if (eventType == EVENT_TYPE_MODULE_PUSH) {

    }
    handlePublish(modules);
}

function handlePublish(commons:Module[] modules) {
    int currentLevel = -1;
    foreach commons:Module module in modules {
        int nextLevel = module.level;
        // Don't wait on level 0 modules since no module depends on them
        if (nextLevel > currentLevel && currentLevel > 0) {
            waitForModuleBuild(currentLevel);
        }
        boolean publishStarted = publishModule(module);
        if (publishStarted) {
            log:printInfo("Module " + module.name + " publish triggerred successfully.");
        } else {
            log:printWarn("Module " + module.name + " publish did not triggerred successfully.");
        }
        currentLevel = nextLevel;
    }
}

function waitForModuleBuild(int currentLevel) {
    commons:logNewLine();
    log:printInfo("Waiting for level " + currentLevel.toString() + " module builds");
    runtime:sleep(getWaitTimeForLevel(currentLevel));
}

function getWaitTimeForLevel(int level) returns int {
    return 1;
    //if (level < 7) {
    //    // The following modules below level 7 takes more than 3 minutes to build
    //    // - Email (3m 30s)
    //    // - Kafka (3m)
    //    // Since these modules are leaf modules (Modules which does not have dependents), we can ignore these to
    //    // calculate the max wait time.
    //    // The non-leaf module with the maximum build time is Auth module. It takes 2m 40s
    //    return MINUTE_IN_MILLIS * 3;
    //} else if (level == 7) {
    //    // Max time - HTTP - 18m 53s
    //    return MINUTE_IN_MILLIS * 20;
    //} else {
    //    // Max time - gRPC - 5m 20s
    //    return MINUTE_IN_MILLIS * 6;
    //}
}

function publishModule(commons:Module module) returns boolean {
    commons:logNewLine();
    log:printInfo("Publishing " + module.name + " Version " + module.'version);
    http:Request request = commons:createRequest(accessTokenHeaderValue);
    string moduleName = module.name.toString();
    string 'version = module.'version.toString();
    string apiPath = "/" + moduleName + commons:DISPATCHES;

    json payload = {
        event_type: PUBLISH_SNAPSHOT_EVENT,
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
    return commons:validateResponse(response, moduleName);
}
