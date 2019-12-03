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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 23278.86 | 4.25 | 2.62 | 11 | 99.56 | 14.089 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 21767.76 | 4.55 | 2.79 | 12 | 99.53 | 14.542 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 23701.6 | 12.61 | 5.96 | 28 | 99.05 | 15.044 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 21930.63 | 13.63 | 13.28 | 30 | 98.97 | 15.086 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 22045.69 | 45.29 | 15.44 | 90 | 97.34 | 15.581 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 21094.8 | 47.26 | 114.73 | 93 | 97.27 | 15.591 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15819.04 | 6.28 | 4.28 | 19 | 99.06 | 15.066 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 11236.95 | 8.85 | 5.74 | 27 | 99.18 | 15.008 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 17978 | 16.63 | 7.46 | 38 | 97.92 | 15.067 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 11245.08 | 26.61 | 8.78 | 54 | 98.06 | 15.006 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 16529.64 | 60.42 | 18.08 | 112 | 94.31 | 15.653 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10511.08 | 95.06 | 17.46 | 145 | 94.42 | 15.737 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 18779.32 | 5.28 | 2.76 | 12 | 99.56 | 14.477 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 13284.97 | 7.47 | 3.38 | 17 | 99.63 | 14.488 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 19452.07 | 15.36 | 6.63 | 33 | 99.04 | 14.743 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 13711.57 | 21.81 | 8.14 | 44 | 99.19 | 14.782 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 17245.55 | 57.91 | 18.35 | 110 | 97.41 | 15.908 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12857.93 | 77.68 | 23.05 | 141 | 97.62 | 15.744 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 14375.18 | 6.91 | 4.41 | 20 | 99.03 | 14.271 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 8166.8 | 12.19 | 6.35 | 32 | 99.24 | 14.277 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13805.87 | 21.67 | 9.21 | 48 | 98.19 | 14.516 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7921.52 | 37.81 | 15.16 | 82 | 98.3 | 14.555 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13669.04 | 73.08 | 22.3 | 135 | 94.82 | 15.873 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7983.93 | 125.16 | 36.46 | 223 | 94.86 | 15.89 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 17097.8 | 5.61 | 2.82 | 13 | 99.6 | 15.161 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 16397.27 | 5.72 | 2.76 | 13 | 99.61 | 14.945 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 17928.65 | 16.13 | 6.95 | 34 | 99.11 | 15.195 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 17197.85 | 16.29 | 6.94 | 35 | 99.13 | 15.59 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 16556.88 | 59.53 | 19.66 | 117 | 97.44 | 16.303 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 16064.62 | 60.25 | 20.8 | 124 | 97.3 | 16.276 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 15627.44 | 6.18 | 2.93 | 14 | 99.58 | 15.161 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12675.03 | 7.6 | 3.26 | 17 | 99.61 | 14.96 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 16257.31 | 17.93 | 7.25 | 37 | 99.05 | 15.62 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 13446.82 | 21.48 | 8.1 | 45 | 99.11 | 15.19 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 14937.66 | 65.96 | 21.58 | 129 | 97.24 | 16.325 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 12407.92 | 79.08 | 24.96 | 151 | 97.35 | 16.301 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 19741.45 | 5.02 | 2.79 | 12 | 99.6 | 14.96 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0.89 | 355.45 | 267.93 | 2817.98 | 77 | 99.9 | 15.146 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 20802.03 | 14.36 | 6.42 | 31 | 99.12 | 15.18 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17936.7 | 16.66 | 6.93 | 35 | 99.24 | 15.177 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 18174.98 | 54.93 | 17.22 | 104 | 97.7 | 16.27 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 17114.58 | 58.34 | 18.22 | 110 | 97.81 | 16.288 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 17057.11 | 5.65 | 2.86 | 13 | 99.64 | 15.148 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16233.96 | 5.81 | 2.82 | 13 | 99.66 | 14.961 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17985.26 | 16.18 | 6.82 | 34 | 99.28 | 15.621 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17085.62 | 16.56 | 6.95 | 36 | 99.3 | 15.555 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17273.65 | 56.91 | 18.9 | 110 | 97.95 | 16.063 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16655.31 | 57.41 | 20.9 | 120 | 97.98 | 16.291 |
