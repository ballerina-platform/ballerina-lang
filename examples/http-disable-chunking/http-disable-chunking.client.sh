$ curl http://localhost:9090/chunkingSample/
{"Outbound request content":"Lenght-18"}

To enable chunking, try changing the chunking option of the clientEndpoint to http:Chunking.ALWAYS.
{"Outbound request content":"chunked"}
