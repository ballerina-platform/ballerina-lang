
import ballerina/internal;
import ballerina/io;
import ballerina/mime;
import ballerina/http;

@final int MAX_INT_VALUE = 2147483647;

documentation {
    Function to pull a package from ballerina central.

    P{{url}} - The endpoint url to be invoked.
    P{{dirPath}} - The path of the directory to save the pulled package.
    P{{pkgPath}} - The package path.
    P{{fileSeparator}} - The file separator based on the operating system.
}
function pullPackage (string url, string dirPath, string pkgPath, string fileSeparator) {
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
    string fullPkgPath = pkgPath;
    string destDirPath = dirPath;
    http:Request req = new;
    req.addHeader("Accept-Encoding", "identity");

    http:Response httpResponse = new;
    var result = httpEndpoint -> get("", request=req);

    match result {
        http:Response response => httpResponse = response;
        http:HttpConnectorError e => {
            io:println("Connection to the remote host failed : " + e.message);
            return;
        }
    }

    http:Response res = new;
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode == "302"){
        string locationHeader;
        if (httpResponse.hasHeader("Location")) {
            locationHeader = httpResponse.getHeader("Location");
            var resultFS = callFileServer(locationHeader);
            match resultFS {
                http:Response response => res = response;
                () => return;
            }
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
                        string pkgName = fullPkgPath.substring(fullPkgPath.lastIndexOf("/") + 1, fullPkgPath.length());
                        fullPkgPath = fullPkgPath + ":" + pkgVersion;

                        // Create the version directory
                        destDirPath = destDirPath + fileSeparator + pkgVersion;
                        
                        string archiveFileName = pkgName + ".zip";
                        string destArchivePath = destDirPath  + fileSeparator + archiveFileName;

                        if (!createDirectories(destDirPath)) {
                            internal:Path pkgArchivePath = new(destArchivePath);
                            if (internal:exists(pkgArchivePath)){
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

documentation {
    Main function which invokes the method to pull the package.
}
function main(string... args){
    pullPackage(args[0], args[1], args[2], args[3]);
}

documentation {
    Function to get the file channel.

    P{{filePath}} - The file path.
    P{{permission}} - The permissions provided.
    R{{}} - `ByteChannel` of the file content.
}
function getFileChannel (string filePath, io:Mode permission) returns (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(untaint filePath, permission);
    return channel;
}

documentation {
    Function to read the bytes from the byte channel.

    P{{channel}} - The byte channel.
    P{{numberOfBytes}} - The number of bytes to be read.
    R{{}} - `blob` of the bytes read as a blob along with the `int` number of bytes read.
}
function readBytes (io:ByteChannel channel, int numberOfBytes) returns (blob, int) {
    blob bytes;
    int numberOfBytesRead;
    (bytes, numberOfBytesRead) = check (channel.read(numberOfBytes));
    return (bytes, numberOfBytesRead);
}

documentation {
    Function to write the bytes from the byte channel.

    P{{channel}} - The byte channel.
    P{{content}} - The content to be written as a blob.
    P{{startOffset}} - The offset.
    R{{}} - `int` number of bytes written.
}
function writeBytes (io:ByteChannel channel, blob content, int startOffset) returns (int) {
    int numberOfBytesWritten = check (channel.write(content, startOffset));
    return numberOfBytesWritten;
}

documentation {
    Function to copy files from source to the destination path.

    P{{pkgSize}} - The size of the package pulled.
    P{{src}} - The byte channel of the source file.
    P{{dest}} - The byte channel of the destination folder.
    P{{fullPkgPath}} - The full package path.
    P{{toAndFrom}} - The pulled package details.
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
            string bar = equals.substring(0, <int>(percentage * 10));
            string spaces = tabspaces.substring(0, 10 - <int>(percentage * 10));
            io:print("\r" + rightPad(msg, 100) + "[" + bar + ">" + spaces + "] " + <int>totalCount + "/" + pkgSize);
        }
    } catch (error err) {
        io:println("");
    }
    io:print("\r" + rightPad(fullPkgPath + toAndFrom, (115 + noOfBytesRead.length())) + "\n");
}

documentation {
    Function to include the right pad.

    P{{logMsg}} - The log message to be printed.
    P{{logMsgLength}} - The length of the log message.
    R{{}} - `string` The log message to be printed after adding the right pad.
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

documentation {
    Function to truncate the string.

    P{{text}} - The string to be truncated.
    R{{}} - `string` The truncated string.
}
function truncateString (string text) returns (string) {
    int indexOfVersion = text.lastIndexOf(":");
    string withoutVersion = text.substring(0, indexOfVersion);
    string versionOfPkg = text.substring(indexOfVersion, text.length());
    int minLength = 57;
    int lengthWithoutVersion = withoutVersion.length();
    if (lengthWithoutVersion > minLength) {
        int noOfCharactersToBeRemoved = lengthWithoutVersion - minLength;
        int half = noOfCharactersToBeRemoved / 2;
        int middleOfWithoutVersion = lengthWithoutVersion / 2;
        int leftFromMiddle = middleOfWithoutVersion - half;
        int rightFromMiddle = middleOfWithoutVersion + half;

        string truncatedLeftStr = withoutVersion.substring(0, leftFromMiddle);
        string truncatedRightStr = withoutVersion.substring(rightFromMiddle, lengthWithoutVersion);

        string truncatedStr = truncatedLeftStr + "…" + truncatedRightStr;
        return truncatedStr + versionOfPkg;
    }
    return text;
}

documentation {
    Function to create directories.

    P{{directoryPath}} - The directory path to be created.
    R{{}} - `boolean` If the directories were created or not.
}
function createDirectories(string directoryPath) returns (boolean) {
    internal:Path dirPath = new(directoryPath);
    if (!internal:exists(dirPath)){
        boolean directoryCreationStatus = check (internal:createDirectory(dirPath));
        return directoryCreationStatus;
    } else {
        return false;
    }
}

documentation {
    Function to invoke the FileServer endpoint.

    P{{url}} - The endpoint url to be invoked.
    R{{}} - `Response` The response got after invoking the endpoint.
}

function callFileServer(string url) returns http:Response? {
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
    http:Request req = new;
    var result = httpEndpoint -> get("", request=req);
    match result {
        http:Response response => return response;
        http:HttpConnectorError e => {
            io:println("Connection to the remote host failed : " + e.message);
            return;
        }
    }
}
