import ballerina.lang.files;
import ballerina.lang.blobs;
import ballerina.lang.system;
import ballerina.lang.strings;

function main(string[] args) {

    blob content;
    //Create file struct
    files:File target = {path : "/tmp/result.txt"};
    //Open file in write mode
    files:open(target, "w");
    //Prepare blob content to write
    content = strings:toBlob("Sample Content", "utf-8");
    //Write file
    files:write(content, target);
    system:println("file written: /tmp/result.txt");
    //Close the file once done
    files:close(target);

    //Get a boolean value whether the file exists.
    boolean b = files:exists(target);
    system:println("file existence: " + b);

    int n;
    //Open file in read mode
    files:open(target, "r");
    //Reads file and returns blob value and number of bytes read
    content,n = files:read(target, 100000);
    //Convert returned blob value to a string
    string s = blobs:toString(content, "utf-8");
    //Print read content
    system:println("file content: " + s);
    files:close(target);

    files:File source = {path : "/tmp/result.txt"};
    files:File destination = {path : "/tmp/copy.txt"};
    //Copy source file to destination
    files:copy(source, destination);
    system:println("file copied: /tmp/result.txt to" +
                   " /tmp/copy.txt");

    //Delete a file by giving the file struct.
    files:delete(destination);
    system:println("file deleted: /tmp/copy.txt");

    destination = {path : "/tmp/move.txt"};
    //Move source file to destination
    files:move(source, destination);
    system:println("file moved: /tmp/result.txt to" +
                   " /tmp/move.txt");

    files:delete(destination);
    system:println("file deleted: /tmp/move.txt");
}
