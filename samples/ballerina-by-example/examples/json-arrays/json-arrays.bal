import ballerina.lang.system;
import ballerina.lang.errors;
import ballerina.lang.strings;

function main (string[] args) {
    //JSON Arrays. They are arrays of any JSON value.
    json j1 = [1, false, null, "foo",
               {first:"John", last:"Pala"}];
    system:println(j1);

    // Access JSON array elements by index.
    json j2 = j1[4];
    system:println(j2.first);

    // Add or change elements in a JSON array
    j1[4] = 8.00;
    system:println(j1);

    // JSON array in an object literal.
    json p = {fname:"John", lname:"Stallone",
                 family:[{fname:"Peter", lname:"Stallone"},
                         {fname:"Emma", lname:"Stallone"},
                         {fname:"Jena", lname:"Stallone"},
                         {fname:"Paul", lname:"Stallone"}]};
    p.family[2].fname = "Alisha";
    system:println(p);

    int i = 0;
    try {
        json family = p.family;
        while (true) {
            json e = family[i];
            system:println(e);
            i = i + 1;
        }
    } catch (errors:Error e) {
        string msg = e.msg;
        if (!strings:contains(msg, "array index out of range")) {
            system:println(msg);
            throw e;
        } else {
            system:println("length of array: " + i);
            // Ignore the error.
        }
    }
}
