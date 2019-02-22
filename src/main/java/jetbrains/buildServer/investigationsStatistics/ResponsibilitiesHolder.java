package jetbrains.buildServer.investigationsStatistics;

import jetbrains.buildServer.responsibility.BuildProblemResponsibilityEntry;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.problems.BuildProblem;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ResponsibilitiesHolder {
  private Map<SProject, Map<SBuildType, List<Entry>>> myHolder = new HashMap<>();

  @NotNull
  public List<Entry> get(@NotNull SProject project, @NotNull SBuildType buildType) {
    return myHolder.computeIfAbsent(project, devNull -> Collections.emptyMap()).getOrDefault(buildType, Collections.emptyList());
  }

  void add(@NotNull SProject project,
           @NotNull SBuildType buildType,
           @NotNull SBuild firstFailedIn,
           @NotNull TestNameResponsibilityEntry testNameResponsibilityEntry,
           @NotNull STestRun testRun) {
    Map<SBuildType, List<Entry>> map = myHolder.computeIfAbsent(project, devNull -> new HashMap<>());
    map.computeIfAbsent(buildType, devNull -> new ArrayList<>()).add(new TestRunEntry(firstFailedIn, testNameResponsibilityEntry, testRun));
  }

  void add(@NotNull SProject project,
           @NotNull SBuildType buildType,
           @NotNull SBuild firstFailedIn,
           @NotNull BuildProblemResponsibilityEntry buildProblemResponsibilityEntry,
           @NotNull BuildProblem buildProblem) {
    Map<SBuildType, List<Entry>> map = myHolder.computeIfAbsent(project, devNull -> new HashMap<>());
    map.computeIfAbsent(buildType, devNull -> new ArrayList<>()).add(new BuildProblemEntry(firstFailedIn, buildProblemResponsibilityEntry, buildProblem));
  }

  abstract class Entry {
    @NotNull
    final SBuild build;
    @NotNull
    final ResponsibilityEntry responsibility;

    Entry(@NotNull SBuild build,
          @NotNull ResponsibilityEntry responsibility) {
      this.build = build;
      this.responsibility = responsibility;
    }
  }

  class TestRunEntry extends Entry {
    @NotNull
    final STestRun testRun;

    TestRunEntry(@NotNull SBuild build,
                 @NotNull TestNameResponsibilityEntry responsibility,
                 @NotNull STestRun testRun) {
      super(build, responsibility);
      this.testRun = testRun;
    }
  }

  class BuildProblemEntry extends Entry {
    @NotNull
    final BuildProblem buildProblem;

    BuildProblemEntry(@NotNull SBuild build,
                      @NotNull BuildProblemResponsibilityEntry responsibility,
                      @NotNull BuildProblem buildProblem) {
      super(build, responsibility);
      this.buildProblem = buildProblem;
    }
  }
}
