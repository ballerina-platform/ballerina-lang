function main (string[] args) {
    string[] texts = null;
    //Use a try block to surrounds a code segment that an error may occur.
    try {
        println("start Accessing texts");
        //Accessing a null variable 'texts' causes a NullReferenceError to be thrown.
        string value = texts[0];
        println(value);
        //A Catch block executes, when an error is thrown from the enclosing try
        //block and the thrown error type and catch clause's error type are matched, or
        //if there is no match, then the catch is the first in the order, where thrown
        //error type and catch clause's error type are structurally equivalent.
    } catch (error err) {
        println("error occured: " + err.msg);
    //Catching specific error type 'NullReferenceException'.
    } catch (NullReferenceException err) {
        println("NullReferenceException error occurred");
    } finally {
        println("finally Block executed");
    }
}