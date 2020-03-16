type Student record {|
    string name;
    *A;
    int age?;
    float gpa = 0;
    *B;
    any...;
|};
