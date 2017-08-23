$ ballerina run worker-interaction.bal
[default -> w1] i: 100 k: 2.34
[w1 <- default] iw: 100 kw: 2.34
[w1 -> default] jw: {"name":"Ballerina"}
[default <- w1] j: {"name":"Ballerina"}
