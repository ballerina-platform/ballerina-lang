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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22988.46 | 4.31 | 8.55 | 11 | 99.56 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 21313.03 | 4.65 | 8.91 | 13 | 99.57 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 24434.47 | 12.23 | 9.92 | 28 | 98.99 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 22009.75 | 13.57 | 6.42 | 30 | 99.07 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 22215.49 | 44.95 | 15.39 | 90 | 97.13 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 21007.77 | 47.52 | 15.49 | 92 | 97.3 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 16059.33 | 6.19 | 17.29 | 20 | 98.9 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10829.84 | 8.93 | 42.24 | 28 | 98.17 | 16.091 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 17583.49 | 17.01 | 15.43 | 41 | 97.67 | 22.997 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10176.91 | 28.6 | 85.18 | 61 | 95.01 | 24.362 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 15942.72 | 62.66 | 20.64 | 121 | 93.52 | 13.683 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9050.84 | 110.41 | 87.39 | 168 | 83.7 | 28.907 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 18557.74 | 5.34 | 13.05 | 13 | 99.55 | 15.128 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 13093.11 | 7.59 | 3.51 | 18 | 99.59 | 15.166 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 19690.27 | 15.17 | 6.67 | 33 | 98.97 | 16.16 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 13689.82 | 21.84 | 8.29 | 45 | 99.15 | 16.591 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 17451.55 | 57.2 | 19.12 | 111 | 97.21 | 19.948 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12874.8 | 77.59 | 23.5 | 143 | 97.52 | 21.73 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 13718.53 | 7.25 | 18.77 | 22 | 98.91 | 15.145 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7670.74 | 12.99 | 28.75 | 34 | 98.5 | 15.251 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14817.89 | 20.19 | 13.65 | 47 | 97.88 | 16.391 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7873.92 | 38.04 | 15.7 | 84 | 96.18 | 16.768 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13365.93 | 74.74 | 24.76 | 144 | 94.19 | 22.762 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6972.5 | 143.32 | 47.58 | 259 | 87.62 | 20.881 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 16609.83 | 5.78 | 2.92 | 13 | 99.59 | 15.535 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15759.54 | 5.98 | 18.59 | 14 | 99.57 | 15.545 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 17273.47 | 16.81 | 7.08 | 36 | 99.09 | 16.27 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 16816.51 | 16.71 | 7.11 | 36 | 99.1 | 16.021 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 16055.34 | 61.17 | 20.34 | 122 | 97.31 | 17.078 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 15815.55 | 60.34 | 21.61 | 126 | 97.32 | 17.133 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 15027.73 | 6.44 | 3.05 | 15 | 99.57 | 15.479 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12434.54 | 7.75 | 3.35 | 18 | 99.6 | 15.414 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 15615.79 | 18.72 | 7.58 | 39 | 99.04 | 16.805 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 13041.07 | 22.2 | 8.41 | 47 | 99.08 | 18.297 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 14259.44 | 68.99 | 22.13 | 133 | 97.1 | 20.056 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 12176.84 | 81.07 | 25.25 | 154 | 97.27 | 19.348 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 19384.63 | 5.11 | 2.84 | 12 | 99.59 | 15.771 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16796.94 | 5.9 | 3.02 | 14 | 99.63 | 15.786 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19868.44 | 15.04 | 6.55 | 32 | 99.11 | 16.367 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17501.84 | 17.07 | 7.11 | 36 | 99.21 | 16.73 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 18014.1 | 55.44 | 17.99 | 107 | 97.5 | 21.219 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16391.51 | 60.88 | 18.71 | 113 | 97.76 | 20.977 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16251.08 | 5.95 | 3.03 | 14 | 99.65 | 15.611 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15630.71 | 6.07 | 2.94 | 14 | 99.66 | 15.621 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17791.03 | 16.37 | 6.98 | 35 | 99.25 | 16.802 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16969.79 | 16.67 | 7.12 | 37 | 99.28 | 16.754 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17507.34 | 55.86 | 19.43 | 112 | 97.83 | 19.333 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16394.07 | 59.48 | 21.56 | 122 | 97.88 | 20.531 |
