import ballerina/io;
import ballerina/math;
import ballerina/task;

public type Person record {
    string name;
    int age;
    int maxAge;
};

boolean runService = true;

public function main() {
    int interval = 1000;
    task:Listener timer = new({ interval: interval });
    int maxAge = math:randomInRange(10, 15);
    Person person = { name: "Sam", age: 0, maxAge: maxAge };
    _ = timer.attach(timerService, serviceParameter = person);
    _ = timer.start();

    while (runService) {
        // Wai for the service to stop
    }
    io:println("Service stopped");
    _ = timer.cancel();
}

service timerService = service {
    resource function onTrigger(Person person) returns error? {
        person.age = person.age + 1;
        if (person.age == person.maxAge) {
            error e = error("Intended error");
            return e;
        }
        io:println("Person Name: " + person.name + " Age: " + person.age);
    }

    resource function onError(error e, Person person) {
        io:println(e);
        io:println("Stooping " + person.name + " at Person age: " + person.age);
        runService = false;
    }
};
