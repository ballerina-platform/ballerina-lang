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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22300.31 | 4.44 | 2.99 | 12 | 99.54 | 10.483 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20467.62 | 4.84 | 3.14 | 13 | 99.57 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22736.71 | 13.14 | 6.4 | 30 | 98.57 | 17.829 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20537.05 | 14.55 | 6.77 | 33 | 98.84 | 17.525 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 20306.38 | 49.18 | 16.38 | 96 | 97.19 | 13.448 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19020.05 | 52.48 | 16.46 | 100 | 97.46 | 9565 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14659.6 | 6.78 | 4.62 | 21 | 99.01 | 15.394 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10389.62 | 9.58 | 5.86 | 28 | 99.14 | 15.322 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16338.25 | 18.31 | 7.87 | 41 | 97.93 | 15.617 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10802.62 | 27.71 | 8.85 | 55 | 98.04 | 15.312 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14544.66 | 68.68 | 19.56 | 123 | 94.65 | 16.449 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10404.55 | 96.01 | 18.08 | 146 | 93.93 | 16.657 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17733.15 | 5.59 | 2.97 | 13 | 99.53 | 15.081 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12452.13 | 7.98 | 3.54 | 18 | 99.61 | 15.045 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18226.69 | 16.4 | 6.99 | 35 | 99.03 | 16.948 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12595.39 | 23.75 | 8.72 | 48 | 99.19 | 16.487 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 16038.88 | 62.28 | 19.06 | 116 | 97.35 | 22.083 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11951.17 | 83.58 | 24.22 | 150 | 97.67 | 19.021 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 11658.36 | 8.53 | 5.34 | 25 | 99.14 | 14.077 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7554.1 | 13.19 | 6.74 | 34 | 99.23 | 14.104 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14035.46 | 21.32 | 8.93 | 47 | 97.93 | 14.362 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7271.79 | 41.19 | 16.15 | 88 | 98.38 | 14.64 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12618.86 | 79.16 | 24.23 | 146 | 94.84 | 15.625 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7295.5 | 136.96 | 39.16 | 242 | 94.95 | 15.782 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15901.71 | 6.07 | 3.07 | 14 | 99.58 | 15.579 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15323.72 | 6.18 | 3 | 14 | 99.59 | 15.592 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16563.43 | 17.59 | 7.44 | 38 | 99.08 | 16.112 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15829.73 | 17.96 | 7.45 | 39 | 99.11 | 16.114 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15004.48 | 65.79 | 21.46 | 128 | 97.44 | 17.029 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14622.69 | 66.85 | 22.32 | 134 | 97.45 | 17.014 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14607.42 | 6.65 | 3.14 | 15 | 99.56 | 15.662 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11850.71 | 8.16 | 3.51 | 19 | 99.6 | 16.104 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14750.05 | 19.88 | 7.93 | 41 | 99.06 | 17.03 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12222.97 | 23.82 | 8.8 | 49 | 99.11 | 16.489 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13591.97 | 72.88 | 22.93 | 139 | 97.29 | 23.586 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11481.47 | 86.17 | 26.08 | 161 | 97.38 | 20.27 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18786.62 | 5.28 | 2.96 | 13 | 99.5 | 15.702 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16317.46 | 6.08 | 3.09 | 14 | 99.63 | 15.837 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19384.76 | 15.41 | 6.83 | 33 | 99.11 | 16.66 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16800.55 | 17.79 | 7.44 | 38 | 99.22 | 16.189 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16897.49 | 59.1 | 18.48 | 111 | 97.65 | 18.872 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15846.29 | 63.01 | 19.14 | 117 | 97.8 | 17.932 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15975.9 | 6.05 | 3.11 | 14 | 99.63 | 15.983 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14871.26 | 6.41 | 3.14 | 15 | 99.66 | 16.551 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16630.97 | 17.59 | 7.35 | 37 | 99.28 | 18.616 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15775.04 | 18.16 | 7.49 | 39 | 99.32 | 16.861 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16208.87 | 61.02 | 20.32 | 120 | 97.93 | 20.333 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15452.4 | 63.21 | 22.86 | 128 | 97.93 | 20.333 |
