$ ballerina run distributed-transactions.bal
CUSTOMER table create status in first DB:0
SALARY table create status in second DB:0
2018-04-20 16:44:02,684 INFO  [ballerina.transactions] - Created transaction: bf62eb016a524874b2fb006cf6a1acce
Inserted count to CUSTOMER table: 1
Generated key for the inserted row: 1
Inserted count to SALARY table: 1
2018-04-20 16:44:02,693 INFO  [ballerina.transactions] - Running 2-phase commit for transaction: bf62eb016a524874b2fb006cf6a1acce:1
Transaction: bf62eb016a524874b2fb006cf6a1acce:1 committed
CUSTOMER table drop status: 0
SALARY table drop status: 0
