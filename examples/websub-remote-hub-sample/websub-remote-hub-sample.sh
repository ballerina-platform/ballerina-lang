# This sample requires starting up the Hub Service via hub.bal prior to running the Publisher main program. The
# Subscriber Service needs to be started up only once publisher registers the topic at the hub.

$ ballerina run hub.bal -e b7a.websub.hub.remotepublish=true
2018-04-12 18:48:00,530 INFO  [] - Starting up the Ballerina Hub Service
ballerina: started HTTPS/WSS endpoint localhost:9292
ballerina: Default Ballerina WebSub Hub started up at https://localhost:9292/websub/hub

$ ballerina run pub.bal
2018-04-12 18:48:05,263 INFO  [] - Topic registration successful!

$ ballerina run subscriber.bal
ballerina: initiating service(s) in 'subscriber.bal'
2018-03-23 05:32:30,451 INFO  [ballerina.websub] - Initializing WebSub signature validation filter
ballerina: started HTTP/WS server connector localhost:8181

Output from  the hub:

2018-04-12 18:48:05,214 INFO  [websub.hub] - Topic registration successful at Hub, for topic[http://www.websubpubtopic.com]
2018-04-12 18:48:06,808 INFO  [websub.hub] - Intent verification successful for mode: [subscribe], for callback URL: [http://0.0.0.0:8181/websub]
2018-04-12 18:48:15,465 INFO  [websub.hub] - Event notification done for Topic [http://www.websubpubtopic.com]

Output from  the publisher:

2018-04-12 18:48:15,273 INFO  [] - Publishing update to remote Hub
2018-04-12 18:48:15,462 INFO  [] - Update notification successful!

Output from the subscriber:

2018-04-12 18:48:06,632 INFO  [ballerina.websub] - Subscription Request successful at Hub[https://localhost:9292/websub/hub], for Topic[http://www.websubpubtopic.com], with Callback [http://0.0.0.0:8181/websub]
2018-04-12 18:48:06,738 INFO  [ballerina.websub] - Intent Verification agreed - Mode [subscribe], Topic [http://www.websubpubtopic.com], Lease Seconds [86400000]
2018-04-12 18:48:06,738 INFO  [] - Intent verified for subscription request
2018-04-12 18:48:15,556 INFO  [] - WebSub Notification Received: {"action":"publish","mode":"remote-hub"}
