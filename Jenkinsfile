node('COMPONENT_ECS') {
    stage('Run'){
        withCredentials( [usernamePassword(credentialsId: "${env.NEXUS_CREDENTIALS_ID}", usernameVariable: 'USERNAME',
                         passwordVariable: 'PASSWORD')]) {
            withEnv(["JAVA_HOME=${tool env.JDK}", "NEXUS_USERNAME=${USERNAME}", "NEXUS_PASSWORD=${PASSWORD}" ]) {
                sh """
                    git clone https://github.com/ballerina-platform/ballerina-lang
                    cd ballerina-lang/
                    #Temporary javadoc creation and spot bug check will be skipped
                    ./gradlew build --console=plain --stacktrace -scan -x createJavadoc -x spotbugsMain -x openapi-ballerina:ballerina-to-openapi-generator:test -x test -x check
                    ./gradlew publish
                """
            }
        }
    }
}
