$ ballerina run observe-metrics.bal --observe
ballerina: started Prometheus HTTP endpoint 0.0.0.0/0.0.0.0:9797
ballerina: started publishing tracers to Jaeger on localhost:5775
---Counter---
count: 1.0
count: 6.0

---Gauge---
gauge: 1.0
gauge: 4.0
gauge: 3.0
gauge: 1.0
current system time in milliseconds: 1.5235350528E12

---Summary---
count : 5
max: 8.0
mean: 4.2
percentile values: {"0.5":4.1875, "0.75":5.1875, "0.98":8.4375, "0.99":8.4375, "0.999":8.4375}

---Timer---
count: 5
max: 60.0 seconds
mean: 12.806040200000002 seconds
percentile values: {"0.5":0.030408672, "0.75":4.026531808, "0.98":60.129542112, "0.99":60.129542112, "0.999":60.129542112}
