package com.prezi.grub.commands;

import com.prezi.grub.GrubVersion;
import io.airlift.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

@Command(name = "version", description = "Display program version")
public class VersionCommand implements Callable<Integer> {
	private static final Logger log = LoggerFactory.getLogger(VersionCommand.class);

	@Override
	public Integer call() throws Exception {
		log.info("Grub version " + GrubVersion.VERSION);
		return 0;
	}
}
