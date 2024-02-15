public function test() {
    do {
        fail error SampleError("Transaction Failure", error("Database Error"), userCode = 20,
                            info = ["Detail_Info_1", "Detail_Info_2"]);
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2]) {
        io:println(info1);
    }
}
