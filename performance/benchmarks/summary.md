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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 15025.49 | 6.61 | 3.55 | 16 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 13870.83 | 7.16 | 3.76 | 17 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 14830.53 | 20.17 | 7.73 | 41 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 14262.9 | 20.97 | 7.81 | 42 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 13654.05 | 73.16 | 19.39 | 128 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 13585.33 | 73.53 | 19.22 | 128 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 9831.54 | 10.13 | 5.47 | 27 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 7326.85 | 13.6 | 6.48 | 34 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 10129.28 | 29.56 | 9.92 | 58 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 7290.86 | 41.09 | 10.1 | 73 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 9699.61 | 103.02 | 18.39 | 157 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 7232.03 | 138.18 | 25.44 | 206 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 12440.48 | 7.99 | 3.85 | 18 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 11038.61 | 9.01 | 4.09 | 20 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 12010.3 | 24.92 | 9.04 | 50 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 11301.1 | 26.48 | 9.33 | 52 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 11901.6 | 83.93 | 22.97 | 148 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 10879.99 | 91.81 | 24.22 | 159 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 8586.81 | 11.6 | 5.83 | 29 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6462.44 | 15.42 | 7.38 | 38 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 8660.87 | 34.58 | 12.15 | 69 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6539.93 | 45.81 | 14.79 | 89 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 8409.61 | 118.82 | 26.52 | 193 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6086.27 | 164.2 | 32.17 | 251 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 11322.04 | 8.64 | 4.01 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 10945.08 | 8.85 | 3.96 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 11132.1 | 26.55 | 9.44 | 53 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 11031.77 | 26.56 | 9.43 | 53 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 11025.96 | 89.83 | 25.27 | 162 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 10616.47 | 92.68 | 26.29 | 168 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 10198.32 | 9.62 | 4.2 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 10090.46 | 9.65 | 4.11 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 10278.38 | 28.82 | 10.13 | 57 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 10315.52 | 28.49 | 9.86 | 57 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 10166.61 | 97.66 | 27 | 175 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 9715.16 | 102.19 | 27.78 | 182 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 12641.08 | 7.86 | 3.8 | 18 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 11088.19 | 8.97 | 4.06 | 20 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 12406.8 | 24.12 | 8.77 | 48 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 11324.98 | 26.42 | 9.25 | 52 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 11905.45 | 83.89 | 22.13 | 145 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 11324.98 | 88.19 | 23.28 | 153 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 11305.02 | 8.66 | 3.94 | 19 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 10964.9 | 8.85 | 3.93 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 11893.14 | 24.85 | 8.84 | 49 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 11509.44 | 25.43 | 9.04 | 51 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 11933.3 | 83.09 | 22.78 | 147 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 11252.48 | 87.45 | 24.29 | 156 | N/A | N/A |
