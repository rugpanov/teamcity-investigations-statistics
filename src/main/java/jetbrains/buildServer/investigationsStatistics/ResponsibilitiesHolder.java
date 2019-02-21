package jetbrains.buildServer.investigationsStatistics;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ResponsibilitiesHolder {
  private Map<SProject, Map<SBuildType, List<Pair<SBuild, ? extends ResponsibilityEntry>>>> myHolder = new HashMap<>();

  @NotNull
  public List<Pair<SBuild, ? extends ResponsibilityEntry>> get(@NotNull SProject project, @NotNull SBuildType buildType) {
    return myHolder.computeIfAbsent(project, devNull -> Collections.emptyMap()).getOrDefault(buildType, Collections.emptyList());
  }

  void add(@NotNull SProject project,
           @NotNull SBuildType buildType,
           @NotNull SBuild firstFailedIn,
           @NotNull ResponsibilityEntry testNameResponsibilityEntry) {
    Map<SBuildType, List<Pair<SBuild, ? extends ResponsibilityEntry>>> map = myHolder.computeIfAbsent(project, devNull -> new HashMap<>());
    map.computeIfAbsent(buildType, devNull -> new ArrayList()).add(new Pair<>(firstFailedIn, testNameResponsibilityEntry));
  }
}
