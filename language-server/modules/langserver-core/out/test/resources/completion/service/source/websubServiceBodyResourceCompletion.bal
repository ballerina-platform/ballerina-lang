import ballerina/websub;

@websub:SubscriberServiceConfig {

}
service websubSubscriber on new websub:Listener(9092) {
    resource function onNotification(websub:Notification notification) {

	}
}
