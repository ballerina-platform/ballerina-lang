
import ballerina/io;
import ballerina/mime;
import ballerina/http;

# This functions pulls a module from ballerina central.
#
# + definedEndpoint - Endpoint defined with the proxy configurations
# + accessToken - Access token
# + mdFileContent - Module.md file content of the module
# + summary - Summary of the module
# + homePageURL - Website URL of the module
# + repositoryURL - Source code URL of the module
# + apiDocURL - API documentation URL of the module
# + authors - Authors of the module
# + keywords - Keywords which describes the module
# + license - License of the module
# + url - URL to be invoked to push the module
# + dirPath - Directory path where the archived module resides
# + ballerinaVersion - Ballerina version the module is built
# + msg - Message printed when the module is pushed successfully which includes module info
# + baloVersion - Balo version of the module
function pushPackage (http:Client definedEndpoint, string accessToken, string mdFileContent, string summary, string homePageURL, string repositoryURL,
                string apiDocURL, string authors, string keywords, string license, string url, string dirPath, string ballerinaVersion, string msg, string baloVersion) {
    
    endpoint http:Client httpEndpoint = definedEndpoint;
    mime:Entity mdFileContentBodyPart = addStringBodyParts("description", mdFileContent);
    mime:Entity summaryBodyPart = addStringBodyParts("summary", summary);
    mime:Entity homePageURLBodyPart = addStringBodyParts("websiteURL", homePageURL);
    mime:Entity repositoryURLBodyPart = addStringBodyParts("repositoryURL", repositoryURL);
    mime:Entity apiDocURLBodyPart = addStringBodyParts("apiDocURL", apiDocURL);
    mime:Entity authorsBodyPart = addStringBodyParts("authors", authors);
    mime:Entity keywordsBodyPart = addStringBodyParts("keywords", keywords);
    mime:Entity licenseBodyPart = addStringBodyParts("license", license);
    mime:Entity ballerinaVersionBodyPart = addStringBodyParts("ballerinaVersion", ballerinaVersion);
    mime:Entity baloVersionBodyPart = addStringBodyParts("baloVersion", baloVersion);

    // Artifact
    mime:Entity filePart = new;
    filePart.setContentDisposition(getContentDispositionForFormData("artifact"));
    filePart.setFileAsEntityBody(untaint dirPath);
    filePart.setContentType(mime:APPLICATION_OCTET_STREAM);

    mime:Entity[] bodyParts = [filePart, mdFileContentBodyPart, summaryBodyPart, homePageURLBodyPart, repositoryURLBodyPart,
                               apiDocURLBodyPart, authorsBodyPart, keywordsBodyPart, licenseBodyPart, ballerinaVersionBodyPart, 
                               baloVersionBodyPart];
    http:Request req = new;
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setBodyParts(bodyParts, contentType = mime:MULTIPART_FORM_DATA);

    var result = httpEndpoint -> post("", req);
    http:Response httpResponse = new;
    match result {
        http:Response response => httpResponse = response;
        error e => {
            io:println("connection to the remote host failed : " + e.message);
            return;
        }
    }
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode.hasPrefix("5")) {
        io:println("remote registry failed for url :" + url);
    } else if (statusCode != "200") {
        json jsonResponse = check (httpResponse.getJsonPayload());
        string message = jsonResponse.message.toString();
        io:println(message);
    } else {
        io:println(msg);
    }
}

# This function will invoke the method to push the module.
# + args - Arguments passed
public function main (string... args) {
    http:Client httpEndpoint;
    string host = args[13];
    string port = args[14];
    if (host != "" && port != "") {
        try {
          httpEndpoint = defineEndpointWithProxy(args[9], host, port, args[15], args[16]);
        } catch (error err) {
          io:println("failed to resolve host : " + host + " with port " + port);
          return;
        }
    } else  if (host != "" || port != "") {
        io:println("both host and port should be provided to enable proxy");     
        return;   
    } else {
        httpEndpoint = defineEndpointWithoutProxy(args[9]);
    }        
    pushPackage(httpEndpoint, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[12], args[11], args[17]);    
}

# This function defines an endpoint with proxy configurations.
#
# + url - URL to be invoked
# + hostname - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Endpoint defined
function defineEndpointWithProxy (string url, string hostname, string port, string username, string password) returns http:Client{
    endpoint http:Client httpEndpoint {
        url: url,
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
            proxy : getProxyConfigurations(hostname, port, username, password)
    };
    return httpEndpoint;
}

# This function defines an endpoint without proxy configurations.
#
# + url - URL to be invoked
# + return - Endpoint defined
function defineEndpointWithoutProxy (string url) returns http:Client{
    endpoint http:Client httpEndpoint {
        url: url,
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        }
    };
    return httpEndpoint;
}

# This function will get the content disposition of the form data sent.
#
# + partName - Multipart name
# + return - `ContentDisposition` of the multipart
function getContentDispositionForFormData(string partName) returns (mime:ContentDisposition){
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name =  partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

# This function will add string part information in multiparts.
#
# + key - Name of the multipart
# + value - String value to be included in the multipart
# + return - `Entity` with the part information.
function addStringBodyParts (string key, string value) returns (mime:Entity) {
    mime:Entity stringBodyPart = new;
    stringBodyPart.setContentDisposition(getContentDispositionForFormData(key));
    stringBodyPart.setText(untaint value);
    stringBodyPart.setContentType(mime:TEXT_PLAIN);
    return stringBodyPart;
}

# This function sets the proxy configurations for the endpoint.
#
# + hostName - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Proxy configurations for the endpoint
function getProxyConfigurations(string hostName, string port, string username, string password) returns http:ProxyConfig {
    int portInt = check <int> port;
    http:ProxyConfig proxy = { host : hostName, port : portInt , userName: username, password : password };
    return proxy;
}
