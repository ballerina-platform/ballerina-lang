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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 20562.73 | 4.82 | 3.02 | 13 | 99.44 | 19.922 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 18826.91 | 5.27 | 9.49 | 14 | 99.52 | 19.963 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21299.27 | 14.03 | 6.42 | 31 | 99.02 | 19.963 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 19637.32 | 15.22 | 14.17 | 34 | 99.04 | 19.963 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19212.12 | 51.98 | 16.71 | 100 | 97.33 | 19.932 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18350.34 | 54.42 | 16.53 | 102 | 97.54 | 19.923 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 100 | 246.12 | 398 | 3277.72 | 30079 | 98.07 | 394.554 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 100 | 231.01 | 425.94 | 2528.12 | 7039 | 52.24 | 1475.345 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 100 | 513.3 | 560.74 | 1305.25 | 1343 | 97.11 | 410.309 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 100 | 604.07 | 488.42 | 773.08 | 4079 | 55.41 | 1395.719 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 100 | 938.45 | 1063.25 | 395.92 | 1759 | 96.4 | 370.57 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 100 | 37465.18 | 23.27 | 25.04 | 126 | 6.02 | 1825.024 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 16906.15 | 5.87 | 3.1 | 14 | 99.35 | 23.746 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12105.28 | 8.21 | 11.7 | 18 | 99.56 | 23.951 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17208.55 | 17.37 | 7.13 | 37 | 99.02 | 24.345 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12464.81 | 24 | 8.82 | 49 | 99.14 | 24.791 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15426.84 | 64.74 | 19.43 | 120 | 97.47 | 24.676 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11763.06 | 84.92 | 24.15 | 151 | 97.67 | 24.849 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 100 | 40.61 | 2452.61 | 306.32 | 3551 | 98.18 | 148.512 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 100 | 40.56 | 2455.36 | 309.92 | 3583 | 98.17 | 255.091 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 100 | 39.05 | 7585.19 | 362.38 | 8767 | 97.79 | 183.662 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 100 | 40.32 | 7346.72 | 387.75 | 8703 | 97.95 | 350.988 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 100 | 36.84 | 26015.69 | 704.34 | 28159 | 98.22 | 397.062 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 100 | 36.36 | 26362.84 | 990.22 | 28671 | 96.43 | 653.166 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15187.75 | 6.38 | 3.21 | 15 | 99.47 | 23.837 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14548.27 | 6.56 | 3.16 | 15 | 99.53 | 23.821 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15685.86 | 18.67 | 7.71 | 40 | 99.03 | 24.269 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 14825.01 | 19.46 | 7.84 | 42 | 99.09 | 24.27 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14120.27 | 70.15 | 21.65 | 133 | 97.53 | 24.673 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14273.95 | 68.91 | 22.31 | 135 | 97.46 | 24.538 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13636.18 | 7.14 | 3.33 | 16 | 99.49 | 24.796 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11399.04 | 8.51 | 3.6 | 19 | 99.53 | 24.63 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 13978.34 | 21.06 | 8.3 | 44 | 98.68 | 25.362 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11626.51 | 25.27 | 9.19 | 52 | 98.97 | 25.163 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 12959.29 | 76.25 | 23.65 | 144 | 97.34 | 24.35 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11016.43 | 89.86 | 26.59 | 165 | 97.41 | 22.861 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 17713.68 | 5.6 | 3.11 | 14 | 99.51 | 24.013 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15503.42 | 6.4 | 3.26 | 15 | 99.57 | 24.222 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18069.56 | 16.54 | 7.05 | 35 | 99.09 | 24.51 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15887.58 | 18.82 | 7.7 | 40 | 99.2 | 24.455 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16264.92 | 61.41 | 18.84 | 115 | 97.64 | 23.571 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14995.56 | 66.61 | 19.24 | 121 | 97.83 | 22.776 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 14818.65 | 6.56 | 3.33 | 15 | 99.58 | 23.975 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14264.05 | 6.72 | 3.25 | 16 | 99.6 | 23.963 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 15710.9 | 18.69 | 7.66 | 39 | 99.23 | 24.511 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15019.82 | 19.3 | 7.69 | 41 | 99.26 | 24.457 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15412.39 | 64.19 | 20.23 | 121 | 97.87 | 25.096 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14809.99 | 66.2 | 22.41 | 132 | 97.87 | 24.912 |
