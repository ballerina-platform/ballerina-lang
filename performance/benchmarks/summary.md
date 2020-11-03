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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 16810.29 | 5.91 | 3.85 | 17 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 15454.98 | 6.43 | 4.07 | 18 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 18545.8 | 16.12 | 7.34 | 36 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 17641.97 | 16.95 | 7.61 | 38 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 17495.43 | 57.09 | 18.11 | 109 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 17099.49 | 58.39 | 18.34 | 111 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 10370.26 | 9.6 | 6.03 | 29 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 7646.74 | 13.03 | 5.63 | 33 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 10854.98 | 27.58 | 8.49 | 53 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 7856.2 | 38.13 | 8.85 | 69 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 10440.16 | 95.71 | 16.2 | 145 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 7158.77 | 139.6 | 26.63 | 207 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 14543.79 | 6.83 | 4.11 | 19 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12749.13 | 7.79 | 4.41 | 21 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 15431.05 | 19.38 | 8.23 | 42 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 13753.23 | 21.75 | 8.9 | 47 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 14186.42 | 70.39 | 20.5 | 129 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 13144.05 | 75.98 | 21.94 | 138 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 9313.44 | 10.69 | 6.97 | 33 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6870.93 | 14.5 | 7.79 | 40 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 9637.2 | 31.07 | 10.73 | 63 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6886.17 | 43.5 | 12.06 | 82 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 9024.97 | 110.71 | 21.67 | 175 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6413.38 | 155.81 | 31.02 | 236 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 13002.49 | 7.5 | 4.32 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 12306.17 | 7.82 | 4.34 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 13569.76 | 21.66 | 8.92 | 47 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 13712.95 | 21.05 | 8.88 | 47 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 13557.36 | 73.03 | 23.28 | 141 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 12649.85 | 78.11 | 24.25 | 150 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 12349.97 | 7.91 | 4.39 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11463.83 | 8.45 | 4.52 | 22 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 12700.29 | 23.2 | 9.35 | 50 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12076.93 | 24.13 | 9.57 | 52 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 12060.2 | 81.91 | 24.6 | 153 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11589.72 | 84.58 | 26.05 | 161 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 14483.57 | 6.86 | 4.21 | 19 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 12976.98 | 7.65 | 4.38 | 20 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 15595.57 | 19.17 | 8.21 | 42 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 13811.15 | 21.66 | 8.88 | 47 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 14516.16 | 68.8 | 19.98 | 126 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 13532.25 | 73.8 | 20.93 | 133 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 12919.71 | 7.55 | 4.32 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 12287.77 | 7.85 | 4.49 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 14619.43 | 20.07 | 8.41 | 43 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 13732.49 | 21.17 | 8.66 | 46 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 14794.05 | 67.06 | 20.44 | 127 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 13460.01 | 72.92 | 22.49 | 139 | N/A | N/A |
