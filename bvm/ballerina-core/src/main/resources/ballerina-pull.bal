import ballerina.io;
import ballerina.net.http;
import ballerina.runtime;
import ballerina.file;

function pullPackage (string url, string destDirPath, string fullPkgPath, string fileSeparator) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(url, getConnectorConfigs());
    }
    http:OutRequest req = {};
    req.addHeader("Accept-Encoding", "identity");
    var resp, errRes = httpEndpoint.get("", req);
    if (errRes != null) {
        error err = {message:errRes.message};
        throw err;
    }
    if (resp.statusCode != 200) {
        var jsonResponse, errJson = resp.getJsonPayload();
        if (errJson != null) {
            error err = {message:errJson.message};
            throw err;
        } else {
            io:println(jsonResponse.msg.toString());
        }
    } else {
        var pkgSize, conversionErr = <int>resp.getHeader("content-length");
        var sourceChannel, sourceChannelErr = resp.getByteChannel();
        if (sourceChannelErr != null) {
            error err = {message:sourceChannelErr.message};
            throw err;
        }

        // Get the package version from the canonical header of the response
        string headerValue = resp.getHeader("Link");
        string canonicalLinkURL = headerValue.subString(headerValue.indexOf("<") + 1, headerValue.indexOf(">"));
        string pkgVersion = canonicalLinkURL.subString(canonicalLinkURL.lastIndexOf("/") + 1, canonicalLinkURL.length());

        string pkgName = fullPkgPath.subString(fullPkgPath.lastIndexOf("/") + 1, fullPkgPath.length());
        fullPkgPath = fullPkgPath + ":" + pkgVersion;

        // Create the version directory
        destDirPath = destDirPath + fileSeparator + pkgVersion;
        createDirectories(destDirPath);

        string archiveFileName = pkgName + ".zip";
        string destArchivePath = destDirPath  + fileSeparator + archiveFileName;

        io:ByteChannel destDirChannel = getFileChannel(destArchivePath, "w");
        string toAndFrom = " [central.ballerina.io -> home repo]";

        io:IOError destDirChannelCloseError;
        io:IOError srcCloseError;
        copy(pkgSize, sourceChannel, destDirChannel, fullPkgPath, toAndFrom);
        if (destDirChannel != null) {
            destDirChannelCloseError = destDirChannel.close();
        }
        srcCloseError = sourceChannel.close();
    }
}

function main(string[] args){
    pullPackage(args[0], args[1], args[2], args[3]); 
}

function getFileChannel (string filePath, string permission) (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    return channel;
}

function readBytes (io:ByteChannel channel, int numberOfBytes) (blob, int) {
    blob bytes;
    int numberOfBytesRead;
    io:IOError readError;

    bytes, numberOfBytesRead, readError = channel.read(numberOfBytes, 0);
    if (readError != null) {
        throw readError.cause;
    }
    return bytes, numberOfBytesRead;
}

function writeBytes (io:ByteChannel channel, blob content, int startOffset, int size) (int) {
    int numberOfBytesWritten;
    io:IOError writeError;
    numberOfBytesWritten, writeError = channel.write(content, startOffset, size);
    if (writeError != null) {
        throw writeError.cause;
    }
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
    boolean done = false;

    try {
        while (!done) {
            readContent, readCount = readBytes(src, bytesChunk);
            if (readCount <= 0) {
                done = true;
            }
            if (dest != null) {
                numberOfBytesWritten = writeBytes(dest, readContent, 0, readCount);
            }
            totalCount = totalCount + readCount;
            float percentage = totalCount / pkgSize;
            runtime:sleepCurrentWorker(100);
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

function rightPad (string msg, int length) (string) {
    int i = -1;
    length = length - msg.length();
    string char = " ";
    while (i < length) {
        msg = msg + char;
        i = i + 1;
    }
    return msg;
}

function truncateString (string text) (string) {
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

function getConnectorConfigs() (http:Options) {
    http:Options option = {
                              ssl: {
                                       trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                                       trustStorePassword:"ballerina"
                                   },
                              followRedirects: {}
                          };
    return option;
}

function createDirectories (string directoryPath) {
    file:AccessDeniedError adError;
    file:IOError ioError;
    boolean folderCreated;

    file:File folder = {path:directoryPath};
    if (!folder.exists()) {
        folderCreated, adError, ioError = folder.mkdirs();
    }
    if (folderCreated) {
        if (adError != null) {
            error err = {message:"Error occurred while creating the directories", cause:adError.cause};
            throw err;
        } else if (ioError != null) {
            error err = {message:"Error occurred while creating the directories", cause:ioError.cause};
            throw err;
        }
    }
}
