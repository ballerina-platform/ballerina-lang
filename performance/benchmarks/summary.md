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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21416.96 | 4.63 | 2.89 | 12 | 99.57 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19718.92 | 5.02 | 3.03 | 13 | 99.59 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22037.37 | 13.56 | 6.39 | 30 | 99.07 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20081.41 | 14.88 | 6.78 | 33 | 99.14 | 11.286 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19176.76 | 52.08 | 16.71 | 100 | 97.4 | 16.133 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18851.79 | 52.96 | 17.01 | 102 | 97.43 | 11.841 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14390.76 | 6.91 | 4.5 | 21 | 99.06 | 15.277 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10870.25 | 9.15 | 5.86 | 28 | 99.11 | 15.35 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16220.72 | 18.44 | 8.2 | 42 | 97.71 | 15.62 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10906.91 | 27.44 | 9.4 | 56 | 98.09 | 15.621 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14323.33 | 69.74 | 19.97 | 125 | 94.88 | 16.507 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10176.29 | 98.19 | 18.18 | 149 | 94.27 | 16.666 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17247.59 | 5.75 | 2.95 | 13 | 99.55 | 15.139 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12097.24 | 8.22 | 3.59 | 19 | 99.63 | 15.059 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17518.74 | 17.07 | 7.15 | 36 | 99.07 | 16.918 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12027.39 | 24.88 | 9.18 | 51 | 99.2 | 15.802 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15440.77 | 64.67 | 20.08 | 121 | 97.47 | 20.25 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11610.79 | 86.03 | 24.12 | 152 | 97.78 | 20.28 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12900.48 | 7.71 | 4.78 | 22 | 99.05 | 14.04 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7313.44 | 13.63 | 6.82 | 35 | 99.19 | 14.033 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13492.98 | 22.18 | 11.46 | 49 | 98.09 | 14.319 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7126.04 | 42.04 | 16.17 | 88 | 98.41 | 14.478 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12242.53 | 81.58 | 24.01 | 148 | 95.01 | 15.553 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6859.85 | 145.67 | 39.98 | 252 | 95.24 | 15.598 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15335.64 | 6.32 | 3.09 | 14 | 99.59 | 15.524 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14636.32 | 6.54 | 3.03 | 15 | 99.6 | 15.726 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15364.99 | 19.14 | 7.68 | 40 | 99.14 | 15.996 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 14876.51 | 19.4 | 7.78 | 42 | 99.17 | 15.99 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14550.66 | 68.12 | 21.59 | 130 | 97.47 | 9898 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14132.05 | 69.22 | 22.56 | 136 | 97.44 | 17.056 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13837.89 | 7.05 | 3.2 | 16 | 99.57 | 16.205 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11472.94 | 8.47 | 3.52 | 19 | 99.6 | 15.663 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14039.92 | 21 | 8.16 | 44 | 99.08 | 16.962 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11694.77 | 25.05 | 9.06 | 51 | 99.13 | 17.041 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 12875.59 | 76.88 | 23.08 | 143 | 97.44 | 20.28 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10881.13 | 90.92 | 26.32 | 165 | 97.46 | 20.265 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18297.84 | 5.42 | 2.92 | 13 | 99.58 | 15.715 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15963.42 | 6.21 | 3.11 | 14 | 99.64 | 15.68 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18497.77 | 16.16 | 6.95 | 35 | 99.16 | 16.182 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15899.21 | 18.8 | 7.61 | 40 | 99.26 | 16.235 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15959.66 | 62.58 | 19.14 | 117 | 97.81 | 20.291 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14815.02 | 67.41 | 19.58 | 122 | 97.94 | 20.174 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15521.73 | 6.26 | 3.1 | 15 | 99.64 | 15.73 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14713.86 | 6.52 | 3.06 | 15 | 99.66 | 15.742 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16109.96 | 18.25 | 7.43 | 38 | 99.31 | 16.862 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15386.27 | 18.78 | 7.58 | 40 | 99.31 | 16.866 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15498.98 | 64.07 | 19.9 | 121 | 97.99 | 21.474 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14968.43 | 65.85 | 22.79 | 129 | 98.07 | 20.241 |
