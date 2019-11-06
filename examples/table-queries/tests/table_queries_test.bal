import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrintln(any... s) {
    foreach var arg in s {
        outputs[counter] = arg;
        counter += 1;
    }
}

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "print"
}
public function mockPrint(any... s) {
    foreach var arg in s {
        outputs[counter] = arg;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();
    test:assertEquals(string.convert(outputs[1]), "The personTable:  ");
    test:assertEquals(string.convert(outputs[2]),
    "[{\"id\":1, \"age\":25, \"salary\":1000.5, \"name\":\"jane\", \"married\":true}, " +
    "{\"id\":2, \"age\":26, \"salary\":1050.5, \"name\":\"kane\", \"married\":false}, " +
    "{\"id\":3, \"age\":27, \"salary\":1200.5, \"name\":\"jack\", \"married\":true}, " +
    "{\"id\":4, \"age\":28, \"salary\":1100.5, \"name\":\"alex\", \"married\":false}]");

    test:assertEquals(string.convert(outputs[4]), "The orderTable: ");
    test:assertEquals(string.convert(outputs[5]),
    "[{\"personId\":1, \"orderId\":1234, \"items\":\"pen, book, eraser\", \"amount\":34.75}, " +
    "{\"personId\":1, \"orderId\":2314, \"items\":\"dhal, rice, carrot\", \"amount\":14.75}, " +
    "{\"personId\":2, \"orderId\":5643, \"items\":\"Macbook Pro\", \"amount\":2334.75}, " +
    "{\"personId\":3, \"orderId\":8765, \"items\":\"Tshirt\", \"amount\":20.75}]");

    test:assertEquals(string.convert(outputs[6]), "\ntable<Person> personTableCopy = from personTable select *;");
    test:assertEquals(string.convert(outputs[7]), "personTableCopy: ");

    test:assertEquals(string.convert(outputs[8]),
    "[{\"id\":1, \"age\":25, \"salary\":1000.5, \"name\":\"jane\", \"married\":true}, " +
    "{\"id\":2, \"age\":26, \"salary\":1050.5, \"name\":\"kane\", \"married\":false}, " +
    "{\"id\":3, \"age\":27, \"salary\":1200.5, \"name\":\"jack\", \"married\":true}, " +
    "{\"id\":4, \"age\":28, \"salary\":1100.5, \"name\":\"alex\", \"married\":false}]");

    test:assertEquals(string.convert(outputs[9]),
    "\ntable<Person> orderedPersonTable = from personTable select * order by salary;");
    test:assertEquals(string.convert(outputs[10]), "orderedPersonTable: ");

    test:assertEquals(string.convert(outputs[11]),
    "[{\"id\":1, \"age\":25, \"salary\":1000.5, \"name\":\"jane\", \"married\":true}, " +
    "{\"id\":2, \"age\":26, \"salary\":1050.5, \"name\":\"kane\", \"married\":false}, " +
    "{\"id\":4, \"age\":28, \"salary\":1100.5, \"name\":\"alex\", \"married\":false}, " +
    "{\"id\":3, \"age\":27, \"salary\":1200.5, \"name\":\"jack\", \"married\":true}]");

    test:assertEquals(string.convert(outputs[12]),
    "\ntable<Person> personTableCopyWithFilter = from personTable where name == 'jane' select *;");
    test:assertEquals(string.convert(outputs[13]), "personTableCopyWithFilter: ");

    test:assertEquals(string.convert(outputs[14]), "[{\"id\":1, \"age\":25, \"salary\":1000.5, \"name\":\"jane\"," +
    " \"married\":true}]");

    test:assertEquals(string.convert(outputs[15]),
    "\ntable<PersonPublicProfile > childTable = from personTable select name as knownName, age;");
    test:assertEquals(string.convert(outputs[16]), "childTable: ");

    test:assertEquals(string.convert(outputs[17]),
    "[{\"knownName\":\"jane\", \"age\":25}, " +
    "{\"knownName\":\"kane\", \"age\":26}, " +
    "{\"knownName\":\"jack\", \"age\":27}, " +
    "{\"knownName\":\"alex\", \"age\":28}]");

    test:assertEquals(string.convert(outputs[18]),
    "\ntable<SummedOrder> summedOrderTable = from orderTable select personId, sum(amount) group by personId;");
    test:assertEquals(string.convert(outputs[19]), "summedOrderTable: ");

    test:assertEquals(string.convert(outputs[20]),
    "[{\"personId\":1, \"amount\":49.5}, " +
    "{\"personId\":2, \"amount\":2334.75}, " +
    "{\"personId\":3, \"amount\":20.75}]");

    test:assertEquals(string.convert(outputs[22]), "orderDetailsTable: ");

    test:assertEquals(string.convert(outputs[23]),
    "[{\"orderId\":1234, \"personName\":\"jane\", \"items\":\"pen, book, eraser\", \"amount\":34.75}, " +
    "{\"orderId\":2314, \"personName\":\"jane\", \"items\":\"dhal, rice, carrot\", \"amount\":14.75}, " +
    "{\"orderId\":5643, \"personName\":\"kane\", \"items\":\"Macbook Pro\", \"amount\":2334.75}, " +
    "{\"orderId\":8765, \"personName\":\"jack\", \"items\":\"Tshirt\", \"amount\":20.75}]");

    test:assertEquals(string.convert(outputs[25]), "orderDetailsWithFilter: ");

    test:assertEquals(string.convert(outputs[26]),
    "[{\"orderId\":5643, \"personName\":\"kane\", \"items\":\"Macbook Pro\", \"amount\":2334.75}]");
}


