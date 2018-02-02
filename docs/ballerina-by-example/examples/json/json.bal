function main (string[] args) {
    // JSON string value.
    json j1 = "Apple";
    println(j1);

    // JSON number value.
    json j2 = 5.36;
    println(j2);

    // JSON true value.
    json j3 = true;
    println(j3);

    // JSON false value.
    json j4 = false;
    println(j4);

    // JSON null value.
    json j5 = null;

    //JSON Objects.
    json j6 = {name:"apple", color:"red", price:j2};
    println(j6);

    //JSON Arrays. They are arrays of any JSON value.
    json j7 = [1, false, null, "foo",
               {first:"John", last:"Pala"}];
    println(j7);
}
