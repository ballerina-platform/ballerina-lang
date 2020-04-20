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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22279.9 | 4.45 | 2.93 | 13 | 99.15 | 11.276 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20140.51 | 4.92 | 3.12 | 14 | 99.18 | 10.74 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22406.64 | 13.34 | 6.34 | 31 | 98.44 | 17.524 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20274.17 | 14.74 | 6.64 | 33 | 98.95 | 12.079 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19500.88 | 51.2 | 16.3 | 98 | 97.34 | 10.565 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19143.24 | 52.16 | 16.6 | 100 | 97.21 | 14.48 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14538.04 | 6.84 | 4.49 | 21 | 99.02 | 15.23 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10849.41 | 9.18 | 5.97 | 28 | 99.11 | 15.334 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16320.94 | 18.33 | 7.89 | 41 | 97.92 | 15.254 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10678.81 | 28.04 | 8.99 | 56 | 98.05 | 15.293 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14166.82 | 70.52 | 18.35 | 122 | 94.61 | 16.587 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10420.8 | 95.88 | 17.87 | 146 | 93.93 | 16.503 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17708.43 | 5.6 | 2.87 | 13 | 99.53 | 15.049 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12291.08 | 8.09 | 3.54 | 19 | 99.58 | 15.003 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18187.08 | 16.44 | 6.87 | 35 | 99.01 | 16.568 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12593.48 | 23.76 | 8.66 | 48 | 99.18 | 16.506 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 16160.56 | 61.81 | 19.29 | 116 | 97.33 | 20.386 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11896.18 | 83.97 | 24.1 | 150 | 97.62 | 20.581 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 11593.66 | 8.58 | 5.27 | 25 | 99.15 | 14.098 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7428.45 | 13.42 | 6.82 | 35 | 99.19 | 14.106 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13879.49 | 21.56 | 9.04 | 48 | 98.04 | 14.652 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7590.03 | 39.47 | 15.49 | 84 | 98.32 | 14.673 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12655.94 | 78.94 | 24.03 | 145 | 94.74 | 15.641 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7265.36 | 137.53 | 38.78 | 241 | 94.83 | 15.783 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15618.33 | 6.22 | 3.02 | 14 | 99.58 | 15.665 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15320.41 | 6.22 | 3.24 | 16 | 99.59 | 15.603 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16471.77 | 17.82 | 7.37 | 38 | 99.09 | 16.046 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15743.68 | 18.32 | 7.39 | 39 | 99.12 | 16.037 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14882.93 | 66.45 | 21.05 | 127 | 97.32 | 17.001 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14573.3 | 67.53 | 21.39 | 130 | 97.41 | 17.014 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14308.89 | 6.81 | 3.11 | 15 | 99.56 | 16.156 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11887.25 | 8.17 | 3.45 | 19 | 99.59 | 15.566 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14841.53 | 19.85 | 7.72 | 41 | 99.03 | 16.593 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12163.71 | 24.08 | 8.79 | 50 | 99.1 | 17.036 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13313.53 | 74.68 | 22.8 | 140 | 97.27 | 22.099 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11375.05 | 86.33 | 25.99 | 159 | 97.36 | 19.411 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18471.62 | 5.37 | 2.88 | 13 | 99.46 | 15.813 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15837.04 | 6.27 | 3.06 | 14 | 99.63 | 15.692 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19234.53 | 15.54 | 6.75 | 34 | 99.09 | 16.67 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16740.97 | 17.86 | 7.25 | 38 | 99.21 | 16.662 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16883.53 | 59.16 | 18.44 | 111 | 97.63 | 18.486 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15775.21 | 63.31 | 19.71 | 119 | 97.75 | 18.025 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15556.97 | 6.25 | 3.08 | 15 | 99.64 | 15.779 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14654.49 | 6.56 | 3.04 | 15 | 99.66 | 15.767 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16485.53 | 17.86 | 7.27 | 38 | 99.28 | 17.2 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15739.74 | 18.4 | 7.46 | 40 | 99.31 | 16.266 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16087.18 | 61.76 | 19.42 | 116 | 97.91 | 20.434 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15115.37 | 64.6 | 21.15 | 126 | 98.04 | 23.222 |
