package packaging.push;

import ballerina/file;
import ballerina/io;
import ballerina/mime;
import ballerina/http;

function pushPackage (string accessToken, string mdFileContent, string summary, string homePageURL, string repositoryURL,
    string apiDocURL, string authors, string keywords, string license, string url, string dirPath, string msg) {
    endpoint http:Client httpEndpoint {
        targets: [
        {
            url: url,
            secureSocket: {
                trustStore: {
                    filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                },
                verifyHostname:false,
                shareSession: true
            }
        }
        ]
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
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    filePart.contentDisposition = getContentDispositionForFormData("artifact");
    file:Path filePath = new(dirPath);
    filePart.setFileAsEntityBody(untaint filePath);

    mime:Entity[] bodyParts = [filePart, mdFileContentBodyPart, summaryBodyPart, homePageURLBodyPart, repositoryURLBodyPart,
                                           apiDocURLBodyPart, authorsBodyPart, keywordsBodyPart, licenseBodyPart];
    http:Request req = new;
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setMultiparts(bodyParts, mime:MULTIPART_FORM_DATA);
    
    var result = httpEndpoint -> post("", req);
    http:Response httpResponse = check result;
    if (httpResponse.statusCode != 200) {
        json jsonResponse = check (httpResponse.getJsonPayload());
        string message = (jsonResponse.message.toString() but {()=> "error occurred when pushing the package"});
        io:println(message);
    } else {
        io:println(msg);
    }
}

function main (string[] args) {
    pushPackage(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
}

function getContentDispositionForFormData(string partName) returns (mime:ContentDisposition){
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name =  partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

function addStringBodyParts (string key, string value) returns (mime:Entity) {
    mime:Entity stringBodyPart = new;
    mime:MediaType contentTypeOfStringPart = mime:getMediaType(mime:TEXT_PLAIN);
    stringBodyPart.contentType = contentTypeOfStringPart;
    stringBodyPart.contentDisposition = getContentDispositionForFormData(key);
    stringBodyPart.setText(value);
    return stringBodyPart;
}
