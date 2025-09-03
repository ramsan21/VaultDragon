Thanks for the clearer shot—still the same core error:

maven-surefire-plugin … Test mechanism :: Cannot invoke
org.apache.maven.surefire.junitcore.TestSet.createThreadAttachedTestMethod(...)
because "testSet" is null

That almost always means the forked test JVM died (or a test class failed to load) before Surefire could build its internal TestSet. Upgrading the plugin alone won’t fix it if the root cause is classloading/JDK/provider mismatch.

Quick, copy-paste fixes (POM)

Try this minimal, safe config for Java 17+ + JUnit 5. Drop it into your <build><plugins> (or replace your existing Surefire block):

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.2.5</version>
  <configuration>
    <!-- JDK 9+ module path can break some libs; keep on classpath -->
    <useModulePath>false</useModulePath>
    <!-- more diagnostics -->
    <trimStackTrace>false</trimStackTrace>
    <printSummary>true</printSummary>
    <redirectTestOutputToFile>true</redirectTestOutputToFile>
    <!-- make failures reproducible -->
    <forkCount>1</forkCount>
    <reuseForks>false</reuseForks>
  </configuration>
</plugin>

<!-- JUnit 5 (adjust version if your org pins it) -->
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.10.2</version>
  <scope>test</scope>
</dependency>

<!-- If you still have JUnit 4 tests, add the vintage engine too -->
<dependency>
  <groupId>org.junit.vintage</groupId>
  <artifactId>junit-vintage-engine</artifactId>
  <version>5.10.2</version>
  <scope>test</scope>
</dependency>

If your project is JUnit 4 only, remove the two JUnit-5 deps above and keep just:

<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>4.13.2</version>
  <scope>test</scope>
</dependency>

Run with strong diagnostics (locally or in your ADO pipeline)

mvn -e -X -DtrimStackTrace=false -Dsurefire.printSummary=true \
    -Dsurefire.useFile=true -DforkCount=1 -DreuseForks=false \
    -DtestFailureIgnore=false test

Then open: target/surefire-reports/*.dumpstream, *.dump, and *.txt.
These usually contain the real root cause (e.g., NoClassDefFoundError, IncompatibleClassChangeError, native crash, etc.).

Fast triage checklist
	1.	Mixed frameworks?
	•	If you have a mix of JUnit4/5, include vintage (shown above).
	•	If you use TestNG, remove JUnit engines and add TestNG instead.
	2.	Java 17 gotchas
	•	Keep <useModulePath>false</useModulePath> (many libs fail on the module path).
	•	If you use PowerMock or old Mockito inline on JDK 17, upgrade (older versions crash the fork).
	3.	Classpath or static init crash
	•	A failing static initializer (e.g., Spring context, missing env var, logging binding) can kill the fork before tests register → yields this exact “testSet is null”.
	•	Try mvn -q -Dtest=ExactFailingClass test to isolate, or temporarily add:

<includes>
  <include>**/SomeTinySmokeTest.java</include>
</includes>

If a tiny smoke test passes, one of your heavier tests (Spring context, DB, etc.) is crashing the fork.

	4.	Memory / native issues in CI
	•	In Azure DevOps, set a bigger heap for forks:
MAVEN_OPTS: "-Xmx1024m -XX:+ExitOnOutOfMemoryError"
	•	Also try no forking to confirm: mvn test -Dsurefire.forkCount=0.
	5.	Surefire/Jupiter alignment
	•	Keep Surefire ≥ 3.1.x with JUnit 5.
	•	Remove any old maven-surefire-plugin:2.x overrides in submodules.

ADO pipeline snippet (helps you capture dumps)

- script: |
    mvn -B -e -X test \
      -DtrimStackTrace=false -Dsurefire.printSummary=true \
      -DforkCount=1 -DreuseForks=false
  displayName: "Run unit tests with diagnostics"

- task: PublishTestResults@2
  inputs:
    testResultsFormat: JUnit
    testResultsFiles: "**/target/surefire-reports/TEST-*.xml"
    mergeTestResults: true
    testRunTitle: "JUnit results"

- task: PublishBuildArtifacts@1
  inputs:
    PathtoPublish: "$(System.DefaultWorkingDirectory)"
    ArtifactName: "surefire-reports"
    publishLocation: "Container"


⸻

If you paste your current test dependencies block (JUnit/TestNG/Mockito/PowerMock/etc.), I’ll pinpoint the minimal set of changes. Otherwise, try the POM snippet first; in most Java 17 + JUnit setups this clears the testSet is null crash quickly.