function main (string[] args) {
    //JSON Arrays. They are arrays of any JSON value.
    json j1 = [1, false, null, "foo",
               {first:"John", last:"Pala"}];
    println(j1);

    // Access JSON array elements by index.
    json j2 = j1[4];
    println(j2.first);

    // Add or change elements in a JSON array.
    j1[4] = 8.00;
    println(j1);

    // JSON array in an object literal.
    json p = {
                 fname:"John", lname:"Stallone",
                 family:[{fname:"Peter", lname:"Stallone"},
                         {fname:"Emma", lname:"Stallone"},
                         {fname:"Jena", lname:"Stallone"},
                         {fname:"Paul", lname:"Stallone"}]
             };
    p.family[2].fname = "Alisha";
    println(p);

    // Get the length of the JSON array.
    json family = p.family;
    int l = lengthof family;
    println("length of array: " + l);

    // Loop through the array.
    int i = 0;
    while (i < l) {
        println(family[i]);
        i = i + 1;
    }
}
