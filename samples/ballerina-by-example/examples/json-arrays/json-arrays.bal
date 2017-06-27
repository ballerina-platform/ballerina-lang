import ballerina.lang.system;

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
}
