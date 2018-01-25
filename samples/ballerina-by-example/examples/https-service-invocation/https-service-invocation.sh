#Run the service
$ ballerina run https-service-consumption.bal -s
ballerina: deploying service(s) in 'https-service-consumption.bal'
ballerina: started HTTPS/WSS server connector http-9095

#Run the main function containing ballerina client
$ ballerina run https-service-consumption.bal
Response code: 200
Response: Hello World!