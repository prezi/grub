Grub example
============

This example generates an basic Java application built with Gradle.

You can generate the example project by:

	grub generate --local example --directory my-project

Once it's generated, you can test it via:

```text
$ cd my-project
$ ./gradlew run
```

Transcript:

```text
$ grub generate --local example -d build/test-example -f 
Copying template from /Users/lptr/Workspace/grub/example
Generating project in /Users/lptr/Workspace/grub/build/my-project
03:27:11.294 [Connection worker] DEBUG o.g.t.i.provider.DefaultConnection - Tooling API provider 2.0 created.
03:27:11.445 [Connection worker] DEBUG o.g.t.i.provider.ProviderConnection - Configuring logging to level: INFO
Tooling API is using target Gradle version: 2.0.
Connected to the daemon. Dispatching Build{id=df6107a0-038e-47fa-bd28-0e57e229302d.1, currentDir=/Users/lptr/Workspace/grub} request.
The name of the package
packageName (required) [com.example.grub]: 
What shall we greet?
what (required) [World]: 
The Gradle wrapper version
gradleVersion (required) [2.0]: 
:wrapper

BUILD SUCCESSFUL

Total time: 1.33 secs

$ cd my-project
$ ./gradlew run
:compileJava
:processResources UP-TO-DATE
:classes
:run
Hello World!

BUILD SUCCESSFUL

Total time: 0.634 secs
```
