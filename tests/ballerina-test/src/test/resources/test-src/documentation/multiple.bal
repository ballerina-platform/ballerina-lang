import ballerina/net.http;

documentation { Documentation for Test annotation
F{{a}} annotation `field a` documentation
F{{b}} annotation `field b` documentation
F{{c}} annotation `field c` documentation}
annotation Test {
    string a;
    string b;
    string c;
}

documentation { Documentation for state enum
F{{foo}} enum `field foo` documentation
F{{bar}} enum `field bar` documentation}
enum state {
    foo,
    bar
}

documentation { Documentation for Test struct
F{{a}} struct `field a` documentation
F{{b}} struct `field b` documentation
F{{c}} struct `field c` documentation}
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
 T{{p}} input struct Person source used for transformation
 T{{e}} output struct Employee struct which Person transformed to
 P{{defaultAddress}} address which serves Eg: `POSTCODE 112`
}
transformer <Person p, Employee e> Foo(any defaultAddress) {
    e.name = p.firstName;
}

documentation {PizzaService HTTP Service}
service<http> PizzaService {

    documentation {Check orderPizza resource. P{{conn}} HTTP connection. P{{req}} In request.}
    resource orderPizza(http:Connection conn, http:Request req) {
        http:Response res = {};
        _ = conn.respond(res);
    }

    documentation {Check status resource. P{{conn}} HTTP connection. P{{req}} In request.}
    resource checkStatus(http:Connection conn, http:Request req) {
        http:Response res = {};
        _ = conn.respond(res);
    }
}

