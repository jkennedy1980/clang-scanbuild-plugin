package jenkins.plugins.clangscanbuild.history;

import hudson.model.AbstractBuild;

import java.util.List;

import jenkins.plugins.clangscanbuild.reports.GraphPoint;

public interface ClangScanBuildHistoryGatherer {

	public List<GraphPoint> gatherHistoryDataSet( AbstractBuild<?,?> latestBuild );
	
}
