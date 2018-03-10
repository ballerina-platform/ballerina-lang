function f1 (string inputData) (@untainted{} string) {
    string data = "sample_data" + inputData;
    return f1 (data);
}
