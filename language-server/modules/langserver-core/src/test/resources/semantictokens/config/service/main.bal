import serviceProject.http;
import serviceProject.time;

type Type int|time:TimeZone|time:Error;

type Employee record {
    readonly string name;
    int salary;
};

service / on new http:Listener(8080) {

    int salary = 100;
    resource function get find/[string first]/[string second]() returns string {
        Employee emp = {name: "John", "salary": self.salary};
        emp["salary"] = 200;
        return "First: " + first + ", and second: " + second;
    }

    resource function 'default [string... paths]() returns string {
        return "";
    }
}
