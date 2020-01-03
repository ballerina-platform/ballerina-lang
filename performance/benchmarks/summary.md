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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 22535.09 | 4.4 | 2.86 | 12 | 99.56 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 20523.61 | 4.82 | 3.01 | 13 | 99.58 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 22654.08 | 13.19 | 6.29 | 29 | 99.09 | 12.289 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20343.84 | 14.68 | 6.72 | 32 | 99.03 | 17.859 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19634.25 | 50.86 | 16.45 | 98 | 97.34 | 19.521 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19759.68 | 50.53 | 16.27 | 97 | 97.35 | 19.658 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14860.33 | 6.69 | 4.41 | 20 | 99.07 | 15.395 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 11060.2 | 8.99 | 5.81 | 27 | 99.1 | 7314 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16962.34 | 17.63 | 7.83 | 40 | 97.96 | 15.585 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 11163.31 | 26.81 | 9.03 | 55 | 98.09 | 15.584 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 15276.46 | 65.39 | 19.57 | 120 | 94.65 | 16.642 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10736.15 | 93.06 | 17.62 | 142 | 94.18 | 16.589 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17855.48 | 5.55 | 2.92 | 13 | 99.55 | 15.056 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12404.38 | 8.01 | 3.54 | 18 | 99.63 | 15.083 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 18415.71 | 16.23 | 7 | 35 | 99.06 | 15.716 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12841.43 | 23.29 | 8.58 | 47 | 99.21 | 16.156 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 16127.92 | 61.92 | 19.45 | 117 | 97.51 | 18.169 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12241.33 | 81.61 | 23.73 | 147 | 97.74 | 18.998 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 13226.67 | 7.52 | 4.77 | 22 | 99.02 | 14.021 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7656.98 | 13.01 | 6.7 | 34 | 99.25 | 14.033 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14705.4 | 20.34 | 8.65 | 45 | 98.05 | 14.562 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7810.81 | 38.35 | 15.16 | 82 | 98.37 | 14.562 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12010.89 | 83.18 | 23.46 | 148 | 95.19 | 15.678 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7225.42 | 138.3 | 39.51 | 245 | 94.98 | 15.614 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 16134.11 | 6 | 3.01 | 14 | 99.59 | 15.716 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15554.73 | 6.1 | 2.93 | 14 | 99.6 | 15.512 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 17020 | 17.12 | 7.2 | 36 | 99.11 | 16.088 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 16001.24 | 17.88 | 7.27 | 38 | 99.16 | 16.09 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 15376.03 | 64.27 | 20.98 | 126 | 97.48 | 16.891 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 15151.56 | 64.3 | 21.93 | 130 | 97.53 | 17.049 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14629.97 | 6.65 | 3.1 | 15 | 99.57 | 15.656 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 12143.7 | 7.97 | 3.39 | 18 | 99.6 | 16.087 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 15411.97 | 19.01 | 7.68 | 40 | 99.06 | 16.993 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12555.42 | 23.18 | 8.56 | 48 | 99.14 | 15.966 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13901.34 | 71.12 | 22.61 | 137 | 97.35 | 19.306 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11636.14 | 85.13 | 25.36 | 158 | 97.4 | 18.852 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18899.84 | 5.25 | 2.89 | 12 | 99.57 | 15.671 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16214.58 | 6.12 | 3.08 | 14 | 99.64 | 15.782 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19699.42 | 15.16 | 6.76 | 33 | 99.14 | 16.206 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17072.19 | 17.51 | 7.29 | 37 | 99.24 | 16.267 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17284.48 | 57.76 | 18.02 | 108 | 97.68 | 18.338 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16044.05 | 62.23 | 18.66 | 115 | 97.91 | 17.032 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15884.44 | 6.1 | 3.11 | 14 | 99.65 | 15.648 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14991.64 | 6.38 | 3.06 | 15 | 99.67 | 15.766 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16915.13 | 17.3 | 7.22 | 37 | 99.31 | 17.3 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16208.82 | 17.69 | 7.33 | 38 | 99.33 | 17.237 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16773.11 | 58.71 | 19.72 | 115 | 97.94 | 19.752 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15936.35 | 61.43 | 21.36 | 124 | 98.09 | 17.919 |