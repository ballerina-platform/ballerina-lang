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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21908.43 | 4.52 | 2.84 | 12 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20151.6 | 4.92 | 2.98 | 13 | 99.59 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21437.05 | 13.73 | 10.5 | 31 | 99.04 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20003.89 | 14.94 | 6.59 | 32 | 99.12 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19738.02 | 50.59 | 16.69 | 99 | 97.13 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19433.83 | 51.38 | 16.95 | 100 | 97.31 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14910.71 | 6.67 | 14.89 | 21 | 99.05 | 13.245 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10122.42 | 9.84 | 44.35 | 30 | 98.17 | 16.573 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15601.45 | 19.18 | 13.31 | 46 | 97.39 | 16.178 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10621.01 | 28.19 | 35.24 | 61 | 94.92 | 24.279 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14519.55 | 68.8 | 21.94 | 130 | 93.77 | 17.204 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9108.95 | 107.62 | 43.58 | 168 | 82.88 | 25.761 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17448.35 | 5.69 | 2.9 | 13 | 99.53 | 15.147 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12142.03 | 8.19 | 3.58 | 19 | 99.62 | 15.167 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17482.67 | 17.1 | 7.17 | 37 | 99.03 | 16.484 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12228.94 | 24.47 | 8.96 | 50 | 99.19 | 16.677 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15859.7 | 62.98 | 19.66 | 118 | 97.32 | 19.719 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11788.23 | 84.75 | 24.28 | 152 | 97.65 | 20.481 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12533.08 | 7.93 | 11.99 | 23 | 98.99 | 15.193 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7262.23 | 13.73 | 15.9 | 35 | 98.45 | 15.296 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 12901.56 | 23.2 | 18.33 | 53 | 98.07 | 16.753 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7263.09 | 41.24 | 26.22 | 89 | 96.25 | 17.597 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12214.21 | 81.79 | 26.68 | 155 | 94.32 | 20.287 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6660.38 | 150.04 | 46.67 | 269 | 87.91 | 19.604 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15861.64 | 6.1 | 3.01 | 14 | 99.57 | 15.569 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14937.23 | 6.38 | 3.03 | 15 | 99.6 | 15.53 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16145.89 | 18.14 | 7.51 | 39 | 99.06 | 16.048 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15309.86 | 18.75 | 7.69 | 41 | 99.11 | 16.032 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14996.68 | 65.89 | 21.43 | 128 | 97.11 | 17.061 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14556.91 | 66.87 | 22.26 | 132 | 97.33 | 17.088 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14190.96 | 6.86 | 3.18 | 16 | 99.57 | 15.464 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11710.2 | 8.28 | 3.51 | 19 | 99.54 | 15.452 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14349.64 | 20.5 | 8.09 | 43 | 99.04 | 18.172 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11836.46 | 24.71 | 9.07 | 51 | 99.1 | 16.844 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13388.87 | 74.27 | 22.83 | 140 | 97.19 | 20.267 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10942.58 | 90.29 | 26.89 | 166 | 97.4 | 20.48 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18247.03 | 5.43 | 2.92 | 13 | 99.58 | 15.769 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15795.4 | 6.28 | 3.14 | 15 | 99.64 | 15.781 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18403.28 | 16.24 | 7.01 | 35 | 99.11 | 17.113 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15842.74 | 18.87 | 12.37 | 40 | 99.23 | 17.18 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16220.34 | 61.55 | 18.83 | 115 | 97.66 | 21.805 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14720.01 | 67.84 | 20.55 | 126 | 97.82 | 18.799 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16138.02 | 6.01 | 3.05 | 14 | 99.63 | 15.641 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15134.42 | 6.31 | 3.03 | 15 | 99.65 | 15.634 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17037.23 | 17.21 | 7.18 | 37 | 99.22 | 18.18 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16035.29 | 17.93 | 7.47 | 39 | 99.26 | 18.181 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16499.69 | 59.99 | 19.5 | 115 | 97.82 | 19.7 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15603.17 | 62.54 | 21.19 | 125 | 97.88 | 18.988 |
