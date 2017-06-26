import ballerina.lang.errors;
import ballerina.lang.files;
import ballerina.lang.system;
import ballerina.lang.strings;

function main (string[] args) {
    string[] data = null;
    // Suppose we are creating a file, and writing some data.
    files:File target = {path:"/tmp/result.txt"};
    //Use a try block to surrounds a code segment that an error may occur.
    try {
        files:open(target, "w");
        blob content1 = strings:toBlob("Content1", "utf-8");
        files:write(content1, target);
        system:println("content1 is written to file");

        //Accessing a null variable 'data' cause NullReferenceError to be thrown.
        blob content2 = strings:toBlob(data[0], "utf-8");
        //Non of the following lines will get executed after the error.
        files:write(content2, target);
        system:println("content2 is written to file");
    //Use Ballerina 'ballerina.lang.errors:Error' type to catch any error.
    } catch (errors:Error err) {
        system:println("Caught error " + err.msg);
        return;
    //Catching specific error type 'ballerina.lang.errors:NullReferenceError'.
    } catch (errors:NullReferenceError err) {
        //This catch clause get executed, as the thrown error type is NullReferenceError.
        system:println("Caught NullReferenceError. Exiting program");
        return;
    } finally {
        //Closing the file.
        files:close(target);
        system:println("File Closed");
    }
}