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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 20864.41 | 4.75 | 9.05 | 13 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 18971.24 | 5.22 | 3.18 | 13 | 99.6 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21474.85 | 13.92 | 6.53 | 31 | 99.03 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 19734.74 | 15.14 | 6.84 | 33 | 99.12 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19556.41 | 51.07 | 16.49 | 99 | 97.31 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 19013.01 | 52.52 | 16.91 | 101 | 97.36 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14172.7 | 6.94 | 18.35 | 22 | 98.92 |  |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9461.17 | 10.37 | 54.83 | 30 | 98.35 | 17.159 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16047.22 | 18.65 | 18.82 | 44 | 97.33 | 18.283 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 9242.98 | 31.79 | 70.94 | 64 | 95.22 | 30.873 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14365.46 | 69.54 | 22.57 | 132 | 93.98 | 13.146 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 8900.05 | 112.27 | 61.55 | 172 | 83.53 | 24.896 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 16934.17 | 5.86 | 9.89 | 13 | 99.54 | 15.15 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 11904.17 | 8.35 | 3.64 | 19 | 99.62 | 15.13 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17203.49 | 17.38 | 7.14 | 37 | 99.05 | 16.808 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12530.01 | 23.88 | 8.77 | 49 | 99.17 | 16.867 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15681.33 | 63.68 | 19.15 | 118 | 97.37 | 20.707 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11679.25 | 85.53 | 24.21 | 152 | 97.66 | 21.624 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12468.23 | 7.98 | 12.11 | 24 | 99 | 15.284 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7027.69 | 14.18 | 16.32 | 37 | 98.53 | 15.378 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13466.65 | 22.22 | 9.75 | 50 | 97.96 | 16.52 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7184.59 | 41.69 | 26.42 | 90 | 96.3 | 17.312 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 12059.29 | 82.85 | 26.22 | 155 | 94.46 | 20.69 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6423.76 | 155.57 | 47.53 | 275 | 87.9 | 20.667 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 14907.69 | 6.49 | 3.17 | 15 | 99.59 | 15.512 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 13807.18 | 6.7 | 3.17 | 15 | 99.61 | 15.532 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 15932.28 | 18.36 | 7.62 | 39 | 99.09 | 16.012 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15010.43 | 19.12 | 7.71 | 41 | 99.14 | 16.323 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14476.27 | 68.46 | 21.64 | 132 | 97.4 | 17.077 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14348.18 | 67.54 | 22.35 | 134 | 97.45 | 17.062 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 13593.47 | 7.15 | 3.32 | 16 | 99.57 | 15.569 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11381.21 | 8.51 | 3.59 | 19 | 99.6 | 15.561 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14069.27 | 20.89 | 8.3 | 44 | 99.07 | 17.895 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 11900.85 | 24.5 | 9.01 | 50 | 99.1 | 18.278 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13038.17 | 75.87 | 23.81 | 145 | 97.29 | 19.773 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11088.73 | 89.24 | 26.63 | 165 | 97.36 | 19.693 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 17491.89 | 5.67 | 3.13 | 14 | 99.55 | 15.856 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 15360.34 | 6.46 | 3.23 | 15 | 99.64 | 15.875 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 18190.61 | 16.43 | 7.07 | 35 | 99.13 | 16.501 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16155.89 | 18.5 | 7.47 | 39 | 99.22 | 16.552 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16288.35 | 61.3 | 18.78 | 114 | 97.57 | 20.47 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15076.54 | 66.2 | 19.3 | 120 | 97.84 | 18.524 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 14821.62 | 6.55 | 3.3 | 15 | 99.65 | 15.677 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14198.04 | 6.74 | 3.23 | 16 | 99.66 | 15.637 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16187.41 | 18.09 | 7.52 | 38 | 99.28 | 17.709 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15098.18 | 19.08 | 7.7 | 41 | 99.3 | 17.591 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15585.98 | 63.38 | 21.45 | 123 | 97.86 | 21.897 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 14884.37 | 65.79 | 21.37 | 129 | 98.07 | 18.102 |
