# Ballerina Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Passthrough HTTP service (h1c -> h1c) | An HTTP Service, which forwards all requests to an HTTP back-end service. |
| Passthrough HTTPS service (h1 -> h1) | An HTTPS Service, which forwards all requests to an HTTPS back-end service. |
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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 9559.94 | 10.42 | 4.64 | 23 | 99.79 | 14.08 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 9075.48 | 10.98 | 4.98 | 25 | 99.79 | 14.557 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 9746.62 | 30.73 | 10.37 | 60 | 99.57 | 15.057 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 9389.98 | 31.9 | 10.86 | 63 | 99.58 | 15.056 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 9191.98 | 108.71 | 24.44 | 180 | 98.85 | 15.651 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0.01 | 8923.59 | 106.73 | 291.65 | 174 | 98.87 | 15.663 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 8691.87 | 11.46 | 4.83 | 25 | 99.76 | 14.512 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 7219.35 | 13.8 | 5.25 | 29 | 99.77 | 14.478 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 8754.52 | 34.21 | 10.93 | 65 | 99.52 | 14.759 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 7283.16 | 41.13 | 12.25 | 76 | 99.52 | 15.03 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 8509.79 | 117.41 | 25.54 | 191 | 98.65 | 15.995 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 7135.66 | 140.04 | 27.82 | 219 | 98.63 | 15.922 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 8134.63 | 12.15 | 4.8 | 26 | 99.77 | 15.167 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 8075.89 | 12.18 | 5.37 | 26 | 99.77 | 15.164 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 8406.13 | 35.41 | 11.01 | 67 | 99.53 | 15.634 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 8299.24 | 35.69 | 10.92 | 66 | 99.53 | 15.612 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 8159.79 | 122.2 | 27.06 | 201 | 98.59 | 16.288 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 8099.59 | 122.3 | 28.3 | 203 | 98.64 | 16.338 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 7756.42 | 12.76 | 4.96 | 27 | 99.75 | 15.157 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 7010.01 | 14.08 | 5.1 | 29 | 99.75 | 15.171 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 7853.33 | 37.94 | 11.4 | 70 | 99.48 | 15.608 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 7081.15 | 41.93 | 12.32 | 76 | 99.46 | 15.543 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 7617.3 | 130.89 | 27.69 | 212 | 98.47 | 16.318 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 6909.15 | 143.63 | 29.59 | 228 | 98.41 | 16.342 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 9019.11 | 11.04 | 4.71 | 24 | 99.79 | 15.175 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 8454.97 | 11.78 | 14.2 | 26 | 99.8 | 15.189 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 9116 | 32.85 | 10.72 | 63 | 99.58 | 15.383 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 8684.57 | 34.49 | 11.04 | 66 | 99.6 | 15.367 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 8651 | 115.51 | 25.33 | 189 | 98.84 | 16.384 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 8417.14 | 118.71 | 26.14 | 195 | 98.88 | 16.378 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 8272.75 | 11.96 | 4.66 | 25 | 99.8 | 15.179 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 8087.36 | 12.19 | 4.65 | 26 | 99.81 | 15.159 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 8463.25 | 35.21 | 10.86 | 66 | 99.61 | 15.586 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 8252.94 | 35.96 | 10.93 | 66 | 99.62 | 15.6 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 8493.98 | 117.45 | 22.22 | 182 | 98.91 | 16.342 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 8242.56 | 120.41 | 26.72 | 196 | 98.94 | 16.274 |
