[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.zregvart/junit-github-reporter/badge.svg?style=flat)](https://search.maven.org/search?q=g:io.github.zregvart%20AND%20a:junit-github-reporter)

# How to use

## Add as a test dependency

For example when using Maven:

```xml
<dependency>
  <groupId>io.github.zregvart</groupId>
  <artifactId>junit-github-reporter</artifactId>
  <version>${junit-github-reporter.version}</version>
  <scope>test</scope>
</dependency>
```

## Add it as an additional classpath element

Strict dependency checks will find the `junit-github-reporter` as unused, then add to Surefire's classpath using this:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <additionalClasspathElements>
      <additionalClasspathElement>${settings.localRepository}/io/github/zregvart/junit-github-reporter/${junit-github-reporter.version}/junit-github-reporter-${junit-github-reporter.version}.jar</additionalClasspathElement>
    </additionalClasspathElements>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>io.github.zregvart</groupId>
      <artifactId>junit-github-reporter</artifactId>
      <version>${junit-github-reporter.version}</version>
    </dependency>
  </dependencies>
</plugin>
```

# Release

To release run:

```shell
$ ./mvnw clean deploy -Pbasepom.oss-release
```
