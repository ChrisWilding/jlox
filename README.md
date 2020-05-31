# Lox

## Prerequisites

[Java 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot)

[Maven](https://maven.apache.org/download.cgi)

## Usage

To launch the lox repl execute -

```sh
$ mvn compile
$ mvn exec:java -Dexec.mainClass="dev.wilding.lox.Lox"
```

Lox also accepts a script argument -

```sh
$ mvn compile
$ mvn exec:java -Dexec.mainClass="dev.wilding.lox.Lox" -Dexec.args="example.lox"
```