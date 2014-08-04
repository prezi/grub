package com.prezi.grub.gradle.internal;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

public class GlobsUtils {
	public static Collection<Pattern> readGlobs(File globsFile) throws IOException {
		if (globsFile == null || !globsFile.exists()) {
			return Collections.emptySet();
		}
		Set<Pattern> patterns = Sets.newLinkedHashSet();
		for (String line : Files.readLines(globsFile, Charsets.UTF_8)) {
			line = line.trim();
			if (line.length() == 0 || line.startsWith("#")) {
				continue;
			}
			String pattern;
			if (File.separatorChar == '\\') {
				pattern = Globs.toWindowsRegexPattern(line);
			} else {
				pattern = Globs.toUnixRegexPattern(line);
			}
			patterns.add(Pattern.compile(pattern));
		}
		return patterns;
	}


}
