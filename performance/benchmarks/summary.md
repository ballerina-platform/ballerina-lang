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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 19559.2 | 5.07 | 3.33 | 14 | 99.56 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 18242.25 | 5.44 | 3.42 | 15 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 20472.11 | 14.61 | 6.85 | 33 | 98.98 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 18984.34 | 15.75 | 6.97 | 35 | 99.06 | 10.457 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 18648.53 | 53.56 | 17.36 | 103 | 96.94 | 18.774 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18219.61 | 54.79 | 17.33 | 104 | 97.12 | 17.724 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 12091.93 | 8.23 | 5.79 | 27 | 98.82 | 14.104 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 8414.98 | 11.84 | 5.98 | 33 | 97.99 | 14.155 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 12536.12 | 23.88 | 9.22 | 51 | 97.79 | 15.221 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 8854.49 | 33.83 | 9.92 | 66 | 95.28 | 15.838 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 11881.56 | 84.09 | 17.87 | 134 | 93.75 | 15.775 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 7905.21 | 126.41 | 27.06 | 184 | 85.12 | 11.09 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 16027.19 | 6.19 | 3.36 | 15 | 99.53 | 14.833 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 11606.27 | 8.57 | 3.74 | 19 | 99.61 | 14.858 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 16651.96 | 17.96 | 7.9 | 40 | 98.98 | 17.031 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12013.35 | 24.91 | 9.22 | 51 | 99.13 | 15.747 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15088.99 | 66.19 | 20.56 | 124 | 97.21 | 19.997 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11386.14 | 87.73 | 25.4 | 157 | 97.5 | 20.089 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 10426.37 | 9.55 | 6.06 | 29 | 98.96 | 15.619 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6369.68 | 15.65 | 8.22 | 42 | 98.5 | 15.836 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 10842.51 | 27.61 | 12.19 | 64 | 98.01 | 16.19 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6616.96 | 45.28 | 18 | 97 | 96.39 | 16.345 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 10364.74 | 96.4 | 28.43 | 174 | 94.48 | 18.963 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 5954.71 | 167.85 | 47.2 | 287 | 88.34 | 19.353 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 14669.12 | 6.64 | 3.46 | 16 | 99.57 | 15.523 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14245.19 | 6.74 | 3.4 | 17 | 99.58 | 15.694 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15420.44 | 19.08 | 8.04 | 42 | 99.04 | 16.048 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 14595.5 | 19.89 | 8.12 | 43 | 99.08 | 16.048 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14315.02 | 69.3 | 21.45 | 131 | 97.15 | 16.727 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 13975.43 | 69.82 | 22.57 | 135 | 97.25 | 16.886 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13286.28 | 7.36 | 3.51 | 17 | 99.55 | 15.312 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11092.34 | 8.79 | 3.73 | 20 | 99.59 | 15.412 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 13490.5 | 21.92 | 8.63 | 46 | 99.01 | 16.319 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11691.26 | 25.09 | 9.37 | 52 | 99.04 | 17.181 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13015.1 | 76.32 | 23.58 | 143 | 96.99 | 19.927 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10806.75 | 91.34 | 27.44 | 168 | 97.19 | 20.412 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 16947.03 | 5.86 | 3.33 | 15 | 99.58 | 15.952 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14808.8 | 6.7 | 3.5 | 16 | 99.63 | 15.908 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 17339.3 | 17.25 | 7.38 | 38 | 99.08 | 16.026 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15463.53 | 19.34 | 8 | 42 | 99.18 | 16.23 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15781.37 | 63.27 | 19.27 | 117 | 97.41 | 21.31 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14664.08 | 68.11 | 20.38 | 125 | 97.61 | 20.068 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 14739.7 | 6.61 | 3.55 | 16 | 99.63 | 15.497 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14224.11 | 6.76 | 3.48 | 17 | 99.64 | 15.636 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16042.26 | 18.36 | 7.61 | 39 | 99.2 | 17.137 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15179.71 | 19.14 | 8.01 | 42 | 99.24 | 16.861 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15607.22 | 63.32 | 20.19 | 121 | 97.63 | 20.315 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14791.95 | 66.11 | 21.81 | 129 | 97.69 | 20.293 |
