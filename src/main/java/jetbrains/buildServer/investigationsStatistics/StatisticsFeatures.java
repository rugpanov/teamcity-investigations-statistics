package jetbrains.buildServer.investigationsStatistics;

import jetbrains.buildServer.BuildProblemTypes;
import jetbrains.buildServer.responsibility.BuildProblemResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.problems.BuildLogCompileErrorCollector;
import jetbrains.buildServer.serverSide.problems.BuildProblem;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.serverSide.impl.problems.types.CompilationErrorTypeDetailsProvider.COMPILE_BLOCK_INDEX;

class StatisticsFeatures {
  @NotNull
  final User username;
  @NotNull
  final Map<String, String> features = new HashMap<>();

  public StatisticsFeatures(@NotNull ResponsibilitiesHolder.Entry statisticsEntry) {
    username = statisticsEntry.responsibility.getResponsibleUser();
    features.put("responsibilityComment", statisticsEntry.responsibility.getComment());

    if (statisticsEntry instanceof ResponsibilitiesHolder.TestRunEntry) {
      init((ResponsibilitiesHolder.TestRunEntry) statisticsEntry);
    } else {
      init((ResponsibilitiesHolder.BuildProblemEntry) statisticsEntry);
    }
  }

  private void init(ResponsibilitiesHolder.TestRunEntry statisticsEntry) {
    features.put("stacktrace", statisticsEntry.testRun.getFullText());

    putBuildMetaInfo(statisticsEntry.build);
  }

  private void init(ResponsibilitiesHolder.BuildProblemEntry statisticsEntry) {
    String buildProblemDescription = ((BuildProblemResponsibilityEntry)statisticsEntry.responsibility).getBuildProblemInfo().getBuildProblemDescription();
    if (buildProblemDescription != null) {
      features.put("buildProblemDescription", buildProblemDescription);
    }

    String buildProblemData = getBuildProblemText(statisticsEntry.buildProblem, statisticsEntry.build);
    if (buildProblemData != null) {
      features.put("buildProblemData", buildProblemData);
    }

    putBuildMetaInfo(statisticsEntry.build);
  }


  private void putBuildMetaInfo(SBuild build) {
    // do smt
  }

  @Nullable
  private String getBuildProblemText(@NotNull final BuildProblem problem, @NotNull final SBuild build) {
    StringBuilder problemSpecificText = new StringBuilder();

    if (problem.getBuildProblemData().getType().equals(BuildProblemTypes.TC_COMPILATION_ERROR_TYPE)) {
      final Integer compileBlockIndex = getCompileBlockIndex(problem);
      if (compileBlockIndex != null) {
        final List<LogMessage> errors =
                new BuildLogCompileErrorCollector().collectCompileErrors(compileBlockIndex, build);
        for (LogMessage error : errors) {
          problemSpecificText.append(error.getText()).append(" ");
        }
      }
    }

    return problemSpecificText.length() != 0 ? problemSpecificText.toString() : null;
  }

  @Nullable
  private static Integer getCompileBlockIndex(@NotNull final BuildProblem problem) {
    final String compilationBlockIndex = problem.getBuildProblemData().getAdditionalData();
    if (compilationBlockIndex == null) return null;

    try {
      return Integer.parseInt(
              StringUtil.stringToProperties(compilationBlockIndex, StringUtil.STD_ESCAPER2).get(COMPILE_BLOCK_INDEX));
    } catch (Exception e) {
      return null;
    }
  }
}
