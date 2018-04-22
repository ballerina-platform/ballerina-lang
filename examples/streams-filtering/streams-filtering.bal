import ballerina/io;
import ballerina/runtime;

// Create an object type named `StatusCount`.
type StatusCount {
    string status;
    int totalCount;
};

// Create an object type named `Teacher` and define the attributes.
type Teacher {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

function testAggregationQuery (stream<StatusCount> filteredStatusCountStream,
                                                    stream<Teacher> teacherStream) {

    // Create a forever statement block with the respective streaming query.
    // Write a query to filter out the teachers who are older than 18 years, wait until three teacher
    // object are collected by the stream, group the 3 teachers based on their marital status, and calculate the
    // unique marital status count of the teachers.
    // Once the query is executed, publish the result to the `filteredStatusCountStream` stream.
    forever{
        from teacherStream where age > 18 window lengthBatch(3)
        select status, count(status) as totalCount
        group by status
        having totalCount > 1
        => (StatusCount [] status) {
                filteredStatusCountStream.publish(status);
        }
    }
}

function main (string... args) {

    //Create a stream that is constrained by the StatusCount struct type.
    stream<StatusCount> filteredStatusCountStream;

    //Create a stream that is constrained by the Teacher struct type.
    stream<Teacher> teacherStream;

    //Invoke the method which contains the forever streaming statement.
    testAggregationQuery(filteredStatusCountStream, teacherStream);

    //Create sample events. These events are sent into the `teacherStream` input stream.
    Teacher t1 = {name:"Raja", age:25, status:"single", batch:"LK2014", school:"Hindu College"};
    Teacher t2 = {name:"Shareek", age:33, status:"single", batch:"LK1998", school:"Thomas College"};
    Teacher t3 = {name:"Nimal", age:45, status:"married", batch:"LK1988", school:"Ananda College"};

    //Subscribe the `filteredStatusCountStream` stream to the `printStatusCount` function. Each time the stream
    // receives an event, this function is called.
    filteredStatusCountStream.subscribe(printStatusCount);

    //Publish the events that were generated in the above step.
    teacherStream.publish(t1);
    teacherStream.publish(t2);
    teacherStream.publish(t3);

    runtime:sleep(1000);
}

//Print the output events.
function printStatusCount (StatusCount s) {
    io:println("Event received; status: " + s.status +" and total occurrences: "+s.totalCount);
}

