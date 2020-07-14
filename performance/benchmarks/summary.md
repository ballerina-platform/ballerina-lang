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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21899.06 | 4.52 | 2.92 | 12 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20172.43 | 4.91 | 3.01 | 13 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22006.75 | 13.58 | 6.47 | 31 | 99.02 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20140.22 | 14.84 | 6.76 | 33 | 99.1 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19221.11 | 51.95 | 16.93 | 100 | 97.35 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18740.37 | 53.28 | 17.23 | 103 | 97.42 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14686.91 | 6.76 | 18.12 | 22 | 98.89 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9694.23 | 9.87 | 70.3 | 31 | 98.26 | 16.036 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15578.92 | 19.21 | 8.95 | 45 | 97.37 | 19.159 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10258.19 | 28.41 | 39.31 | 63 | 94.7 | 27.448 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14680.76 | 68.02 | 22.34 | 130 | 93.74 | 17.518 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 8240.16 | 117.14 | 69.21 | 177 | 84.08 | 20.673 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17214.25 | 5.76 | 2.97 | 13 | 99.54 | 15.131 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12043.94 | 8.25 | 3.61 | 19 | 99.62 | 15.149 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17399.68 | 17.18 | 7.18 | 37 | 99.03 | 16.658 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12277.03 | 24.38 | 8.99 | 50 | 99.17 | 16.983 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15705.98 | 63.59 | 20.07 | 120 | 97.3 | 19.438 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11614.38 | 86 | 24.88 | 154 | 97.65 | 20.935 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12376.56 | 8.03 | 19.71 | 24 | 99 | 15.161 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7100.12 | 14.04 | 21.75 | 36 | 98.51 | 15.286 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 12812.34 | 23.36 | 10.3 | 53 | 98.07 | 17.264 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7265.08 | 41.23 | 16.65 | 89 | 96.27 | 17.608 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12109.49 | 82.51 | 26.08 | 154 | 94.45 | 22.125 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6570.71 | 152.08 | 47.04 | 271 | 87.92 | 20.898 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15794.6 | 6.12 | 3.02 | 14 | 99.58 | 15.549 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15030.73 | 6.32 | 3 | 15 | 99.59 | 15.536 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16437.45 | 17.78 | 7.46 | 38 | 99.07 | 16.036 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15555.6 | 18.41 | 7.54 | 40 | 99.1 | 16.274 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15212.74 | 64.65 | 21.73 | 128 | 97.25 | 17.039 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14568.16 | 66.78 | 22.78 | 134 | 97.34 | 17.143 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14324.86 | 6.78 | 3.16 | 15 | 99.56 | 15.787 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11798.28 | 8.2 | 3.48 | 19 | 99.6 | 15.509 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14595.77 | 20.1 | 7.96 | 42 | 99.04 | 17.324 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11975.08 | 24.36 | 9 | 50 | 99.09 | 18.171 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13686.89 | 72.51 | 23.07 | 139 | 97.14 | 21.493 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11212.74 | 87.44 | 26.93 | 163 | 97.37 | 20.464 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18188.43 | 5.45 | 2.99 | 13 | 99.57 | 15.818 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15889.44 | 6.24 | 3.1 | 14 | 99.63 | 15.898 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18251.45 | 16.38 | 7.06 | 35 | 99.11 | 16.355 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16227.05 | 18.42 | 7.51 | 39 | 99.23 | 17.491 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16573.21 | 60.27 | 18.38 | 112 | 97.62 | 19.605 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15250.65 | 65.47 | 19.96 | 121 | 97.8 | 19.188 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16220.97 | 5.96 | 3.06 | 14 | 99.62 | 15.714 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15195.17 | 6.26 | 2.98 | 15 | 99.65 | 15.649 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17123.27 | 17.08 | 7.21 | 36 | 99.24 | 17.604 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16325.07 | 17.46 | 7.33 | 38 | 99.27 | 17.254 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16561.27 | 59.72 | 20.09 | 116 | 97.79 | 21.39 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15688.69 | 62.16 | 21.86 | 126 | 97.89 | 20.557 |
