import ballerina.net.http;

documentation { Documentation for Test annotation
- #a annotation `field a` documentation
- #b annotation `field b` documentation
- #c annotation `field c` documentation}
annotation Test {
    string a;
    string b;
    string c;
}


documentation { Documentation for state enum
- #foo enum `field foo` documentation
- #bar enum `field bar` documentation}
enum state {
    foo,
    bar
}


documentation { Documentation for Test struct
- #a struct `field a` documentation
- #b struct `field b` documentation
- #c struct `field c` documentation}
struct Test {
    int a;
    int b;
    int c;
}


struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    string address;
    any ageAny;
}

documentation {
 Transformer Foo Person -> Employee
 - #p input struct Person source used for transformation
 - #e output struct Employee struct which Person transformed to
 - #defaultAddress address which serves Eg: `POSTCODE 112`
}
transformer <Person p, Employee e> Foo(any defaultAddress) {
    e.name = p.firstName;
}

documentation { PizzaService HTTP Service }
service<http> PizzaService {

    documentation {
    Check orderPizza resource.
    - #conn HTTP connection.
    - #req In request.
    }
    resource orderPizza(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }

    documentation {
    Check status resource.
    - #conn HTTP connection.
    - #req In request.
    }
    resource checkStatus(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }
}
