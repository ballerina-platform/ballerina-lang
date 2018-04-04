import images.jpeg;
import images.gif;
import images.png;
import formats.yaml;
import ballerina/http;

@jpeg:IConfig {
    value: "routerFunc"
}
@yaml:YAMLConfig {
    value: "routerFunc"
}
public function routerFunc() {
}

@jpeg:IData {
    value: "routerService"
}
@gif:IData {
    value: "routerService"
}
@png:IData {
    value: "routerService"
}
service<http> routerService {

    @gif:IConfig {
        value: "route"
    }
    @png:IConfig {
        value: "route"
    }
    resource route(http:Connection conn, http:Request req) {

    }

    resource routeRes(http:Connection conn, http:Request req) {

    }
}

@gif:IData {
    value: "routerService2"
}
@png:IData {
    value: "routerService2"
}
service<http> routerService2 {

    @gif:IConfig {
        value: "route2"
    }
    resource route2(http:Connection conn, http:Request req) {

    }
}

@png:IData {
    value: "routeCon"
}
public connector routeCon() {

    @jpeg:IConfig {
        value: "getRoutes"
    }
    @yaml:YAMLConfig {
        value: "routerFunc"
    }
    action getRoutes() {

    }
}


@png:IData {
    value: "RouteConfig"
}
struct RouteConfig {

}



@gif:EnumData {
    value: "state"
}
enum state {
    INITIALIZED,
    ACTIVE,
    DESTROYED
}

// TODO Annotations cannot be attached to global variables at the moment.
//@png:Element {
//    value: "a"
//}
//int a = 5;


@png:Element {
    value: "PI"
}
const float PI = 3.14;


@jpeg:Element {
    value: "RouteData"
}
@png:Element {
    value: "RouteData"
}
public annotation RouteData attach transformer {
    string value;
}



@png:IData {
    value: "Employee"
}
struct Employee {
    string name;
    int age;
    string address;
}

struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
    string street;
}


@jpeg:Element {
    value: "setCityToNewYork"
}
@png:Element {
    value: "setCityToNewYork"
}
transformer <Person p, Employee e> setCityToNewYork() {
    e.name = p.firstName + " " + p.lastName;
    e.age = p.age;
    e.address = p.street + ", " + "New York";
}
