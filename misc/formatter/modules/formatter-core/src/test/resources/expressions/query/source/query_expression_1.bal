type Student record {
   string name;
};

type Report record {
   string name;
   string degree;
};

// This is a `table` type with `Report` members uniquely identified by their `id` field.
type ReportTable table<Report> key(id);
public function foo() {
   Student[] list1 = [{ name: "Michelle" }];
   Student[] list2 = [{ name: "John" }];

   Report[] list3 =   from   var student in list1
         where    student.name == "Michelle"   let   string degreeName = "Bachelor of Medicine" limit    2

     join   var   name   in   list2   on    student.deptId
     equals    department.deptId
     //The result of the query expression is a list (`reportList`) whose members are the result of
                                                         //the `select` clause.
                                                      select
{
           name  :   student  .  name  ,
   degree  :   degreeName}
       ;
}
