$ ballerina run main fork-join.bal
[W1 worker]: inside worker
      Value of integer variable is [23]
      Value of string variable is [Colombo]
[W2 worker]: starting worker
      Value of float variable is [10.344]
[default worker] within join:
      Value of integer from W1 is [23]
[default worker] within join:
      Value of string from W1 is [Colombo]
[default worker] within join:
      Value of float from W2 [10.344]
