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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 14248.08 | 6.98 | 3.69 | 17 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 14357.28 | 6.92 | 3.69 | 17 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 15044.65 | 19.89 | 7.61 | 40 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 14456.83 | 20.69 | 7.76 | 42 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 13909.09 | 71.82 | 19.1 | 125 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 14519.85 | 68.79 | 18.42 | 121 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 9597.01 | 10.38 | 5.44 | 27 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 6789.88 | 14.68 | 6.66 | 35 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 9813.68 | 30.52 | 10 | 59 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 7496.06 | 39.96 | 10.16 | 72 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 9408.6 | 106.2 | 19.08 | 162 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 6692.18 | 149.34 | 25.4 | 216 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 11864.82 | 8.38 | 3.96 | 19 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 10508.71 | 9.47 | 4.21 | 21 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 11112.31 | 26.94 | 9.54 | 54 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 10561.01 | 28.34 | 9.86 | 56 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 11287.71 | 88.51 | 23.43 | 154 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 10382.83 | 96.21 | 24.94 | 166 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 8221.85 | 12.12 | 6.01 | 30 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6139.86 | 16.23 | 7.74 | 40 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 8212.49 | 36.47 | 13.48 | 75 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6088.61 | 49.21 | 15.34 | 94 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 7993.46 | 124.99 | 27.95 | 204 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 5808.18 | 172.06 | 33.06 | 263 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 10882.78 | 9.01 | 4.14 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 11007.82 | 8.83 | 4.02 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 11360.12 | 26.05 | 9.17 | 52 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 10764.52 | 27.29 | 9.47 | 54 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 11003.69 | 90.12 | 25.17 | 161 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 10346.39 | 95.03 | 26.24 | 169 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 10308.24 | 9.53 | 4.22 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 10206.29 | 9.55 | 4.1 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 10340.54 | 28.67 | 9.95 | 57 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 10150.24 | 28.95 | 10.14 | 58 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 10022.67 | 98.88 | 26.95 | 175 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10238.14 | 96.37 | 27.07 | 174 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 12709.72 | 7.82 | 3.82 | 18 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 11268.18 | 8.82 | 3.95 | 20 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 12239.21 | 24.45 | 8.76 | 49 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 11658.39 | 25.67 | 9.08 | 51 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 12335.09 | 80.98 | 21.76 | 142 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 11614.8 | 85.99 | 22.87 | 150 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 11156.39 | 8.79 | 3.98 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 11276.25 | 8.61 | 3.84 | 19 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 11675.45 | 25.36 | 9.03 | 51 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 11695.29 | 25.07 | 8.9 | 51 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 12263.04 | 80.77 | 22.53 | 142 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 11355.49 | 86.58 | 24.2 | 155 | N/A | N/A |
