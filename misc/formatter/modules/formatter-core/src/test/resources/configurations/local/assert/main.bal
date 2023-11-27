import ballerina/io;

const int MAX_STUDENTS       = 100;
const string WELCOME_MESSAGE = "Welcome";
const int MAX_AGE            = 20;

type Employee record {
  string firstName;
  string lastName;
  decimal score;
};

public function main() {
  Employee[] employees = [
    { firstName: "Hayden", lastName: "Welsh", score: 1000.00 },
    { firstName: "John", lastName: "Frank", score: 5000.00 },
    { firstName: "Michael", lastName: "Wayne", score: 10000.00 },
    { firstName: "Tom", lastName: "Cruise", score: 2000.00 }
  ];

  Employee[] top3 = from var e in employees
                    order by e.score descending
                    limit 3
                    select e;

  foreach var emp in top3 { io:println(emp); }
}
