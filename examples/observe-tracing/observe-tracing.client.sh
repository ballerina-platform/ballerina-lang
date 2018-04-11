$ curl -X GET http://localhost:9090/store/add
# Server response:
Item Added Successfully
# The trace can be viewed in Jaeger UI. The default url is: http://localhost:16686
# To observe an error trace, change the `clientEP` in line 10 of `observe-tracing.bal`
# to "http://localhost:9091" and resend curl command
