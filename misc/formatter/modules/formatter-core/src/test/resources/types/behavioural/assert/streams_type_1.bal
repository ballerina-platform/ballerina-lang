import ballerina/io;

//Defines a class called `OddNumberGenerator`. Each class has its own next() method,
//which gets invoked when the stream's `next()` function gets called.
class OddNumberGenerator {
    int i = 1;

    public isolated function next() returns record {| int value; |}|error? {
        self.i += 2;
        return {value: self.i};
    }
}

public function main() {
    OddNumberGenerator oddGen = new;

    //Creating a stream passing an OddNumberGenerator class to the stream constructor
    var oddNumberStream = new stream<int, error>(oddGen);

    record {| int value; |}|error? oddNumber = oddNumberStream.next();

    if (oddNumber is ResultValue) {
        io:println("Retrieved odd number: ", oddNumber.value);
    }

    Student[] studentList = [s1, s2, s3];

    //Iterable types can be converted to a stream.
    stream<Student> studentStream = studentList.toStream();

    //The `filter` and `map` functions return streams and work lazily.
    stream<Subscription> subscriptionStream = studentStream.filter(function(Student student) returns boolean {
                                                                       return student.score > 1;
                                                                   }).'map(function(Student student) returns 
                                                                           Subscription {
                                                                               Subscription subscription = {
                                                                                   firstName: student.firstName,
                                                                                   lastName: student.lastName,
                                                                                   score: student.score,
                                                                                   degree: "Bachelor of Medicine"
                                                                               };
                                                                               return subscription;
                                                                           });

    io:println("Calculate the average score of the subscribed students: ");
    //The `reduce` function reduces the stream to a single value.
    float? avg = subscriptionStream.reduce(function(float accum, Student student) returns float {
                                               return accum + <float>student.score / studentList.length();
                                           }, 0.0);

    if (avg is float) {
        io:println("Average: ", avg);
    }

    //A stream can be iterated at most for once. Hence, another stream gets created from the record list.
    stream<Student> studentStream2 = studentList.toStream();

    io:println("Calls next method manually and get the next iteration value: ");
    //Calls the `next()` operation to retrieve the data from the stream.
    record {| Student value; |}|error? student = studentStream2.next();
    if (student is StudentValue) {
        io:println(student.value);
    }

    io:println("Use foreach method to loop through the rest of the stream: ");

    //If there is any error during the iteration of the
    // studentList2 stream, the result stream will terminate and return the error.
    error? e = studentStream2.forEach(function(Student student) {
                                          io:println("Student ", student.firstName, " has a score of ", student.score);
                                      });

    //Check and handle the error during the iteration of the stream.
    if (e is error) {
        io:println("ForEach operation on the stream failed: ", e);
    }

    stream<Student> studentStream3 = studentList.toStream();
    var iterator = studentStream3.iterator();

    //Calls the `next()` operation on the iterator to retrieve the next data from the stream.
    record {| Student value; |}|error? nextStudent = iterator.next();
    if (nextStudent is StudentValue) {
        io:println(nextStudent.value);
    }
}
