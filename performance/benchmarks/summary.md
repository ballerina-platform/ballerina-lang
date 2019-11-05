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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22074.07 | 4.49 | 2.85 | 11 | 99.57 | 14.062 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20363.08 | 4.86 | 2.98 | 12 | 99.59 | 14.57 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22110.8 | 13.51 | 6.46 | 30 | 99.08 | 15.291 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20006.06 | 14.93 | 6.72 | 32 | 99.17 | 15.081 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19418.85 | 51.42 | 17.01 | 101 | 97.39 | 15.608 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19124.5 | 52.2 | 16.61 | 100 | 97.47 | 15.592 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15191.6 | 6.54 | 4.44 | 20 | 99.09 | 15.036 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10555.82 | 9.43 | 6 | 29 | 99.2 | 15.003 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16806.1 | 17.8 | 7.94 | 40 | 98.02 | 15.035 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10073.66 | 29.72 | 8.71 | 56 | 98.22 | 15.005 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 15349.21 | 65.08 | 19.49 | 120 | 94.63 | 15.942 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9934.64 | 100.58 | 17.5 | 151 | 94.51 | 15.991 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17636.2 | 5.62 | 2.95 | 13 | 99.53 | 14.465 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12258.9 | 8.11 | 3.58 | 19 | 99.63 | 14.462 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17803.19 | 16.79 | 7.07 | 35 | 99.09 | 15.036 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12367.07 | 24.19 | 8.86 | 49 | 99.22 | 15.039 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15773.2 | 63.32 | 19.07 | 117 | 97.54 | 15.903 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11776.54 | 84.82 | 23.66 | 150 | 97.77 | 15.991 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 13530.67 | 7.34 | 4.65 | 21 | 99.08 | 14.259 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7702.54 | 12.93 | 6.64 | 34 | 99.27 | 14.272 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14479.44 | 20.66 | 8.92 | 46 | 98.13 | 14.806 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7409.27 | 40.42 | 15.84 | 86 | 98.4 | 14.561 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12767.47 | 78.24 | 22.79 | 141 | 95.05 | 15.92 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7083.57 | 141.06 | 39.5 | 247 | 95.28 | 15.974 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15511.44 | 6.23 | 3.04 | 14 | 99.6 | 15.166 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14914.13 | 6.37 | 2.97 | 14 | 99.61 | 15.159 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15488.22 | 18.9 | 7.65 | 39 | 99.17 | 15.644 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 14662.02 | 19.6 | 7.81 | 42 | 99.2 | 15.631 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14394.8 | 68.87 | 21.85 | 133 | 97.57 | 16.303 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 13986.02 | 69.24 | 22.8 | 137 | 97.6 | 16.335 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14552.04 | 6.67 | 3.11 | 15 | 99.59 | 15.159 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11942.65 | 8.09 | 3.43 | 18 | 99.62 | 15.151 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14556.95 | 20.14 | 8 | 42 | 99.1 | 15.63 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11725.22 | 24.88 | 9.06 | 51 | 99.16 | 15.625 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 12854.54 | 76.9 | 23.83 | 145 | 97.5 | 16.3 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11007.07 | 89.65 | 26.39 | 165 | 97.48 | 16.387 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18284.21 | 5.42 | 2.95 | 13 | 99.59 | 15.164 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15852.13 | 6.26 | 3.13 | 14 | 99.64 | 15.161 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18246.56 | 16.38 | 7.05 | 35 | 99.17 | 15.396 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15921.79 | 18.77 | 7.61 | 39 | 99.29 | 15.391 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16159.47 | 61.79 | 18.7 | 115 | 97.78 | 16.373 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14720.78 | 67.81 | 19.63 | 123 | 98.02 | 16.362 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15756.8 | 6.14 | 3.07 | 14 | 99.66 | 15.164 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15041.07 | 6.32 | 2.99 | 14 | 99.68 | 15.155 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16657.74 | 17.56 | 7.28 | 37 | 99.3 | 15.615 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15611.93 | 18.35 | 7.52 | 39 | 99.34 | 15.596 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15824.12 | 62.37 | 20.07 | 120 | 98.03 | 16.368 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15226.77 | 64.26 | 21.53 | 129 | 97.97 | 16.361 |
