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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21338.54 | 4.64 | 3.12 | 13 | 99.56 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19498.15 | 5.09 | 3.26 | 14 | 99.59 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22018.66 | 13.57 | 6.47 | 30 | 98.96 | 17.173 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 19915.38 | 15 | 6.82 | 33 | 99.14 | 13.729 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 18962.15 | 52.67 | 16.83 | 101 | 97.18 | 23.107 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18405.36 | 54.26 | 16.72 | 102 | 97.61 | 9849 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14305.72 | 6.95 | 4.63 | 21 | 99.03 | 15.469 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10586.7 | 9.4 | 5.99 | 28 | 99.14 | 15.364 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15654.98 | 19.11 | 8.11 | 42 | 97.99 | 15.632 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10707.1 | 27.95 | 9.33 | 56 | 98.13 | 15.627 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14088.26 | 70.91 | 19.73 | 126 | 94.72 | 16.522 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10082.17 | 99.1 | 18.28 | 150 | 94.26 | 16.444 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17216.22 | 5.76 | 3.03 | 13 | 99.5 | 15.079 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12197.26 | 8.15 | 3.54 | 18 | 99.62 | 15.082 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17528.63 | 17.05 | 7.14 | 36 | 99.07 | 16.543 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12311.16 | 24.3 | 8.92 | 49 | 99.2 | 16.481 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15229.99 | 65.59 | 19.42 | 120 | 97.53 | 18.969 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11550.76 | 86.47 | 24.14 | 152 | 97.74 | 19.072 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12671.59 | 7.84 | 4.95 | 23 | 99.05 | 14.021 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7314.26 | 13.62 | 6.89 | 35 | 99.24 | 14.034 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13826.81 | 21.64 | 9.26 | 48 | 98.09 | 14.571 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7067.98 | 42.38 | 16.28 | 89 | 98.39 | 14.581 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12243.41 | 81.6 | 24.63 | 150 | 94.98 | 15.638 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6854.93 | 145.77 | 38.98 | 250 | 95.2 | 15.726 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15435.75 | 6.28 | 3.12 | 14 | 99.59 | 15.886 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14027.3 | 6.84 | 3.18 | 16 | 99.6 | 15.864 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15790.09 | 18.54 | 7.6 | 39 | 99.13 | 16.091 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 14937.79 | 19.27 | 7.8 | 41 | 99.15 | 16.097 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 13761.12 | 72.1 | 22.01 | 136 | 97.58 | 16.888 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14249.98 | 69.11 | 22.45 | 136 | 97.49 | 17.06 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14089.17 | 6.91 | 3.23 | 15 | 99.57 | 15.615 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11426.42 | 8.5 | 3.52 | 19 | 99.6 | 15.726 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 13582.07 | 21.71 | 8.44 | 45 | 99.08 | 16.032 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11828.62 | 24.73 | 9.08 | 51 | 99.13 | 16.02 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 12758.89 | 77.95 | 24.53 | 148 | 97.36 | 19.693 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10794.63 | 92.04 | 26.08 | 166 | 97.46 | 19.812 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 17552.28 | 5.65 | 3.07 | 13 | 99.59 | 15.697 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15404.51 | 6.44 | 3.22 | 15 | 99.52 | 15.61 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18199.01 | 16.42 | 7.04 | 35 | 99.16 | 16.195 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15355.57 | 19.47 | 7.82 | 41 | 99.27 | 16.493 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15899.86 | 62.81 | 19.19 | 117 | 97.79 | 22.311 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15174.33 | 65.83 | 19.94 | 122 | 97.86 | 17.877 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15402.81 | 6.3 | 3.19 | 15 | 99.64 | 15.721 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14696.73 | 6.51 | 3.15 | 15 | 99.65 | 15.676 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 15977.32 | 18.38 | 7.58 | 39 | 99.29 | 18.162 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15243.49 | 18.96 | 7.68 | 41 | 99.33 | 17.554 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15621.23 | 63.44 | 20.45 | 122 | 97.92 | 19.828 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14867.45 | 65.97 | 21.22 | 127 | 98.09 | 19.74 |
