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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 10254.57 | 9.71 | 4.37 | 22 | 99.8 | 14.078 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 9848.06 | 10.12 | 4.59 | 23 | 99.8 | 14.55 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 10482.3 | 28.57 | 9.82 | 56 | 99.57 | 15.066 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 9897.9 | 30.26 | 20.3 | 60 | 99.59 | 15.08 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 9936.44 | 100.57 | 23.61 | 168 | 98.81 | 15.626 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0.01 | 9514.17 | 100.86 | 240.6 | 167 | 98.84 | 15.556 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 8256.54 | 12.07 | 6.55 | 32 | 99.56 | 15.053 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 6853.53 | 14.55 | 8.06 | 40 | 99.53 | 15.004 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 8436.57 | 35.51 | 13.59 | 75 | 99.1 | 15.053 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 7046.36 | 42.51 | 16.86 | 91 | 98.96 | 15.004 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 8114.81 | 123.15 | 30.13 | 208 | 97.46 | 15.695 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 6529.01 | 153.06 | 38.93 | 261 | 96.95 | 15.824 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 9175.3 | 10.86 | 4.6 | 24 | 99.76 | 14.479 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 7646.93 | 13.03 | 4.98 | 28 | 99.77 | 14.51 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 9462.52 | 31.65 | 10.36 | 61 | 99.52 | 14.767 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 7742.35 | 38.69 | 11.63 | 71 | 99.53 | 14.999 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 8978.31 | 111.3 | 24.58 | 182 | 98.65 | 16.003 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 7536.39 | 132.59 | 26.78 | 208 | 98.63 | 15.939 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 7606.95 | 13.1 | 6.81 | 34 | 99.53 | 14.255 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 5325.03 | 18.73 | 9.07 | 46 | 99.52 | 14.268 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 7605.79 | 39.39 | 14.26 | 80 | 99.04 | 14.528 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 5408.69 | 55.4 | 18.98 | 109 | 98.96 | 14.529 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 7241.85 | 137.99 | 32.1 | 229 | 97.26 | 15.78 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 5005.39 | 199.72 | 43.01 | 317 | 96.93 | 15.857 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 8405.9 | 11.75 | 4.7 | 25 | 99.78 | 15.163 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 8297.37 | 11.85 | 4.6 | 25 | 99.78 | 15.169 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 8643.7 | 34.41 | 11.24 | 67 | 99.52 | 15.621 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 8395.31 | 35.26 | 10.86 | 66 | 99.53 | 15.647 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 8319.96 | 119.81 | 26.67 | 198 | 98.63 | 9493 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 8233.45 | 120.3 | 27.99 | 200 | 98.65 | 16.354 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 8056.02 | 12.27 | 4.78 | 26 | 99.76 | 15.178 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 7253.13 | 13.59 | 4.97 | 28 | 99.75 | 15.171 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 8105.52 | 36.71 | 11.14 | 68 | 99.48 | 15.63 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 7306.91 | 40.56 | 11.67 | 73 | 99.47 | 15.556 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 7833.87 | 127.09 | 27.41 | 207 | 98.49 | 16.279 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 7077.89 | 140.76 | 28.64 | 224 | 98.43 | 16.364 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 9261.97 | 10.75 | 4.62 | 24 | 99.79 | 15.173 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 8701.87 | 11.45 | 13.96 | 25 | 99.8 | 15.175 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 9596.98 | 31.2 | 10.31 | 61 | 99.57 | 15.434 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 8972.49 | 33.38 | 10.77 | 64 | 99.6 | 15.403 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 9089.3 | 109.92 | 24.78 | 182 | 98.83 | 16.402 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 8733.16 | 114.4 | 25.55 | 188 | 98.88 | 16.355 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 8440.73 | 11.71 | 4.58 | 25 | 99.81 | 15.171 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 8204.35 | 12.01 | 4.59 | 25 | 99.81 | 15.155 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 8843.3 | 33.66 | 10.34 | 63 | 99.61 | 15.622 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 8560.88 | 34.61 | 10.77 | 65 | 99.63 | 15.567 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 8656.2 | 115.28 | 22.15 | 179 | 98.89 | 16.28 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 8457.37 | 117.66 | 25.29 | 190 | 98.91 | 16.378 |
