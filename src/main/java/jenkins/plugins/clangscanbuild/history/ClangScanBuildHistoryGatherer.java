package jenkins.plugins.clangscanbuild.history;

import hudson.model.AbstractBuild;

import java.util.List;

public interface ClangScanBuildHistoryGatherer {

	public List<ClangScanBuildBugSummary> gatherHistory( AbstractBuild<?,?> latestBuild );
	
}
