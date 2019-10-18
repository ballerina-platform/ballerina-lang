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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22178.16 | 4.47 | 2.88 | 12 | 99.57 | 14.091 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20204.31 | 4.9 | 3 | 13 | 99.57 | 14.558 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22360.66 | 13.36 | 6.31 | 30 | 99.08 | 15.055 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20256.72 | 14.75 | 6.77 | 33 | 99.09 | 15.062 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19334.71 | 51.65 | 17.04 | 101 | 97.47 | 15.599 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18132.48 | 52.81 | 190.26 | 98 | 97.71 | 15.552 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14960.74 | 6.64 | 4.41 | 20 | 99.08 | 15.059 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10419.65 | 9.55 | 6 | 28 | 99.21 | 15.012 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16684.02 | 17.93 | 8.03 | 41 | 98.01 | 15.072 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 11190.05 | 26.75 | 9.53 | 56 | 98.15 | 15.3 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 13546.27 | 73.76 | 18.12 | 125 | 95.05 | 15.796 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9713.33 | 102.86 | 17.57 | 153 | 94.55 | 15.64 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17662.3 | 5.61 | 2.88 | 13 | 99.55 | 14.464 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12438.66 | 7.99 | 3.5 | 18 | 99.63 | 14.467 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18255.28 | 16.37 | 6.96 | 35 | 99.06 | 15.042 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12520.38 | 23.89 | 8.75 | 48 | 99.22 | 15.01 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15761.81 | 63.35 | 19 | 117 | 97.56 | 15.928 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11882.95 | 84.06 | 23.74 | 150 | 97.77 | 15.902 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 13565.45 | 7.32 | 4.71 | 22 | 99.02 | 14.249 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7536.19 | 13.22 | 6.71 | 34 | 99.28 | 14.281 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14059.69 | 21.28 | 9.05 | 47 | 98.14 | 14.531 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7366 | 40.66 | 15.81 | 86 | 98.37 | 14.551 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12543.17 | 79.62 | 23.53 | 144 | 94.93 | 15.823 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7350.71 | 135.93 | 39.1 | 241 | 95.06 | 15.786 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15526.53 | 6.22 | 3.06 | 14 | 99.6 | 15.164 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 14992.55 | 6.33 | 2.93 | 14 | 99.61 | 15.178 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16059.73 | 18.23 | 7.45 | 38 | 99.15 | 15.62 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15333.73 | 18.61 | 7.58 | 40 | 99.18 | 15.621 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14605.52 | 67.85 | 21.67 | 131 | 97.58 | 16.31 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14309.54 | 67.61 | 22.64 | 135 | 97.58 | 16.348 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13919.9 | 6.98 | 3.2 | 16 | 99.59 | 15.148 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11594.59 | 8.35 | 3.52 | 19 | 99.62 | 15.156 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14299.5 | 20.53 | 8.05 | 43 | 99.1 | 15.604 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11684.91 | 24.97 | 9.13 | 51 | 99.15 | 15.625 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13208.32 | 74.73 | 23.3 | 141 | 97.44 | 16.31 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11087.03 | 89.34 | 25.89 | 163 | 97.51 | 9449 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18281.78 | 5.42 | 2.94 | 13 | 99.6 | 15.185 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0.5 | 631.78 | 150.81 | 2116.72 | 47 | 99.88 | 15.185 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18592.77 | 16.07 | 6.94 | 34 | 99.17 | 15.402 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15907.98 | 18.8 | 7.55 | 39 | 99.28 | 15.354 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16240.73 | 61.5 | 18.83 | 113 | 97.83 | 16.366 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15312.54 | 65.22 | 19.82 | 122 | 97.94 | 16.29 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15642.34 | 6.18 | 3.1 | 14 | 99.65 | 15.177 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14683.58 | 6.49 | 3.07 | 15 | 99.67 | 14.962 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 15931.15 | 18.41 | 7.51 | 39 | 99.32 | 15.621 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15271.01 | 18.8 | 7.65 | 40 | 99.31 | 15.599 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15791.98 | 62.75 | 19.79 | 118 | 98.03 | 16.31 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14969.86 | 65.07 | 21.81 | 131 | 97.97 | 16.435 |
