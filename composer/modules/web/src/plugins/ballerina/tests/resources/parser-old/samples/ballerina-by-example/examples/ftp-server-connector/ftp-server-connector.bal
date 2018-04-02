import ballerina/net.ftp;
import ballerina/lang.messages;
import ballerina/lang.files;
import ballerina/lang.blobs;
import ballerina/lang.strings;
import ballerina/lang.system;

@ftp:configuration {
    dirURI:"ftp://127.0.0.1/observed-dir/",
    pollingInterval:"2000",
    actionAfterProcess:"MOVE",
    moveAfterProcess:"ftp://127.0.0.1/move-to/",
    parallel:"false",
    createMoveDir:"true"
}
service<ftp> ftpServerConnector {
    resource fileResource (message m) {
        // Use the FTP Client Connector to connect to a remote FTP server.
        ftp:ClientConnector c = create ftp:ClientConnector();

        // Create a File struct using the URL returned by 'm'.
        string url = messages:getStringPayload(m);
        files:File file = {path:url};

        // Read the specified file and get its string content.
        blob txt = c.read(file);
        string content = blobs:toString(txt, "UTF-8");

        // Print the content of the file to the console.
        system:println("Content of the file at: " + url);
        system:println(content);

        // Append to the content and convert it to a blob.
        content = content + "ballerina";
        blob output = strings:toBlob(content, "UTF-8");

        // A File struct for the pointing to the location.
        files:File target = {path:
                             "ftp://127.0.0.1/another-dir/current-output.txt"};

        // Write that content to another remote location.
        c.write(output, target);

        response:send(m);
    }
}
