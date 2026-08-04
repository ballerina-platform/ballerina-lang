import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class BallerinaStdLibBuildPluginTest {

    @TempDir
    File testProjectDir

    @Test
    void copyBallerinaProjectCreatesEmptyDependenciesTomlWhenMissing() {
        new File(testProjectDir, 'settings.gradle') << "rootProject.name = 'sample'\n"
        new File(testProjectDir, 'build.gradle') << """
            plugins {
                id 'ballerinaStdLibBuild'
            }
        """.stripIndent()

        def ballerinaDir = new File(testProjectDir, 'src/main/ballerina')
        assert ballerinaDir.mkdirs()
        new File(ballerinaDir, 'Ballerina.toml') << '''
[package]
org = "foo"
name = "sample"
version = "1.2.3"
'''

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .withArguments('copyBallerinaProject')
                .build()

        assert result.output.contains('BUILD SUCCESSFUL')
        assert new File(testProjectDir, 'build/ballerina-src/Dependencies.toml').isFile()
    }
}
