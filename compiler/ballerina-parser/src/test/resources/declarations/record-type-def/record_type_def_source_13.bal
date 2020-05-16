function foo() {
    record {|
        string name;
        float gpa = 0;
        int age;

        record {
            string name;
            int age = 50;
            *B;
        } parent;

        *A;
        string...;
    |} student;

    int status;
}
