$ ballerina run fork-join-variable-access.bal
[default worker] within join:
      Value of integer from W1 is [23]
[default worker] within join:
      Value of string from W1 is [Colombo]
[default worker] within join:
      Value of string from W2 [Ballerina]
# The value type variables have not changed since they are passed in as a copy of the original variable.
[default worker] after fork-join:
      Value of integer variable is [100]
      Value of string variable is [WSO2]
# The reference type variables have got updated since they are passed in as a reference to the workers.
[default worker] after fork-join:
      Value of name is [Rajasinghe]
      Value of era is [Kandy]

