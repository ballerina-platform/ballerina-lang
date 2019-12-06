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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 23255.53 | 4.26 | 2.68 | 11 | 99.56 | 14.072 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 21242.1 | 4.66 | 2.85 | 12 | 99.59 | 14.565 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 23585.23 | 12.67 | 5.97 | 28 | 99.05 | 15.059 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 21993.94 | 13.58 | 6.15 | 30 | 99.13 | 15.081 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 21375.78 | 46.71 | 15.39 | 92 | 97.4 | 15.604 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 20646.85 | 48.37 | 15.73 | 94 | 97.43 | 15.59 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15835.61 | 6.28 | 4.13 | 19 | 99.06 | 15.043 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 11083.58 | 8.98 | 5.79 | 27 | 99.17 | 14.998 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 17724.59 | 16.87 | 7.53 | 38 | 97.91 | 15.031 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10796.81 | 27.73 | 8.91 | 55 | 98.15 | 14.999 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14261.51 | 70.05 | 16.91 | 118 | 95 | 16.029 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10312.66 | 96.88 | 17.39 | 146 | 94.53 | 15.726 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 18950.26 | 5.23 | 2.67 | 12 | 99.56 | 14.462 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 13199.32 | 7.53 | 3.33 | 17 | 99.63 | 14.441 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 19299.01 | 15.49 | 6.54 | 33 | 99.05 | 14.855 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 13133.1 | 22.78 | 8.46 | 47 | 99.19 | 15.013 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 16728.62 | 59.71 | 18.24 | 111 | 97.46 | 16.141 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12627.18 | 79.11 | 23.08 | 143 | 97.65 | 15.947 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 14117.36 | 7.04 | 4.49 | 21 | 99.03 | 14.282 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 8036.3 | 12.4 | 6.33 | 32 | 99.25 | 14.27 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13715.53 | 21.82 | 9.22 | 49 | 98.2 | 14.786 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 8216.96 | 36.45 | 14.39 | 78 | 98.34 | 14.521 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13307.38 | 75.06 | 22.46 | 137 | 94.8 | 15.874 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7857.74 | 127.15 | 36.76 | 226 | 94.87 | 15.937 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 17416.28 | 5.52 | 2.92 | 14 | 99.59 | 15.161 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 16376.93 | 5.76 | 2.87 | 14 | 99.62 | 15.064 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 17874.39 | 16.25 | 6.83 | 35 | 99.11 | 15.555 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 16866.99 | 16.78 | 6.95 | 36 | 99.16 | 15.6 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 16409.45 | 59.73 | 19.94 | 119 | 97.35 | 16.29 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 15767.6 | 60.78 | 21.01 | 124 | 97.47 | 16.27 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 15297.4 | 6.35 | 2.92 | 14 | 99.58 | 15.145 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12553.62 | 7.69 | 3.31 | 18 | 99.61 | 15.177 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 15618.7 | 18.78 | 7.44 | 39 | 99.08 | 15.59 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12899.25 | 22.55 | 8.38 | 47 | 99.12 | 15.588 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 14126.85 | 70 | 21.63 | 133 | 97.34 | 9414 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 12365.99 | 78.98 | 24.97 | 150 | 97.42 | 9558 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 19858.94 | 4.99 | 2.69 | 11 | 99.6 | 15.146 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 17385.8 | 5.7 | 2.84 | 13 | 99.6 | 15.231 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 20616.74 | 14.49 | 6.34 | 31 | 99.15 | 15.454 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 18145.93 | 16.47 | 6.81 | 35 | 99.24 | 15.463 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17848.74 | 55.96 | 17.44 | 105 | 97.72 | 16.448 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16356.37 | 61.06 | 18.65 | 114 | 97.86 | 16.461 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 16908.95 | 5.72 | 2.84 | 13 | 99.64 | 15.097 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15926.11 | 5.98 | 2.73 | 13 | 99.67 | 15.096 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 17416.62 | 16.81 | 6.93 | 35 | 99.28 | 15.593 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16874.38 | 16.94 | 6.96 | 36 | 99.31 | 15.579 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17063.38 | 57.73 | 18.79 | 111 | 97.94 | 16.244 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16196.54 | 59.8 | 20.35 | 119 | 98.09 | 16.319 |
