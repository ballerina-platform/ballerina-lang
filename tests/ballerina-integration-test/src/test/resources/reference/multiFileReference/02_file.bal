import ballerina/http;

Person person = {
    name: "Sumedha",
    age: 30
};

public (Employee, Person) pp2 = (employee, person);

//service s on new http:Listener(8080) {
//    resource function k(http:Caller caller, http:Request r) {
//        http:Response res = new;
//
//        var p = multiFileReference:employee.name;
//        res.setTextPayload("hell0 " + p);
//
//        var respi = caller->respond(res);
//    }
//}
