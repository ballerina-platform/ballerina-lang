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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22038.27 | 4.5 | 2.98 | 13 | 99.44 | 19.935 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19806.12 | 5.01 | 12.71 | 14 | 99.47 | 19.952 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21862.77 | 13.67 | 6.22 | 30 | 99.02 | 19.936 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20018.11 | 14.93 | 6.71 | 33 | 99.09 | 19.936 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19618.06 | 50.9 | 16.17 | 97 | 97.41 | 19.976 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18875.19 | 52.91 | 16.25 | 99 | 97.48 | 19.987 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14472.28 | 6.87 | 15.16 | 22 | 98.91 | 20.247 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 7101.89 | 13.47 | 147.37 | 36 | 97.13 | 20.289 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16091.91 | 18.6 | 12.79 | 43 | 97.77 | 20.281 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 7346.9 | 40.76 | 107.46 | 76 | 91.79 | 20.307 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14384 | 69.46 | 25.58 | 128 | 93.78 | 20.312 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 5082.4 | 194.85 | 98.09 | 445 | 69.87 | 103.137 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17558.15 | 5.65 | 2.97 | 13 | 99.35 | 23.964 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12239.71 | 8.13 | 3.59 | 19 | 99.56 | 23.985 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18168.13 | 16.46 | 6.98 | 35 | 98.98 | 24.332 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12725.24 | 23.52 | 8.65 | 48 | 99.14 | 24.807 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15838.78 | 63.04 | 18.7 | 116 | 97.45 | 23.895 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12109.07 | 82.49 | 24.41 | 149 | 97.61 | 24.077 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12699.06 | 7.83 | 16.17 | 24 | 98.88 | 24.191 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 5938.85 | 16.79 | 32.98 | 45 | 97.28 | 24.145 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13534.11 | 22.11 | 9.93 | 51 | 97.89 | 24.869 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 5796.3 | 51.7 | 18.57 | 104 | 93.02 | 25.609 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12192.19 | 81.94 | 26.25 | 154 | 94.3 | 26.244 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 4474.04 | 223.46 | 62.63 | 487 | 75.3 | 173.757 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15706.71 | 6.18 | 3.1 | 14 | 99.5 | 23.873 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15059.81 | 6.34 | 3.07 | 15 | 99.42 | 23.836 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16227.78 | 18.08 | 7.44 | 38 | 99.03 | 24.346 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15570.93 | 18.52 | 7.58 | 40 | 99.07 | 24.255 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14908.67 | 66.08 | 20.87 | 127 | 97.33 | 24.59 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14722.71 | 66.6 | 21.91 | 131 | 97.33 | 24.563 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14310.09 | 6.81 | 3.18 | 16 | 99.5 | 23.888 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11823.68 | 8.2 | 3.44 | 19 | 99.53 | 24.01 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14585.71 | 20.21 | 7.98 | 42 | 98.96 | 24.904 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12024.75 | 24.32 | 8.93 | 50 | 99.05 | 25.578 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13383.19 | 74.26 | 22.7 | 140 | 97.26 | 27.698 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11525.53 | 85.82 | 25.9 | 159 | 97.33 | 23.787 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18400.96 | 5.39 | 2.97 | 13 | 99.51 | 24.021 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15862.84 | 6.26 | 14.12 | 15 | 99.55 | 24.632 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19375.97 | 15.43 | 6.81 | 34 | 99.04 | 24.916 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16587.01 | 18.03 | 7.41 | 39 | 99.18 | 24.468 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16966.76 | 58.86 | 18.56 | 112 | 97.61 | 23.064 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15815.6 | 63.14 | 19.11 | 117 | 97.74 | 23.654 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15391.13 | 6.32 | 3.17 | 15 | 99.58 | 23.996 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14826.66 | 6.47 | 3.16 | 15 | 99.6 | 24.005 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16614.94 | 17.68 | 7.34 | 38 | 99.22 | 24.483 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15618.92 | 18.48 | 7.59 | 40 | 99.25 | 24.503 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16078.69 | 61.72 | 19.37 | 117 | 97.79 | 24.888 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15298.43 | 63.7 | 21.1 | 125 | 97.96 | 24.857 |
