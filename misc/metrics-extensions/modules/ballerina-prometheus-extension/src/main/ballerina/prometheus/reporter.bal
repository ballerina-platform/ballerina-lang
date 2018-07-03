import ballerina/http;
import ballerina/io;
import ballerina/observe;
import ballerina/config;

@final string METRIC_TYPE_GAUGE = "gauge";
@final string METRIC_TYPE_SUMMARY = "summary";
@final string EMPTY_STRING = "";

@final string PROMETHEUS_PORT_CONFIG = "b7a.observability.metrics.prometheus.port";
@final string PROMETHEUS_HOST_CONFIG = "b7a.observability.metrics.prometheus.host";
@final int REPORTER_PORT = config:getAsInt(PROMETHEUS_PORT_CONFIG, default = 9797);
@final string REPORTER_HOST = config:getAsString(PROMETHEUS_HOST_CONFIG, default = "0.0.0.0");

endpoint http:Listener prometheusListener {
 host: REPORTER_HOST,
 port: REPORTER_PORT
};

@http:ServiceConfig {
 basePath: "/metrics"
}
service<http:Service> PrometheusReporter bind prometheusListener {

 @http:ResourceConfig {
  methods: ["GET"],
  path: "/",
  produces: ["application/text"]
 }
 getMetrics(endpoint caller, http:Request req) {
  observe:Metric[] metrics = observe:getAllMetrics();
  string payload = EMPTY_STRING;
  foreach metric in metrics {
   payload += generateMetricHelp(metric.name, metric.desc);
   payload += generateMetricInfo(metric.name, metric.metricType);
   payload += generateMetric(metric.name, metric.tags, metric.value);
   if (metric.metricType.equalsIgnoreCase(METRIC_TYPE_GAUGE) && metric.summary != null){
    observe:Snapshot[]? summaries = metric.summary;
    match summaries {
     observe:Snapshot[] snapshots => {
      foreach aSnapshot in snapshots{
       string expiry = <string>aSnapshot.expiry;
       string summaryMetricName = getMetricName(metric.name, expiry);
       payload += generateMetricHelp(summaryMetricName, "A Summary of " + metric.name + " for window of "
         + aSnapshot.expiry);
       payload += generateMetricInfo(summaryMetricName, METRIC_TYPE_SUMMARY);
       payload += generateMetric(getMetricName(summaryMetricName, "mean"), (), aSnapshot.mean);
       payload += generateMetric(getMetricName(summaryMetricName, "max"), (), aSnapshot.max);
       payload += generateMetric(getMetricName(summaryMetricName, "min"), (), aSnapshot.min);
       payload += generateMetric(getMetricName(summaryMetricName, "stdDev"), (), aSnapshot.stdDev);
       foreach percentileValue in aSnapshot.percentileValues  {
        payload += generatePercentileMetric(summaryMetricName, percentileValue.percentile, percentileValue.value);
       }
      }
     }
     () => {payload += "\n";}
    }
   }
  }

  http:Response res = new;
  res.setPayload(payload);
  _ = caller->respond(res);
 }
}

function generateMetricInfo(string name, string metricType) returns string {
 return "# TYPE " + name + " " + metricType + "\n";
}

function generateMetricHelp(string name, string description) returns string {
 if (!description.equalsIgnoreCase(EMPTY_STRING)) {
  return "# HELP " + name + " " + description + "\n";
 }
 return EMPTY_STRING;
}

function generateMetric(string name, map<string>? labels, int|float value) returns string {
 string strValue = "";
 match value {
  int intValue => strValue = <string>intValue;
  float floatValue => strValue = <string>floatValue;
 }
 match labels {
  map<string> mapLabels => {
   json jsonLabels = check <json>labels;
   return (name + jsonLabels.toString() + " " + strValue + "\n");
  }
  () => return (name + " " + strValue + "\n");
 }
}

function getMetricName(string name, string summaryType) returns string {
 return name + "_" + summaryType;
}

function generatePercentileMetric(string name, float percentile, float value) returns string {
 string strValue = <string>value;
 return (name + "{quantile=" + percentile + "} " + strValue + "\n");
}
