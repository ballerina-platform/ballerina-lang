import ballerina/io;
import ballerina/runtime;

type StatusCount {
    string status;
    int totalCount;
};

type Teacher {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

function testAggregationQuery (stream<StatusCount> filteredStatusCountStream,
                                                    stream<Teacher> teacherStream) {

    //Create forever statement block with respective streaming query.
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

function main (string[] args) {

    //Create stream constrained by the StatusCount struct type.
    stream<StatusCount> filteredStatusCountStream;

    //Create stream constrained by the Teacher struct type.
    stream<Teacher> teacherStream;

    //Invoke the method which contains the forever streaming statement.
    testAggregationQuery(filteredStatusCountStream, teacherStream);

    //Create some events to pump into the input stream 'teacherStream'.
    Teacher t1 = {name:"Raja", age:25, status:"single", batch:"LK2014", school:"Hindu College"};
    Teacher t2 = {name:"Shareek", age:33, status:"single", batch:"LK1998", school:"Thomas College"};
    Teacher t3 = {name:"Nimal", age:45, status:"married", batch:"LK1988", school:"Ananda College"};

    //Subscribe to the stream 'filteredStatusCountStream' which contains the output events.
    filteredStatusCountStream.subscribe(printStatusCount);

    //Publish events that generated in earlier step.
    teacherStream.publish(t1);
    teacherStream.publish(t2);
    teacherStream.publish(t3);

    runtime:sleepCurrentWorker(1000);
}

//Print the output events.
function printStatusCount (StatusCount s) {
    io:println("Event received; status: " + s.status +" and total occurrences: "+s.totalCount);
}

