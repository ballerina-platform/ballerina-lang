import ballerina/io;

//Defines an object called `OddNumberGenerator`. Each object has its own next() method,
//which gets invoked when the stream's next() gets called.
type OddNumberGenerator object {
    int i = 1;
    
    public function next() returns record {|int value;|}|error? {
        self.i += 2;
        return {value: self.i};
    }
};

type ResultValue record {|
    int value;
|};

type Student record {
    string firstName;
    string lastName;
    float score;
};

type StudentValue record {|
    Student value;
|};

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
    OddNumberGenerator oddGen = new;

    //Creating a stream passing an OddNumberGenerator object to the stream constructor
    var oddNumberStream = new stream<int, error>(oddGen);

    record {|int value;|}|error? oddNumber = oddNumberStream.next();

    if (oddNumber is ResultValue) {
        io:println("Retrieved odd number: ", oddNumber.value);
    }

    io:println("Filter records and map them to a different type :");

    Student s1 = {firstName: "Alex", lastName: "George", score: 1.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 0.9};
    Student s3 = {firstName: "John", lastName: "David", score: 1.2};

    Student[] studentList = [s1, s2, s3];

    //Iterable types can be converted to a stream.
    stream<Student> studentStream = studentList.toStream();

    //The `filter` and `map` functions return streams and work lazily.
    stream<Subscription> subscriptionStream = studentStream.filter(function (Student student) returns boolean {
        return student.score > 1;
    }).'map(function (Student student) returns Subscription {
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
    float? avg = subscriptionStream.reduce(function (float accum, Student student) returns float {
        return accum + <float>student.score / studentList.length();
    }, 0.0);

    if (avg is float) {
        io:println("Average: ", avg);
    }

    //A stream can be iterated at most for once. Hence, another stream gets created from the record list.
       stream<Student> studentStream2 = studentList.toStream();

    io:println("Calls next method manually and get the next iteration value: ");
    //Calls the `next()` operation to retrieve the data from the stream.
    record {|Student value;|}|error? student = studentStream2.next();
    if (student is StudentValue) {
        io:println(student.value);
    }

    io:println("Use foreach method to loop through the rest of the stream: ");

    //If there is any error during the iteration of the
    // studentList2 stream, the result stream will terminate and return the error.
    error? e = studentStream2.forEach(function (Student student) {
        io:println("Student ", student.firstName, " has a score of ", student.score);
    });

    //Check and handle the error during the iteration of the stream.
    if (e is error) {
        io:println("ForEach operation on the stream failed!");
        io:println(e);
    }

    stream<Student> studentStream3 = studentList.toStream();
    var iterator = studentStream3.iterator();

    //Calls the `next()` operation on the iterator to retrieve the next data from the stream.
    record {|Student value;|}|error? nextStudent = iterator.next();
    if (nextStudent is StudentValue) {
        io:println(nextStudent.value);
    }
}
