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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 23016.39 | 4.3 | 2.82 | 12 | 99.55 |  |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 21504.77 | 4.6 | 8.85 | 12 | 99.56 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 23485.56 | 12.72 | 10.13 | 29 | 99 |  |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 21433.98 | 13.94 | 10.53 | 31 | 99.08 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 21045.24 | 47.44 | 15.72 | 93 | 97.05 |  |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 20485.92 | 48.72 | 15.93 | 94 | 97.3 |  |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 15635.35 | 6.36 | 20.12 | 21 | 99.05 | 9657 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9193.72 | 10.32 | 99.11 | 30 | 98.38 | 10.426 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16775.35 | 17.84 | 12.6 | 42 | 97.89 | 11.732 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 9708.52 | 29.97 | 83.59 | 61 | 95.31 | 20.347 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 15415.85 | 64.8 | 20.36 | 121 | 94.13 | 10.565 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9414.56 | 106.14 | 58.37 | 164 | 83.31 | 17.011 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 19383.48 | 5.11 | 2.87 | 13 | 99.3 | 48.133 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 15260.06 | 6.5 | 3.12 | 15 | 99.38 | 48.124 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 19661.75 | 15.2 | 6.76 | 33 | 98.74 | 114.209 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 16042.99 | 18.64 | 7.52 | 40 | 98.86 | 113.351 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 17777.42 | 56.17 | 18.04 | 108 | 96.61 | 151.475 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 14861.89 | 67.21 | 20.49 | 125 | 96.69 | 149.388 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 13168.29 | 7.33 | 15.61 | 22 | 98.95 | 47.408 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 8431.08 | 11.82 | 33.44 | 35 | 98.3 | 48.17 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14409.88 | 20.77 | 17.33 | 49 | 97.75 | 80.613 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 9009.35 | 33.24 | 29.61 | 76 | 95.27 | 80.777 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13242.55 | 75.44 | 24.49 | 143 | 93.57 | 149.256 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 7692.45 | 129.9 | 41.67 | 219 | 84.53 | 148.471 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 17224.95 | 5.57 | 2.93 | 13 | 99.56 | 15.743 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 16479.32 | 5.68 | 2.92 | 14 | 99.56 | 15.743 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 17468.04 | 16.65 | 7.24 | 36 | 99.06 | 16.207 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 16768.1 | 16.85 | 7.28 | 37 | 99.09 | 16.205 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 16328.31 | 60.46 | 20.47 | 121 | 97.08 | 17.302 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 15716.29 | 61.2 | 21.92 | 128 | 97.32 | 17.252 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 16200.81 | 5.95 | 2.99 | 14 | 99.35 | 47.576 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 14888.17 | 6.38 | 3.02 | 15 | 99.37 | 47.565 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 16337.77 | 17.88 | 7.52 | 39 | 98.68 | 49.677 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 15007.47 | 19.13 | 7.81 | 42 | 98.74 | 49.673 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 14705.36 | 66.73 | 22.31 | 132 | 96.31 | 133.366 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 13709.2 | 70.55 | 23.88 | 140 | 96.34 | 83.53 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 19553.16 | 5.07 | 9.22 | 12 | 99.56 | 16.112 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16746.58 | 5.92 | 3.03 | 14 | 99.62 | 16.211 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19815.91 | 15.08 | 11 | 33 | 99.06 | 16.699 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17081.83 | 17.5 | 7.25 | 37 | 99.21 | 16.77 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17594.85 | 56.75 | 19.93 | 107 | 97.54 | 21.867 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16012.42 | 62.37 | 19.17 | 117 | 97.76 | 20.041 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 17511.84 | 5.49 | 2.97 | 13 | 99.61 | 16.076 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16462.89 | 5.71 | 2.95 | 14 | 99.63 | 15.902 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 18660.46 | 15.59 | 6.93 | 34 | 99.2 | 17.917 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 17085.87 | 16.59 | 7.2 | 37 | 99.26 | 17.388 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 17678.73 | 55.8 | 19.13 | 110 | 97.71 | 19.354 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 16540.93 | 58.87 | 21.55 | 121 | 97.83 | 22.272 |
