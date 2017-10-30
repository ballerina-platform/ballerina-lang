import ballerina.lang.files;

function main (string[] args) {
    string[] data = null;
    // Suppose we are creating a file, and writing some data.
    files:File target = {path:"/tmp/result.txt"};
    //Use a try block to surrounds a code segment that an error may occur.
    try {
        files:open(target, "w");
        string content= "Content1";
        blob c1 = content.toBlob("utf-8");
        files:write(c1, target);
        println("content is written to file");

        //Accessing a null variable 'data' cause NullReferenceError to be thrown.
        blob c2 = data[0].toBlob("utf-8");
        //None of the following lines will get executed after the error.
        files:write(c2, target);
        println("content is written to file");
        //A Catch block executes, when an error is thrown from the enclosing try
        //block and the thrown error type and catch clause's error type are matched, or
        //if there is no match, then the catch is the first in the order, where thrown
        //error type and catch clause's error type are structurally equivalent.
    } catch (error err) {
        println("error: " + err.msg);
    //Catching specific error type 'NullReferenceException'.
    } catch (NullReferenceException err) {
        println("NullReferenceException error occurred");
    } finally {
        //Closing the file.
        files:close(target);
        println("File Closed");
    }
}