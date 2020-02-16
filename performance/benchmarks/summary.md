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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21053.02 | 4.71 | 2.95 | 12 | 99.56 | 10.392 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19194.15 | 5.16 | 3.1 | 13 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21418.62 | 13.95 | 6.61 | 32 | 98.51 | 15.804 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 19263.19 | 15.51 | 6.87 | 34 | 98.62 | 15.438 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19835.94 | 50.33 | 16.5 | 98 | 97.23 | 14.101 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18831.14 | 53.04 | 17.05 | 102 | 97.3 | 16.111 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14131.32 | 7.04 | 4.6 | 21 | 99.05 | 15.383 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10673.51 | 9.32 | 5.94 | 28 | 99.12 | 15.323 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15879.94 | 18.84 | 8.11 | 42 | 97.97 | 15.237 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10705.73 | 27.96 | 8.95 | 55 | 98.07 | 15.324 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14001.53 | 71.35 | 19.93 | 127 | 94.54 | 16.587 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9317.69 | 107.22 | 16.85 | 154 | 94.5 | 16.471 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17134.42 | 5.79 | 3.02 | 13 | 99.54 | 15.001 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12065.66 | 8.24 | 3.61 | 18 | 99.62 | 15.028 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17555.21 | 17.03 | 7.08 | 36 | 99.04 | 15.599 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12356.62 | 24.21 | 8.81 | 49 | 99.19 | 15.563 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15764.17 | 63.36 | 19.88 | 120 | 97.35 | 20.859 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11671.1 | 85.59 | 24.17 | 152 | 97.67 | 20.457 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12375.45 | 8.04 | 4.9 | 23 | 99.07 | 14.086 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7331.24 | 13.59 | 6.89 | 35 | 99.23 | 14.086 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13484.55 | 22.19 | 9.18 | 49 | 98.1 | 14.616 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7483.98 | 40.02 | 15.74 | 86 | 98.31 | 14.63 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12096.43 | 82.59 | 23.71 | 148 | 94.95 | 15.676 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7072.79 | 141.3 | 39.49 | 247 | 94.93 | 15.761 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15217.03 | 6.36 | 3.14 | 15 | 99.58 | 15.576 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14494.01 | 6.57 | 3.13 | 15 | 99.6 | 15.582 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15561.08 | 18.8 | 7.63 | 39 | 99.13 | 16.096 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 14880.38 | 19.28 | 7.75 | 41 | 99.16 | 16.094 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14336.51 | 69.17 | 21.93 | 133 | 97.25 | 17.081 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 13944.96 | 70.38 | 22.57 | 138 | 97.5 | 17.005 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13720.27 | 7.1 | 3.27 | 16 | 99.57 | 16.07 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11400.13 | 8.5 | 3.55 | 19 | 99.6 | 16.13 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 13961.72 | 21.08 | 8.19 | 44 | 99.07 | 16.305 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11865.21 | 24.58 | 9.09 | 51 | 99.11 | 18.067 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13014.48 | 76.39 | 22.97 | 143 | 97.34 | 19.724 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10996.13 | 90.24 | 25.85 | 164 | 97.45 | 20.214 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 17709.94 | 5.6 | 3.05 | 13 | 99.58 | 15.789 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15519.64 | 6.39 | 3.19 | 15 | 99.62 | 15.656 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18439.95 | 16.21 | 7.02 | 35 | 99.12 | 16.631 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15815.44 | 18.91 | 7.55 | 39 | 99.25 | 16.249 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16237.03 | 61.49 | 18.62 | 114 | 97.72 | 17.935 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14991.96 | 66.6 | 19.83 | 122 | 97.84 | 20.694 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15041.34 | 6.46 | 3.22 | 15 | 99.64 | 16.016 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14411.35 | 6.64 | 3.2 | 15 | 99.66 | 15.778 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16095.18 | 18.21 | 7.57 | 39 | 99.28 | 18.278 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15307.77 | 18.79 | 7.67 | 41 | 99.31 | 16.208 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15597.68 | 63.64 | 20.37 | 120 | 97.99 | 20.768 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14813.89 | 66.16 | 21.56 | 130 | 98.04 | 20.243 |
