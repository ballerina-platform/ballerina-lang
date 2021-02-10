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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 13836.84 | 7.18 | 3.85 | 18 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 12829.48 | 7.75 | 4.11 | 19 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 14423.44 | 20.74 | 8.42 | 44 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 14116.33 | 21.19 | 8.56 | 45 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 13180.93 | 75.79 | 21.66 | 136 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 13466 | 74.18 | 21.52 | 134 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 9321.29 | 10.68 | 6.68 | 31 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 7279.25 | 13.69 | 7.36 | 38 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 9938.07 | 30.13 | 13.39 | 69 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 7396.76 | 40.5 | 12.42 | 80 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 9345.32 | 106.93 | 32.53 | 196 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 7017.08 | 142.41 | 28.46 | 222 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 11106.07 | 8.96 | 4.2 | 21 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 9875.52 | 10.08 | 4.49 | 23 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 10468.64 | 28.6 | 10.56 | 58 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 10270.39 | 29.15 | 10.67 | 59 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 10765.75 | 92.8 | 25.46 | 164 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 9963.69 | 100.26 | 26.88 | 175 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 7811.88 | 12.75 | 7.28 | 35 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 5909.07 | 16.87 | 9.19 | 46 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 7693.87 | 38.93 | 16.7 | 87 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6008.62 | 49.86 | 19.5 | 106 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 7835.63 | 127.53 | 38.18 | 232 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 5748.53 | 173.85 | 44.73 | 299 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 10973.22 | 8.93 | 4.17 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 10582.86 | 9.19 | 4.19 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 10896.3 | 27.16 | 10.1 | 56 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 10818.18 | 27.1 | 10.15 | 56 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 10323.05 | 96.33 | 26.95 | 172 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 10473.01 | 94.89 | 27.05 | 172 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 9460.82 | 10.41 | 4.53 | 23 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 9743.69 | 10.02 | 4.36 | 23 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 9749.81 | 30.44 | 10.93 | 61 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 9680.24 | 30.44 | 10.85 | 61 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 9951.54 | 99.96 | 28.02 | 180 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 9614.89 | 103.03 | 28.94 | 185 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 11571.39 | 8.6 | 4.08 | 20 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 11089.04 | 8.97 | 4.07 | 20 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 12412.27 | 24.11 | 9.3 | 50 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 11907.05 | 25.13 | 9.6 | 52 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 11916.55 | 83.83 | 23.99 | 150 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 11518 | 86.71 | 24.89 | 155 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 10798.29 | 9.09 | 4.09 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 11057.58 | 8.8 | 3.95 | 20 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 11558.11 | 25.61 | 9.57 | 52 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 10989.67 | 26.73 | 9.93 | 55 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 11331.33 | 87.49 | 24.24 | 155 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 11424.83 | 86.85 | 24.33 | 156 | N/A | N/A |
