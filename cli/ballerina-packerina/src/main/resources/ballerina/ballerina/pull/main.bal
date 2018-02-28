package ballerina.pull;

import ballerina.net.http;
import ballerina.compression;
import ballerina.io;
import ballerina.file;
import ballerina.mime;
import ballerina.runtime;

const string FileSeparator = "/";

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], getConnectorConfigs(args));
    }
    http:OutRequest req = {};
    req.addHeader("Accept-Encoding", "identity");
    var resp, errRes = httpEndpoint.get("", req);
    if (errRes != null) {
        error err = {message: errRes.message};
        throw err;
    }
    if (resp.statusCode != 200) {
        json jsonResponse = resp.getJsonPayload();
        io:println(jsonResponse.msg.toString());
    } else {
        string homeRepoDirPath = args[1];
        string pkgName = args[2];
        string projectRepoDirPath = args[3];
        string fullPkgPath = args[4];
        string pkgVersion = args[5];
        // If there is no version in the requested package
        if (pkgVersion == "*") {
            // Get the package version from the canonical header of the response
            string headerValue = resp.getHeader("Link");
            string canonicalLinkURL = headerValue.subString(headerValue.indexOf("<") + 1, headerValue.indexOf(">"));
            pkgVersion = canonicalLinkURL.subString(canonicalLinkURL.lastIndexOf("/") + 1, canonicalLinkURL.length());
            fullPkgPath = args[4] + ":" + pkgVersion;
            homeRepoDirPath = homeRepoDirPath.replace("*", pkgVersion);
        }
        var pkgSize, conversionErr = <int> resp.getHeader("Content-Length");

        // Create directories of the home repo to which the package will be pulled
        createDirectories(homeRepoDirPath);

        mime:Entity entity = resp.getEntity();
        io:ByteChannel sourceChannel = entity.getByteChannel();
        string tmpFilePath = runtime:getProperty("java.io.tmpdir") + FileSeparator + "tmp.zip";
        io:ByteChannel destinationChannel = getFileChannel(tmpFilePath, "w");

        string packageName = fullPkgPath + " [central.ballerina.io -> home repo]";
        copy(pkgSize, sourceChannel, destinationChannel, packageName);
        compression:unzipFile(tmpFilePath, homeRepoDirPath, pkgName);

        if (projectRepoDirPath != null){
            projectRepoDirPath = projectRepoDirPath.replace("*", pkgVersion);
            // Create directories of the project repo to which the package will be pulled
            createDirectories(projectRepoDirPath);
            compression:unzipFile(tmpFilePath, projectRepoDirPath, pkgName);
            io:println(fullPkgPath + " [central.ballerina.io -> project repo]");
        }

        file:File tmpFile = {path:tmpFilePath};
        tmpFile.delete();
        sourceChannel.close();
        destinationChannel.close();
    }
}

function getConnectorConfigs(string [] args) (http:Options) {
    string proxyHost = args[6];
    string proxyPort = args[7];
    string proxyUsername = args[8];
    string proxyPassword = args[9];
    http:Options option;
    int portInt = 0;
    if (proxyHost != ""){
      if (proxyPort != ""){
          var portI, _ = <int> proxyPort;
          portInt = portI;
      }
      option = {
         proxy:{
           host:proxyHost,
           port:portInt,
           userName:proxyUsername,
           password:proxyPassword
         }
      };
    } else {
        option = {};
    }
    return option;
}

function createDirectories(string directoryPath) {
    file:AccessDeniedError adError;
    file:IOError ioError;
    boolean folderCreated;

    file:File folder = { path: directoryPath };
    folderCreated, adError, ioError = folder.mkdirs();

    if (folderCreated) {
        if (adError != null) {
            error err = { message: "Error occurred while creating the directories", cause: adError.cause };
            throw err;
        } else if (ioError != null) {
            error err = { message: "I/O error occurred while creating the directories", cause: ioError.cause };
            throw err;
        }
    }
}

function getFileChannel (string filePath, string permission) (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    return channel;
}

function readBytes (io:ByteChannel channel, int numberOfBytes) (blob, int) {
    blob bytes;
    int numberOfBytesRead;

    bytes, numberOfBytesRead = channel.readBytes(numberOfBytes);
    return bytes, numberOfBytesRead;
}

function writeBytes (io:ByteChannel channel, blob content, int startOffset) (int) {

    int numberOfBytesWritten = channel.writeBytes(content, startOffset);
    return numberOfBytesWritten;
}

function copy (int pkgSize, io:ByteChannel src, io:ByteChannel dst, string packageName) {

    int bytesChunk = 8;
    blob readContent;
    int readCount = -1;
    float totalCount = 0.0;
    int numberOfBytesWritten = 0;

    while (readCount != 0) {
        readContent, readCount = readBytes(src, bytesChunk);
        numberOfBytesWritten = writeBytes(dst, readContent, 0);
        totalCount = totalCount + readCount;
        float percentage = totalCount / pkgSize;
        runtime:sleepCurrentWorker(100);
        string equals = "==========";
        string tabspaces = "          ";
        string bar = equals.subString(0, <int> (percentage * 10));
        string spaces = tabspaces.subString(0, 10 - <int> (percentage * 10));
        io:print("\r" + packageName + "[" + bar + ">" + spaces + "] " + <int> totalCount + "/" + pkgSize);
    }
    io:print("\r" + packageName);
    io:print("                             \n");
}