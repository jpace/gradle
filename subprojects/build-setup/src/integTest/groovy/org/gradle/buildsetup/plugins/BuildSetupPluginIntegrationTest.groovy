/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.buildsetup.plugins

import org.gradle.integtests.fixtures.WellBehavedPluginTest

class BuildSetupPluginIntegrationTest extends WellBehavedPluginTest {

    @Override
    String getMainTask() {
        return "setupBuild"
    }

    @Override
    String getPluginId() {
        "build-setup"
    }

    def "setupBuild shows up on tasks overview "() {
        when:
        def executed = run 'tasks'
        then:
        executed.output.contains "setupBuild - Initializes a new Gradle build."
    }

    def "can be executed without existing pom"() {
        given:
        assert !buildFile.exists()
        when:
        run 'setupBuild'
        then:
        wrapperIsGenerated()
    }

    def "build file generation is skipped when build file already exists"() {
        given:
        assert buildFile.createFile()

        when:
        def executed = run('setupBuild')

        then:
        executed.assertTasksExecuted(":setupBuild")
        executed.output.contains("The build file 'build.gradle' already exists. Skipping build initialization.")
    }

    def "build file generation is skipped when settings file already exists"() {
        given:
        assert settingsFile.createFile()

        when:
        def executed = run('setupBuild')

        then:
        executed.assertTasksExecuted(":setupBuild")
        executed.output.contains("The settings file 'settings.gradle' already exists. Skipping build initialization.")
    }

    def "build file generation is skipped when custom build file exists"() {
        given:
        def customBuildScript = testDirectory.file("customBuild.gradle").createFile()

        when:
        executer.usingBuildScript(customBuildScript)
        def executed = run('setupBuild')

        then:
        executed.assertTasksExecuted(":setupBuild")
        executed.output.contains("The build file 'customBuild.gradle' already exists. Skipping build initialization.")
    }

    def "build file generation is skipped when part of a multi-project build with non-standard settings file location"() {
        given:
        def customSettings = testDirectory.file("customSettings.gradle")
        customSettings << """
include 'child'
"""
        when:
        executer.usingSettingsFile(customSettings)
        def executed = run('setupBuild')

        then:
        executed.assertTasksExecuted(":setupBuild")
        executed.output.contains("This Gradle project appears to be part of an existing multi-project Gradle build. Skipping build initialization.")
    }

    def "pom conversion is triggered when pom and no gradle file found"() {
        setup:
        file("pom.xml") << """
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>util</groupId>
  <artifactId>util</artifactId>
  <version>2.5</version>
  <packaging>jar</packaging>
</project>"""
        when:
//            def executed = succeeds('setupBuild', '--type', 'java-library"')
        def executed = succeeds('setupBuild')
        then:
        executed.assertTasksExecuted(":maven2Gradle", ":wrapper", ":setupBuild")
    }

    def "pom conversion not triggered when setupBuild-type passed"() {
        setup:
        file("pom.xml") << """
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>util</groupId>
  <artifactId>util</artifactId>
  <version>2.5</version>
  <packaging>jar</packaging>
</project>"""
        when:
        def executed = succeeds('setupBuild', '--type', 'java-library')
        then:
        executed.assertTasksExecuted(":generateBuildFile", ":generateSettingsFile", ":setupProjectLayout", ":wrapper", ":setupBuild")
    }

    def "gives decent error message when triggered with unknown setupBuild-type"() {
        when:
        fails('setupBuild', '--type', 'some-unknown-library')
        then:
        errorOutput.contains("Declared setup-type 'some-unknown-library' is not supported.")
    }

    private def wrapperIsGenerated() {
        file("gradlew").assertExists()
        file("gradlew.bat").assertExists()
        file("gradle/wrapper/gradle-wrapper.jar").assertExists()
        file("gradle/wrapper/gradle-wrapper.properties").assertExists()
    }
}