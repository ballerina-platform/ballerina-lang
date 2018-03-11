import ballerina.io;
import ballerina.runtime;

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

StatusCount[] globalStatusCountArray = [];
int index = 0;
stream<StatusCount> statusCountStream = {};
stream<Teacher> teacherStream = {};

streamlet windowStreamlet () {
    query q1 {
        from teacherStream where age > 18 window lengthBatch(3)
        select status, count(status) as totalCount
        group by status
        insert into statusCountStream
    }
}


function testWindowQuery () (StatusCount []) {

    windowStreamlet pStreamlet = {};

    Teacher t1 = {name:"Raja", age:25, status:"single", batch:"LK2014", school:"Hindu College"};
    Teacher t2 = {name:"Shareek", age:33, status:"single", batch:"LK1998", school:"Thomas College"};
    Teacher t3 = {name:"Nimal", age:45, status:"married", batch:"LK1988", school:"Ananda College"};

    statusCountStream.subscribe(printStatusCount);

    teacherStream.publish(t1);
    teacherStream.publish(t2);
    teacherStream.publish(t3);

    runtime:sleepCurrentWorker(1000);
    return globalStatusCountArray;
}

function printStatusCount (StatusCount s) {
    io:println("printStatusCount function invoked for status:" + s.status +" and count :"+s.totalCount);
    addToGlobalStatusCountArray(s);
}

function addToGlobalStatusCountArray (StatusCount s) {
    globalStatusCountArray[index] = s;
    index = index + 1;
}