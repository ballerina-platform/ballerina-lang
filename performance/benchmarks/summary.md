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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 23914.69 | 4.14 | 2.8 | 11 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 22460.73 | 4.41 | 11.94 | 12 | 99.53 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 24631.83 | 12.12 | 6.08 | 28 | 99 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 22345.54 | 13.36 | 6.27 | 30 | 99.1 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 22125.82 | 45.11 | 15.15 | 88 | 97.28 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 21506.65 | 46.41 | 15.86 | 93 | 97.25 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15844.64 | 6.27 | 14.49 | 21 | 99.12 | 9483 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10733.48 | 9.27 | 68.17 | 29 | 98.35 | 11.356 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 17372.76 | 17.22 | 20.2 | 40 | 97.8 | 13.183 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 11403.5 | 25.88 | 54.26 | 57 | 95 | 18.805 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 16084.87 | 62.1 | 19.31 | 116 | 93.99 | 10.957 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9942.23 | 100.49 | 50 | 159 | 83.06 | 15.043 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 20219.47 | 4.9 | 9.07 | 12 | 99.34 | 48.129 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 16192.91 | 6.12 | 3.05 | 14 | 99.43 | 48.133 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 20844.17 | 14.33 | 6.53 | 32 | 98.71 | 114.34 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 16707.93 | 17.89 | 7.37 | 38 | 98.84 | 81.473 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 18614.58 | 53.62 | 18.33 | 108 | 96.2 | 150.152 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 15291.76 | 65.31 | 20.36 | 124 | 96.6 | 149.646 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 14072.79 | 7.06 | 15.34 | 22 | 99 | 47.404 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 8879.9 | 11.21 | 26.88 | 33 | 98.29 | 48.154 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 15169.94 | 19.72 | 19.67 | 47 | 97.71 | 80.609 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 9306.64 | 31.39 | 31.22 | 72 | 95.47 | 80.654 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13966.35 | 71.52 | 23.96 | 138 | 93.38 | 150.172 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7825.36 | 127.7 | 37.82 | 215 | 84.62 | 149.614 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 18202.05 | 5.22 | 2.84 | 12 | 99.57 | 15.75 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 17178.53 | 5.36 | 2.76 | 13 | 99.59 | 15.723 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 19022.6 | 14.99 | 6.92 | 33 | 99.01 | 16.221 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 17807.41 | 15.39 | 7.06 | 35 | 99.08 | 16.223 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 17581.33 | 55.62 | 20.27 | 116 | 97.1 | 17.299 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 16778.28 | 56.4 | 21.86 | 124 | 97.24 | 17.278 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 16910.36 | 5.65 | 2.93 | 13 | 99.29 | 47.761 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 15646.71 | 5.99 | 2.93 | 14 | 99.37 | 47.567 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 17049.35 | 16.94 | 7.41 | 37 | 98.74 | 50.009 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 16053.85 | 17.48 | 7.52 | 39 | 98.72 | 49.665 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 15981.14 | 61.32 | 21.48 | 125 | 96.33 | 52.85 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 14785.51 | 64.15 | 23.49 | 135 | 96.39 | 84.831 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 20523.51 | 4.82 | 2.83 | 12 | 99.57 | 16.22 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 17766.94 | 5.57 | 2.97 | 13 | 99.62 | 16.055 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 20830.81 | 14.33 | 6.55 | 31 | 99.09 | 16.745 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 18434 | 16.2 | 6.96 | 35 | 99.19 | 17.878 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 18797.78 | 53.12 | 17.46 | 103 | 97.5 | 19.47 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 17162.7 | 58.16 | 18.81 | 112 | 97.68 | 21.704 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 18679.15 | 5.08 | 2.95 | 13 | 99.61 | 15.913 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 17127.65 | 5.41 | 3.1 | 15 | 99.64 | 15.932 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 20145.06 | 14.17 | 6.76 | 32 | 99.17 | 18.463 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 18167.56 | 15.21 | 6.95 | 34 | 99.25 | 17.469 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 19023.99 | 50.83 | 18.53 | 103 | 97.78 | 18.28 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 17908.6 | 52.86 | 21.02 | 123 | 97.9 | 21.55 |
