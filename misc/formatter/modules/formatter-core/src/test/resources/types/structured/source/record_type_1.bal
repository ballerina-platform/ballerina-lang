    type Student   record   {
       string name;
     Grades grades;}   ;
    type Address   record   {|
       string city;|};
    type Grades    record  {|
       int maths;  int...;
    |};


function testRecords() returns record {}[] {
    record {||}[] recArr = [r1, r2, r3, r4];
    record {int a;}[] recArr;
    Person p = {name: "john", age: 20};
    return recArr;
}
