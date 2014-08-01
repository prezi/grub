package com.prezi.grub;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.prezi.grub.commands.GenerateCommand;
import com.prezi.grub.commands.VersionCommand;
import io.airlift.command.Cli;
import io.airlift.command.Help;
import io.airlift.command.ParseException;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class GrubCli {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GrubCli.class);

	@SuppressWarnings("unchecked")
	public static void main(String... args) {
		Cli.CliBuilder<Callable<?>> builder = Cli.builder("grub");
		builder
				.withDescription("generates projects from templates")
				.withDefaultCommand(Help.class)
				.withCommands(
						GenerateCommand.class,
						VersionCommand.class,
						Help.class
				);

		Cli<Callable<?>> parser = builder.build();
		int exitValue;
		try {
			Callable<?> callable = null;
			try {
				callable = parser.parse(args);
			} catch (ParseException e) {
				List<String> argList = Arrays.asList(args);
				if (argList.contains("-v") || argList.contains("--verbose")) {
					throw e;
				}

				logger.error("{}", e.getMessage());
				System.exit(-1);
			}

			boolean verbose = false;
			try {
				if (callable instanceof GenerateCommand) {
					GenerateCommand command = (GenerateCommand) callable;
					Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
					if (command.isVerbose()) {
						rootLogger.setLevel(Level.DEBUG);
						verbose = true;
					} else if (command.isQuiet()) {
						rootLogger.setLevel(Level.WARN);
					}
				}
				Object result = callable.call();
				if (result instanceof Integer) {
					exitValue = (Integer) result;
				} else {
					exitValue = 0;
				}
			} catch (GrubException e) {
				if (verbose) {
					throw e;
				}

				logGrubExceptions(e);
				exitValue = -1;
			}
		} catch (Exception e) {
			logger.error("Exception:", e);
			exitValue = -1;
		}
		System.exit(exitValue);
	}

	private static void logGrubExceptions(Throwable t) {
		if (t != null) {
			logGrubExceptions(t.getCause());
			if (t instanceof GrubException) {
				logger.error("{}", t.getMessage());
			}
		}
	}
}
