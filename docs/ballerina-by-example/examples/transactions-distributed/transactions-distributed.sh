$ ballerina run iniator.bal
ballerina: initiating service(s) in 'initiator'
ballerina: started HTTP/WS server connector localhost:8080
ballerina: started HTTP/WS server connector 10.100.1.182:53871

$ ballerina run participant.bal
ballerina: initiating service(s) in 'participant'
ballerina: started HTTP/WS server connector 10.100.1.182:54774
ballerina: started HTTP/WS server connector localhost:8889

$ curl -v localhost:8080

Output from initiator:

2018-03-03 15:35:27,327 INFO  [] - Initiating transaction...
2018-03-03 15:35:27,337 INFO  [ballerina.coordinator] - Created transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,019 INFO  [ballerina.coordinator] - Registered participant: 13a70eeada20418a8afe2cd38031962c for transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,080 INFO  [] - Got response from bizservice
2018-03-03 15:35:28,085 INFO  [ballerina.coordinator] - Committing transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,085 INFO  [ballerina.coordinator] - Running 2-phase commit for transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,088 INFO  [ballerina.coordinator] - Preparing participant: 13a70eeada20418a8afe2cd38031962c
2018-03-03 15:35:28,104 INFO  [ballerina.coordinator] - Participant: 13a70eeada20418a8afe2cd38031962c, status: prepared
2018-03-03 15:35:28,104 INFO  [ballerina.coordinator] - Notify(commit) participant: 13a70eeada20418a8afe2cd38031962c
2018-03-03 15:35:28,122 INFO  [] - Sent response back to client

Output from participant:

2018-03-03 15:35:27,679 INFO  [] - Received update stockquote request
2018-03-03 15:35:27,693 INFO  [ballerina.coordinator] - Registering for transaction: f08b9405b4eb4586bb2e1363d165d62b with coordinator: http://10.100.1.182:54812/balcoordinator/initiator/register
2018-03-03 15:35:28,043 INFO  [ballerina.coordinator] - Registered with coordinator for transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,044 INFO  [] - Update stock quote request received. symbol:GOOG, price:210.19559392218983
2018-03-03 15:35:28,078 INFO  [] - Sent response back to initiator
2018-03-03 15:35:28,095 INFO  [ballerina.coordinator] - Prepare received for transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,096 INFO  [ballerina.coordinator] - Prepared transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,110 INFO  [ballerina.coordinator] - Notify(commit) received for transaction: f08b9405b4eb4586bb2e1363d165d62b
