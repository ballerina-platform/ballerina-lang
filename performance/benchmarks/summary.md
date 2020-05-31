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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22145.25 | 4.47 | 10.05 | 12 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20465.19 | 4.84 | 3.14 | 13 | 99.59 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22695.74 | 13.16 | 6.42 | 30 | 99.05 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 21269.2 | 14.04 | 16.01 | 32 | 99.1 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 20414.78 | 48.91 | 16.05 | 95 | 97.41 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 20043.99 | 49.8 | 16.36 | 97 | 97.38 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14621.47 | 6.8 | 11.17 | 22 | 98.99 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10375.67 | 9.46 | 34.34 | 29 | 98.33 | 18.264 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15956.55 | 18.05 | 18.56 | 42 | 97.81 | 23.493 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 9958.19 | 30.07 | 58.42 | 63 | 95.62 | 23.521 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14987.31 | 66.66 | 21.64 | 127 | 94.06 | 16.468 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 8833.27 | 113.12 | 59.2 | 170 | 85.88 | 23.966 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17653.64 | 5.61 | 3 | 13 | 99.56 | 15.305 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12570.43 | 7.9 | 3.54 | 18 | 99.63 | 15.325 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18190.49 | 16.43 | 7.1 | 35 | 99.05 | 16.253 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12994.78 | 23.02 | 8.64 | 47 | 99.18 | 16.335 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 16260.79 | 61.38 | 19.48 | 117 | 97.4 | 19.945 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12337.57 | 80.95 | 23.82 | 147 | 97.62 | 19.567 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12563.55 | 7.91 | 12.04 | 24 | 99.04 | 15.466 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7205.35 | 13.55 | 32.66 | 35 | 98.64 | 15.565 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13662.4 | 21.9 | 9.98 | 51 | 97.99 | 16.825 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7564.03 | 39.59 | 21.43 | 86 | 96.63 | 16.582 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12492.75 | 79.97 | 25.64 | 150 | 94.69 | 18.748 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6903.05 | 144.76 | 47.36 | 261 | 88.87 | 21.721 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 16334.55 | 5.88 | 2.97 | 13 | 99.59 | 15.688 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15476.79 | 6.09 | 2.94 | 14 | 99.61 | 15.704 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 17251.28 | 16.74 | 7.17 | 36 | 99.08 | 16.451 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 16310.07 | 17.2 | 7.35 | 38 | 99.12 | 16.434 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15568.26 | 62.87 | 21.14 | 124 | 97.3 | 17.241 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 15219.44 | 63.59 | 22.29 | 130 | 97.42 | 17.307 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14853.54 | 6.49 | 3.14 | 15 | 99.56 | 15.604 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12139.68 | 7.93 | 3.48 | 19 | 99.6 | 15.607 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 15579.76 | 18.66 | 7.68 | 40 | 99.03 | 18.132 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12644.07 | 22.88 | 8.62 | 48 | 99.1 | 17.02 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 14204.94 | 69.52 | 22.91 | 136 | 97.21 | 19.516 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11524.69 | 84.65 | 26.32 | 159 | 97.34 | 19.881 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18565.51 | 5.34 | 2.99 | 13 | 99.6 | 16.021 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15806.66 | 6.27 | 3.17 | 14 | 99.66 | 15.932 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19333.98 | 15.45 | 6.85 | 34 | 99.13 | 16.879 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17067.74 | 17.51 | 7.3 | 37 | 99.22 | 16.523 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17496.35 | 57.07 | 18.75 | 111 | 97.62 | 21.22 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15856.58 | 62.98 | 19.4 | 118 | 97.8 | 21.259 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16349.19 | 5.89 | 3.03 | 14 | 99.64 | 15.792 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15298.34 | 6.2 | 2.95 | 14 | 99.67 | 15.792 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 18287.6 | 15.84 | 6.99 | 34 | 99.23 | 17.268 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16796.78 | 16.84 | 7.17 | 36 | 99.3 | 17.291 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17700.41 | 55.53 | 20.35 | 112 | 97.71 | 18.565 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16315.51 | 59.49 | 20.67 | 124 | 97.98 | 20.022 |
