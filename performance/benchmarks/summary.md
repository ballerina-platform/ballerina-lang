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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21542.82 | 4.6 | 2.84 | 12 | 99.57 | 10.163 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19702.18 | 5.03 | 3.01 | 13 | 99.39 | 11.352 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21927.4 | 13.63 | 6.4 | 31 | 98.48 | 16.142 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20170.62 | 14.82 | 6.72 | 33 | 98.57 | 15.274 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 20303.48 | 49.19 | 16.33 | 96 | 97.11 | 16.497 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19148.61 | 52.15 | 16.41 | 99 | 97.37 | 13.876 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14413.34 | 6.9 | 4.58 | 21 | 99.02 | 15.347 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10662.08 | 9.34 | 5.91 | 28 | 99.15 | 15.273 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16228.44 | 18.44 | 7.87 | 41 | 97.89 | 15.188 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10903.1 | 27.46 | 8.97 | 55 | 98.08 | 15.654 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14546 | 68.68 | 19.37 | 123 | 94.68 | 16.501 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10221.46 | 97.76 | 17.17 | 146 | 94.05 | 16.604 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17483.29 | 5.68 | 2.88 | 13 | 99.54 | 15.524 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12384.67 | 8.03 | 3.46 | 18 | 99.54 | 14.945 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18239.58 | 16.39 | 6.81 | 35 | 99.02 | 16.45 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12797.42 | 23.38 | 8.52 | 48 | 99.17 | 16.907 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15895.34 | 62.84 | 19.07 | 116 | 97.39 | 19.513 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12004.33 | 83.21 | 23.94 | 149 | 97.67 | 20.28 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12645.23 | 7.87 | 4.86 | 23 | 99.04 | 14.003 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7472.29 | 13.33 | 6.76 | 35 | 99.23 | 14.028 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13819.49 | 21.65 | 8.99 | 48 | 98.07 | 14.3 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7608.68 | 39.37 | 15.46 | 84 | 98.32 | 14.3 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12513.98 | 79.84 | 23.2 | 143 | 94.82 | 15.634 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7020.67 | 142.33 | 40.16 | 249 | 94.86 | 15.643 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15810.91 | 6.14 | 3.03 | 14 | 99.58 | 15.549 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15106.11 | 6.34 | 2.95 | 15 | 99.6 | 15.537 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16176.3 | 18.21 | 7.27 | 38 | 99.12 | 16.045 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15459.84 | 18.76 | 7.48 | 40 | 99.14 | 16.062 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15014.42 | 65.98 | 20.72 | 126 | 97.45 | 16.929 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14321.53 | 68.4 | 20.97 | 130 | 97.54 | 16.768 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14284.26 | 6.83 | 3.09 | 15 | 99.56 | 15.659 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11758.84 | 8.28 | 3.44 | 19 | 99.6 | 16.104 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14373.92 | 20.55 | 8.01 | 43 | 99.07 | 16.471 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12212.32 | 24.01 | 8.72 | 50 | 99.11 | 16.981 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13449.84 | 73.88 | 22.03 | 137 | 97.3 | 20.169 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11300.06 | 87.13 | 26.09 | 161 | 97.42 | 20.222 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18051.21 | 5.49 | 2.94 | 13 | 99.53 | 15.716 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16078.89 | 6.17 | 3.04 | 14 | 99.63 | 15.581 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18931.06 | 15.79 | 6.71 | 34 | 99.11 | 16.14 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16530.65 | 18.08 | 7.34 | 38 | 99.24 | 16.181 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16776.58 | 59.53 | 17.89 | 110 | 97.68 | 18.738 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15446.01 | 64.65 | 19.49 | 120 | 97.84 | 18.353 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15735.55 | 6.18 | 3.33 | 16 | 99.64 | 15.471 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14814.91 | 6.5 | 3.62 | 18 | 99.66 | 15.935 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16688.32 | 17.63 | 7.55 | 38 | 99.27 | 17.096 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15721.64 | 18.47 | 7.48 | 40 | 99.31 | 17.068 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16066.98 | 61.69 | 18.79 | 114 | 97.97 | 20.245 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15209.7 | 64.62 | 20.47 | 122 | 98.07 | 20.146 |
