import ballerina/io;
import ballerina/net.ftp;
import ballerina/lang.files;
import ballerina/lang.blobs;


function main(string... args) {

    // Creating a client endpoint that is used to connect to the FTP server.
    endpoint ftp:Client clientEndpoint {
        protocol: "sftp",
        host: "localhost",
        username: "ftpUser",
        passPhrase: "helloWorld"
    };

    // Checking if a specified file exists in the local machine.
    file:File newDir = {path: "/ballerina-user/README.txt"};
    boolean filesExists;
    filesExists = check clientEndpoint->exists(newDir);
    io:println("File exists: " + filesExists);

    // Creating a new directory in a remote location.
    files:File newDir = {path: "/ballerina-user/new-dir/"};
    clientEndpoint->createFile(newDir, false);

    // Reading a file that is in a remote directory.
    files:File txtFile = {path: "/ballerina-user/final-results.txt"};
    io:ByteChannel channel = check clientEndpoint->read(txtFile);

    // Copying a remote file to another location.
    files:File copyOfTxt = {path: "/ballerina-user/backup/copy-final-results.txt"};
    clientEndpoint->copy(txtFile, copyOfTxt);

    // Moving a remote file to another location.
    files:File mvSrc = {path: "/ballerina-user/aa.txt"};
    files:File mvTarget = {path: "/ballerina-user/move/moved-aa.txt"};
    clientEndpoint->move(mvSrc, mvTarget);

    // Deleting a specified remote file.
    files:File del = {path: "/ballerina-user/cc.txt"};
    clientEndpoint->delete(del);

    // Writing to a remote file.
    files:File wrt = {path: "/ballerina-user/dd.txt"};
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");
    clientEndpoint->write(content, wrt, "o");
}
