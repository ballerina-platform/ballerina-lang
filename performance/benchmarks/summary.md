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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 17618.77 | 5.63 | 6.49 | 29 | 99.56 | 13.978 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 16429.89 | 6.04 | 6.56 | 29 | 99.6 | 13.953 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 18125.33 | 16.5 | 11.98 | 54 | 99.16 | 14.701 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 16785.04 | 17.81 | 12.24 | 57 | 99.21 | 14.933 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 15792.48 | 63.25 | 26.19 | 142 | 97.89 | 15.506 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 15331.92 | 65.14 | 26.25 | 144 | 97.92 | 15.42 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14316.29 | 6.94 | 6.26 | 29 | 98.84 | 14.808 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9240.11 | 10.78 | 7.69 | 35 | 99.04 | 14.837 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15468.85 | 19.34 | 14.14 | 64 | 97.19 | 14.87 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10125.4 | 29.57 | 14.2 | 72 | 97.96 | 14.814 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 13698.8 | 72.92 | 32.15 | 163 | 94.45 | 15.785 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10070.12 | 99.22 | 31.95 | 188 | 93.94 | 15.798 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 13880.36 | 7.16 | 7.2 | 33 | 99.58 | 14.417 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 9982.87 | 9.97 | 7.86 | 36 | 99.65 | 14.42 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 14102.68 | 21.21 | 13 | 62 | 99.2 | 14.623 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 10003.11 | 29.92 | 13.78 | 71 | 99.28 | 14.874 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 12322.86 | 81.06 | 28.59 | 165 | 98.01 | 15.879 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 9395.01 | 106.33 | 31.74 | 197 | 98.13 | 15.902 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 11380.81 | 8.74 | 8.19 | 36 | 99.02 | 14.175 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6714.3 | 14.84 | 10.86 | 49 | 99.26 | 14.176 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 11643.15 | 25.71 | 16.34 | 77 | 98.2 | 14.444 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6867.65 | 43.62 | 20.96 | 108 | 98.39 | 14.457 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 10582.26 | 94.4 | 46.58 | 199 | 95.6 | 15.765 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6557.47 | 152.39 | 51.68 | 295 | 95.46 | 15.889 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 12305.67 | 7.98 | 7.51 | 34 | 99.64 | 14.992 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 11997.7 | 8.12 | 7.41 | 34 | 99.64 | 15.057 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 12580.47 | 23.53 | 13.92 | 67 | 99.09 | 15.494 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 12214.57 | 23.88 | 14.35 | 68 | 99.23 | 15.453 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 11629.94 | 85.4 | 33.17 | 184 | 98.03 | 16.209 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 11359.59 | 87.38 | 36.12 | 182 | 97.9 | 16.168 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 11267.26 | 8.73 | 7.78 | 35 | 99.61 | 15.021 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 9482.8 | 10.36 | 7.96 | 37 | 99.65 | 15.016 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 11385.22 | 26.05 | 14.14 | 69 | 99.23 | 15.468 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 9508.8 | 31.06 | 14.82 | 76 | 99.25 | 15.467 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 10428.77 | 95.58 | 32.89 | 194 | 97.91 | 9205 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 8850.13 | 112.47 | 41.39 | 219 | 97.99 | 9199 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 14275.01 | 6.96 | 7.02 | 32 | 99.63 | 14.893 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 12298.17 | 8.08 | 7.44 | 34 | 99.68 | 15.022 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 14692.28 | 20.36 | 12.94 | 61 | 99.26 | 15.088 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 12656.89 | 23.64 | 13.61 | 66 | 99.32 | 15.154 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 12951 | 77.13 | 27.58 | 156 | 98.18 | 16.077 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 12080.45 | 82.69 | 28.5 | 166 | 98.36 | 16.131 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 12557.83 | 7.81 | 7.48 | 34 | 99.64 | 15.079 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 11754.72 | 8.29 | 7.6 | 35 | 99.68 | 15.041 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 13105.1 | 22.56 | 13.36 | 64 | 99.37 | 15.482 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 12441.58 | 23.51 | 13.81 | 66 | 99.41 | 15.474 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 12373.39 | 80.5 | 28.65 | 165 | 98.43 | 16.235 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 12022.61 | 82.62 | 29.97 | 172 | 98.29 | 16.18 |
