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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21381.21 | 4.63 | 3.07 | 13 | 99.56 | 10.134 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19423.72 | 5.1 | 3.19 | 14 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21505.55 | 13.89 | 6.56 | 31 | 98.47 | 15.505 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 19981.41 | 14.95 | 6.95 | 34 | 98.58 | 15.699 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19808.33 | 50.4 | 16.24 | 97 | 97.33 | 9416 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19015.94 | 52.46 | 16.8 | 101 | 97.43 | 11.685 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 13985.34 | 7.11 | 4.81 | 22 | 99.06 | 15.41 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10100.49 | 9.85 | 5.91 | 29 | 99.15 | 15.425 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15982.63 | 18.71 | 8.02 | 41 | 97.98 | 15.613 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10483.88 | 28.55 | 8.94 | 56 | 98.12 | 15.333 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14335.18 | 69.69 | 19.01 | 123 | 94.67 | 16.629 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10018.92 | 99.73 | 17.45 | 148 | 94.22 | 16.609 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17276.54 | 5.74 | 3.03 | 13 | 99.54 | 15.506 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12234.49 | 8.12 | 3.59 | 18 | 99.62 | 14.979 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17923.05 | 16.68 | 7.12 | 36 | 99.04 | 15.667 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12535.82 | 23.86 | 8.76 | 48 | 99.19 | 16.11 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15770.57 | 63.33 | 19.09 | 117 | 97.45 | 19.883 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11865.55 | 84.19 | 24.01 | 150 | 97.66 | 22.078 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12569.48 | 7.91 | 4.77 | 22 | 99.05 | 14.09 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7399.03 | 13.46 | 6.85 | 35 | 99.22 | 14.088 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13660.68 | 21.9 | 9.07 | 48 | 98.08 | 14.617 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7484.74 | 40.02 | 15.66 | 85 | 98.34 | 14.633 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 11411.42 | 87.54 | 24.75 | 155 | 95.25 | 15.731 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7244.5 | 137.92 | 38.96 | 242 | 94.93 | 15.743 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15173.42 | 6.38 | 3.13 | 15 | 99.58 | 15.597 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14663.08 | 6.5 | 3.07 | 15 | 99.6 | 15.587 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15741.38 | 18.61 | 7.57 | 39 | 99.11 | 16.096 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15103.4 | 18.99 | 7.73 | 41 | 99.13 | 16.063 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14119.29 | 70.29 | 21.52 | 133 | 97.49 | 16.983 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14025.07 | 69.68 | 22.24 | 135 | 97.51 | 16.779 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13930.51 | 6.98 | 3.23 | 16 | 99.56 | 16.068 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11509.9 | 8.41 | 3.51 | 19 | 99.6 | 16.532 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14348.71 | 20.5 | 8.04 | 43 | 99.05 | 17.438 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11779.9 | 24.82 | 9.07 | 51 | 99.12 | 16.98 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13000.34 | 76.49 | 22.66 | 142 | 97.28 | 20.236 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11021.79 | 89.99 | 26.08 | 165 | 97.4 | 21.15 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 17909.46 | 5.54 | 2.99 | 13 | 99.57 | 15.802 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15545.79 | 6.38 | 3.14 | 15 | 99.63 | 15.757 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18424.53 | 16.22 | 6.93 | 35 | 99.13 | 16.188 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15863.96 | 18.85 | 7.61 | 40 | 99.24 | 16.172 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16165.11 | 61.78 | 18.91 | 116 | 97.71 | 18.381 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15063.03 | 66.3 | 19.51 | 121 | 97.84 | 19.253 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15195.86 | 6.39 | 3.17 | 15 | 99.64 | 15.879 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14329.9 | 6.68 | 3.2 | 16 | 99.67 | 15.73 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 15895.66 | 18.43 | 7.53 | 39 | 99.3 | 16.497 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15254.14 | 18.88 | 7.58 | 40 | 99.32 | 17.389 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15688.39 | 63.17 | 21.92 | 120 | 97.91 | 19.761 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14776.54 | 66.44 | 21.8 | 130 | 98.06 | 20.365 |
