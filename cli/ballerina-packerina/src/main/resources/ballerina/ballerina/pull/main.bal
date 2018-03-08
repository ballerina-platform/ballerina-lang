package ballerina.pull;

import ballerina.compression;
import ballerina.file;
import ballerina.io;
import ballerina.net.http;
import ballerina.runtime;

const string FileSeparator = "/";

function pullPackage (string url, string homeRepoDirPath, string pkgName, string projectRepoDirPath,
                      string fullPkgPath, string pkgVersion) (boolean) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(url, {});
    }
    http:OutRequest req = {};
    req.addHeader("Accept-Encoding", "identity");
    var resp, errRes = httpEndpoint.get("", req);
    if (errRes != null) {
        error err = {message:errRes.message};
        throw err;
        return false;
    }
    if (resp.statusCode != 200) {
        var jsonResponse, errJson = resp.getJsonPayload();
        if (errJson != null) {
            error err = {message:errJson.message};
            throw err;
        } else {
            io:println(jsonResponse.msg.toString());
        }
        return false;
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
        var pkgSize, conversionErr = <int>resp.getHeader("content-length");

        boolean homeDirExists = ifFileExists(homeRepoDirPath);
        var sourceChannel, sourceChannelErr = resp.getByteChannel();
        if (sourceChannelErr != null) {
            error err = {message:sourceChannelErr.message};
            throw err;
            return false;
        }
        io:ByteChannel homeDirChannel = null;
        string toAndFrom = " [central.ballerina.io -> home repo]";
        string fileName = pkgName + ".zip";

        // Create directories of the home repo
        createDirectories(homeRepoDirPath);
        string homeRepoFilePath = homeRepoDirPath + FileSeparator + fileName;
        homeDirChannel = getFileChannel(homeRepoFilePath, "w");

        io:ByteChannel projectDirChannel = null;
        if (projectRepoDirPath != null) {
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
        copy(pkgSize, sourceChannel, homeDirChannel, projectDirChannel, fullPkgPath, toAndFrom, pkgName,
             homeRepoDirPath, projectRepoDirPath);
        if (homeDirChannel != null) {
            homeDirChannel.close();
        }
        sourceChannel.close();
        return true;
    }
}

function ifFileExists (string filePath) (boolean) {
    file:File fileDir = {path:filePath};
    return fileDir.exists();
}

function pull (string[] args) (boolean) {
    return pullPackage(args[0], args[1], args[2], args[3], args[4], args[5]);
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
            error err = {message:"I/O error occurred while creating the directories", cause:ioError.cause};
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

function copy (int pkgSize, io:ByteChannel src, io:ByteChannel homeDst, io:ByteChannel projectDst, string fullPkgPath,
               string toAndFrom, string packageName, string homeDirPath, string projectDirPath) {
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
        string bar = equals.subString(0, <int>(percentage * 10));
        string spaces = tabspaces.subString(0, 10 - <int>(percentage * 10));
        io:print("\r" + rightPad(msg, 100) + "[" + bar + ">" + spaces + "] " + <int>totalCount + "/" + pkgSize);
    }

    if (readCount == 0) {
        if (homeDst != null) {
            uncompressFile(homeDirPath, packageName);
        }
        if (projectDst != null) {
            uncompressFile(projectDirPath, packageName);
        }

    }
    io:print("\r" + rightPad(fullPkgPath + toAndFrom, (115 + noOfBytesRead.length())) + "\n");
}

function uncompressFile (string dirPath, string packageName) {
    string packageZipFilePath = dirPath + FileSeparator + packageName + ".zip";
    compression:unzipFile(packageZipFilePath, dirPath, packageName);
    file:File tmpFile = {path:packageZipFilePath};
    tmpFile.delete();
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