/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.investigationsStatistics;

import jetbrains.buildServer.investigationsStatistics.common.Utils;
import jetbrains.buildServer.responsibility.BuildProblemResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.serverSide.problems.BuildProblem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

public class NewResponsibilitiesDispatcher {
  private ResponsibilitiesHolder myHolder = new ResponsibilitiesHolder();
  private final InvestigationsManager myInvestigationsManager;
  @NotNull
  private final ProjectManager myProjectManager;

  public NewResponsibilitiesDispatcher(@NotNull final ExecutorServices executorServices,
                                       @NotNull final ProjectManager projectManager,
                                       @NotNull final InvestigationsManager investigationsManager) {
    myProjectManager = projectManager;
    myInvestigationsManager = investigationsManager;
    try {
      executorServices.getLowPriorityExecutorService().submit(this::processExistResponsibilities);
    } catch (RejectedExecutionException e) {
      // server shutdown, do nothing
    }
  }

  private void processExistResponsibilities() {
    for (SProject project : myProjectManager.getProjects()) {
      for (SBuildType buildType : project.getBuildTypes()) {
        SFinishedBuild lastFinishedBuild = Utils.getLastFinishedBuild(buildType);
        for (STestRun testRun : getAllFailedTests(lastFinishedBuild)) {
          processFailedTestRun(project, buildType, testRun);
        }

        for (BuildProblem buildProblem : Utils.getBuildProblems(lastFinishedBuild)) {
          processBuildProblem(project, buildType, buildProblem);
        }
      }
    }
  }


  @NotNull
  private List<STestRun> getAllFailedTests(@Nullable SFinishedBuild lastFinishedBuild) {
    if (lastFinishedBuild == null) {
      return Collections.emptyList();
    }

    BuildStatisticsOptions options =
            new BuildStatisticsOptions(BuildStatisticsOptions.FIRST_FAILED_IN_BUILD |
                    BuildStatisticsOptions.COMPILATION_ERRORS |
                    BuildStatisticsOptions.IGNORED_TESTS,
                    -1);
    return lastFinishedBuild.getBuildStatistics(options).getFailedTestsIncludingMuted();
  }

  private void processFailedTestRun(SProject project, SBuildType buildType, STestRun testRun) {
    @Nullable TestNameResponsibilityEntry responsibility =
            myInvestigationsManager.getInvestigation(project, testRun.getTest());
    if (responsibility != null) {
      @Nullable SBuild firstFailedIn = testRun.getFirstFailed();
      if (firstFailedIn != null) {
        myHolder.add(project, buildType, firstFailedIn, responsibility, testRun);
      }
    }
  }


  private void processBuildProblem(@NotNull SProject project,
                                   @NotNull SBuildType buildType,
                                   @NotNull BuildProblem buildProblem) {
    @Nullable
    BuildProblemResponsibilityEntry responsibility = myInvestigationsManager.getInvestigation(project, buildProblem);
    if (responsibility != null) {
      @Nullable SFinishedBuild firstFailedIn = FirstFailedBuildCalculator.getFirstFailedBuild(buildType, buildProblem);
      if (firstFailedIn != null) {
        myHolder.add(project, buildType, firstFailedIn, responsibility, buildProblem);
      }
    }
  }
}