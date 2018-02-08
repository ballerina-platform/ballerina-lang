$ ballerina run iterable-operations.bal
total words count 5
["ANT", "BEAR", "CAT", "DEAR", "ELEPHANT"]
Average of positive numbers 7.0

Execution Order
- map operation's value :apple
-- foreach operation's value :apple
- map operation's value :["red","green"]
-- foreach operation's value :["red","green"]
- map operation's value :5
-- foreach operation's value :5
