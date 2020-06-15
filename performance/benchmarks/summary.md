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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 20547.24 | 4.64 | 8.89 | 12 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20518.39 | 4.83 | 3.03 | 13 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22189.27 | 13.47 | 6.36 | 30 | 98.97 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 21142.46 | 14.13 | 6.59 | 32 | 99 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 20430.9 | 48.87 | 16 | 95 | 97.05 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 20147.04 | 49.57 | 16.46 | 97 | 97.06 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14269.91 | 6.97 | 15.28 | 22 | 98.85 | 8525 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 8744.69 | 11.38 | 98.19 | 32 | 98.17 | 11.562 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15624.81 | 19.14 | 21.44 | 45 | 97.49 | 9036 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 8730.88 | 32.71 | 78.42 | 65 | 95.34 | 10133 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 12629.09 | 79.12 | 30.61 | 132 | 93.73 | 10.944 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 8581.06 | 116.46 | 47.51 | 172 | 83.56 | 15.292 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17636.76 | 5.62 | 2.99 | 13 | 99.53 | 15.418 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12454.98 | 7.98 | 11.55 | 19 | 99.6 | 15.356 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18275.71 | 16.36 | 7.09 | 35 | 98.94 | 16.778 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12998.35 | 23.01 | 8.63 | 47 | 99.11 | 16.307 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 16472.39 | 60.62 | 19.98 | 118 | 97.03 | 20.285 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12165.17 | 82.11 | 24.04 | 148 | 97.43 | 19.779 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 11126.89 | 8.94 | 26.55 | 27 | 99.08 | 15.312 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7056.46 | 14.12 | 16.27 | 37 | 98.49 | 15.378 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 11765.52 | 25.44 | 15.97 | 59 | 98.08 | 16.868 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6373.75 | 44.83 | 37.8 | 96 | 96.44 | 16.966 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 10941.68 | 91.32 | 29.77 | 166 | 94.52 | 20.053 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6420.56 | 155.66 | 47.73 | 269 | 87.47 | 20.583 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 16021.5 | 6.03 | 3.04 | 14 | 99.56 | 15.741 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15245.77 | 6.22 | 3.01 | 15 | 99.58 | 15.729 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16376.17 | 17.83 | 7.51 | 38 | 99.01 | 16.244 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15515.5 | 18.43 | 7.65 | 40 | 99.06 | 16.413 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15301.47 | 64.69 | 21.13 | 127 | 97.11 | 17.247 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14560.32 | 66.69 | 22.48 | 134 | 97.18 | 17.271 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14682.41 | 6.61 | 3.12 | 15 | 99.54 | 15.475 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12101.29 | 7.99 | 3.43 | 18 | 99.58 | 15.47 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 15112.66 | 19.4 | 7.88 | 41 | 98.96 | 17.475 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12274.41 | 23.77 | 8.92 | 50 | 99.03 | 18.366 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13844.27 | 71.67 | 22.84 | 137 | 96.89 | 20.435 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11447.4 | 86.59 | 26.18 | 161 | 97.14 | 21.539 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18300.42 | 5.42 | 3.02 | 13 | 99.57 | 16.064 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15830.92 | 6.27 | 3.14 | 15 | 99.62 | 15.963 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18352.25 | 16.28 | 7.07 | 35 | 99.05 | 16.575 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16271.03 | 18.37 | 7.6 | 39 | 99.16 | 16.535 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16691.68 | 59.84 | 19.04 | 114 | 97.33 | 21.128 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15441.17 | 64.68 | 20.03 | 121 | 97.58 | 19.437 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16213.08 | 5.97 | 3.09 | 14 | 99.61 | 15.831 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15390.44 | 6.19 | 3.04 | 15 | 99.64 | 15.826 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17895.98 | 16.3 | 7.01 | 35 | 99.17 | 17.921 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16689.72 | 17.12 | 7.25 | 37 | 99.22 | 17.823 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17492.16 | 56.41 | 19.48 | 111 | 97.51 | 18.134 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16262.93 | 59.67 | 21.02 | 122 | 97.76 | 18.235 |
