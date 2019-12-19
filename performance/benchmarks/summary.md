# Ballerina Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Passthrough HTTP service (h1c -> h1c) | An HTTP Service, which forwards all requests to an HTTP back-end service. |
| Passthrough HTTPS service (h1 -> h1) | An HTTPS Service, which forwards all requests to an HTTPS back-end service. |
| JSON to XML transformation HTTP service | An HTTP Service, which transforms JSON requests to XML and then forwards all requests to an HTTP back-end service. |
| JSON to XML transformation HTTPS service | An HTTPS Service, which transforms JSON requests to XML and then forwards all requests to an HTTPS back-end service. |
| Passthrough HTTP/2(over TLS) service (h2 -> h2) | An HTTPS Service exposed over HTTP/2 protocol, which forwards all requests to an HTTP/2(over TLS) back-end service. |
| Passthrough HTTP/2(over TLS) service (h2 -> h1) | An HTTPS Service exposed over HTTP/2 protocol, which forwards all requests to an HTTPS back-end service. |
| Passthrough HTTP/2(over TLS) service (h2 -> h1c) | An HTTPS Service exposed over HTTP/2 protocol, which forwards all requests to an HTTP back-end service. |
| HTTP/2 client and server downgrade service (h2 -> h2) | An HTTP/2(with TLS) server accepts requests from an HTTP/1.1(with TLS) client and the HTTP/2(with TLS) client sends requests to an HTTP/1.1(with TLS) back-end service. Both the upstream and the downgrade connection is downgraded to HTTP/1.1(with TLS). |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

A majority of test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the Ballerina service processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a Ballerina service. The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 2G |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 300, 1000 |
| Message Size (Bytes) | The request payload size in Bytes. | 50, 1024 |
| Back-end Delay (ms) | The delay added by the back-end service. | 0 |

The duration of each test is **900 seconds**. The warm-up period is **300 seconds**.
The measurement results are collected after the warm-up period.

A [**c5.xlarge** Amazon EC2 instance](https://aws.amazon.com/ec2/instance-types/) was used to install Ballerina.

The following are the measurements collected from each performance test conducted for a given combination of
test parameters.

| Measurement | Description |
| --- | --- |
| Error % | Percentage of requests with errors |
| Average Response Time (ms) | The average response time of a set of results |
| Standard Deviation of Response Time (ms) | The “Standard Deviation” of the response time. |
| 99th Percentile of Response Time (ms) | 99% of the requests took no more than this time. The remaining samples took at least as long as this |
| Throughput (Requests/sec) | The throughput measured in requests per second. |
| Average Memory Footprint After Full GC (M) | The average memory consumed by the application after a full garbage collection event. |

The following is the summary of performance test results collected for the measurement period.

|  Scenario Name | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | Ballerina GC Throughput (%) | Average Ballerina Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21523 | 4.6 | 2.78 | 11 | 99.58 | 14.091 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19489.6 | 5.08 | 2.96 | 12 | 99.6 | 14.55 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22144.8 | 13.49 | 6.29 | 30 | 99.11 | 15.061 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20127.8 | 14.85 | 6.66 | 32 | 99.17 | 15.092 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19821.08 | 50.38 | 16.42 | 98 | 97.5 | 15.622 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18850.35 | 52.97 | 16.69 | 101 | 97.64 | 15.629 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14474.68 | 6.87 | 4.48 | 20 | 99.15 | 15.047 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10223.54 | 9.74 | 6.12 | 29 | 99.24 | 15.062 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16695.84 | 17.92 | 7.77 | 40 | 98.03 | 15.064 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 11087.24 | 27 | 9.18 | 55 | 98.15 | 14.979 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14920.15 | 66.95 | 18.87 | 120 | 94.91 | 16.166 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9554.9 | 104.57 | 17.75 | 155 | 94.72 | 15.895 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 16949.87 | 5.85 | 2.97 | 13 | 99.57 | 14.555 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12232.92 | 8.13 | 3.55 | 18 | 99.63 | 14.566 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18011.23 | 16.6 | 6.9 | 35 | 99.09 | 15.033 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12635.9 | 23.68 | 8.63 | 48 | 99.22 | 15.037 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15672.23 | 63.72 | 18.85 | 117 | 97.59 | 16.126 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11979.91 | 83.39 | 23.48 | 148 | 97.75 | 16.005 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 11403.81 | 8.72 | 5.29 | 25 | 99.09 | 14.276 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7534.15 | 13.22 | 6.68 | 34 | 99.27 | 14.27 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14371.55 | 20.82 | 8.87 | 46 | 98.15 | 14.825 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7254.59 | 41.29 | 15.9 | 87 | 98.44 | 14.826 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12742.06 | 78.4 | 23.6 | 144 | 95.03 | 15.857 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7416.3 | 134.73 | 38.31 | 238 | 95.1 | 15.975 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15389.55 | 6.29 | 3.03 | 14 | 99.61 | 15.162 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14632.19 | 6.53 | 2.98 | 15 | 99.63 | 15.089 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16409 | 17.8 | 7.29 | 37 | 99.14 | 15.562 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15521.83 | 18.45 | 7.44 | 39 | 99.18 | 15.555 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14709.6 | 67.01 | 20.98 | 128 | 97.62 | 16.302 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14394.62 | 67.68 | 21.77 | 133 | 97.51 | 16.257 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14209.57 | 6.84 | 3.15 | 15 | 99.59 | 15.143 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11634.5 | 8.33 | 3.51 | 19 | 99.62 | 15.084 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14826.08 | 19.79 | 7.8 | 41 | 99.1 | 15.562 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12239.06 | 23.84 | 8.73 | 49 | 99.14 | 15.629 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13189.95 | 75.4 | 22.42 | 141 | 97.45 | 16.248 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11315.64 | 86.78 | 26.27 | 161 | 97.51 | 16.291 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 17902.13 | 5.54 | 2.97 | 13 | 99.61 | 15.229 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15842.02 | 6.26 | 3.07 | 14 | 99.65 | 15.136 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19152.05 | 15.6 | 6.79 | 33 | 99.17 | 15.705 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16454.85 | 18.17 | 7.32 | 38 | 99.28 | 15.461 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16884.31 | 59.14 | 18.33 | 111 | 97.78 | 16.389 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15411.06 | 64.81 | 19.28 | 120 | 97.8 | 16.359 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15227.54 | 6.39 | 3.13 | 15 | 99.66 | 15.161 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14598.6 | 6.58 | 3.03 | 15 | 99.68 | 15.104 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16397.34 | 17.89 | 7.31 | 37 | 99.31 | 15.61 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15464.09 | 18.65 | 7.49 | 40 | 99.35 | 15.571 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15861.77 | 62.61 | 20.37 | 120 | 97.99 | 16.321 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15327.69 | 63.71 | 21.39 | 128 | 98.08 | 16.282 |
