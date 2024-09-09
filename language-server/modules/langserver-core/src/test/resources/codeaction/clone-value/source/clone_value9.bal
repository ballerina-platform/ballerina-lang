isolated stream<string[]> streamName = new;

isolated function isolatedFn3() returns record {| string[] value; |}? {
    lock {
        return streamName.next();
    }
}
