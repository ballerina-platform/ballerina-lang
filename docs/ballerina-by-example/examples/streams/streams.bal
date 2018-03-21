import ballerina/io;
import ballerina/runtime;

struct StatusCount {
    string status;
    int totalCount;
}

struct Teacher {
    string name;
    int age;
    string status;
    string batch;
    string school;
}


stream<StatusCount> filteredStatusCountStream = {};
stream<Teacher> teacherStream = {};

streamlet aggregationStreamlet () {
    from teacherStream where age > 18 window lengthBatch(3)
    select status, count(status) as totalCount
    group by status
    having totalCount > 1
    insert into filteredStatusCountStream
}


function main (string[] args) {

    aggregationStreamlet pStreamlet = {};

    Teacher t1 = {name:"Raja", age:25, status:"single", batch:"LK2014", school:"Hindu College"};
    Teacher t2 = {name:"Shareek", age:33, status:"single", batch:"LK1998", school:"Thomas College"};
    Teacher t3 = {name:"Nimal", age:45, status:"married", batch:"LK1988", school:"Ananda College"};

    filteredStatusCountStream.subscribe(printStatusCount);

    teacherStream.publish(t1);
    teacherStream.publish(t2);
    teacherStream.publish(t3);

    runtime:sleepCurrentWorker(1000);
    pStreamlet.stop();
}

function printStatusCount (StatusCount s) {
    io:println("Event received; status: " + s.status +" and total occurrences: "+s.totalCount);
}

