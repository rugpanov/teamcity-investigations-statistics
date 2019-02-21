package jetbrains.buildServer.investigationsStatistics.common;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.problems.BuildProblem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Utils {

  @NotNull
  public static List<BuildProblem> getBuildProblems(@Nullable SFinishedBuild finishedBuild) {
    if (finishedBuild == null) {
      return Collections.emptyList();
    }

    final BuildPromotionEx buildPromo = (BuildPromotionEx) finishedBuild.getBuildPromotion();
    return buildPromo.getBuildProblems();
  }

  @Nullable
  public static SFinishedBuild getLastFinishedBuild(SBuildType buildType) {
    BranchEx branch = ((BuildTypeEx) buildType).getBranch(Branch.DEFAULT_BRANCH_NAME);
    return branch.getLastChangesFinished();
  }
}
