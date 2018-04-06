# This sample requires starting up the subscriber service after the publisher starts up the hub

$ ballerina run publisher.bal
2018-03-23 05:30:01,517 INFO  [] - Starting up the Ballerina Hub Service
ballerina: Default Ballerina WebSub Hub started up at http://localhost:9999/websub/hub
ballerina: started HTTP/WS server connector localhost:9999

$ ballerina run subscriber.bal
ballerina: initiating service(s) in 'subscriber.bal'
2018-03-23 05:32:30,451 INFO  [ballerina.net.websub] - Initializing WebSub signature validation filter
ballerina: started HTTP/WS server connector localhost:8181

Output from  the publisher:

2018-03-23 05:32:31,114 INFO  [net.websub.hub] - Intent verification successful for mode: [subscribe], for callback URL: [http://localhost:8181/websub/]
2018-03-23 05:32:43,054 INFO  [] - Publishing update to internal Hub
2018-03-23 05:32:43,239 INFO  [] - Publishing update to remote Hub
2018-03-23 05:32:43,257 INFO  [net.websub.hub] - Event notification done for Topic [http://www.websubpubtopic.com]

Output from the subscriber:

2018-03-23 05:32:30,915 INFO  [ballerina.net.websub] - Subscription Request successful at Hub[http://localhost:9999/websub/hub], for Topic[http://www.websubpubtopic.com]
2018-03-23 05:32:31,015 INFO  [ballerina.net.websub] - Intent Verification agreed - Mode [subscribe], Topic [http://www.websubpubtopic.com], Lease Seconds [86400000]
2018-03-23 05:32:31,016 INFO  [] - Intent verified for subscription request
2018-03-23 05:32:43,623 INFO  [] - WebSub Notification Received: {"action":"publish","mode":"internal-hub"}
2018-03-23 05:32:43,643 INFO  [] - WebSub Notification Received: {"action":"publish","mode":"remote-hub"}
