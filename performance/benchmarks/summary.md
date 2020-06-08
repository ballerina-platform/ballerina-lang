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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 20847.91 | 4.75 | 13.39 | 13 | 99.52 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19181.98 | 5.17 | 3.17 | 14 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 20544.34 | 14.55 | 6.72 | 32 | 98.99 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 19267.6 | 15.51 | 6.98 | 34 | 99.05 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19207.28 | 51.99 | 16.78 | 100 | 97.15 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18509.36 | 53.95 | 17.16 | 103 | 97.22 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 13804.3 | 7.2 | 21.43 | 23 | 98.86 | 10037 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9051.21 | 10.58 | 71.66 | 32 | 98.07 | 8794 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 14819.43 | 20.19 | 9.27 | 47 | 97.4 | 14.585 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 9229.3 | 30.99 | 63.15 | 65 | 94.96 | 16.498 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14039.64 | 71.15 | 22.85 | 134 | 93.57 | 11.197 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 8175.25 | 120.09 | 61.57 | 178 | 83.55 | 15.491 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 16679.88 | 5.95 | 3.08 | 14 | 99.53 | 15.405 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 11850 | 8.39 | 3.71 | 19 | 99.61 | 15.307 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17251 | 17.33 | 7.31 | 37 | 98.97 | 17.129 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12091.5 | 24.74 | 9.08 | 51 | 99.14 | 17.059 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15527.59 | 64.3 | 19.83 | 120 | 97.18 | 20.059 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11530.85 | 86.63 | 24.92 | 155 | 97.5 | 19.84 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 10761.03 | 9.25 | 21.24 | 27 | 99.08 | 15.334 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6674.18 | 14.93 | 27.11 | 39 | 98.54 | 15.385 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 11336.53 | 26.4 | 19.97 | 60 | 98.12 | 15.771 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6862.15 | 43.66 | 17.48 | 94 | 96.33 | 16.497 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 10680.56 | 93.54 | 30.81 | 172 | 94.48 | 20.541 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6123.61 | 163.2 | 50.98 | 285 | 88 | 18.777 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15463.7 | 6.24 | 3.17 | 15 | 99.56 | 15.696 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14681.65 | 6.46 | 3.15 | 15 | 99.57 | 15.698 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15737.06 | 18.54 | 7.68 | 39 | 99.01 | 16.177 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15086.11 | 18.89 | 7.83 | 41 | 99.07 | 16.205 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14598.76 | 67.74 | 22.07 | 132 | 97.01 | 17.253 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14069.38 | 69.5 | 23.11 | 138 | 97.18 | 17.242 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14020.45 | 6.93 | 3.24 | 16 | 99.55 | 15.435 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11383.94 | 8.51 | 3.59 | 19 | 99.59 | 15.522 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14511.86 | 20.19 | 8.15 | 43 | 98.97 | 16.976 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11745.56 | 24.83 | 9.21 | 51 | 99.06 | 17.491 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13257.46 | 74.91 | 23.53 | 143 | 97.05 | 21.3 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11076.43 | 89.22 | 27.05 | 166 | 97.16 | 21.319 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 17220.53 | 5.76 | 3.13 | 14 | 99.57 | 15.936 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15169.74 | 6.54 | 10.47 | 15 | 99.61 | 16.036 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18082.08 | 16.53 | 7.13 | 36 | 99.06 | 16.515 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15837.13 | 18.87 | 7.75 | 40 | 99.16 | 16.741 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16044.84 | 62.24 | 19.34 | 116 | 97.49 | 21.874 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14846.46 | 67.27 | 20.63 | 125 | 97.61 | 20.87 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15769.03 | 6.13 | 3.14 | 15 | 99.61 | 15.799 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14711.64 | 6.48 | 3.14 | 15 | 99.64 | 15.771 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16983.76 | 17.21 | 7.32 | 37 | 99.18 | 17.438 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15622.76 | 18.36 | 7.6 | 40 | 99.26 | 18.105 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16326.69 | 60.19 | 19.67 | 116 | 97.56 | 22.443 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15375.79 | 63.11 | 21.56 | 127 | 97.78 | 22.605 |
