import ballerina.lang.system;
import ballerina.net.ftp;
import ballerina.lang.files;
import ballerina.lang.blobs;
import ballerina.lang.strings;

function main (string[] args) {
    // Create an FTP Client Connector
    ftp:ClientConnector c = create ftp:ClientConnector();

    // Checking if a specified file exists.
    files:File target = {path:"ftp://127.0.0.1/ballerina-user/aa.txt"};
    boolean filesExists = ftp:ClientConnector.exists(c, target);
    system:println("File exists: " + filesExists);

    // Creating a new directory at a remote location.
    files:File newDir = {path:"ftp://127.0.0.1/ballerina-user/new-dir/"};
    ftp:ClientConnector.createFile(c, newDir, "folder");

    // Reading a file in a remote directory.
    files:File txtFile = {path:"ftp://127.0.0.1/ballerina-user/bb.txt"};
    blob contentB = ftp:ClientConnector.read(c, txtFile);
    system:println(blobs:toString(contentB, "UTF-8"));

    // Copying a remote file to another location.
    files:File copyOfTxt = {path:
                            "ftp://127.0.0.1/ballerina-user/new-dir/copy-of-bb.txt"};
    ftp:ClientConnector.copy(c, txtFile, copyOfTxt);

    // Moving a remote file to another location.
    files:File mvSrc = {path:"ftp://127.0.0.1/ballerina-user/aa.txt"};
    files:File mvTarget = {path:
                           "ftp://127.0.0.1/ballerina-user/move/moved-aa.txt"};
    ftp:ClientConnector.move(c, mvSrc, mvTarget);

    // Deleting a specified remote file.
    files:File del = {path:"ftp://127.0.0.1/ballerina-user/cc.txt"};
    ftp:ClientConnector.delete(c, del);

    // Writing to a remote file.
    files:File wrt = {path:"ftp://127.0.0.1/ballerina-user/dd.txt"};
    blob contentD = strings:toBlob("Hello World!", "UTF-8");
    ftp:ClientConnector.write(c, contentD, wrt);
}
