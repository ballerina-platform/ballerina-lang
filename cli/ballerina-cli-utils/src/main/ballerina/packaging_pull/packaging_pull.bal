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

const int MAX_INT_VALUE = 2147483647;
const string VERSION_REGEX = "(\\d+\\.)(\\d+\\.)(\\d+)";

DefaultLogFormatter logFormatter = new DefaultLogFormatter();
boolean isBuild = false;

# This object denotes the default log formatter used when pulling a module directly.
#
# + offset - Offset from the terminal width.
type DefaultLogFormatter object {
    int offset = 0;
    function formatLog(string msg) returns string {
        return msg;
    }
};

# This object denotes the build log formatter used when pulling a module while building.
#
# + offset - Offset from the terminal width.
type BuildLogFormatter object {
    int offset = 10;
    function formatLog(string msg) returns string {
        return "\t" + msg;
    }
};

# Creates an error record.

# + errMessage - The error message.
# + return - Newly created error record.
function createError (string errMessage) returns error {
    error endpointError = error(logFormatter.formatLog(errMessage));
    return endpointError;
}

# This function pulls a module from ballerina central.
#
# + args - Arguments for pulling a module
# + return - nil if no error occurred, else error.
public function invokePull (string... args) returns error? {
    http:Client httpEndpoint;
    string url = args[0];
    string dirPath = args[1];
    string pkgPath = args[2];
    string fileSeparator = args[3];
    string host = args[4];
    string strPort = args[5];
    string proxyUsername = args[6];
    string proxyPassword = args[7];
    string terminalWidth = args[8];
    string versionRange = args[9];
    isBuild = untaint boolean.convert(args[10]);

    if (isBuild) {
        logFormatter = new BuildLogFormatter();
    }

    if (host != "" && strPort != "") {
        // validate port
        var port = int.convert(strPort);
        if (port is int) {
            http:Client|error result = trap defineEndpointWithProxy(url, host, port, proxyUsername, proxyPassword);
            if (result is http:Client) {
                httpEndpoint = result;
                return pullPackage(httpEndpoint, url, pkgPath, dirPath, versionRange, fileSeparator, terminalWidth);
            } else {
                return createError("failed to resolve host : " + host + " with port " + port);
            }
        } else {
            return createError("invalid port : " + strPort);
        }
    } else  if (host != "" || strPort != "") {
        return createError("both host and port should be provided to enable proxy");
    } else {
        httpEndpoint = defineEndpointWithoutProxy(url);
        return pullPackage(httpEndpoint, url, pkgPath, dirPath, versionRange, fileSeparator, terminalWidth);
    }
}

# Pulling a module
#
# + httpEndpoint - The endpoint to call
# + url - Central URL
# + pkgPath - Module Path
# + dirPath - Directory path
# + versionRange - Balo version range
# + fileSeparator - System file separator
# + terminalWidth - Width of the terminal
# + return - Error if occurred, else nil
function pullPackage(http:Client httpEndpoint, string url, string pkgPath, string dirPath, string versionRange,
                     string fileSeparator, string terminalWidth) returns error? {
    http:Client centralEndpoint = httpEndpoint;

    string fullPkgPath = pkgPath;
    string destDirPath = dirPath;
    http:Request req = new;
    req.addHeader("Accept-Encoding", "identity");

    http:Response httpResponse = new;
    var result = centralEndpoint -> get(untaint versionRange, message=req);
    if (result is http:Response) {
        httpResponse = result;
    } else {
        return createError("connection to the remote host failed : " + result.reason());
    }

    http:Response res = new;
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode.hasPrefix("5")) {
        return createError("remote registry failed for url :" + url);
    } else if (statusCode != "200") {
        var resp = httpResponse.getJsonPayload();
        if (resp is json) {
            if (statusCode == "404" && isBuild && resp.message.toString().contains("could not find module")) {
                // To ignore printing the error
                return createError("");
            } else {
                return createError(resp.message.toString());
            }
        } else {
            return createError("error occurred when pulling the module");
        }
    } else {
        string contentLengthHeader;
        int pkgSize = MAX_INT_VALUE;

        if (httpResponse.hasHeader("content-length")) {
            contentLengthHeader = httpResponse.getHeader("content-length");
            pkgSize = check int.convert(contentLengthHeader);
        } else {
            return createError("module size information is missing from remote repository. please retry.");
        }

        io:ReadableByteChannel sourceChannel = check (httpResponse.getByteChannel());

        string resolvedURI = httpResponse.resolvedRequestedURI;
        if (resolvedURI == "") {
            resolvedURI = url;
        }

        string [] uriParts = resolvedURI.split("/");
        string pkgVersion = uriParts[uriParts.length() - 2];
        boolean valid = check pkgVersion.matches(VERSION_REGEX);

        if (valid) {
            string pkgName = fullPkgPath.substring(fullPkgPath.lastIndexOf("/") + 1, fullPkgPath.length());
            string archiveFileName = pkgName + ".zip";

            fullPkgPath = fullPkgPath + ":" + pkgVersion;
            destDirPath = destDirPath + fileSeparator + pkgVersion;
            string destArchivePath = destDirPath  + fileSeparator + archiveFileName;

            if (!createDirectories(destDirPath)) {
                internal:Path pkgArchivePath = new(destArchivePath);
                if (pkgArchivePath.exists()) {
                    return createError("module already exists in the home repository");
                }
            }

            io:WritableByteChannel wch = io:openWritableFile(untaint destArchivePath);

            string toAndFrom = " [central.ballerina.io -> home repo]";
            int rightMargin = 3;
            int width = (check int.convert(terminalWidth)) - rightMargin;
            check copy(pkgSize, sourceChannel, wch, fullPkgPath, toAndFrom, width);

            var destChannelClose = wch.close();
            if (destChannelClose is error) {
                return createError("error occured while closing the channel: " + destChannelClose.reason());
            } else {
                var srcChannelClose = sourceChannel.close();
                if (srcChannelClose is error) {
                    return createError("error occured while closing the channel: " + srcChannelClose.reason());
                } else {
                    return ();
                }
            }
        } else {
            return createError("module version could not be detected");
        }
    }
}

# A dummy main function to generate the balx.
public function main() {}

# This function defines an endpoint with proxy configurations.
#
# + url - URL to be invoked
# + hostname - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Endpoint defined
function defineEndpointWithProxy (string url, string hostname, int port, string username, string password) returns http:Client {
    http:Client httpEndpointWithProxy = new (url, config = {
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
    });
    return httpEndpointWithProxy;
}

# This function defines an endpoint without proxy configurations.
#
# + url - URL to be invoked
# + return - Endpoint defined
function defineEndpointWithoutProxy (string url) returns http:Client{
    http:Client httpEndpointWithoutProxy = new (url, config = {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
        followRedirects: { enabled: true, maxCount: 5 }
    });
    return httpEndpointWithoutProxy;
}

# This function will read the bytes from the byte channel.
#
# + byteChannel - Byte channel
# + numberOfBytes - Number of bytes to be read
# + return - Read content as byte[] along with the number of bytes read, or error if read failed
function readBytes(io:ReadableByteChannel byteChannel, int numberOfBytes) returns (byte[], int)|error {
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
function writeBytes(io:WritableByteChannel byteChannel, byte[] content, int startOffset) returns int|error {
    int numberOfBytesWritten = check (byteChannel.write(content, startOffset));
    return numberOfBytesWritten;
}

# This function will copy files from source to the destination path.
#
# + pkgSize - Size of the module pulled
# + src - Byte channel of the source file
# + dest - Byte channel of the destination folder
# + fullPkgPath - Full module path
# + toAndFrom - Pulled module details
# + width - Width of the terminal
# + return - Nil if successful, error if read failed
function copy(int pkgSize, io:ReadableByteChannel src, io:WritableByteChannel dest,
              string fullPkgPath, string toAndFrom, int width) returns error? {
    int terminalWidth = width - logFormatter.offset;
    int bytesChunk = 8;
    byte[] readContent = [];
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
    while (!completed) {
        (readContent, readCount) = check readBytes(src, bytesChunk);
        if (readCount <= startVal) {
            completed = true;
        }
        if (dest != ()) {
            numberOfBytesWritten = check writeBytes(dest, readContent, startVal);
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
    io:println("\r" + logFormatter.formatLog(rightPad(fullPkgPath + toAndFrom, terminalWidth)));
    return;
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
        var result = dirPath.createDirectory();
        if (result is error) {
            return false;
        } else {
            return true;
        }
    } else {
        return false;
    }
}

# This function will close the byte channel.
#
# + byteChannel - Byte channel to be closed
function closeChannel(io:ReadableByteChannel|io:WritableByteChannel byteChannel) {
    if (byteChannel is io:ReadableByteChannel) {
        var channelCloseError = byteChannel.close();
        if (channelCloseError is error) {
            io:println(logFormatter.formatLog("Error occured while closing the channel: " + channelCloseError.reason()));
        } else {
            return;
        }
    } else  {
        var channelCloseError = byteChannel.close();
        if (channelCloseError is error) {
            io:println(logFormatter.formatLog("Error occured while closing the channel: " + channelCloseError.reason()));
        } else {
            return;
        }
    }
}

# This function sets the proxy configurations for the endpoint.
#
# + hostName - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Proxy configurations for the endpoint
function getProxyConfigurations(string hostName, int port, string username, string password) returns http:ProxyConfig {
    http:ProxyConfig proxy = { host : hostName, port : port , userName: username, password : password };
    return proxy;
}
