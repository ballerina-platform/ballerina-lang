
import ballerina/internal;
import ballerina/io;
import ballerina/mime;
import ballerina/http;

@final int MAX_INT_VALUE = 2147483647;
@final string VERSION_REGEX = "(\\d+\\.)(\\d+\\.)(\\d+)";

documentation {
    This function pulls a package from ballerina central.

    P{{definedEndpoint}} Endpoint defined with the proxy configurations
    P{{url}} url to be invoked
    P{{dirPath}} Path of the directory to save the pulled package
    P{{pkgPath}} Package path
    P{{fileSeparator}} File separator based on the operating system
    P{{terminalWidth}} Width of the terminal
}
function pullPackage (http:Client definedEndpoint, string url, string dirPath, string pkgPath, string fileSeparator, string terminalWidth) {
    endpoint http:Client httpEndpoint = definedEndpoint;
    string fullPkgPath = pkgPath;
    string destDirPath = dirPath;
    http:Request req = new;
    req.addHeader("Accept-Encoding", "identity");

    http:Response httpResponse = new;
    var result = httpEndpoint -> get("", request=req);

    match result {
        http:Response response => httpResponse = response;
        error e => {
            io:println("connection to the remote host failed : " + e.message);
            return;
        }
    }

    http:Response res = new;
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode.hasPrefix("5")) {
        io:println("remote registry failed for url :" + url);
    } else if (statusCode != "200") {
        var jsonResponse = httpResponse.getJsonPayload();
        match jsonResponse {
            json resp => {
                string message = resp.message.toString();
                io:println(message);
            }
            error err => {
                io:println("error occurred when pulling the package");
            }
        }
    } else {
        string contentLengthHeader;
        int pkgSize = MAX_INT_VALUE;

        if (httpResponse.hasHeader("content-length")) {
            contentLengthHeader = httpResponse.getHeader("content-length");
            pkgSize = check <int> contentLengthHeader;
        } else {
            io:println("warning: package size information is missing from the remote repository");
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
                if (internal:pathExists(pkgArchivePath)){
                    io:println("package already exists in the home repository");
                    return;                              
                }        
            }

            io:ByteChannel destDirChannel = getFileChannel(destArchivePath, io:WRITE);
            string toAndFrom = " [central.ballerina.io -> home repo]";
            int rightMargin = 3;
            int width = (check <int> terminalWidth) - rightMargin;
            copy(pkgSize, sourceChannel, destDirChannel, fullPkgPath, toAndFrom, width);
                                
            closeChannel(destDirChannel);
            closeChannel(sourceChannel);
        } else {
            io:println("package version could not be detected");
        }
    }
}

documentation {
    This function will invoke the method to pull the package.
}
function main(string... args){
    http:Client httpEndpoint;
    string host = args[4];
    string port = args[5];
    if (host != "" && port != "") {
        try {
          httpEndpoint = defineEndpointWithProxy(args[0], host, port, args[6], args[7]);
        } catch (error err) {
          io:println("failed to resolve host : " + host + " with port " + port);
          return;
        }
    } else  if (host != "" || port != "") {
        io:println("both host and port should be provided to enable proxy");     
        return;   
    } else {
        httpEndpoint = defineEndpointWithoutProxy(args[0]);
    }
    pullPackage(httpEndpoint, args[0], args[1], args[2], args[3], args[8]);
}

documentation {
    This function defines an endpoint with proxy configurations.

    P{{url}} URL to be invoked
    P{{hostname}} Host name of the proxy
    P{{port}} Port of the proxy
    P{{username}} Username of the proxy
    P{{password}} Password of the proxy
    R{{}} Endpoint defined
}
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

documentation {
    This function defines an endpoint without proxy configurations.

    P{{url}} URL to be invoked
    R{{}} Endpoint defined
}
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

documentation {
    This function will get the file channel.

    P{{filePath}} File path
    P{{permission}} Permissions provided
    R{{}} `ByteChannel` of the file content
}
function getFileChannel (string filePath, io:Mode permission) returns (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(untaint filePath, permission);
    return channel;
}

documentation {
    This function will read the bytes from the byte channel.

    P{{channel}} Byte channel
    P{{numberOfBytes}} Number of bytes to be read
    R{{}} Bytes read as a blob along with the number of bytes read.
}
function readBytes (io:ByteChannel channel, int numberOfBytes) returns (blob, int) {
    blob bytes;
    int numberOfBytesRead;
    (bytes, numberOfBytesRead) = check (channel.read(numberOfBytes));
    return (bytes, numberOfBytesRead);
}

documentation {
    This function will write the bytes from the byte channel.

    P{{channel}} Byte channel
    P{{content}} Content to be written as a blob
    P{{startOffset}} Offset
    R{{}} number of bytes written.
}
function writeBytes (io:ByteChannel channel, blob content, int startOffset) returns (int) {
    int numberOfBytesWritten = check (channel.write(content, startOffset));
    return numberOfBytesWritten;
}

documentation {
    This function will copy files from source to the destination path.

    P{{pkgSize}} Size of the package pulled
    P{{src}} Byte channel of the source file
    P{{dest}} Byte channel of the destination folder
    P{{fullPkgPath}} Full package path
    P{{toAndFrom}} Pulled package details
    P{{width}} Width of the terminal
}
function copy (int pkgSize, io:ByteChannel src, io:ByteChannel dest, string fullPkgPath, string toAndFrom, int width) {
    int terminalWidth = width;
    int bytesChunk = 8;
    blob readContent;
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
            io:print("\r" + rightPad(msg, rightpadLength) + size);
        }
    } catch (error err) {
        io:println("");
    }
    io:println("\r" + rightPad(fullPkgPath + toAndFrom, terminalWidth));
}

documentation {
    This function adds the right pad.

    P{{logMsg}} Log message to be printed
    P{{logMsgLength}} Length of the log message
    R{{}} The log message to be printed after adding the right pad
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
    This function truncates the string.

    P{{text}} String to be truncated
    P{{maxSize}} Maximum size of the log message printed
    R{{}} Truncated string.
}
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

documentation {
    This function creates directories.

    P{{directoryPath}} Directory path to be created
    R{{}} If the directories were created or not
}
function createDirectories(string directoryPath) returns (boolean) {
    internal:Path dirPath = new(directoryPath);
    if (!internal:pathExists(dirPath)){
        boolean directoryCreationStatus = check (internal:createDirectory(dirPath));
        return directoryCreationStatus;
    } else {
        return false;
    }
}

documentation {
    This function will close the byte channel.

    P{{channel}} Byte channel to be closed
}
function closeChannel(io:ByteChannel channel) {
    match channel.close() {
        error channelCloseError => {
            io:println("Error occured while closing the channel: " + channelCloseError.message);
        }
        () => return;
    }
}

documentation {
    This function sets the proxy configurations for the endpoint.

    P{{hostName}} Host name of the proxy
    P{{port}} Port of the proxy
    P{{username}} Username of the proxy
    P{{password}} Password of the proxy
    R{{}} Proxy configurations for the endpoint
}
function getProxyConfigurations(string hostName, string port, string username, string password) returns http:ProxyConfig {
    int portInt = check <int> port;
    http:ProxyConfig proxy = { host : hostName, port : portInt , userName: username, password : password };
    return proxy;
}
