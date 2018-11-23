skipTest=""

# Change directory to provided path
cd ${1}

# Change skip tests based on argument
if [ ${2} ]
then
    skipTest="--skiptests"
fi

exclude=("date-time"
        "proto-to-ballerina"
        "swagger-to-ballerina"
        "taint-checking"
        "websub-hub-client-sample"
        "websub-remote-hub-sample"
        "counter-metrics"
        "config-api"
        "log-api"
        "secured-service-with-basic-auth"
        "testerina-function-mocks"
        "jms-queue-message-receiver-with-client-acknowledgment"
        "gauge-metrics")
packages=($( sed -n 's/.*"url": "\([^"]*\)"/\1/p' index.json ))
echo `date "+%Y-%m-%d-%H:%M:%S"`" : Start building ${#packages[@]} Ballerina By Examples and ${#exclude[@]} will be skipped"

# Remove excludes
for i in "${exclude[@]}"
do
         packages=(${packages[@]//*$i*})
done

for package in "${packages[@]}"
do
  sh ../bin/ballerina build ${package} ${skipTest} || exit 1
done
echo `date "+%Y-%m-%d-%H:%M:%S"`" : Ballerina By Examples built successfully!"