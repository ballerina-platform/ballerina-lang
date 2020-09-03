type Student record {
   string firstName;
   float gpa;
};

public function foo() {
   Student[] studentList = [{ firstName: "Michelle", gpa: 3.5 }];
   var result = from var student in studentList
       do {
           var name = { firstName: student.firstName };
       };
}
