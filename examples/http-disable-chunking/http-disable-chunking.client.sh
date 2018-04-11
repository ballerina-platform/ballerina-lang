$ curl http://localhost:9092/chunkingSample/
{"Outbound request content":"Lenght-18"}

To enable chunking, try changing the chunking option of the clientEndpoint to http:Chunking.ALWAYS.
{"Outbound request content":"chunked"}
