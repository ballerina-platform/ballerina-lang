import ballerina/io;
import ballerina/runtime;

// Create a record type named `StatusCount`.
type StatusCount record {
    string status;
    int totalCount;
};

// Create a record type named `Teacher` and define the attributes.
type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

function testAggregationQuery(
    stream<StatusCount> filteredStatusCountStream,
    stream<Teacher> teacherStream) {
    // Create a forever statement block with an appropriate streaming query.
    // Write a query to filter teachers who are older than 18 years, wait for the stream to collect three teacher
    // objects, group the 3 teachers based on their marital status, and then obtain the 
    // unique marital status counts.
    // Once the query is executed, publish the result to the `filteredStatusCountStream` stream.
    forever {
        from teacherStream where age > 18 window lengthBatch(3)
        select status, count(status) as totalCount
        group by status
        having totalCount > 1
        => (StatusCount[] status) {
            filteredStatusCountStream.publish(status);
        }
    }
}

function main(string... args) {
    // Create a stream that is constrained by the `StatusCount` struct type.
    stream<StatusCount> filteredStatusCountStream;

    // Create a stream that is constrained by the `Teacher` struct type.
    stream<Teacher> teacherStream;

    //Invoke the method that contains the forever streaming statement.
    testAggregationQuery(filteredStatusCountStream, teacherStream);

    // Create sample events, and send the events to the `teacherStream` input stream.
    Teacher t1 = {name: "Sam", age: 25, status: "single",
        batch: "LK2014", school: "Hampden High School"};
    Teacher t2 = {name: "Jordan", age: 33, status: "single",
        batch: "LK1998", school: "Columbia High School"};
    Teacher t3 = {name: "Morgan", age: 45, status: "married",
        batch: "LK1988", school: "Central High School"};

    // Subscribe the `filteredStatusCountStream` stream to the `printStatusCount` function. Each time the stream
    // receives an event, call the `printStatusCount` function.
    filteredStatusCountStream.subscribe(printStatusCount);

    // Publish the events that are generated.
    teacherStream.publish(t1);
    teacherStream.publish(t2);
    teacherStream.publish(t3);

    runtime:sleep(1000);
}

// Print the output events.
function printStatusCount(StatusCount s) {
    io:println("Event received; status: " + s.status +
            ", total occurrences: " + s.totalCount);
}

