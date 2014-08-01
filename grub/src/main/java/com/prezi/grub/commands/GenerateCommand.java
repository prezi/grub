package com.prezi.grub.commands;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.prezi.grub.GrubException;
import com.prezi.grub.internal.ProcessUtils;
import groovy.text.GStringTemplateEngine;
import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.io.FileUtils;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "generate", description = "Generate a new project")
public class GenerateCommand implements Callable<Integer> {
	protected static final Logger logger = LoggerFactory.getLogger(GenerateCommand.class);
	private static final String CONFIG_FILE = "grub.ini";
	public static final String GRUB_FILE = "template.grub";
	private static final String INIT_GRUB = "init.grub";

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

	@Option(name = {"-f", "--force"},
			description = "Overwrite existing target directory")
	private boolean force;

	@Option(name = {"--debug"},
			description = "Turn on debug mode")
	private boolean debug;

	@Arguments(title = "template",
			description = "URL of the template",
			required = true)
	private String template;

	protected File getTargetDirectory() {
		File directory = targetDirectory;
		return directory != null ? directory : new File(System.getProperty("user.dir") + "/" + template);
	}

	public boolean isVerbose() {
		return verbose;
	}

	public boolean isQuiet() {
		return quiet;
	}

	@Override
	public Integer call() throws Exception {
		File targetDirectory = getTargetDirectory();
		if (!force && targetDirectory.exists() && !targetDirectory.equals(new File(System.getProperty("user.dir")))) {
			throw new GrubException("Target directory already exists: " + targetDirectory);
		}

		GStringTemplateEngine engine = new GStringTemplateEngine();
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8));

		File templateDirectory = Files.createTempDir();
		try {
			logger.info("Cloning template");
			ProcessUtils.executeIn(
					new File(System.getProperty("user.dir")),
					Arrays.asList("git", "clone", template, templateDirectory.getPath()));

			File grubFile = new File(templateDirectory, GRUB_FILE);
			if (!grubFile.isFile()) {
				throw new GrubException("Cannot find 'template.grub' in template " + template);
			}

			// Adding init.grub
			File initGrub = new File(templateDirectory, INIT_GRUB);
			Resources.asByteSource(Resources.getResource(INIT_GRUB)).copyTo(Files.asByteSink(initGrub));

			FileUtils.deleteDirectory(targetDirectory);
			FileUtils.forceMkdir(targetDirectory);

			Map<String, String> properties = Maps.newLinkedHashMap();
			properties.put("template", templateDirectory.getAbsolutePath());
			properties.put("target", targetDirectory.getAbsolutePath());

			File configFile = new File(templateDirectory, CONFIG_FILE);
			if (configFile.exists()) {
				logger.debug("Loading configuration from {}", configFile);
				HierarchicalINIConfiguration config = new HierarchicalINIConfiguration(configFile);
				for (String property : config.getSections()) {
					SubnodeConfiguration propertySection = config.getSection(property);

					String title = propertySection.getString("title", property);
					String description = propertySection.getString("description", null);
					boolean required = propertySection.getBoolean("required", true);
					String defaultValue = propertySection.getString("default", null);
					// Process default value
					if (defaultValue != null) {
						defaultValue = engine.createTemplate(defaultValue).make().toString();
					}

					StringBuilder prompt = new StringBuilder();
					if (description != null) {
						prompt.append(description).append(System.lineSeparator());
					}
					prompt.append(title);
					if (required) {
						prompt.append(" (required)");
					}
					if (defaultValue != null) {
						prompt.append(" [").append(defaultValue).append(']');
					}
					prompt.append(": ");

					String value;
					while (true) {
						System.out.print(prompt);
						value = input.readLine();
						if (Strings.isNullOrEmpty(value)) {
							if (defaultValue == null) {
								if (required) {
									System.out.println("Property \"" + property + "\" is required.");
									continue;
								}
							} else {
								value = defaultValue;
							}
						}
						break;
					}
					properties.put(property, value);
				}
			} else {
				logger.debug("No grub configuration file found");
			}

			logger.info("Generating template");
			GradleConnector connector = GradleConnector.newConnector();
			ProjectConnection connection = connector.forProjectDirectory(targetDirectory).connect();
			try {
				ImmutableList.Builder<String> args = ImmutableList.builder();
				if (verbose) {
					args.add("--info");
				} else {
					args.add("--quiet");
				}
				args.add("--init-script", initGrub.getPath());
				args.add("--build-file", grubFile.getPath());
				for (Map.Entry<String, String> property : properties.entrySet()) {
					args.add("-P" + property.getKey() + "=" + property.getValue());
				}

				ImmutableList<String> arguments = args.build();
				connection.newBuild()
						.withArguments(arguments.toArray(new String[arguments.size()]))
						.forTasks("generate")
						.run();
			} finally {
				connection.close();
			}
			FileUtils.deleteQuietly(grubFile);
		} finally {
			if (!debug) {
				FileUtils.deleteDirectory(templateDirectory);
			} else {
				logger.debug("Leaving template directory untouched: {}", templateDirectory);
			}
		}
		return 0;
	}
}
