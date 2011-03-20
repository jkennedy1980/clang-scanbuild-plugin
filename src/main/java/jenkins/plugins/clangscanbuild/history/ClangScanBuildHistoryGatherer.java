package jenkins.plugins.clangscanbuild.history;

import hudson.model.AbstractBuild;

import java.util.Map;

public interface ClangScanBuildHistoryGatherer {

	public Map<Integer,Integer> gatherHistory( AbstractBuild<?,?> latestBuild );
	
}
