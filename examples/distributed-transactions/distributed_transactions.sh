$ ballerina run distributed-transactions.bal
2018-04-23 20:38:47,018 INFO  [] - CUSTOMER table create status in first DB:0
2018-04-23 20:38:47,039 INFO  [] - SALARY table create status in second DB:0
2018-04-23 20:38:47,053 INFO  [ballerina.transactions] -
Created transaction: fc5c5c4c-81e3-435f-b22f-801659613fd6
2018-04-23 20:38:47,062 INFO  [] - Inserted count to CUSTOMER table: 1
2018-04-23 20:38:47,063 INFO  [] - Generated key for the inserted row: 1
2018-04-23 20:38:47,065 INFO  [] - Inserted count to SALARY table: 1
2018-04-23 20:38:47,066 INFO  [ballerina.transactions] -
Running 2-phase commit for transaction: fc5c5c4c-81e3-435f-b22f-801659613fd6:1
2018-04-23 20:38:47,074 INFO  [] -
Transaction: fc5c5c4c-81e3-435f-b22f-801659613fd6:1 committed
2018-04-23 20:38:47,078 INFO  [] - CUSTOMER table drop status: 0
2018-04-23 20:38:47,084 INFO  [] - SALARY table drop status: 0
