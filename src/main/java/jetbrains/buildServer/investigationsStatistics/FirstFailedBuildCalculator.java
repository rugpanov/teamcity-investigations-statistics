package jetbrains.buildServer.investigationsStatistics;

import jetbrains.buildServer.investigationsStatistics.common.Utils;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.impl.problems.BuildProblemImpl;
import jetbrains.buildServer.serverSide.problems.BuildProblem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FirstFailedBuildCalculator {

  @Nullable
  public static SFinishedBuild getFirstFailedBuild(@NotNull SBuildType buildType, @NotNull BuildProblem buildProblemForStatistics) {
    if (isNewProblem(buildProblemForStatistics)) {
      return Utils.getLastFinishedBuild(buildType);
    }

    SFinishedBuild previousBuild = null;
    for (SFinishedBuild finishedBuild : buildType.getHistoryFull(true)) {
      if (!isDefaultBranch(finishedBuild)) {
        continue;
      }

      boolean alreadyFixed = true;
      for (BuildProblem problemInBuild : Utils.getBuildProblems(finishedBuild)) {
        if (problemInBuild.getId() != buildProblemForStatistics.getId()) {
          continue;
        }

        alreadyFixed = false;
        if (isNewProblem(problemInBuild)) {
          return finishedBuild;
        }
      }

      if (alreadyFixed) {
        return previousBuild;
      }

      previousBuild = finishedBuild;
    }

    return null;
  }

  private static boolean isNewProblem(BuildProblem problemInBuild) {
    BuildProblemImpl buildProblemImpl = (BuildProblemImpl) problemInBuild;

    @Nullable
    Boolean isNew = buildProblemImpl.isNew();
    return isNew != null && isNew;
  }

  private static boolean isDefaultBranch(SFinishedBuild finishedBuild) {
    @Nullable
    Branch branch = finishedBuild.getBranch();
    return branch == null || branch.isDefaultBranch();
  }
}
