import ballerina.lang.string;
import ballerina.lang.system;

function main (string[] args) {

    //following are the basic functions of string.

    system:println( string:contains("test", "es"));             //prints true.
    system:println( string:equalsIgnoreCase("test", "Test"));   //prints true.
    system:println( string:hasPrefix("test", "te"));            //prints true.
    system:println( string:hasSuffix("test", "st"));            //prints true.
    system:println( string:indexOf("test", "e"));               //prints 1.
    system:println( string:lastIndexOf("test", "t"));           //prints 3.
    system:println( string:replace("test", "est", "one"));      //prints tone.
    system:println( string:replaceAll("test", "t", "one"));     //prints oneesone.
    system:println( string:replaceFirst("test", "t", "one"));   //prints oneest.
    system:println( string:toLowerCase("TEST"));                //prints test.
    system:println( string:toUpperCase("test"));                //prints TEST.
    system:println( string:trim(" test "));                     //prints test.
    //accepts values of int, long, float, double, boolean, string, xml, json
    system:println( string:valueOf(1000));                      //prints 1000.
    system:println( string:length("test"));                     //prints 4.
    system:println( string:unescape("\"test\""));               //prints "test".

}
