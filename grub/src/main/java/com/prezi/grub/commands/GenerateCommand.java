package com.prezi.grub.commands;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.prezi.grub.GrubException;
import com.prezi.grub.internal.ProcessUtils;
import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;
import org.apache.commons.io.FileUtils;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "generate", description = "Generate a new project")
public class GenerateCommand implements Callable<Integer> {
	protected static final Logger logger = LoggerFactory.getLogger(GenerateCommand.class);
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

	@Option(name = {"--local"},
			description = "The template is a local directory, not a Git repository")
	private boolean local;

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

		File templateDirectory = Files.createTempDir();
		try {
			if (local) {
				copyTemplate(templateDirectory);
			} else {
				cloneTemplate(templateDirectory);
			}

			File grubFile = new File(templateDirectory, GRUB_FILE);
			if (!grubFile.isFile()) {
				throw new GrubException("Cannot find 'template.grub' in template " + template);
			}

			// Adding init.grub
			File initGrub = new File(templateDirectory, INIT_GRUB);
			Resources.asByteSource(Resources.getResource(INIT_GRUB)).copyTo(Files.asByteSink(initGrub));

			FileUtils.deleteDirectory(targetDirectory);
			FileUtils.forceMkdir(targetDirectory);

			Map<String, Object> parameters = Maps.newLinkedHashMap();
			parameters.put("template", templateDirectory.getAbsolutePath());

			// Add prefix to template.grub
			File processedGrubFile = new File(targetDirectory, GRUB_FILE);
			if (processedGrubFile.exists()) {
				if (!force) {
					throw new GrubException("Grub file already exists in target directory: " + processedGrubFile);
				} else {
					FileUtils.forceDelete(processedGrubFile);
				}
			}
			CharSink processedGrubSink = Files.asCharSink(processedGrubFile, Charsets.UTF_8, FileWriteMode.APPEND);
			processedGrubSink.write("apply plugin: 'grub'; import com.prezi.grub.gradle.*;");
			Files.asCharSource(grubFile, Charsets.UTF_8).copyTo(processedGrubSink);

			logger.info("Generating project in {}", targetDirectory.getAbsolutePath());
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
				args.add("--build-file", processedGrubFile.getAbsolutePath());
				for (Map.Entry<String, Object> property : parameters.entrySet()) {
					args.add("-P" + property.getKey() + "=" + property.getValue());
				}

				ImmutableList<String> arguments = args.build();
				connection.newBuild()
						.withArguments(arguments.toArray(new String[arguments.size()]))
						.setStandardInput(System.in)
						.forTasks("generate")
						.run();
			} finally {
				connection.close();
			}

			if (!debug) {
				FileUtils.deleteQuietly(processedGrubFile);
			}
		} finally {
			if (!debug) {
				FileUtils.deleteDirectory(templateDirectory);
			} else {
				logger.debug("Leaving template directory untouched: {}", templateDirectory);
			}
		}
		return 0;
	}

	private void cloneTemplate(File target) throws IOException {
		logger.info("Cloning template from {}", template);
		ImmutableList.Builder<String> cloneBuilder = ImmutableList.builder();
		cloneBuilder.add("git", "clone");
		if (verbose) {
			cloneBuilder.add("--verbose");
		} else {
			cloneBuilder.add("--quiet");
		}
		cloneBuilder.add(template, target.getAbsolutePath());
		ProcessUtils.executeIn(new File(System.getProperty("user.dir")), cloneBuilder.build());
	}

	private void copyTemplate(File target) throws IOException {
		File source = new File(template);
		logger.info("Copying template from {}", source.getAbsolutePath());
		FileUtils.copyDirectory(source, target);
	}
}
