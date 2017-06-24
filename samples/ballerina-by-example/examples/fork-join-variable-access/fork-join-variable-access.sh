# To run the program, put the code in `fork-join-variable-access.bal`
# and use `$BALLERINA_HOME/bin/ballerina`.
$ $BALLERINA_HOME/bin/ballerina run main fork-join-variable-access.bal
[default worker] before fork-join:
      Value of integer variable is [100]
      Value of string variable is [WSO2]
[default worker] before fork-join:
      Value of name is [Abhaya]
      Value of era is [Anuradhapura]
[W1 worker]: starting worker
      Value of integer variable is [100]
      Value of string variable is [WSO2]
[W2 worker]: starting worker
      Value of integer variable is [100]
      Value of string variable is [WSO2]
[default worker] within join:
      Value of integer from W1 is [23]
[default worker] within join:
      Value of string from W1 is [Colombo]
[default worker] within join:
      Value of string from W2 [Ballerina]
[default worker] after fork-join:
      Value of integer variable is [100]
      Value of string variable is [WSO2]
[default worker] after fork-join:
      Value of name is [Rajasinghe]
      Value of era is [Kandy]
