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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22117 | 4.48 | 11.99 | 12 | 99.54 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20573.67 | 4.82 | 2.93 | 12 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22477.51 | 13.29 | 6.26 | 30 | 99.03 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 21051.12 | 14.19 | 6.48 | 32 | 99.08 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 20592.32 | 48.49 | 16.02 | 95 | 97.27 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19900.23 | 50.18 | 16.4 | 97 | 97.3 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15050 | 6.61 | 10.99 | 21 | 98.98 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10447.96 | 9.45 | 36.33 | 30 | 98.1 | 17.079 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16056.68 | 18.19 | 16.02 | 44 | 97.02 | 19.851 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10932.14 | 27.38 | 32.75 | 61 | 94.99 | 21.052 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 15289.32 | 65.35 | 21.44 | 125 | 93.8 | 13.294 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9333.86 | 107.06 | 43.59 | 168 | 82.88 | 21.902 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17798.14 | 5.57 | 2.87 | 13 | 99.54 | 15.124 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12429.94 | 8 | 3.49 | 18 | 99.52 | 15.136 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18314.11 | 16.32 | 6.9 | 35 | 99.01 | 16.531 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12798.1 | 23.38 | 8.56 | 48 | 99.18 | 16.566 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 16489.54 | 60.57 | 19.22 | 115 | 97.3 | 18.813 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12044.85 | 82.94 | 24.2 | 150 | 97.59 | 21.531 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 13090.33 | 7.59 | 21.96 | 23 | 98.97 | 15.189 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7357.57 | 13.54 | 21.31 | 35 | 98.5 | 15.29 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13635.14 | 21.94 | 9.96 | 51 | 97.97 | 16.972 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7433.32 | 40.3 | 21.69 | 88 | 96.27 | 16.699 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12525.52 | 79.75 | 26.59 | 153 | 94.42 | 20.617 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6536.64 | 152.88 | 47.09 | 271 | 88 | 20.92 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 16541.24 | 5.81 | 2.96 | 14 | 99.56 | 15.531 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15408.49 | 6.12 | 2.92 | 14 | 99.6 | 15.535 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16952.58 | 17.17 | 7.28 | 37 | 99.05 | 16.308 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 16177.13 | 17.55 | 7.4 | 38 | 99.1 | 16.055 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15679.02 | 63.03 | 20.75 | 124 | 97.23 | 17.05 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 15105.08 | 63.75 | 21.94 | 129 | 97.36 | 17.11 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14816.74 | 6.53 | 3.1 | 15 | 99.55 | 15.436 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12092.29 | 7.97 | 3.43 | 18 | 99.6 | 15.385 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 15269.63 | 19.19 | 7.69 | 40 | 99.01 | 17.726 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12526.86 | 23.23 | 8.67 | 48 | 99.08 | 17.22 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13820.74 | 71.47 | 22.76 | 137 | 97.18 | 21.394 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11582.96 | 85.14 | 26.25 | 160 | 97.34 | 20.877 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18678.14 | 5.31 | 2.93 | 13 | 99.58 | 15.77 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16181.73 | 6.13 | 10.1 | 14 | 99.63 | 15.819 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19103.73 | 15.64 | 6.84 | 34 | 99.11 | 17.122 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16971.12 | 17.61 | 7.33 | 38 | 99.21 | 17.441 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17236.91 | 57.95 | 18.36 | 110 | 97.59 | 20.146 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15692.94 | 63.64 | 19.31 | 118 | 97.76 | 21.122 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16528.5 | 5.84 | 3.01 | 14 | 99.62 | 15.603 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15417.6 | 6.17 | 2.98 | 14 | 99.65 | 15.608 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17803.59 | 16.38 | 6.97 | 35 | 99.22 | 17.503 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16609.46 | 17.17 | 7.22 | 37 | 99.27 | 16.76 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17063.85 | 57.82 | 19.58 | 113 | 97.79 | 21.006 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16338.27 | 58.72 | 20.92 | 121 | 97.89 | 19.678 |
