type Employee <fold text='{...}'>record {
    string id;
    string name;
    float salary;
};</fold>

record { int first_field; int second_field; string third_field; } rec =
    wait {first_field: 1, second_field: 2, third_field: 3};