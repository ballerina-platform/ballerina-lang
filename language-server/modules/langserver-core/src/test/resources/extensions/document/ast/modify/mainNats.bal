import ballerina/nats;
public function main() {
nats:Connection connection = new ();
nats:Producer producer = new (connection);
nats:Error? result = producer->publish("Foo", "Test Message");
}
