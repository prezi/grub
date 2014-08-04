Grub
====

Grub is the [Gradle](http://gradle.org)-based universal bootstrapper. It helps you set up new projects based on templates.

[![Build Status](https://travis-ci.org/prezi/grub.svg?branch=master)](https://travis-ci.org/prezi/grub)

## Installation

For now you need to build Grub from source. It's pretty easy:

```text
$ git clone https://github.com/prezi/grub.git
$ ./gradlew install
$ export PATH=`pwd`/grub/grub/build/install/grub/bin:$PATH
```


## Usage

Grub has a Git-like help system. You can run `grub help` or `grub help <command>` to get more information about how things work.

## Generating a new project from a template

	grub generate <template> [-d target-directory]

Where `<template>` is a URL to a Git repository containing the template itself. Grub will use Git from your PATH to clone this repository.

You can find a nice example in the [`example`](example) directory.


## Creating your own template

Grub templates are pretty simple if you already know Gradle (because they are actually Gradle build files), but not too hard if you are not already familiar with it.

Each template can define a set of parameters. When the user wants to generate a new project from the template, Grub will prompt the user to give values to these parameters. Once all parameters are filled, it will copy the files in the template, injecting the parameters into file contents, and file and directory names alike. Grub uses the Groovy [GStringTemplateEngine](http://groovy.codehaus.org/Groovy+Templates#GroovyTemplates-GStringTemplateEngine).

A template has a directory structure like this:

```text
template/
+- src/
|  +- main/
|  |  +- grub/
|  |     +- <template files>
|  |
|  +- .grubverbatim
|
+ template.grub
```

The `src/main/grub` directory holds the actual files that are going to be processed to make up the project itself. File contents are treated as [Groovy templates](http://groovy.codehaus.org/Groovy+Templates#GroovyTemplates-GStringTemplateEngine), so you can use the template's parameters in them:

```java
public class $className {
	// ...
}
```

will generate the following file, provided `className` is "HelloWorld":

```java
public class HelloWorld {
	// ...
}
```

File names are also treated as Groovy templates, so if you have a file called `${className}.java` in your template, it will generate a file called `HelloWorld.java`. Slashes ('/') in parameters will generate the corresponding directory structure, i.e. if `packageDir` is `com/example/grub`, a template directory called `$packageDir` will expand to `com/example/grub`.

If you want to protect some of your files from being processed, you can place a `.grubverbatim` file in `src/main/grub` with [glob](http://unixhelp.ed.ac.uk/CGI/man-cgi?glob+7) syntax. 

The `template.grub` file describes how your template is to be processed. It contains two main parts:

* parameters to prompt the user for
* Gradle tasks to process the template

Here's an example grub-file:

```groovy
parameters {
	packageName {
		description = "The name of the package"
		defaultValue = "com.example.grub"
	}
	packageDir {
		value = { packageName.replaceAll(/\./, '/') }
	}
	what {
		description = "What shall we greet?"
		defaultValue = "World";
	}
	gradleVersion {
		description = "The Gradle wrapper version"
		defaultValue = "2.0"
	}
}

generate {
	doLast {
		// The user will want Git
		exec { commandLine "git", "init" }

		// Generate the wrapper when we are done
		exec { commandLine "gradle", "wrapper" }

		// Show some message
		println "Greeter for ${what} generated in ${projectDir}"
	}
}
```

In the `parameters { ... }` section you can define parameters that will be queried by Grub.

* `description` -- will be displayed before asking the user for the value of the parameter
* `defaultValue` -- the user can accept this value by pressing return, or give a different value
* `value` -- the user won't be prompted for a value, but the one given here will be used (useful for calculating values from other parameters)

For both `value` and `defaultValue` you can supply constant values. If you want to refer to a previously defined parameter, you need to wrap it in a closure (`{ ... }`).

The basic template processing is pretty powerful, but if you want to do more, you can leverage Gradle's power by attaching extra tasks and actions to the `generate` task. In the example above we run two commands, one to set up a Git repository, the other to generate a Gradle wrapper for the newly created project. You can also refer to the template's parameters in here with the `${parameter}` notation.

## License

Grub is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## Thanks

* Grub borrows many ideas from [giter8](https://github.com/n8han/giter8), and tries to implement them in a more flexible way with Groovy.
* The great [Airline](https://github.com/airlift/airline) command-line library makes it easy to extend the command-line interface of Grub.
