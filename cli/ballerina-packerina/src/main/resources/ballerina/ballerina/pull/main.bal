package ballerina.pull;

import ballerina.net.http;
import ballerina.io;
import ballerina.file;
import ballerina.mime;
import ballerina.runtime;

const string FileSeparator = "/";

function pullPackage (string url, string homeRepoDirPath, string pkgName, string projectRepoDirPath,
                      string fullPkgPath, string pkgVersion, string proxyHost, string proxyPort, string proxyUsername,
                      string proxyPassword ){
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(url, getConnectorConfigs(proxyHost, proxyPort, proxyUsername, proxyPassword));
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
        // If there is no version in the requested package
        if (pkgVersion == "*") {
            // Get the package version from the canonical header of the response
            string headerValue = resp.getHeader("Link");
            string canonicalLinkURL = headerValue.subString(headerValue.indexOf("<") + 1, headerValue.indexOf(">"));
            pkgVersion = canonicalLinkURL.subString(canonicalLinkURL.lastIndexOf("/") + 1, canonicalLinkURL.length());
            fullPkgPath = fullPkgPath + ":" + pkgVersion;
            homeRepoDirPath = homeRepoDirPath.replace("*", pkgVersion);
        }
        var pkgSize, conversionErr = <int> resp.getHeader("Content-Length");

        boolean homeDirExists = ifFileExists(homeRepoDirPath);
        mime:Entity entity = resp.getEntity();
        io:ByteChannel sourceChannel = entity.getByteChannel();
        string fileName = pkgName + "-" + pkgVersion + ".balo";
        io:ByteChannel homeDirChannel = null;
        string toAndFrom;

        if (!homeDirExists) {
            // Create directories of the home repo
            createDirectories(homeRepoDirPath);
            string homeRepoFilePath = homeRepoDirPath + FileSeparator + fileName;
            homeDirChannel = getFileChannel(homeRepoFilePath, "w");
            toAndFrom = " [central.ballerina.io -> home repo]";
        }

        io:ByteChannel projectDirChannel = null;
        if (projectRepoDirPath != null){
            projectRepoDirPath = projectRepoDirPath.replace("*", pkgVersion);
            string projectRepoFilePath = projectRepoDirPath + FileSeparator + fileName;
            projectDirChannel = getFileChannel(projectRepoFilePath, "w");
            boolean projectDirExists = ifFileExists(projectRepoDirPath);
            if (homeDirExists && projectDirExists) {
                toAndFrom = " [home repo -> project repo]";
            } else {
                toAndFrom = " [central.ballerina.io -> home repo -> project repo]";
            }
        }

        copy(pkgSize, sourceChannel, homeDirChannel, projectDirChannel, fullPkgPath, toAndFrom);
        if (homeDirChannel != null){
           homeDirChannel.close();
        }
        sourceChannel.close();
    }
}

function ifFileExists(string filePath) (boolean) {
    file:File fileDir = {path: filePath};
    return fileDir.exists();
}

function main (string[] args) {
    pullPackage(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
}

function getConnectorConfigs(string proxyHost, string proxyPort, string proxyUsername, string proxyPassword )
(http:Options) {
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
    if (!folder.exists()){
       folderCreated, adError, ioError = folder.mkdirs();
    }
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

function copy (int pkgSize, io:ByteChannel src, io:ByteChannel homeDst, io:ByteChannel projectDst, string packageName,
string toAndFrom) {
    string truncatedFullPkgPath = truncateString(packageName);
    string msg = truncatedFullPkgPath + toAndFrom;
    int bytesChunk = 8;
    blob readContent;
    int readCount = -1;
    float totalCount = 0.0;
    int numberOfBytesWritten = 0;
    string noOfBytesRead;
    string equals = "==========";
    string tabspaces = "          ";
    while (readCount != 0) {
        readContent, readCount = readBytes(src, bytesChunk);
        if (homeDst != null) {
            numberOfBytesWritten = writeBytes(homeDst, readContent, 0);
        }
        if (projectDst != null) {
            numberOfBytesWritten = writeBytes(projectDst, readContent, 0);
        }
        totalCount = totalCount + readCount;
        float percentage = totalCount / pkgSize;
        runtime:sleepCurrentWorker(100);
        noOfBytesRead = totalCount + "/" + pkgSize;
        string bar = equals.subString(0, <int> (percentage * 10));
        string spaces = tabspaces.subString(0, 10 - <int> (percentage * 10));
        io:print("\r" + rightPad(msg, 100) + "[" + bar + ">" + spaces + "] " + <int> totalCount + "/" + pkgSize);
    }
    io:print("\r" +rightPad( packageName + toAndFrom, (115 + noOfBytesRead.length())) + "\n");
}

function rightPad(string msg, int length) (string ) {
    int i = -1;
    length = length - msg.length();
    string char = " ";
    while (i < length) {
        msg = msg + char;
        i = i + 1;
    }
    return msg;
}

function truncateString (string text) (string){
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