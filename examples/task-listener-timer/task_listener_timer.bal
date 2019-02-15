import ballerina/io;
import ballerina/math;
import ballerina/task;

// Define a custom record type tyo use in timer.
public type Person record {
    string name;
    int age;
    int maxAge;
};

// Use this flag to check whether the service should be stopped or not.
boolean runService = true;

public function main() {
    // Interval in which the timer should trigger.
    int interval = 1000;

    // Initialize the timer using the interval value.
    task:Listener timer = new({ interval: interval });

    // Get random integer, which will trigger stooping of the service.
    // Delay will be equal to the interval as we do not mention the delay here.
    int maxAge = math:randomInRange(10, 15);
    Person person = { name: "Sam", age: 0, maxAge: maxAge };

    // Attaching the service to the timer. This will not start the timer.
    // But it will attach the service to the timer, and also passes the
    // person object into the timer service.
    // Defaultable `serviceParameter` will pass the object into the resources
    // if it is set.
    _ = timer.attach(timerService, serviceParameter = person);

    // Start the timer.
    _ = timer.start();

    // While loop will stop function from exiting untill the service ends.
    while (runService) {
        // Wait for the service to stop
    }

    io:println("Service stopped");

    // Cancel the timer. This will stop the timer and all the services
    // attached to it.
    _ = timer.cancel();
}

// Service which will be attached to the timer.
service timerService = service {
    // On trigger resource which will trigger when timer runs off.
    // Note the usage of Person object passing inside the function, which we
    // attached with the timer.
    resource function onTrigger(Person person) returns error? {
        person.age = person.age + 1;
        if (person.age == person.maxAge) {
            error e = error("Intended error");
            return e;
        }
        io:println("Person Name: " + person.name + " Age: " + person.age);
    }

    // This will trigger when an error occurs inside the onTrigger() resoure.
    resource function onError(error e, Person person) {
        io:println(e);
        io:println("Stooping " + person.name + " at Person age: " + person.age);
        runService = false;
    }
};
