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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22208.88 | 4.46 | 8.73 | 12 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20225.53 | 4.9 | 3 | 13 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22275.48 | 13.42 | 6.41 | 30 | 99.03 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20310.07 | 14.72 | 6.55 | 32 | 99.11 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19929.99 | 50.1 | 16.3 | 97 | 97.35 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19216.05 | 51.97 | 17.04 | 101 | 97.39 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15061.13 | 6.6 | 10.98 | 21 | 98.94 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10530.71 | 9.45 | 52.41 | 30 | 98.08 | 16.584 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15638.4 | 19.14 | 19.13 | 45 | 97.59 | 15.153 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 9193.42 | 31.04 | 70.1 | 63 | 95.13 | 21.196 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 15010.48 | 66.56 | 21.87 | 127 | 93.86 | 13.535 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9349.12 | 106.89 | 53.7 | 170 | 82.8 | 29.333 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17765.24 | 5.58 | 2.97 | 13 | 99.54 | 15.244 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12088.34 | 8.23 | 3.65 | 19 | 99.63 | 15.155 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17715.31 | 16.88 | 7.1 | 36 | 99.03 | 16.894 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12502.79 | 23.93 | 8.85 | 49 | 99.18 | 15.849 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15898.02 | 62.82 | 19.35 | 117 | 97.37 | 21.046 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11834.45 | 84.42 | 24.4 | 151 | 97.63 | 20.097 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12714.23 | 7.82 | 11.96 | 24 | 98.96 | 15.136 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7358.61 | 13.54 | 21.31 | 35 | 98.48 | 15.283 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13359.15 | 22.41 | 10.09 | 52 | 97.85 | 17.007 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7418.3 | 40.38 | 16.49 | 88 | 96.22 | 17.628 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12277.81 | 81.38 | 26.26 | 154 | 94.41 | 20.272 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6628.82 | 150.74 | 47.49 | 271 | 87.92 | 20.942 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 16030.24 | 6.03 | 3.02 | 14 | 99.58 | 15.548 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15127 | 6.28 | 3.06 | 15 | 99.59 | 15.564 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16337.45 | 17.92 | 7.45 | 38 | 99.06 | 16.058 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15464.42 | 18.58 | 7.6 | 40 | 99.12 | 16.075 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15071.9 | 65.28 | 20.77 | 125 | 97.33 | 17.096 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14598.96 | 67.16 | 22.05 | 132 | 97.39 | 17.169 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14499.04 | 6.71 | 3.16 | 15 | 99.56 | 15.526 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11803.59 | 8.21 | 3.54 | 19 | 99.6 | 8467 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14560.28 | 20.19 | 8.05 | 43 | 99.03 | 17.353 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12083.83 | 24.17 | 8.97 | 50 | 99.09 | 18.702 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13353.06 | 74.37 | 23.03 | 141 | 97.22 | 22.153 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11379.56 | 86.53 | 26.65 | 163 | 97.32 | 18.898 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18311.73 | 5.42 | 3.02 | 13 | 99.59 | 15.788 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15912.94 | 6.24 | 3.2 | 15 | 99.63 | 15.903 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18570.88 | 16.1 | 6.95 | 35 | 99.12 | 16.359 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16105.56 | 18.57 | 7.56 | 39 | 99.24 | 16.401 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16771.36 | 59.55 | 18.63 | 112 | 97.58 | 19.891 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15164.84 | 65.84 | 19.94 | 122 | 97.78 | 19.826 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16164.65 | 5.99 | 3.02 | 14 | 99.63 | 15.64 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15152.21 | 6.3 | 2.99 | 15 | 99.65 | 15.639 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17542.1 | 16.68 | 7.12 | 36 | 99.2 | 17.759 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16432.74 | 17.44 | 7.27 | 38 | 99.26 | 17.593 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16999.74 | 58.17 | 19.25 | 113 | 97.71 | 19.329 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15738.89 | 62.09 | 21.51 | 126 | 97.9 | 21.071 |
