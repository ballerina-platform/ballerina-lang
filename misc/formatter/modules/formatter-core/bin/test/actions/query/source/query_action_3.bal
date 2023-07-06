public function main() {
    Report[] reportList = from var student in studentList
       // An inner equijoin is performed here.
       // The `join` clause iterates any iterable value similarly to the `from` clause.
       join var department in departmentList
       // The `on` condition is used to match the `student` with the `department` based on the `deptId`.
       // The iteration is skipped when the condition is not satisfied.
       on student.deptId equals department.deptId
       // The `limit` clause limits the number of output items.
       limit 3
       // The `select` clause is evaluated in each iteration when the `on` condition is satisfied.
       select {
              name: student.firstName + " " + student.lastName,
              deptName: department.deptName,
              degree: "Bachelor of Science",
              intakeYear: student.intakeYear
       };
}
