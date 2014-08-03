package com.prezi.grub.gradle;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.prezi.grub.gradle.config.ParameterContainer;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class GrubPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final ParameterContainer parameters = project.getExtensions().create("parameters", ParameterContainer.class);
		project.afterEvaluate(new Action<Project>() {
			@Override
			public void execute(Project project) {
				ExtraPropertiesExtension extraProperties = project.getExtensions().getExtraProperties();
				Reader reader = new InputStreamReader(System.in, Charsets.UTF_8);
				try {
					Map<String, Object> resolved = parameters.resolve(reader);
					for (Map.Entry<String, Object> entry : resolved.entrySet()) {
						extraProperties.set(entry.getKey(), entry.getValue());
					}
				} catch (IOException e) {
					Throwables.propagate(e);
				}
			}
		});

		ProcessFiles processDefaultTemplateFiles = project.getTasks().create("processDefaultTemplateFiles", ProcessFiles.class);
		processDefaultTemplateFiles.setTemplateDirectory(project.file(project.property("template") + "/src/main/grub"));
		processDefaultTemplateFiles.setTargetDirectory(project.file(project.getProjectDir()));

		Task generateTask = project.getTasks().create("generate");
		generateTask.dependsOn(processDefaultTemplateFiles);
	}
}
