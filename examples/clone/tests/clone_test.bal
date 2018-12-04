import ballerina/test;

@test:Config
function testFunc() {
    // Invoke the main function.
    (Person, Person, string)? result = main();

    // Check if the returned value of `main` function is of type `(Person, Person, string)`
    if (result is (Person, Person, string)) {
        string refCheck = "Source and Clone are at two different memory locations";
        test:assertEquals(result[0].name, result[1].name);
        test:assertEquals(result[0].age, result[1].age);
        test:assertEquals(result[0].married, result[1].married);
        test:assertEquals(result[0].salary, result[1].salary);
        test:assertEquals(result[0].address.country, result[1].address.country);
        test:assertEquals(result[0].address.state, result[1].address.state);
        test:assertEquals(result[0].address.city, result[1].address.city);
        test:assertEquals(result[0].address.street, result[1].address.street);
        test:assertTrue(result[1] !== result[0]);
        test:assertEquals(result[2], refCheck);
    } else {
        test:assertFail();
    }
}
