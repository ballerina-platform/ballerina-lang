public function test() {
    match bar {
        () => {
        }
        "east" => {
            fail error SampleError("Transaction Failure", error("Database Error"), userCode = 20,
                            info = ["Detail_Info_1", "Detail_Info_2"], posInfo = {row: 45});
        }
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2], posInfo = {row: posRow}) {
        io:println(posRow);
    }
}
