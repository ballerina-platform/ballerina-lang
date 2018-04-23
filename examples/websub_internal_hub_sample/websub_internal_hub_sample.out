# This sample requires starting up the Subscriber Service after the Publisher starts up the hub and registers the topic

# If the port is not specified, the hub service will be started up on the default port
$ ballerina run publisher.bal -e b7a.websub.hub.port=9191
2018-04-12 18:32:59,058 INFO  [] - Starting up the Ballerina Hub Service
ballerina: started HTTPS/WSS endpoint localhost:9191
ballerina: Default Ballerina WebSub Hub started up at https://localhost:9191/websub/hub

$ ballerina run subscriber.bal
ballerina: initiating service(s) in 'subscriber.bal'
ballerina: started HTTP/WS endpoint 0.0.0.0:8181

Output from  the publisher:

2018-04-12 18:32:59,847 INFO  [] - Topic registration successful!
2018-04-12 18:33:02,679 INFO  [websub.hub] - Intent verification successful for mode: [subscribe], for callback URL: [http://0.0.0.0:8181/websub]
2018-04-12 18:33:19,860 INFO  [] - Publishing update to internal Hub
2018-04-12 18:33:20,028 INFO  [] - Update notification successful!

Output from the subscriber:

2018-04-12 18:33:02,506 INFO  [ballerina.websub] - Subscription Request successful at Hub[https://localhost:9191/websub/hub], for Topic[http://www.websubpubtopic.com], with Callback [http://0.0.0.0:8181/websub]
2018-04-12 18:33:02,601 INFO  [ballerina.websub] - Intent Verification agreed - Mode [subscribe], Topic [http://www.websubpubtopic.com], Lease Seconds [86400000]
2018-04-12 18:33:02,602 INFO  [] - Intent verified for subscription request
2018-04-12 18:33:20,080 INFO  [] - WebSub Notification Received: {"action":"publish","mode":"internal-hub"}
