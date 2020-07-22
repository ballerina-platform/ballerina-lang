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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 19542.94 | 5.08 | 3.39 | 14 | 99.54 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 17951.05 | 5.52 | 3.55 | 16 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 20234.62 | 14.77 | 6.98 | 34 | 98.39 | 14.855 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 18921.96 | 15.79 | 7.01 | 35 | 99.05 | 17.225 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 18406.26 | 54.25 | 17.69 | 105 | 97.03 | 11.782 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18024.51 | 55.4 | 18.15 | 108 | 96.95 | 15.142 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 11718.39 | 8.49 | 5.98 | 28 | 98.8 | 14.074 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 8612.76 | 11.57 | 6.09 | 33 | 97.82 | 14.111 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 12444.57 | 24.06 | 8.7 | 50 | 97.76 | 15.132 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 8699.65 | 34.43 | 9.94 | 67 | 95.24 | 15.702 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 11544.81 | 86.55 | 17.28 | 135 | 93.82 | 15.646 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 7539.1 | 132.56 | 27.67 | 194 | 85.18 | 17.438 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 15996.87 | 6.2 | 3.39 | 15 | 99.52 | 14.614 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 11603.34 | 8.57 | 3.75 | 19 | 99.6 | 14.674 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 16526.04 | 18.1 | 7.74 | 39 | 98.98 | 15.54 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 11862.74 | 25.23 | 9.32 | 52 | 99.13 | 15.688 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15160.87 | 65.86 | 20.68 | 124 | 97.17 | 18.445 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11392.15 | 87.68 | 25.71 | 158 | 97.45 | 18.537 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 10211.01 | 9.75 | 6.16 | 29 | 98.93 | 15.461 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6342.11 | 15.72 | 8.31 | 42 | 98.43 | 15.536 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 10661.74 | 28.08 | 12.05 | 64 | 97.95 | 15.696 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6404.8 | 46.78 | 18.24 | 99 | 96.4 | 16.17 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 10051.25 | 99.41 | 26.46 | 173 | 94.35 | 16.196 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 5697.33 | 175.43 | 46.82 | 293 | 88.02 | 16.453 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 14637.13 | 6.63 | 3.51 | 16 | 99.56 | 15.101 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 13860.19 | 6.91 | 3.5 | 17 | 99.58 | 15.108 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15223.44 | 19.25 | 8.09 | 42 | 99.04 | 15.62 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 14647.78 | 19.67 | 7.92 | 42 | 99.07 | 15.622 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14758.57 | 66.93 | 22.86 | 134 | 96.71 | 16.563 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14241.72 | 68.88 | 23.48 | 138 | 97.06 | 16.66 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13417.3 | 7.27 | 3.52 | 17 | 99.54 | 15.165 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 10924.67 | 8.9 | 3.78 | 20 | 99.58 | 15.15 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 13553.17 | 21.73 | 8.58 | 46 | 99.01 | 15.592 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11531.23 | 25.34 | 9.49 | 53 | 99.04 | 16.552 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 12814.35 | 76.97 | 23.79 | 145 | 96.99 | 19.274 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10797.43 | 91.02 | 27.78 | 169 | 97.19 | 19.836 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 16748.3 | 5.92 | 3.42 | 15 | 99.57 | 15.243 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14831.72 | 6.69 | 3.66 | 17 | 99.62 | 15.439 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 17396.81 | 17.19 | 7.51 | 38 | 99.07 | 16.239 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15695.85 | 19.05 | 7.96 | 41 | 99.15 | 16.402 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15976.85 | 62.49 | 19.87 | 118 | 97.38 | 21.88 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14868.05 | 67.17 | 21.57 | 127 | 97.53 | 21.059 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 14825.24 | 6.55 | 3.57 | 16 | 99.62 | 15.235 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 13940.28 | 6.88 | 3.53 | 17 | 99.64 | 15.236 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 15850.35 | 18.51 | 7.67 | 40 | 99.2 | 17.833 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15055.23 | 19.16 | 7.94 | 42 | 99.24 | 17.562 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15760.66 | 62.53 | 19.56 | 118 | 97.64 | 18.935 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14587.1 | 67.39 | 22.01 | 132 | 97.85 | 19.455 |
