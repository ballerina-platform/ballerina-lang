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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 16728.64 | 5.66 | 103.92 | 17 | 99.52 | 11.727 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 15605.02 | 6.1 | 111.19 | 18 | 99.53 | 9370 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 17881.49 | 15.96 | 126.72 | 37 | 98.76 | 14.23 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 16974.63 | 16.79 | 127.08 | 39 | 98.77 | 10.461 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 16994.69 | 56.61 | 136.23 | 139 | 96.3 | 11.463 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 16112.46 | 59.26 | 115.95 | 148 | 96.43 | 13.791 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 10806.49 | 8.82 | 104.64 | 27 | 98.74 | 14.672 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 7074.38 | 13.43 | 200.84 | 35 | 98.07 | 14.624 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 10944.35 | 26.27 | 102.61 | 65 | 97.54 | 15.464 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 7328.17 | 39.04 | 141.45 | 97 | 95.31 | 16.232 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 10251.21 | 92.97 | 121.17 | 246 | 93.38 | 16.383 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 6524.12 | 146.06 | 171.76 | 399 | 84.4 | 85.754 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 14229.1 | 6.66 | 92.57 | 19 | 99.51 | 15.326 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 10855.21 | 8.94 | 83.01 | 21 | 99.57 | 15.328 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 14723.67 | 19.36 | 104.61 | 43 | 98.82 | 16.314 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 11553.33 | 25.9 | 85.05 | 54 | 99.04 | 15.876 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 13691.13 | 69.58 | 123.21 | 148 | 96.71 | 19.423 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 10002.11 | 95.28 | 79.66 | 176 | 97.29 | 20.604 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 9577.42 | 9.96 | 81.49 | 30 | 98.89 | 16.086 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6206 | 16.06 | 56.69 | 43 | 98.34 | 16.149 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 9989.77 | 28.79 | 87.49 | 67 | 97.75 | 16.687 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6272.19 | 47.58 | 91.88 | 103 | 95.98 | 16.282 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 9699.19 | 103.02 | 86.21 | 259 | 93.57 | 16.884 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 5287.48 | 189.07 | 102.01 | 449 | 86.84 | 146.689 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 13404.18 | 6.7 | 4.28 | 20 | 99.47 | 15.848 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 12566.13 | 6.9 | 4.3 | 21 | 99.4 | 15.916 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 13864.89 | 19.69 | 8.45 | 44 | 98.84 | 8922 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 13387 | 19.86 | 8.68 | 46 | 98.82 | 16.281 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 12213.22 | 76.99 | 28.31 | 170 | 96.81 | 17.229 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 12585.67 | 74.84 | 28.14 | 169 | 96.57 | 17.261 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 12100.22 | 7.33 | 4.31 | 20 | 99.36 | 15.834 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 9748.36 | 9.48 | 5.07 | 25 | 99.48 | 15.801 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 12640.3 | 21.77 | 9.11 | 48 | 98.73 | 16.388 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 10620.83 | 26.01 | 10.02 | 55 | 98.89 | 16.282 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 11941.42 | 79.42 | 27.32 | 167 | 96.61 | 20.375 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10282.84 | 92.13 | 29.32 | 178 | 96.8 | 19.45 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 14836.65 | 6.42 | 93.99 | 19 | 99.53 | 15.957 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 13073.3 | 7.27 | 95.58 | 20 | 99.58 | 15.933 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 15638.62 | 18.26 | 123.14 | 41 | 98.87 | 16.993 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 14121.83 | 20.33 | 114.98 | 44 | 99.03 | 16.471 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 14425.77 | 66.05 | 111.16 | 144 | 96.92 | 22.095 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 13144.3 | 72.6 | 116.9 | 152 | 97.18 | 19.894 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 13214.23 | 6.59 | 4.3 | 20 | 99.61 | 15.88 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 12569.79 | 7.02 | 4.32 | 21 | 99.63 | 15.826 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 13776.52 | 20.04 | 8.67 | 45 | 99.12 | 18.426 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 13807.39 | 19.48 | 8.65 | 46 | 99.14 | 18.139 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 12996.2 | 72.7 | 29.22 | 168 | 97.07 | 21.305 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 13222.62 | 70.52 | 28.35 | 164 | 97.13 | 20.084 |
