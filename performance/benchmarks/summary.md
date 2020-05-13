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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21416.17 | 4.63 | 3.01 | 12 | 99.56 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19707.98 | 5.03 | 9.29 | 14 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21534.67 | 13.87 | 6.53 | 31 | 99.06 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 19899.2 | 15.01 | 6.74 | 33 | 99.13 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19396.35 | 51.48 | 16.7 | 99 | 97.38 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18770.97 | 53.18 | 16.71 | 101 | 97.39 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14442.62 | 6.88 | 18.29 | 22 | 98.95 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9778.57 | 9.86 | 57.55 | 31 | 98.07 |  |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16176.06 | 18.5 | 16.19 | 44 | 97.25 | 20.1 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 8958.26 | 31.88 | 89.02 | 65 | 95 | 32.022 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14509.83 | 68.85 | 24.85 | 132 | 94.02 | 13.391 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 8809.66 | 113.43 | 64.55 | 173 | 83.05 | 21.92 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17077.09 | 5.81 | 3.06 | 14 | 99.45 | 15.383 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 11982.13 | 8.29 | 3.65 | 19 | 99.63 | 15.36 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17649.98 | 16.94 | 7.1 | 36 | 99.03 | 16.271 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12299.29 | 24.33 | 8.92 | 49 | 99.2 | 16.309 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15636.46 | 63.84 | 20.55 | 122 | 97.35 | 21.616 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11666.41 | 85.63 | 24.57 | 153 | 97.67 | 20.079 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12463.03 | 7.98 | 22.53 | 24 | 98.98 | 15.487 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7185.69 | 13.87 | 29.75 | 36 | 98.46 | 15.581 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13479.12 | 22.2 | 14.56 | 51 | 97.95 | 16.519 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6998.34 | 42.8 | 26.97 | 93 | 96.33 | 17.815 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12043.23 | 82.96 | 27.11 | 158 | 94.31 | 20.872 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6418.02 | 155.71 | 47.7 | 275 | 87.74 | 20.533 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15248.77 | 6.35 | 3.18 | 15 | 99.59 | 15.723 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14665.7 | 6.51 | 3.11 | 15 | 99.61 | 15.702 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16019.49 | 18.28 | 7.5 | 39 | 99.1 | 16.189 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15209.76 | 18.87 | 7.79 | 41 | 99.14 | 16.197 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14487.99 | 67.87 | 21.72 | 132 | 97.44 | 17.183 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14310.34 | 68.53 | 22.51 | 136 | 97.43 | 17.346 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13719.19 | 7.1 | 3.3 | 16 | 99.58 | 15.757 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11575.98 | 8.38 | 3.57 | 19 | 99.51 | 15.622 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14562.63 | 20.18 | 8.25 | 43 | 99.04 | 17.091 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11896.52 | 24.55 | 9.31 | 51 | 99.11 | 17.443 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13234.17 | 75.09 | 23.79 | 143 | 97.28 | 20.089 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11106.55 | 88.97 | 26.59 | 165 | 97.4 | 19.481 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18088.83 | 5.48 | 3.04 | 13 | 99.48 | 15.964 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15705.41 | 6.32 | 3.17 | 15 | 99.57 | 16.025 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18819.04 | 15.88 | 11.32 | 34 | 99.1 | 17.628 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16263.53 | 18.38 | 12.24 | 39 | 99.22 | 16.758 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15895.1 | 62.84 | 19.49 | 118 | 97.73 | 18.546 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15118.33 | 66.04 | 19.85 | 122 | 97.81 | 20.736 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 14871.48 | 6.53 | 3.28 | 15 | 99.65 | 15.808 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14443.86 | 6.62 | 3.23 | 16 | 99.66 | 15.804 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16109.71 | 18.19 | 7.59 | 39 | 99.29 | 17.426 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15208.27 | 18.98 | 7.68 | 41 | 99.32 | 18.182 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15529.98 | 63.89 | 21.59 | 121 | 97.96 | 19.598 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14748.46 | 66.73 | 22.68 | 132 | 98.01 | 20.665 |
