import ballerina.lang.files;
import ballerina.lang.blobs;



function main (string[] args) {
    //Create 'File' struct and open for writing.
    files:File target = {path:"/tmp/result.txt"};
    files:open(target, "w");

    //Here's how you can write a string into a file.
    blob content = "Sample Content".toBlob("utf-8");
    files:write(content, target);
    println("file written: /tmp/result.txt");

    //Close the file once done.
    files:close(target);

    //Check whether the file exists.
    boolean b = files:exists(target);
    println("file existence: " + b);

    //Open file in read mode. Returns the content as blob and the number of bytes read.
    files:open(target, "r");
    var content, n = files:read(target, 100000);
    string s =content.toString("utf-8");
    println("file content: " + s);
    files:close(target);

    //Here's how you can copy a file.
    files:File source = {path:"/tmp/result.txt"};
    files:File destination = {path:"/tmp/copy.txt"};
    files:copy(source, destination);
    println("file copied: /tmp/result.txt to /tmp/copy.txt");

    //How to delete a file.
    files:delete(destination);
    println("file deleted: /tmp/copy.txt");

    //Move source file to destination.
    destination = {path:"/tmp/move.txt"};
    files:move(source, destination);
    println("file moved: /tmp/result.txt to /tmp/move.txt");

    files:delete(destination);
    println("file deleted: /tmp/move.txt");
}
