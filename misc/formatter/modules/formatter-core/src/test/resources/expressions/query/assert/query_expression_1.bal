type Student record {
   string name;
};

type Report record {
   string name;
   string degree;
};

public function foo() {
   Student[] list1 = [{ name: "Michelle" }];
   Student[] list2 = [{ name: "John" }];

   Report[] list3 = from var student in list1
      where student.name == "Michelle"
      let string degreeName = "Bachelor of Medicine"
      join var name in list2
      select {
             name: student.name,
             degree: degreeName
      }
      limit 2;
}
