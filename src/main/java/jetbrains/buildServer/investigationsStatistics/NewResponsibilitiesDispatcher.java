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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.investigationsStatistics.common.Constants;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.ResponsibilityFacadeEx;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListenerEventDispatcher;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.users.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

public class NewResponsibilitiesDispatcher {
  private static final Logger LOGGER = Logger.getInstance(NewResponsibilitiesDispatcher.class.getName());
  @NotNull private final ExecutorServices myExecutorServices;
  @NotNull private ResponsibilityFacadeEx myResponsibilityFacade;
  @NotNull private final ProjectManager myProjectManager;

  public NewResponsibilitiesDispatcher(@NotNull final BuildServerListenerEventDispatcher buildServerListenerEventDispatcher,
                                       @NotNull ExecutorServices executorServices,
                                       @NotNull ProjectManager projectManager,
                                       @NotNull final ResponsibilityFacadeEx responsibilityFacade) {
    myProjectManager = projectManager;
    myExecutorServices = executorServices;
    myResponsibilityFacade = responsibilityFacade;

    try {
      myExecutorServices.getLowPriorityExecutorService().submit(this::processExistResponsibilities);
    } catch (RejectedExecutionException e) {
      // server shutdown, do nothing
    }


    buildServerListenerEventDispatcher.addListener(new BuildServerAdapter() {
      @Override
      public void responsibleChanged(@NotNull final SProject project,
                                     @NotNull final Collection<TestName> testNames,
                                     @NotNull final ResponsibilityEntry entry,
                                     final boolean isUserAction) {
        super.responsibleChanged(project, testNames, entry, isUserAction);
        if (isUserAction && shouldBeReportedAsWrong(entry)) {
          // do smt with wrong investigations
        }
      }

      private boolean shouldBeReportedAsWrong(@Nullable final ResponsibilityEntry entry) {
        return entry != null &&
                entry.getReporterUser() != null &&
                (entry.getState() == ResponsibilityEntry.State.GIVEN_UP ||
                        entry.getState() == ResponsibilityEntry.State.TAKEN) &&
                entry.getComment().startsWith(Constants.ASSIGNED_AUTOMATICALLY_DESCRIPTION_PREFIX);
      }

      @Override
      public void responsibleChanged(@NotNull final SProject project,
                                     @NotNull final Collection<BuildProblemInfo> buildProblems,
                                     @Nullable final ResponsibilityEntry entry) {
        super.responsibleChanged(project, buildProblems, entry);
        if (shouldBeReportedAsWrong(entry)) {
          // do smt with wrong investigations
        }
      }
    });
  }

  private void processExistResponsibilities() {
    for (String projectId : myProjectManager.getProjectIds()) {
      Map<User, List<ResponsibilityEntry>> responsibilities = myResponsibilityFacade.getResponsibilitiesMap(projectId);
      // to smt
    }
  }
}