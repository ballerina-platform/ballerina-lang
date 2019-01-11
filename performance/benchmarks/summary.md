# Ballerina Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Passthrough HTTP service | An HTTP Service, which forwards all requests to a back-end service. |
| Passthrough HTTPS service | An HTTPS Service, which forwards all requests to a back-end service. |
| JSON to XML transformation HTTP service | An HTTP Service, which transforms JSON requests to XML and then forwards all requests to a back-end service. |
| JSON to XML transformation HTTPS service | An HTTPS Service, which transforms JSON requests to XML and then forwards all requests to a back-end service. |
| Passthrough HTTP2 (HTTPS) service | An HTTPS Service exposed over HTTP2 protocol, which forwards all requests to a back-end service. |

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
| Concurrent Users | The number of users accessing the application at the same time. | 50, 150, 500 |
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

|  Scenario Name | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | Ballerina GC Throughput (%) | Average of Ballerina Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  Passthrough HTTP service | 100 | 50 | 0 | 0 | 21578.47 | 4.59 | 6.4 | 37 | 99.46 |  |
|  Passthrough HTTP service | 100 | 1024 | 0 | 0 | 19466.68 | 5.09 | 6.22 | 36 | 99.51 |  |
|  Passthrough HTTP service | 300 | 50 | 0 | 0 | 23168.69 | 12.9 | 11.19 | 66 | 98.72 |  |
|  Passthrough HTTP service | 300 | 1024 | 0 | 0 | 21190.34 | 14.1 | 11.95 | 71 | 98.82 |  |
|  Passthrough HTTP service | 1000 | 50 | 0 | 0 | 21282.2 | 46.91 | 24.18 | 139 | 96.48 |  |
|  Passthrough HTTP service | 1000 | 1024 | 0 | 0 | 19893.71 | 50.19 | 25.41 | 145 | 96.65 |  |
|  Passthrough HTTP2 (HTTPS) service | 100 | 50 | 0 | 0 | 16215.74 | 5.97 | 7.91 | 43 | 99.53 | 24.265 |
|  Passthrough HTTP2 (HTTPS) service | 100 | 1024 | 0 | 0 | 15142.18 | 6.37 | 7.5 | 39 | 99.57 | 24.264 |
|  Passthrough HTTP2 (HTTPS) service | 300 | 50 | 0 | 0 | 17015.74 | 17.14 | 15.86 | 81 | 98.95 | 24.746 |
|  Passthrough HTTP2 (HTTPS) service | 300 | 1024 | 0 | 0 | 15665.8 | 18.5 | 14.6 | 73 | 99.09 | 24.848 |
|  Passthrough HTTP2 (HTTPS) service | 1000 | 50 | 0 | 5.41 | 7105.43 | 140.1 | 201.48 | 979 | 98.14 | 25.563 |
|  Passthrough HTTP2 (HTTPS) service | 1000 | 1024 | 0 | 0.25 | 13076.38 | 74.52 | 83.78 | 497 | 97.63 | 25.596 |
|  Passthrough HTTPS service | 100 | 50 | 0 | 0 | 19925.51 | 4.97 | 7 | 40 | 99.46 | 15.74 |
|  Passthrough HTTPS service | 100 | 1024 | 0 | 0 | 16554.62 | 5.99 | 5.92 | 27 | 99.47 | 24.627 |
|  Passthrough HTTPS service | 300 | 50 | 0 | 0 | 21035.72 | 14.19 | 11.73 | 68 | 98.77 | 25.01 |
|  Passthrough HTTPS service | 300 | 1024 | 0 | 0 | 17255.16 | 17.32 | 11.18 | 60 | 99.02 | 24.652 |
|  Passthrough HTTPS service | 1000 | 50 | 0 | 0 | 19180.48 | 52.07 | 25.94 | 145 | 96.68 | 25.816 |
|  Passthrough HTTPS service | 1000 | 1024 | 0 | 0 | 15941.01 | 62.65 | 25.51 | 145 | 97.19 | 26.021 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14030.77 | 7.08 | 8.67 | 48 | 99.03 | 23.793 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9680.38 | 10.28 | 10.09 | 56 | 99.09 | 23.786 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15451.37 | 19.36 | 13.01 | 76 | 97.6 | 23.788 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10645.28 | 28.12 | 14.95 | 86 | 97.56 | 23.786 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14004.2 | 71.34 | 20.14 | 133 | 92.75 | 25.031 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10047.52 | 99.44 | 24.84 | 170 | 91.51 | 25.003 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12943.15 | 7.68 | 8.04 | 44 | 99.07 | 23.397 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 8627.55 | 11.54 | 9.77 | 54 | 99.14 | 23.403 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 14033.95 | 21.32 | 14.71 | 85 | 97.79 | 23.98 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 9147.42 | 32.73 | 17.57 | 99 | 97.84 | 23.693 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13118.6 | 76.16 | 30.23 | 183 | 93.46 | 25.039 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 8761.37 | 114.05 | 43.93 | 257 | 93.28 | 25.177 |
