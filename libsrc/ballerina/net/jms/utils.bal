package ballerina.net.jms;

native function acknowledge(message m, string deliveryStatus);
native function commit(message m);
native function rollbck(message m);