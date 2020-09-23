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
    handlePublish(modules);
}

function handlePublish(commons:Module[] modules) {
    int currentLevel = -1;
    foreach commons:Module module in modules {
        int nextLevel = module.level;
        if (nextLevel > currentLevel) {
            runtime:sleep(MAX_WAIT_TIME);
        }
        if (module.release) {
            boolean releaseStarted = publishModule(module);
            if (releaseStarted) {
                log:printInfo("Module " + module.name + " publish triggerred successfully.");
            } else {
                log:printWarn("Module " + module.name + " publish did not triggerred successfully.");
            }
        }
        currentLevel = nextLevel;
    }
}

function publishModule(commons:Module module) returns boolean {
    log:printInfo("------------------------------");
    log:printInfo("Releasing " + module.name + " Version " + module.'version);
    http:Request request = commons:createRequest(accessTokenHeaderValue);
    string moduleName = module.name.toString();
    string 'version = module.'version.toString();
    string apiPath = "/" + moduleName + commons:DISPATCHES;

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
    return commons:validateResponse(response, moduleName);
}
