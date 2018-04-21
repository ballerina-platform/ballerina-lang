
import ballerina/io;
import ballerina/mime;
import ballerina/http;

documentation {
    Function to pull a package from ballerina central.

    P{{accessToken}} - The access token.
    P{{mdFileContent}} - The Package.md file content of the package.
    P{{summary}} - The summary of the package.
    P{{homePageURL}} - The website URL of the package.
    P{{repositoryURL}} - The source code URL of the package.
    P{{apiDocURL}} - The api documentation URL of the package.
    P{{authors}} - The authors of the package.
    P{{keywords}} - The keywords which describes the package.
    P{{license}} - The license of the package.
    P{{url}} - The url to be invoked to push the package.
    P{{dirPath}} - The directory path where the archived package resides.
    P{{msg}} - The message printed when the package is pushed successfully which includes package info.
}
function pushPackage (string accessToken, string mdFileContent, string summary, string homePageURL, string repositoryURL,
    string apiDocURL, string authors, string keywords, string license, string url, string dirPath, string msg) {
    endpoint http:Client httpEndpoint {
        url:url,
        secureSocket:{
            trustStore:{
                path:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password:"ballerina"
            },
            verifyHostname:false,
            shareSession:true
        }
    };

    mime:Entity mdFileContentBodyPart = addStringBodyParts("description", mdFileContent);
    mime:Entity summaryBodyPart = addStringBodyParts("summary", summary);
    mime:Entity homePageURLBodyPart = addStringBodyParts("websiteURL", homePageURL);
    mime:Entity repositoryURLBodyPart = addStringBodyParts("repositoryURL", repositoryURL);
    mime:Entity apiDocURLBodyPart = addStringBodyParts("apiDocURL", apiDocURL);
    mime:Entity authorsBodyPart = addStringBodyParts("authors", authors);
    mime:Entity keywordsBodyPart = addStringBodyParts("keywords", keywords);
    mime:Entity licenseBodyPart = addStringBodyParts("license", license);

    // Artifact
    mime:Entity filePart = new;
    filePart.setContentDisposition(getContentDispositionForFormData("artifact"));
    filePart.setFileAsEntityBody(untaint dirPath);
    filePart.setContentType(mime:APPLICATION_OCTET_STREAM);

    mime:Entity[] bodyParts = [filePart, mdFileContentBodyPart, summaryBodyPart, homePageURLBodyPart, repositoryURLBodyPart,
                                           apiDocURLBodyPart, authorsBodyPart, keywordsBodyPart, licenseBodyPart];
    http:Request req = new;
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setBodyParts(bodyParts, contentType = mime:MULTIPART_FORM_DATA);

    var result = httpEndpoint -> post("", request=req);
    http:Response httpResponse = new;
    match result {
        http:Response response => httpResponse = response;
        http:HttpConnectorError e => {
            io:println("Connection to the remote host failed : " + e.message);
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

documentation {
    Main function which invokes the method to push the package.
}
function main (string... args) {
    pushPackage(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
}

documentation {
    Function to get the content disposition of the form data sent.

    P{{partName}} - The multipart name.
    R{{}} - `ContentDisposition`
}
function getContentDispositionForFormData(string partName) returns (mime:ContentDisposition){
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name =  partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

documentation {
    Function to add string part information in multiparts.

    P{{key}} - The name of the multipart.
    P{{value}} - The string value to be included in the multipart.
    R{{}} - `Entity` Mime entity with the part information.
}
function addStringBodyParts (string key, string value) returns (mime:Entity) {
    mime:Entity stringBodyPart = new;
    stringBodyPart.setContentDisposition(getContentDispositionForFormData(key));
    stringBodyPart.setText(value);
    stringBodyPart.setContentType(mime:TEXT_PLAIN);
    return stringBodyPart;
}
