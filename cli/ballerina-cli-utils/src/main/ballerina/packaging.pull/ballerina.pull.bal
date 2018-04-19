package packaging.pull;

import ballerina/file;
import ballerina/io;
import ballerina/mime;
import ballerina/http;

@final int MAX_INT_VALUE = 2147483647;

function pullPackage (string url, string dirPath, string pkgPath, string fileSeparator) {
    endpoint http:Client httpEndpoint {
        url:url,
        secureSocket:{
            trustStore:{
                filePath:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password:"ballerina"
            },
            verifyHostname:false,
            shareSession:true
        }
    };
    string fullPkgPath = pkgPath;
    string destDirPath = dirPath;
    http:Request req = new;
    req.addHeader("Accept-Encoding", "identity");

    var result = httpEndpoint -> get("", req);
    http:Response httpResponse = check result;

    http:Response res = new;
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode == "302"){
        string locationHeader;
        if (httpResponse.hasHeader("Location")) {
            locationHeader = httpResponse.getHeader("Location");
            res = callFileServer(locationHeader);
            if (res.statusCode != 200) {
                json jsonResponse = check (res.getJsonPayload());
                string message = jsonResponse.message.toString();
                io:println(message);
            } else {
                string contentLengthHeader;
                int pkgSize = MAX_INT_VALUE;
                if (res.hasHeader("content-length")) {
                    contentLengthHeader = res.getHeader("content-length");
                    pkgSize = check <int> contentLengthHeader;
                } else {
                    io:println("warning: package size information is missing from the remote repository");
                }

                io:ByteChannel sourceChannel = check (res.getByteChannel());

                string rawPathVal;
                if (res.hasHeader("raw-path")) {
                    rawPathVal = res.getHeader("raw-path");
                    string pkgVersion;
                    string [] pathArray = rawPathVal.split("/");
                    int sizeOfArray = lengthof pathArray;
                    if (sizeOfArray > 3) {
                        pkgVersion = pathArray[sizeOfArray - 2];
                        string pkgName = fullPkgPath.subString(fullPkgPath.lastIndexOf("/") + 1, fullPkgPath.length());
                        fullPkgPath = fullPkgPath + ":" + pkgVersion;

                        // Create the version directory
                        destDirPath = destDirPath + fileSeparator + pkgVersion;
                        
                        string archiveFileName = pkgName + ".zip";
                        string destArchivePath = destDirPath  + fileSeparator + archiveFileName;

                        if (!createDirectories(destDirPath)) {
                            file:Path pkgArchivePath = new(destArchivePath);
                            if (file:exists(pkgArchivePath)){  
                                return;                              
                            }        
                        }

                        io:ByteChannel destDirChannel = getFileChannel(destArchivePath, "w");
                        string toAndFrom = " [central.ballerina.io -> home repo]";

                        copy(pkgSize, sourceChannel, destDirChannel, fullPkgPath, toAndFrom);
                        _ = destDirChannel.close();
                        _ = sourceChannel.close();
                    } else {
                        io:println("package version information is missing from the remote repository");
                    }
                } else {
                    io:println("package version information is missing from the remote repository");
                }
            }
        } else {
            io:println("package location information is missing from the remote repository");
        }
    } else if (statusCode.hasPrefix("5")) {
        io:println("remote registry failed for url :" + url);
    } else {
       io:println("error occurred when pulling the package");
    }
}

function main(string... args){
    pullPackage(args[0], args[1], args[2], args[3]);
}


function getFileChannel (string filePath, string permission) returns (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(untaint filePath, permission);
    return channel;
}

function readBytes (io:ByteChannel channel, int numberOfBytes) returns (blob, int) {
    blob bytes;
    int numberOfBytesRead;
    (bytes, numberOfBytesRead) = check (channel.read(numberOfBytes));
    return (bytes, numberOfBytesRead);
}

function writeBytes (io:ByteChannel channel, blob content, int startOffset) returns (int) {
    int numberOfBytesWritten = check (channel.write(content, startOffset));
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
        io:println("");
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
    file:Path dirPath = new(directoryPath);
    if (!file:exists(dirPath)){
        boolean directoryCreationStatus = check (file:createDirectory(dirPath));
        return directoryCreationStatus;
    } else {
        return false;
    }
}

function callFileServer(string url) returns http:Response {
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
    http:Request req = new;
    var result = httpEndpoint -> get("", req);
    http:Response httpResponse = check result;
    return httpResponse;
}
