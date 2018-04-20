import ballerina/lang.errors;
import ballerina/lang.files;
import ballerina/lang.system;
import ballerina/lang.strings;

function main (string... args) {
    string... data = null;
    // Suppose we are creating a file, and writing some data.
    files:File target = {path:"/tmp/result.txt"};
    //Use a try block to surrounds a code segment that an error may occur.
    try {
        files:open(target, "w");
        blob c1 = strings:toBlob("Content1", "utf-8");
        files:write(c1, target);
        system:println("content is written to file");

        //Accessing a null variable 'data' cause NullReferenceError to be thrown.
        blob c2 = strings:toBlob(data[0], "utf-8");
        //Non of the following lines will get executed after the error.
        files:write(c2, target);
        system:println("content is written to file");
        //A Catch block executes, when an error is thrown from the enclosing try
        //block and the thrown error type and catch clause's error type are matched, or
        //if there is no match, then the catch is the first in the order, where thrown
        //error type and catch clause's error type are structurally equivalent.
    } catch (errors:Error err) {
        system:println("error: " + err.msg);
        //Catching specific error type 'errors:NullReferenceError'.
    } catch (errors:NullReferenceError err) {
        system:println("error: " + err.msg);
        //Finally block always executes when the execution exits from the try block or
        //catch block when an error occurred. It is also useful, not only in an anomalous
        //situation, where the execution exit the try block using a return, break, abort.
        //Use finally block to do cleanup tasks i.e File Close, SQL Connection Close etc.
    } finally {
        //Closing the file.
        files:close(target);
        system:println("File Closed");
    }
}