type Student record {|
    string name;
    int age;
    float gpa = 0;

    record {
        string name;
        int age = 50;
        *B;
    } parent;

    *A;
    string...;
|};
