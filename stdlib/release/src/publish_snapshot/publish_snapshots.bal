import ballerina/config;
import ballerina/http;
import ballerina/log;
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
    log:printInfo("------------------------------");
    log:printInfo("Publishing " + module.name + " Version " + module.'version);
    http:Request request = createRequest(accessTokenHeaderValue);
    string moduleName = module.name.toString();
    string versionString = module.'version.toString();
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
