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
boolean exitLoop = true;

public function main() {
    // Interval in which the timer should trigger.
    int interval = 1000;

    // Initialize the timer scheduler using the interval value.
    // Delay will be equal to the interval as we do not mention the delay here.
    task:Scheduler timer = new({ interval: interval });

    // Define a person object
    Person person = { name: "Kurt Kobain", age: 0, maxAge: 27 };

    // Attaching the service to the timer. This will not start the timer.
    // But it will attach the service to the timer, and also passes the
    // person object into the timer service.
    // Defaultable `serviceParameter` will pass the object into the resources
    // if it is set.
    _ = timer.attachService(lifeService, serviceParameter = person);
    _ = timer.attachService(musicService, serviceParameter = person);

    // Start the timer.
    _ = timer.run();

    // While loop will stop function from exiting until the service ends.
    while (exitLoop) {
        // Wait for the service to stop
    }

    // Cancel the timer. This will stop the timer and all the services
    // attached to it.
    _ = timer.cancel();

    io:println("End.");
}

// Service which will be attached to the timer.
service lifeService = service {
    // On trigger resource which will trigger when timer runs off.
    // Note the usage of Person object passing inside the function, which we
    // attached with the timer.
    resource function onTrigger(Person person) returns error? {
        person.age = person.age + 1;
        if (person.age == person.maxAge) {
            error e = error(" died at ");
            return e;
        }
        io:println(person.name + " is " + person.age + " years old now.");
    }

    // This will trigger when an error occurs inside the onTrigger() resource.
    resource function onError(error e, Person person) {
        exitLoop = false;
        io:println(person.name + <string> e.reason() + person.age);
    }
};

service musicService = service {
    resource function onTrigger(Person person) returns error? {
        if (person.age > 16) {
            io:println(person.name + " is doing music at age " + person.age);
        }
    }

    // This will trigger when an error occurs inside the onTrigger() resource.
    resource function onError(error e, Person person) {
        exitLoop = false;
        io:println(person.name + <string> e.reason() + person.age);
    }
};
