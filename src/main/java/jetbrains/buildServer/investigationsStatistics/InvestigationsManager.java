package jetbrains.buildServer.investigationsStatistics;

import jetbrains.buildServer.BuildProject;
import jetbrains.buildServer.responsibility.ResponsibilityFacadeEx;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.audit.AuditLogProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InvestigationsManager {

  @NotNull
  private final AuditLogProvider myAuditLogProvider;
  @NotNull
  private final ResponsibilityFacadeEx myResponsibilityFacade;

  public InvestigationsManager(@NotNull final AuditLogProvider auditLogProvider,
                               @NotNull final ResponsibilityFacadeEx responsibilityFacade) {
    this.myAuditLogProvider = auditLogProvider;
    myResponsibilityFacade = responsibilityFacade;
  }

//  @Nullable
//  public BuildProblemResponsibilityEntry getInvestigation(@NotNull final SProject project,
//                                         @NotNull final BuildProblem problem) {
//    for (BuildProblemResponsibilityEntry entry : problem.getAllResponsibilities()) {
//      if (belongSameProjectOrParent(entry.getProject(), project)) return entry;
//    }
//    return null;
//  }

  @Nullable
  TestNameResponsibilityEntry getInvestigation(@NotNull final SProject project,
                                               @NotNull final STest test) {
    for (TestNameResponsibilityEntry entry : test.getAllResponsibilities()) {
      if (belongSameProjectOrParent(entry.getProject(), project)) return entry;
    }
    return null;
  }

  private boolean belongSameProjectOrParent(@NotNull final BuildProject parent, @NotNull final BuildProject project) {
    List<String> projectIds = collectProjectHierarchyIds(project);
    return projectIds.contains(parent.getProjectId());
  }

  @NotNull
  private List<String> collectProjectHierarchyIds(@NotNull BuildProject project) {
    List<String> result = new ArrayList<>();
    do {
      result.add(project.getProjectId());
      project = project.getParentProject();
    } while (project != null);
    return result;
  }
}
