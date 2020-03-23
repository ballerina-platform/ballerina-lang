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
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 21872.21 | 4.53 | 2.95 | 12 | 99.56 | 10.151 |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 19640.05 | 5.04 | 3.04 | 13 | 99.45 | 11.172 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 21824.31 | 13.69 | 6.59 | 31 | 98.47 | 15.905 |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 20024.37 | 14.92 | 6.86 | 33 | 98.82 | 17.716 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 19381.39 | 51.5 | 16.62 | 99 | 97.36 | 9762 |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 18837.17 | 53.01 | 16.62 | 101 | 97.38 | 15.087 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14233.96 | 6.98 | 4.71 | 21 | 99.04 | 15.41 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 10092.43 | 9.86 | 5.97 | 29 | 99.17 | 15.426 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 16164.69 | 18.5 | 8.11 | 42 | 97.93 | 15.57 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10861.39 | 27.56 | 9.15 | 55 | 98.06 | 15.703 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14665.63 | 68.12 | 18.99 | 121 | 94.63 | 16.595 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10377.46 | 96.29 | 17.25 | 144 | 94.04 | 16.557 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 17609.01 | 5.63 | 2.97 | 13 | 99.53 | 15.485 |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 12307.45 | 8.07 | 3.58 | 19 | 99.62 | 15.083 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 17805.26 | 16.79 | 7.05 | 36 | 99.04 | 15.557 |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 12687.23 | 23.58 | 8.72 | 48 | 99.18 | 16.662 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 15724.2 | 63.52 | 19.37 | 119 | 97.44 | 19.859 |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 12008.19 | 83.19 | 23.93 | 149 | 97.64 | 20.4 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12757.06 | 7.79 | 4.95 | 23 | 99.03 | 14.091 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7495.95 | 13.29 | 6.76 | 34 | 99.23 | 14.086 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13819.34 | 21.65 | 9.22 | 48 | 98.12 | 14.618 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 7663.7 | 39.08 | 15.37 | 83 | 98.33 | 14.35 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 11564.29 | 86.39 | 24.77 | 154 | 95 | 15.672 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 6993.32 | 142.89 | 39.02 | 247 | 95.08 | 15.764 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 15812.81 | 6.1 | 3.76 | 14 | 99.57 | 15.598 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 15054.53 | 6.29 | 2.99 | 14 | 99.59 | 15.599 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 16377.63 | 17.77 | 7.44 | 38 | 99.1 | 16.116 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 15611.1 | 18.21 | 7.55 | 39 | 99.13 | 16.081 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 14831.15 | 66.45 | 21.81 | 130 | 97.42 | 16.817 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 14598.39 | 67.08 | 22.28 | 135 | 97.42 | 17.042 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 14257.99 | 6.8 | 3.2 | 15 | 99.57 | 15.704 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 11830.56 | 8.16 | 3.53 | 19 | 99.57 | 16.113 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 14799.42 | 19.79 | 7.95 | 42 | 99.04 | 16.571 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 12200.3 | 23.85 | 8.84 | 50 | 99.11 | 16.487 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 13464.1 | 73.71 | 22.88 | 140 | 97.3 | 20.667 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 11369.77 | 87.09 | 25.84 | 161 | 97.29 | 20.302 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 18351.85 | 5.4 | 3 | 13 | 99.58 | 15.631 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 16072.32 | 6.17 | 3.13 | 14 | 99.63 | 15.744 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 19200.66 | 15.56 | 6.88 | 34 | 99.11 | 17.062 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 16688.2 | 17.91 | 7.42 | 38 | 99.23 | 16.224 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 16608.13 | 60.12 | 18.77 | 113 | 97.65 | 18.753 |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15527.18 | 64.32 | 19.23 | 119 | 97.84 | 20.202 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 15512.03 | 6.26 | 3.09 | 14 | 99.64 | 15.509 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 14540.17 | 6.6 | 3.07 | 15 | 99.66 | 16.11 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 16387.29 | 17.9 | 7.39 | 38 | 99.3 | 17.789 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 15682.03 | 18.3 | 7.54 | 40 | 99.32 | 17.098 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 15739.72 | 62.84 | 20.13 | 120 | 97.95 | 20.403 |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 15394.1 | 63.46 | 23.84 | 130 | 97.99 | 22.17 |
