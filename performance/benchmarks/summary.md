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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22580.8 | 4.39 | 2.74 | 11 | 99.57 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 21691.4 | 4.56 | 2.89 | 12 | 99.57 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22783 | 13.12 | 6.17 | 29 | 99.06 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 22316.95 | 13.38 | 6.42 | 30 | 99.06 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 21608.41 | 46.2 | 15.3 | 90 | 97.25 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 20762.29 | 48.08 | 15.85 | 94 | 97.31 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15633.58 | 6.36 | 14.56 | 20 | 98.89 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9840.84 | 9.67 | 82.05 | 30 | 98.22 | 16.712 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 17207.63 | 17.39 | 15.5 | 41 | 97.67 | 22.497 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 11351 | 25.74 | 42.16 | 57 | 94.78 | 28.502 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 16124.95 | 61.94 | 22.9 | 120 | 93.51 | 18.49 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9741.14 | 102.58 | 40.62 | 162 | 82.77 | 29.476 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 18713.27 | 5.3 | 2.8 | 12 | 99.54 | 15.142 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 13009.5 | 7.64 | 3.46 | 18 | 99.62 | 15.134 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 19312.26 | 15.48 | 6.69 | 33 | 99 | 15.794 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 13574.03 | 22.04 | 8.27 | 45 | 99.17 | 16.547 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 17326.85 | 57.64 | 18.48 | 110 | 97.28 | 20.128 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12942.85 | 77.17 | 23.27 | 142 | 97.55 | 19.44 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 13636.36 | 7.29 | 15.56 | 22 | 98.97 | 15.276 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7567.68 | 13.17 | 21.04 | 35 | 98.5 | 15.287 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14290.1 | 20.94 | 14.11 | 49 | 97.92 | 16.284 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7934.09 | 37.75 | 15.63 | 83 | 96.16 | 17.216 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13141.59 | 76.02 | 24.86 | 145 | 94.27 | 21.876 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6961.87 | 143.55 | 48.02 | 259 | 87.77 | 19.079 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 16961.12 | 5.67 | 2.88 | 13 | 99.56 | 15.541 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 16220.64 | 5.79 | 2.84 | 13 | 99.6 | 15.532 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 18000.42 | 16.13 | 6.97 | 35 | 99.04 | 16.323 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 17232.75 | 16.26 | 7.07 | 36 | 99.09 | 16.26 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 16825.06 | 58.55 | 20.07 | 117 | 97.03 | 17.09 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 16185.4 | 58.96 | 21.72 | 125 | 97.29 | 17.138 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 15503.89 | 6.24 | 3 | 14 | 99.57 | 15.688 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12689.63 | 7.58 | 3.26 | 17 | 99.6 | 15.731 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 16176.69 | 18.05 | 7.4 | 38 | 99.01 | 18.284 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 13059.78 | 22.2 | 8.36 | 46 | 99.08 | 16.803 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 15137.69 | 65.14 | 21.28 | 127 | 97.1 | 21.496 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 12532.32 | 78.49 | 24.96 | 150 | 97.21 | 20.124 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 19308.07 | 5.13 | 9.27 | 12 | 99.59 | 15.749 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 17100.19 | 5.8 | 3.07 | 14 | 99.63 | 15.776 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 20284.06 | 14.73 | 6.58 | 32 | 99.1 | 16.347 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 18034.87 | 16.57 | 7.09 | 36 | 99.19 | 16.377 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 18403.44 | 54.24 | 17.68 | 105 | 97.52 | 17.764 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16854.75 | 59.25 | 19.16 | 114 | 97.71 | 20.528 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 17325.89 | 5.56 | 2.91 | 13 | 99.63 | 15.73 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16194.46 | 5.83 | 2.88 | 14 | 99.65 | 15.619 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 18780.36 | 15.48 | 6.72 | 33 | 99.22 | 16.762 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17415.69 | 16.21 | 7.02 | 35 | 99.27 | 18.034 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 18446.71 | 53.31 | 18.59 | 108 | 97.58 | 19.191 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 17359.34 | 55.43 | 20.56 | 121 | 97.93 | 19.609 |
