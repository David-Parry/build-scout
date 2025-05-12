package com.davidparry.scout.common;

import java.io.File;

public interface GradleTasks {

    BuildOutput buildGradleProject(File projectDir);

    String formatOutput(BuildOutput buildOutput);
}
