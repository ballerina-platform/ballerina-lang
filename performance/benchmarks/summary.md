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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 19551.06 | 5.08 | 6.8 | 40 | 99.32 | 17.932 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 18266.21 | 5.43 | 6.89 | 42 | 99.3 | 18.296 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21009.27 | 14.23 | 11.91 | 71 | 98.71 | 21.28 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 19273.65 | 15.51 | 12.19 | 72 | 98.69 | 19.311 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19593.92 | 50.97 | 26.11 | 154 | 96.44 | 17.535 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18602.3 | 53.69 | 26.91 | 157 | 96.68 | 17.71 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 13379.95 | 7.43 | 8.04 | 39 | 99.18 | 24.48 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9807.53 | 10.15 | 9.97 | 56 | 99.21 | 24.376 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 14767.97 | 20.26 | 12.69 | 73 | 97.98 | 24.443 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10512.62 | 28.48 | 16.4 | 94 | 97.96 | 24.497 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 13732.49 | 72.73 | 22.87 | 151 | 94.09 | 25.217 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10043.84 | 99.46 | 29.51 | 192 | 93.38 | 25.184 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17094.23 | 5.81 | 6.39 | 30 | 99.45 | 23.688 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 11890.07 | 8.36 | 6.19 | 28 | 99.54 | 23.655 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17211.22 | 17.37 | 12.73 | 71 | 98.85 | 23.963 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12177.19 | 24.57 | 11.45 | 60 | 99.05 | 24.27 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15508.13 | 64.41 | 29.08 | 162 | 96.87 | 25.175 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11540.54 | 86.57 | 28.75 | 170 | 97.33 | 25.178 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 11451.14 | 8.69 | 7.72 | 45 | 99.2 | 23.657 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7138.12 | 13.96 | 8.89 | 50 | 99.26 | 23.649 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 12596.14 | 23.76 | 15.49 | 89 | 98.07 | 23.952 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7318.24 | 40.93 | 20.81 | 116 | 98.22 | 24.199 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 11827.86 | 84.48 | 34.53 | 208 | 94.48 | 25.248 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6959.06 | 143.61 | 53.62 | 305 | 94.66 | 25.286 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 16156.52 | 6.04 | 8.36 | 45 | 99.47 | 24.616 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15604.33 | 6.15 | 7.15 | 35 | 99.44 | 24.625 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16468.83 | 17.88 | 14.04 | 78 | 98.86 | 24.831 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15810.44 | 18.19 | 13.39 | 72 | 98.76 | 25.072 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15332.36 | 64.45 | 29.44 | 166 | 96.51 | 25.738 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14665.88 | 65.97 | 30.56 | 171 | 96.9 | 25.742 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14321.75 | 6.84 | 6.27 | 29 | 99.38 | 24.638 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11731.18 | 8.34 | 6.1 | 29 | 99.53 | 24.632 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14767.84 | 20 | 12.22 | 66 | 98.86 | 24.857 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11813.71 | 24.85 | 11.84 | 63 | 98.96 | 25.034 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13623.51 | 72.79 | 30.49 | 171 | 96.71 | 25.758 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11227.17 | 87.72 | 30.2 | 177 | 97.01 | 25.736 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 17899.31 | 5.54 | 8.28 | 46 | 99.48 | 24.47 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15896.56 | 6.24 | 6.9 | 31 | 99.54 | 24.526 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18365.9 | 16.28 | 13.96 | 78 | 98.91 | 24.677 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15981.57 | 18.71 | 12.85 | 71 | 99.07 | 24.67 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16522.52 | 60.44 | 29.01 | 162 | 97.07 | 25.624 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15252.08 | 65.49 | 28.68 | 161 | 97.31 | 25.738 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16086.72 | 6.05 | 7.55 | 42 | 99.54 | 24.646 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15583.4 | 6.15 | 7.62 | 42 | 99.58 | 24.647 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17206.23 | 17.02 | 13.62 | 75 | 99.09 | 24.903 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16283.55 | 17.58 | 13.54 | 73 | 99.14 | 25.06 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16804.94 | 58.5 | 29.14 | 158 | 97.29 | 25.746 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15878.26 | 61.45 | 34.3 | 168 | 97.48 | 25.756 |
