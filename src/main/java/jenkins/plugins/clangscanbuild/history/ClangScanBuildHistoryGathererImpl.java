package jenkins.plugins.clangscanbuild.history;

import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.List;

import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;

public class ClangScanBuildHistoryGathererImpl implements ClangScanBuildHistoryGatherer{

	private int numberOfBuildsToGather = 60;
	
	public ClangScanBuildHistoryGathererImpl(){
		super();
	}
	
	public ClangScanBuildHistoryGathererImpl( int numberOfBuildsToGather ){
		this();
		this.numberOfBuildsToGather = numberOfBuildsToGather;
	}
	
	@Override
	public List<ClangScanBuildBugSummary> gatherHistory( AbstractBuild<?, ?> latestBuild ){
		List<ClangScanBuildBugSummary> summaries = new ArrayList<ClangScanBuildBugSummary>();
		if( latestBuild == null ) return summaries;
		
		int gatheredBuilds = 0;
	    for( AbstractBuild<?,?> build = latestBuild; build != null; build = build.getPreviousBuild() ){
	    	if( gatheredBuilds >= numberOfBuildsToGather ) return summaries;
	    	
	    	ClangScanBuildAction action = build.getAction( ClangScanBuildAction.class );
	        if( action == null ) continue;
	        
	        ClangScanBuildBugSummary summary = action.getBugSummary();
	        if( summary != null ){
	        	summaries.add( summary );
	        	gatheredBuilds++;
	        }
	    }
		
		return summaries;
	}

}
