import ballerina/io;

type Student record {
    string firstName;
    string lastName;
    float score;
};

type FullName record {|
   string firstName;
   string lastName;
|};

type Subscription record {|
    string firstName;
    string lastName;
    float score;
    string degree;
|};

public function main() {
    io:println("Filter records and map them to a different type :");

    Student s1 = {firstName: "Alex", lastName: "George", score: 1.5 };
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka",  score: 0.9 };
    Student s3 = {firstName: "John", lastName: "David",  score: 1.2 };

    Student[] studentList = [s1, s2, s3];

    //Iterable types can be converted to a stream
    stream<Student> studentStream = studentList.toStream();

    //`filter` and `map` functions return streams and work lazily
        stream<Subscription> subscriptionStream = studentStream
        . filter(function (Student student) returns boolean {
            return student.score > 1;
        })
        . 'map(function (Student student) returns Subscription {
             Subscription subscription = {
                       firstName: student.firstName,
                       lastName: student.lastName,
                       score: student.score,
                       degress: "Bachelor of Medicine"
                   };
                   return subscription;
        });

    io:println("Calculate the average score of the subscribed students: ");
    //`reduce` function reduces the stream to a single value
     float avg = subscriptionStream.reduce(function (float accum, Student student) returns float {
                return accum + <float>student.score / studentList.length();
            }, 0.0);
            return avg;

     io:println("Average: ", avg);

    //A stream can be iterated over at most once. Hence, creating another stream from the record list
    Student[] studentList2 = [s1, s2, s3];

     io:println("Calls next method manually and get the next iteration value: ");
    record {|Student value;|}? student1 = studentList2.next();

    io:println(filteredStudent);

    io:println("Use foreach method to loop through the stream: ");
    studentList2.forEach(function (Student student) {
                io:println("Student " + student.firstName +" has a score of "+ student.score + " .")
            });
}
