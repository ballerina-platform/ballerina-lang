import ballerina.io;
import ballerina.net.ftp;
import ballerina.lang.files;
import ballerina.lang.blobs;


function main (string[] args) {
    endpoint<ftp:ClientConnector> ftpEp {
        create ftp:ClientConnector();
    }

    // Checking if a specified file exists.
    files:File target = {path:"ftp://127.0.0.1/ballerina-user/aa.txt"};
    boolean filesExists = ftpEp.exists(target);
    io:println("File exists: " + filesExists);

    // Creating a new directory at a remote location.
    files:File newDir = {path:"ftp://127.0.0.1/ballerina-user/new-dir/"};
    ftpEp.createFile(newDir, "folder");

    // Reading a file in a remote directory.
    files:File txtFile = {path:"ftp://127.0.0.1/ballerina-user/bb.txt"};
    blob contentB = ftpEp.read(txtFile);
    io:println(blobs:toString(contentB, "UTF-8"));

    // Copying a remote file to another location.
    files:File copyOfTxt = {path:
                            "ftp://127.0.0.1/ballerina-user/new-dir/copy-of-bb.txt"};
    ftpEp.copy(txtFile, copyOfTxt);

    // Moving a remote file to another location.
    files:File mvSrc = {path:"ftp://127.0.0.1/ballerina-user/aa.txt"};
    files:File mvTarget = {path:
                           "ftp://127.0.0.1/ballerina-user/move/moved-aa.txt"};
    ftpEp.move(mvSrc, mvTarget);

    // Deleting a specified remote file.
    files:File del = {path:"ftp://127.0.0.1/ballerina-user/cc.txt"};
    ftpEp.delete(del);

    // Writing to a remote file.
    files:File wrt = {path:"ftp://127.0.0.1/ballerina-user/dd.txt"};
    blob contentD = "Hello World!".toBlob("UTF-8");
    ftpEp.write(contentD, wrt);
}
