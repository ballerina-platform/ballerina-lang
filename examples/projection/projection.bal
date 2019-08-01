import ballerina/io;
import ballerina/runtime;

// Creates a `record` type that represents the `Person`.
type Person record {
    string name;
    int age;
    string status;
    string address;
    string phoneNo;
};

// Creates a `record` type that represents the `Message`.
type Message record {
    string name;
    string address;
    string message;
};


int index = 0;

// These are streams that are based on the constraint types created above.
stream<Person> personStream = new;
stream<Message> childrenMessageStream = new;

Message[] globalChildrenMessageArray = [];

function initProjectionQuery() {
    // This is a streaming query that filters events based on the attribute age
    // and generates a custom message by calling a user-defined function.
    // The filtered events are pushed to a `stream` called `childrenMessageStream`.
    forever {
        from personStream where personStream.age <= 16
        select personStream.name, personStream.address,
                generateChildrenDayWishMessage(personStream.name) as message
        => (Message[] messages) {
            foreach var m in messages {
                childrenMessageStream.publish(m);
            }
        }
    }
}

function generateChildrenDayWishMessage(string name) returns string {
    string template = string `Hi ${name}!!!`;
    return template + " , wish you a happy children's day";
}

public function main() {

    // Sample events that represent a different person.
    Person[] personArray = [];
    Person t1 = { name: "Raja", age: 12, status: "single",
                    address: "Mountain View", phoneNo: "+19877386134" };
    Person t2 = { name: "Mohan", age: 30, status: "single",
                    address: "Memphis", phoneNo: "+198353536134"};
    Person t3 = { name: "Shareek", age: 16, status: "single",
                    address: "Houston", phoneNo: "+1343434454" };

    personArray[0] = t1;
    personArray[1] = t2;
    personArray[2] = t3;

    // Deploys the projection streaming query.
    initProjectionQuery();

    // The `childrenMessageStream` subscribes to the `printChildrenMessage()` function. Whenever the
    // `childrenMessageStream` stream receives a valid event, this function is called.
    childrenMessageStream.subscribe(printChildrenMessage);

    // Simulates the sample event that represents the `Person`.
    foreach var t in personArray {
        personStream.publish(t);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalChildrenMessageArray.length()) == 2 || count == 10) {
            break;
        }
    }
}

function printChildrenMessage(Message msg) {
    io:println("Child name : ", msg.name, " and message : ", msg.message);
    addToGlobalChildrenMessageArray(msg);
}

function addToGlobalChildrenMessageArray(Message e) {
    globalChildrenMessageArray[index] = e;
    index = index + 1;
}
