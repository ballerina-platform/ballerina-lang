class Person {
    function foo() {
        bar() // semicolon should recover here
        someString.indexOf() // semicolon should recover here
    }

    function bar() {
        someString.indexOf() // semicolon should recover here
        condition = condition - 1;
    }
}
