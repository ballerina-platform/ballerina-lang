node('COMPONENT_ECS') {
    stage('Run'){
        withCredentials( [usernamePassword(credentialsId: "${env.NEXUS_CREDENTIALS_ID}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            withEnv(["JAVA_HOME=${tool env.JDK}", "NEXUS_USERNAME=${USERNAME}", "NEXUS_PASSWORD=${PASSWORD}" ]) {
                sh """
                    git clone https://github.com/ballerina-platform/ballerina-lang
                    cd ballerina-lang/tool-plugins/vscode
                    npm install --unsafe-perm
                    npm run vscode:prepublish
                    cd ../..
                    ./gradlew build -x createJavadoc -x spotbugsMain -x test
                    ./gradlew publish
                """
            }
        }
    }
}