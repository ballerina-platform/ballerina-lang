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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 23455.72 | 4.22 | 2.88 | 11 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 21123.97 | 4.69 | 2.97 | 12 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 23845.44 | 12.52 | 10.06 | 28 | 99.02 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 21776.02 | 13.71 | 6.44 | 30 | 99.11 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 22259.35 | 44.83 | 15.33 | 89 | 97.19 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 21093.19 | 47.33 | 15.42 | 92 | 97.35 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15773.26 | 6.3 | 14.49 | 20 | 98.9 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10428.91 | 9.25 | 41.33 | 29 | 98.11 |  |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 17079.56 | 16.85 | 15.25 | 39 | 97.65 | 18.469 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 11392.06 | 26.28 | 48.41 | 59 | 94.73 | 29.959 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 16018.7 | 62.36 | 20.17 | 119 | 93.78 | 14.657 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9107.06 | 109.72 | 48.33 | 167 | 83.83 | 33.663 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 18874.63 | 5.25 | 2.87 | 12 | 99.55 | 15.153 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 13166.15 | 7.54 | 3.45 | 18 | 99.62 | 15.119 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 19526.61 | 15.3 | 6.59 | 33 | 99.01 | 15.815 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 13772.52 | 21.71 | 8.13 | 44 | 99.16 | 16.088 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 17512.28 | 57.01 | 18.08 | 109 | 97.28 | 20.39 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12948.26 | 77.15 | 22.79 | 141 | 97.57 | 20.355 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 13626.31 | 7.3 | 15.59 | 22 | 98.97 | 15.273 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7830.38 | 12.72 | 20.63 | 33 | 98.46 | 15.211 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 15002.69 | 19.94 | 13.65 | 47 | 97.86 | 16.245 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 8002.22 | 37.42 | 15.56 | 83 | 96.11 | 17.25 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13730.66 | 72.73 | 23.66 | 139 | 94.17 | 21.995 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7028.32 | 142.18 | 45.79 | 259 | 87.68 | 20.647 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 17010.19 | 5.64 | 2.89 | 13 | 99.59 | 15.518 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 16224.09 | 5.78 | 2.8 | 13 | 99.58 | 15.5 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 17918.39 | 16.14 | 6.95 | 34 | 99.06 | 16.018 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 17027.2 | 16.45 | 7.04 | 36 | 99.1 | 16.271 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 16547.1 | 59.58 | 19.88 | 118 | 97.32 | 17.055 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 16250.86 | 59.1 | 21.28 | 124 | 97.3 | 17.06 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 15666.86 | 6.14 | 2.97 | 14 | 99.57 | 8461 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12661.5 | 7.59 | 3.3 | 17 | 99.6 | 15.442 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 16361.5 | 17.77 | 7.28 | 37 | 99.01 | 17.462 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 13431.02 | 21.51 | 8.17 | 45 | 99.07 | 18.281 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 14724.11 | 66.92 | 21.67 | 130 | 97.15 | 19.677 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 12578.77 | 78.4 | 24.92 | 151 | 97.22 | 20.117 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 19635.25 | 5.04 | 2.88 | 12 | 99.58 | 15.804 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 17173 | 5.77 | 3 | 13 | 99.64 | 15.763 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 20366.16 | 14.67 | 6.51 | 31 | 99.11 | 16.309 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17933.76 | 16.66 | 6.95 | 35 | 99.21 | 16.547 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 18589.66 | 53.72 | 17.02 | 102 | 97.53 | 18.863 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 17185.35 | 58.08 | 18.55 | 111 | 97.69 | 20.519 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16699.79 | 5.76 | 3.01 | 14 | 99.64 | 15.594 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15896.28 | 5.95 | 2.84 | 13 | 99.66 | 15.609 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17899.77 | 16.22 | 6.97 | 35 | 99.26 | 18.148 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17063.66 | 16.57 | 7.09 | 36 | 99.3 | 17.202 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17770.58 | 55.34 | 19.6 | 112 | 97.82 | 21.355 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16840.1 | 56.57 | 20.55 | 121 | 97.91 | 19.586 |
