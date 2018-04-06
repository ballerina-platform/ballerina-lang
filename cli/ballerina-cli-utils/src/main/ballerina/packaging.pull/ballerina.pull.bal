package packaging.pull;

import ballerina/file;
import ballerina/io;
import ballerina/mime;
import ballerina/http;

function pullPackage (string url, string dirPath, string pkgPath, string fileSeparator) {
    endpoint http:ClientEndpoint httpEndpoint {
        targets: [
        {
            url: url,
            secureSocket: {
                trustStore: {
                    filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                },
                hostNameVerification:false,
                sessionCreation: true
             }
        }
        ],
        followRedirects : { enabled : true, maxCount : 5 }
    };
    io:println("11");
    string fullPkgPath = pkgPath;
    string destDirPath = dirPath;
    http:Request req = new;
    // http:Response res = new;
    req.addHeader("Accept-Encoding", "identity");

    io:println("22");
    
    //http:Response httpResponse = check (httpEndpoint -> get("", req));

    var result = httpEndpoint -> get("", req);
     io:println("33");
    http:Response httpResponse = check result;

     io:println(httpResponse);

    // match httpResponse {
    //  http:HttpConnectorError errRes => {
    //      var errorResp = <error> errRes;
    //      match errorResp {
    //          error err =>  throw err;
    //      }
    //  }
    //  http:Response response => res = response;
    // }
    if (httpResponse.statusCode != 200) {
        json jsonResponse = check (httpResponse.getJsonPayload());
        string message = (jsonResponse.msg.toString() but {()=> "error occurred when pulling the package"});
        io:println(message);
        // io:println(jsonResponse.msg.toString());
        // match jsonResponse {
        //     mime:EntityError errRes => {
        //         var errorResp = <error> errRes;
        //         match errorResp {
        //             error err =>  throw err;
        //         }
        //     }
        //     json jsonObj => io:println(jsonObj.msg.toString());
        // }
    } else {
        string contentLengthHeader;
        if (httpResponse.hasHeader("content-length")) {
            contentLengthHeader = httpResponse.getHeader("content-length");
        } else {
            error err = {message:"package size information is missing from the remote repository"};
            throw err;
        }
        // int pkgSize;
        int pkgSize = check <int> contentLengthHeader;
        // match conversion{
        //     error conversionErr => throw conversionErr;
        //     int size => pkgSize = size;
        // }
        // io:ByteChannel sourceChannel = {};
        io:ByteChannel sourceChannel = check (httpResponse.getByteChannel());
        io:println("44");
        // match srcChannel {
        //     mime:EntityError errRes => {
        //         var errorResp = <error> errRes;
        //         match errorResp {
        //             error err =>  throw err;
        //         }
        //     }
        //     io:ByteChannel channel => sourceChannel = channel;
        // }

        // Get the package version from the canonical header of the response
        // string linkHeaderVal;
        // if (httpResponse.hasHeader("Link")) {
        //     linkHeaderVal = httpResponse.getHeader("Link");
        // } else {
        //     error err = {message:"package version information is missing from the remote repository"};
        //     throw err;
        // }
       
        // string canonicalLinkURL = linkHeaderVal.subString(linkHeaderVal.indexOf("<") + 1, linkHeaderVal.indexOf(">"));
        // string pkgVersion = canonicalLinkURL.subString(canonicalLinkURL.lastIndexOf("/") + 1, canonicalLinkURL.length());

        string rawPathVal;
        if (httpResponse.hasHeader("raw-path")) {
            rawPathVal = httpResponse.getHeader("raw-path");
         } else {
             error err = {message:"package version information is missing from the remote repository"};
             throw err;
         }
        string pkgVersion;
        string [] pathArray = rawPathVal.split("/");
        int sizeOfArray = lengthof pathArray;
        if (sizeOfArray > 3) {
            pkgVersion = pathArray[sizeOfArray - 2];
         } else {
             error err = {message:"package version information is missing from the remote repository"};
             throw err;
         }
        // string pkgVersion = rawPathVal.subString(indexOfVersion + 1, rawPathVal.length()); // 

        string pkgName = fullPkgPath.subString(fullPkgPath.lastIndexOf("/") + 1, fullPkgPath.length());
        fullPkgPath = fullPkgPath + ":" + pkgVersion;

        // Create the version directory
        destDirPath = destDirPath + fileSeparator + pkgVersion;
        if (!createDirectories(destDirPath)) {
            return;
        }
        string archiveFileName = pkgName + ".zip";
        string destArchivePath = destDirPath  + fileSeparator + archiveFileName;

        io:ByteChannel destDirChannel = getFileChannel(destArchivePath, "w");
        string toAndFrom = " [central.ballerina.io -> home repo]";

        io:println("55");
        
        // io:IOError destDirChannelCloseError = new;
        // io:IOError srcCloseError = new;

        copy(pkgSize, sourceChannel, destDirChannel, fullPkgPath, toAndFrom);
        // if (destDirChannel != null) {
        _ = destDirChannel.close();
        // }
        _ = sourceChannel.close();
    }
}

public function main(string[] args){
    // pullPackage(args[0], args[1], args[2], args[3]);
    pullPackage("https://api.staging-central.ballerina.io/packages/natasha/hello/3.0.0", 
    "/home/natasha/Desktop/package/hello", "natasha/hello", "/");

}


function getFileChannel (string filePath, string permission) returns (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(untaint filePath, permission);
    return channel;
}

function readBytes (io:ByteChannel channel, int numberOfBytes) returns (blob, int) {
    blob bytes;
    int numberOfBytesRead;
    (bytes, numberOfBytesRead) = check (channel.read(numberOfBytes));
    // match bytesRead {
    //     (blob, int) byteResponse => {
    //         (bytes, numberOfBytesRead) = byteResponse;
    //     }
    //     io:IOError errRes => {
    //             var errorResp = <error> errRes;
    //             match errorResp {
    //                 error err =>  throw err;
    //             }
    //     }
    // }
    return (bytes, numberOfBytesRead);
}

function writeBytes (io:ByteChannel channel, blob content, int startOffset) returns (int) {
    // int numberOfBytesWritten;
    int numberOfBytesWritten = check (channel.write(content, startOffset));
    // match bytesWritten {
    //     io:IOError errRes => {
    //             var errorResp = <error> errRes;
    //             match errorResp {
    //                 error err =>  throw err;
    //             }
    //     }
    //     int noOfBytes => numberOfBytesWritten = noOfBytes;
    // }
    return numberOfBytesWritten;
}

function copy (int pkgSize, io:ByteChannel src, io:ByteChannel dest, string fullPkgPath, string toAndFrom) {
    string truncatedFullPkgPath = truncateString(fullPkgPath);
    string msg = truncatedFullPkgPath + toAndFrom;
    int bytesChunk = 8;
    blob readContent;
    int readCount = -1;
    float totalCount = 0.0;
    int numberOfBytesWritten = 0;
    string noOfBytesRead;
    string equals = "==========";
    string tabspaces = "          ";
    boolean completed = false;
    try {
        while (!completed) {
            (readContent, readCount) = readBytes(src, bytesChunk);
            if (readCount <= 0) {
                completed = true;
            }
            if (dest != null) {
                numberOfBytesWritten = writeBytes(dest, readContent, 0);
            }
            totalCount = totalCount + readCount;
            float percentage = totalCount / pkgSize;
            noOfBytesRead = totalCount + "/" + pkgSize;
            string bar = equals.subString(0, <int>(percentage * 10));
            string spaces = tabspaces.subString(0, 10 - <int>(percentage * 10));
            io:print("\r" + rightPad(msg, 100) + "[" + bar + ">" + spaces + "] " + <int>totalCount + "/" + pkgSize);
        }
    } catch (error err) {
        throw err;
    }
    io:print("\r" + rightPad(fullPkgPath + toAndFrom, (115 + noOfBytesRead.length())) + "\n");
}

function rightPad (string logMsg, int logMsgLength) returns (string) {
    string msg = logMsg;
    int length = logMsgLength;
    int i = -1;
    length = length - msg.length();
    string char = " ";
    while (i < length) {
        msg = msg + char;
        i = i + 1;
    }
    return msg;
}

function truncateString (string text) returns (string) {
    int indexOfVersion = text.lastIndexOf(":");
    string withoutVersion = text.subString(0, indexOfVersion);
    string versionOfPkg = text.subString(indexOfVersion, text.length());
    int minLength = 57;
    int lengthWithoutVersion = withoutVersion.length();
    if (lengthWithoutVersion > minLength) {
        int noOfCharactersToBeRemoved = lengthWithoutVersion - minLength;
        int half = noOfCharactersToBeRemoved / 2;
        int middleOfWithoutVersion = lengthWithoutVersion / 2;
        int leftFromMiddle = middleOfWithoutVersion - half;
        int rightFromMiddle = middleOfWithoutVersion + half;

        string truncatedLeftStr = withoutVersion.subString(0, leftFromMiddle);
        string truncatedRightStr = withoutVersion.subString(rightFromMiddle, lengthWithoutVersion);

        string truncatedStr = truncatedLeftStr + "…" + truncatedRightStr;
        return truncatedStr + versionOfPkg;
    }
    return text;
}

function createDirectories(string directoryPath) returns (boolean) {
    file:Path dirPath = file:getPath(directoryPath);
    if (!file:exists(dirPath)){
        boolean directoryCreationStatus = check (file:createDirectory(dirPath));
        return directoryCreationStatus;
    } else {
        return false;
    }
}