$ ballerina run worker-interaction.bal
[w1 -> w2] i: 100 k: 2.34
[w2 <- w1] iw: 100 kw: 2.34
[w2 -> w1] jw: {"name":"Ballerina"}
[w1 <- w2] j: {"name":"Ballerina"}