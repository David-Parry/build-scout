package com.davidparry.scout.common;

import java.io.File;

public interface GradleTasks {

    BuildOutput buildGradleProject(File projectDir, boolean check);

    String formatOutput(BuildOutput buildOutput);
}
