package com.prezi.grub.commands;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;
import org.gradle.tooling.GradleConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "generate", description = "Generate a new project")
public class GenerateCommand implements Callable<Integer> {
	protected static final Logger logger = LoggerFactory.getLogger(GenerateCommand.class);

	@Option(name = {"-v", "--verbose"},
			description = "Verbose mode")
	private boolean verbose;

	@Option(name = {"-q", "--quiet"},
			description = "Quite mode")
	private boolean quiet;

	@Option(name = {"-d", "--directory"},
			title = "directory",
			description = "Target directory to create the project in")
	private File targetDirectory;

	@Arguments(title = "template",
			description = "URL of the template",
			required = true)
	private String template;

	protected File getTargetDirectory() {
		File directory = targetDirectory;
		return directory != null ? directory : new File(System.getProperty("user.dir"));
	}

	public boolean isVerbose() {
		return verbose;
	}

	public boolean isQuiet() {
		return quiet;
	}

	@Override
	public Integer call() throws Exception {
		GradleConnector connector = GradleConnector.newConnector();
		return 0;
	}
}
