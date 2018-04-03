$ ballerina run distributed-transactions.bal
CUSTOMER table create status in first DB:0
SALARY table create status in second DB:0
2018-03-03 11:36:08,163 INFO  [ballerina.transactions] - Created transaction: 79d3396039da44fbadb233955f695256
Inserted count to CUSTOMER table:1
Generated key for the inserted row:1
Inserted count to SALARY table:1
2018-03-03 11:36:08,206 INFO  [ballerina.transactions] - Committing transaction: 79d3396039da44fbadb233955f695256
2018-03-03 11:36:08,207 INFO  [ballerina.transactions] - Running 2-phase commit for transaction: 79d3396039da44fbadb233955f695256
Transaction committed
CUSTOMER table drop status:0
SALARY table drop status:0
