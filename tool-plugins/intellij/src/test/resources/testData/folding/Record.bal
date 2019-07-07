type Employee record <fold text='{...}'>{
    string id;
    string name;
    float salary;
}</fold>;

record <fold text='{...}'>{ int first_field; int second_field; string third_field; }</fold> rec = wait {first_field: 1, second_field: 2, third_field: 3};