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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 18829.46 | 5.27 | 3.61 | 16 | 99.49 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 17260.14 | 5.75 | 3.71 | 16 | 99.52 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 19773.71 | 15.12 | 7.19 | 35 | 98.29 | 16.604 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 18369.96 | 16.27 | 7.33 | 36 | 98.91 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 18133.18 | 55.07 | 17.82 | 106 | 96.58 | 10.875 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 17583.7 | 56.77 | 18.71 | 110 | 96.53 | 13.002 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 11426.36 | 8.71 | 6.09 | 28 | 98.83 | 14.319 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 8055.79 | 12.37 | 5.97 | 34 | 97.78 | 14.683 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 12292.35 | 24.35 | 9.68 | 53 | 97.55 | 15.326 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 8482.99 | 35.31 | 10.7 | 71 | 94.54 | 15.758 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 11448.18 | 87.28 | 18.77 | 139 | 92.91 | 16.217 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 7340.15 | 136.15 | 29.33 | 194 | 82.77 | 10.455 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 15409.35 | 6.44 | 3.51 | 16 | 99.44 | 15.026 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 11302.93 | 8.8 | 3.9 | 20 | 99.53 | 14.921 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 16308.74 | 18.34 | 7.94 | 40 | 98.82 | 15.878 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 11727.52 | 25.52 | 9.55 | 53 | 99.02 | 17.136 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 14639.71 | 68.22 | 21.43 | 128 | 96.73 | 21.265 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11180.61 | 89.34 | 26.22 | 162 | 97.13 | 19.537 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 10138.29 | 9.82 | 6.19 | 30 | 98.9 | 15.217 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6250.06 | 15.95 | 8.34 | 42 | 98.31 | 15.845 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 10710.17 | 27.95 | 12.15 | 64 | 97.78 | 17.206 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6421.03 | 46.66 | 19.02 | 100 | 95.86 | 16.463 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 9904.42 | 100.85 | 29.34 | 181 | 93.93 | 16.555 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 5673.32 | 176.15 | 49.88 | 299 | 86.44 | 16.994 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 14122.87 | 6.88 | 3.64 | 17 | 99.51 | 15.708 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 13583.37 | 7.05 | 3.59 | 17 | 99.53 | 15.706 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15248.55 | 19.12 | 8.2 | 42 | 98.87 | 15.73 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 14440.6 | 19.84 | 8.29 | 44 | 98.93 | 16.052 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 13961.67 | 70.74 | 22.79 | 136 | 96.76 | 16.717 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 13469.86 | 72.24 | 23.9 | 142 | 96.8 | 16.77 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 12792.99 | 7.62 | 3.74 | 18 | 99.5 | 15.494 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 10967.28 | 8.85 | 3.83 | 20 | 99.53 | 15.451 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 13777.68 | 21.3 | 8.71 | 46 | 98.83 | 16.328 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11270.63 | 25.95 | 9.65 | 54 | 98.93 | 15.963 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 12672.18 | 77.92 | 24.83 | 149 | 96.61 | 19.615 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10694.15 | 91.69 | 28.77 | 173 | 96.77 | 20.577 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 16110.84 | 6.16 | 3.6 | 16 | 99.51 | 15.8 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14179.16 | 7 | 3.73 | 17 | 99.5 | 15.638 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 16929.32 | 17.66 | 7.81 | 39 | 98.92 | 16.275 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15171.35 | 19.71 | 8.28 | 43 | 99.02 | 16.639 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15335.7 | 65.11 | 20.25 | 122 | 97.01 | 21.873 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14226.21 | 70.21 | 21.7 | 131 | 97.19 | 20.412 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 14303.43 | 6.8 | 3.65 | 17 | 99.57 | 15.502 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 13464.67 | 7.13 | 3.66 | 18 | 99.6 | 15.504 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 15583.63 | 18.81 | 7.98 | 41 | 99.08 | 16.976 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 14788.45 | 19.49 | 8.23 | 43 | 99.12 | 18.24 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15484.47 | 63.49 | 20.69 | 123 | 97.21 | 19.551 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14409.75 | 67.45 | 23.2 | 133 | 97.49 | 18.931 |
