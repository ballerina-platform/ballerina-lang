$ ballerina run iniator.bal
ballerina: deploying service(s) in 'initiator'
ballerina: started HTTP/WS server connector localhost:8080
ballerina: started HTTP/WS server connector 10.100.1.182:53871

$ ballerina run participant.bal
ballerina: deploying service(s) in 'participant'
ballerina: started HTTP/WS server connector 10.100.1.182:54774
ballerina: started HTTP/WS server connector localhost:8889

$ curl -v localhost:8080

Output from initiator:

2018-03-03 15:35:27,327 INFO  [] - Initiating transaction...
2018-03-03 15:35:27,337 INFO  [ballerina.transactions.coordinator] - Created transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,019 INFO  [ballerina.transactions.coordinator] - Registered participant: 13a70eeada20418a8afe2cd38031962c for transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,080 INFO  [] - Got response from bizservice
2018-03-03 15:35:28,085 INFO  [ballerina.transactions.coordinator] - Committing transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,085 INFO  [ballerina.transactions.coordinator] - Running 2-phase commit for transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,088 INFO  [ballerina.transactions.coordinator] - Preparing participant: 13a70eeada20418a8afe2cd38031962c
2018-03-03 15:35:28,104 INFO  [ballerina.transactions.coordinator] - Participant: 13a70eeada20418a8afe2cd38031962c, status: prepared
2018-03-03 15:35:28,104 INFO  [ballerina.transactions.coordinator] - Notify(commit) participant: 13a70eeada20418a8afe2cd38031962c
2018-03-03 15:35:28,122 INFO  [] - Sent response back to client

Output from participant:

2018-03-03 15:35:27,679 INFO  [] - Received update stockquote request
2018-03-03 15:35:27,693 INFO  [ballerina.transactions.coordinator] - Registering for transaction: f08b9405b4eb4586bb2e1363d165d62b with coordinator: http://10.100.1.182:54812/balcoordinator/initiator/register
2018-03-03 15:35:28,043 INFO  [ballerina.transactions.coordinator] - Registered with coordinator for transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,044 INFO  [] - Update stock quote request received. symbol:GOOG, price:210.19559392218983
2018-03-03 15:35:28,078 INFO  [] - Sent response back to initiator
2018-03-03 15:35:28,095 INFO  [ballerina.transactions.coordinator] - Prepare received for transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,096 INFO  [ballerina.transactions.coordinator] - Prepared transaction: f08b9405b4eb4586bb2e1363d165d62b
2018-03-03 15:35:28,110 INFO  [ballerina.transactions.coordinator] - Notify(commit) received for transaction: f08b9405b4eb4586bb2e1363d165d62b
