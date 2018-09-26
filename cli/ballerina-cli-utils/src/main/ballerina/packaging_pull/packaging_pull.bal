// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;
import ballerina/internal;
import ballerina/io;
import ballerina/mime;

@final int MAX_INT_VALUE = 2147483647;
@final string VERSION_REGEX = "(\\d+\\.)(\\d+\\.)(\\d+)";
DefaultLogFormatter logFormatter;

# This object denotes the default log formatter used when pulling a package directly.
#
# + offset - Offset from the terminal width.
type DefaultLogFormatter object {
    int offset = 0;
    function formatLog(string msg) returns string {
        return msg;
    }
};

# This object denotes the build log formatter used when pulling a package while building.
#
# + offset - Offset from the terminal width.
type BuildLogFormatter object {
    int offset = 10;
    function formatLog(string msg) returns string {
        return "\t" + msg;
    }
};

# This function pulls a package from ballerina central.
#
# + definedEndpoint - Endpoint defined with the proxy configurations
# + url - Url to be invoked
# + dirPath - Path of the directory to save the pulled package
# + pkgPath - Package path
# + fileSeparator - File separator based on the operating system
# + terminalWidth - Width of the terminal
# + versionRange - Supported version range
# + return - 1 if package is not found, else 0 if package already exists or successfully pulled
function pullPackage (http:Client definedEndpoint, string url, string dirPath, string pkgPath, string fileSeparator,
                      string terminalWidth, string versionRange) returns int {
    endpoint http:Client httpEndpoint = definedEndpoint;
    string fullPkgPath = pkgPath;
    string destDirPath = dirPath;
    http:Request req = new;
    req.addHeader("Accept-Encoding", "identity");

    http:Response httpResponse = new;
    var result = httpEndpoint -> get(untaint versionRange, message=req);

    match result {
        http:Response response => httpResponse = response;
        error e => {
            error endpointError = {
                message: logFormatter.formatLog("connection to the remote host failed : " + e.message)
            };
            throw endpointError;
        }
    }

    http:Response res = new;
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode.hasPrefix("5")) {
        error serverError = {
            message: logFormatter.formatLog("remote registry failed for url :" + url)
        };
        throw serverError;
    } else if (statusCode != "200") {
        var jsonResponse = httpResponse.getJsonPayload();
        match jsonResponse {
            json resp => {
                if (statusCode == "404") {
                    return 1;
                } else {
                    error pullError = {
                        message: logFormatter.formatLog(resp.message.toString())
                    };
                    throw pullError;
                }
            }
            error err => {
                error pullError = {
                    message: logFormatter.formatLog("error occurred when pulling the package")
                };
                throw pullError;
            }
        }
    } else {
        string contentLengthHeader;
        int pkgSize = MAX_INT_VALUE;

        if (httpResponse.hasHeader("content-length")) {
            contentLengthHeader = httpResponse.getHeader("content-length");
            pkgSize = check <int> contentLengthHeader;
        } else {
            error pullError = {
                message: logFormatter.formatLog("warning: package size information is missing from remote repository")
            };
            throw pullError;
        }

        io:ByteChannel sourceChannel = check (httpResponse.getByteChannel());

        string resolvedURI = httpResponse.resolvedRequestedURI;
        if (resolvedURI == "") {
            resolvedURI = url;
        }

        string [] uriParts = resolvedURI.split("/");
        string pkgVersion = uriParts[lengthof uriParts - 2];
        boolean valid = check pkgVersion.matches(VERSION_REGEX);

        if (valid) {
            string pkgName = fullPkgPath.substring(fullPkgPath.lastIndexOf("/") + 1, fullPkgPath.length());
            string archiveFileName = pkgName + ".zip";

            fullPkgPath = fullPkgPath + ":" + pkgVersion;
            destDirPath = destDirPath + fileSeparator + pkgVersion;
            string destArchivePath = destDirPath  + fileSeparator + archiveFileName;

            if (!createDirectories(destDirPath)) {
                internal:Path pkgArchivePath = new(destArchivePath);
                if (pkgArchivePath.exists()){
                    io:println(logFormatter.formatLog("package already exists in the home repository"));
                    return 0;
                }
            }

            io:ByteChannel destDirChannel = getFileChannel(destArchivePath, io:WRITE);
            string toAndFrom = " [central.ballerina.io -> home repo]";
            int rightMargin = 3;
            int width = (check <int> terminalWidth) - rightMargin;
            copy(pkgSize, sourceChannel, destDirChannel, fullPkgPath, toAndFrom, width);

            closeChannel(destDirChannel);
            closeChannel(sourceChannel);
            return 0;
        } else {
            error versionError = {
                message: logFormatter.formatLog("package version could not be detected")
            };
            throw versionError;
        }
    }
}

# This function will invoke the method to pull the package. If error occurs, it is thrown.
# + args - package pull arguments
# + return - 1 if package is not found, else 0 if package already exists or successfully pulled
public function main(string... args) returns int {
    http:Client httpEndpoint;
    string host = args[4];
    string port = args[5];
    boolean isBuild = <boolean>args[10];
    if (isBuild) {
        logFormatter = new BuildLogFormatter();
    } else {
        logFormatter = new DefaultLogFormatter();
    }

    if (host != "" && port != "") {
        try {
            httpEndpoint = defineEndpointWithProxy(args[0], host, port, args[6], args[7]);
        } catch (error err) {
            error endpointError = {
                message: logFormatter.formatLog("failed to resolve host : " + host + " with port " + port)
            };
            throw endpointError;
        }
    } else  if (host != "" || port != "") {
        error endpointError = {
            message: logFormatter.formatLog("both host and port should be provided to enable proxy")
        };
        throw endpointError;
    } else {
        httpEndpoint = defineEndpointWithoutProxy(args[0]);
    }
    return pullPackage(httpEndpoint, args[0], args[1], args[2], args[3], args[8], args[9]);
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
        followRedirects: { enabled: true, maxCount: 5 },
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
        },
        followRedirects: { enabled: true, maxCount: 5 }
    };
    return httpEndpoint;
}

# This function will get the file channel.
#
# + filePath - File path
# + permission - Permissions provided
# + return - `ByteChannel` of the file content
function getFileChannel (string filePath, io:Mode permission) returns (io:ByteChannel) {
    io:ByteChannel byteChannel = io:openFile(untaint filePath, permission);
    return byteChannel;
}

# This function will read the bytes from the byte channel.
#
# + byteChannel - Byte channel
# + numberOfBytes - Number of bytes to be read
# + return - Read content as byte[] along with the number of bytes read.
function readBytes (io:ByteChannel byteChannel, int numberOfBytes) returns (byte[], int) {
    byte[] bytes;
    int numberOfBytesRead;
    (bytes, numberOfBytesRead) = check (byteChannel.read(numberOfBytes));
    return (bytes, numberOfBytesRead);
}

# This function will write the bytes from the byte channel.
#
# + byteChannel - Byte channel
# + content - Content to be written as a byte[]
# + startOffset - Offset
# + return - number of bytes written.
function writeBytes (io:ByteChannel byteChannel, byte[] content, int startOffset) returns int {
    int numberOfBytesWritten = check (byteChannel.write(content, startOffset));
    return numberOfBytesWritten;
}

# This function will copy files from source to the destination path.
#
# + pkgSize - Size of the package pulled
# + src - Byte channel of the source file
# + dest - Byte channel of the destination folder
# + fullPkgPath - Full package path
# + toAndFrom - Pulled package details
# + width - Width of the terminal
function copy (int pkgSize, io:ByteChannel src, io:ByteChannel dest, string fullPkgPath, string toAndFrom, int width) {
    int terminalWidth = width - logFormatter.offset;
    int bytesChunk = 8;
    byte[] readContent;
    int readCount = -1;
    float totalCount = 0.0;
    int numberOfBytesWritten = 0;
    string noOfBytesRead;
    string equals = "==========";
    string tabspaces = "          ";
    boolean completed = false;
    int rightMargin = 5;
    int totalVal = 10;
    int startVal = 0;
    int rightpadLength = terminalWidth - equals.length() - tabspaces.length() - rightMargin;
    try {
        while (!completed) {
            (readContent, readCount) = readBytes(src, bytesChunk);
            if (readCount <= startVal) {
                completed = true;
            }
            if (dest != null) {
                numberOfBytesWritten = writeBytes(dest, readContent, startVal);
            }
            totalCount = totalCount + readCount;
            float percentage = totalCount / pkgSize;
            noOfBytesRead = totalCount + "/" + pkgSize;
            string bar = equals.substring(startVal, <int> (percentage * totalVal));
            string spaces = tabspaces.substring(startVal, totalVal - <int>(percentage * totalVal));
            string size = "[" + bar + ">" + spaces + "] " + <int>totalCount + "/" + pkgSize;
            string msg = truncateString(fullPkgPath + toAndFrom, terminalWidth - size.length());
            io:print("\r" + logFormatter.formatLog(rightPad(msg, rightpadLength) + size));
        }
    } catch (error err) {
        io:println("");
    }
    io:println("\r" + logFormatter.formatLog(rightPad(fullPkgPath + toAndFrom, terminalWidth)));
}

# This function adds the right pad.
#
# + logMsg - Log message to be printed
# + logMsgLength - Length of the log message
# + return - The log message to be printed after adding the right pad
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

# This function truncates the string.
#
# + text - String to be truncated
# + maxSize - Maximum size of the log message printed
# + return - Truncated string.
function truncateString (string text, int maxSize) returns (string) {
    int lengthOfText = text.length();
    if (lengthOfText > maxSize) {
        int endIndex = 3;
        if (maxSize > endIndex) {
            endIndex = maxSize - endIndex;
        }
        string truncatedStr = text.substring(0, endIndex);
        return truncatedStr + "…";
    }
    return text;
}

# This function creates directories.
#
# + directoryPath - Directory path to be created
# + return - If the directories were created or not
function createDirectories(string directoryPath) returns (boolean) {
    internal:Path dirPath = new(directoryPath);
    if (!dirPath.exists()){
        match dirPath.createDirectory() {
            () => {
                return true;
            }
            error => {
                return false;
            }
        }
    } else {
        return false;
    }
}

# This function will close the byte channel.
#
# + byteChannel - Byte channel to be closed
function closeChannel(io:ByteChannel byteChannel) {
    match byteChannel.close() {
        error channelCloseError => {
            error channelError = {
                message: logFormatter.formatLog("Error occured while closing the channel: " + channelCloseError.message)
            };
            throw channelError;
        }
        () => return;
    }
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
