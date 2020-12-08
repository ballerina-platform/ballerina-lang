import ballerina/lang.test;

listener test:MockListener echoEP = new(9090);
listener test:MockListener echoEP2 = new(9091);

# PizzaService HTTP Service
service /PizzaService on echoEP {

    # Check orderPizza resource.
    # + conn - HTTP connection.
    # + req - In request.
    resource function get orderPizza(string conn, string req) {
    }

    # Check status resource.
    # + conn - HTTP connection.
    # + req - In request.
    resource function get checkStatus(string conn, string req) {

    }
}

# Test type `typeDef`
# Test service `helloWorld`
# Test variable `testVar`
# Test var `testVar`
# Test function `add`
# Test parameter `x`
# Test const `constant`
# Test annotation `annot`
service /PizzaService2 on echoEP2 {

    # Check orderPizza resource.
    # + conn - HTTP connection.
    # + req - In request.
    resource function get orderPizza(string conn, string req) {

    }

    # Check status resource.
    # + conn - HTTP connection.
    # + req - In request.
    resource function get checkStatus(string conn, string req) {

    }
}
