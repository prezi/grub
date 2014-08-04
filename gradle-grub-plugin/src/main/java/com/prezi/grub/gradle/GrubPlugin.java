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

	public static final String GENERATE_TASK = "generate";
	public static final String DEFAULT_PROCESS_FILES_TASK = "processDefaultTemplateFiles";
	public static final String TEMPLATE_PROPERTY = "template";
	public static final String DEFAULT_TEMPLATE_LOCATION = "src/main/grub";

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

		ProcessFiles processDefaultTemplateFiles = project.getTasks().create(DEFAULT_PROCESS_FILES_TASK, ProcessFiles.class);
		processDefaultTemplateFiles.setTemplateDirectory(project.file(project.property(TEMPLATE_PROPERTY) + "/" + DEFAULT_TEMPLATE_LOCATION));
		processDefaultTemplateFiles.setTargetDirectory(project.getProjectDir());

		Task generateTask = project.getTasks().create(GENERATE_TASK);
		generateTask.dependsOn(processDefaultTemplateFiles);
	}
}
