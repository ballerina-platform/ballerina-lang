function f1 (string inputData) returns @untainted string {
    string data = "sample_data" + inputData;
    return f1 (data);
}
