package jenkins.plugins.clangscanbuild.history;

import hudson.model.AbstractBuild;

import java.util.HashMap;
import java.util.Map;

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
	public Map<Integer,Integer> gatherHistory( AbstractBuild<?, ?> latestBuild ){
		Map<Integer,Integer> bugCountsByBuildNumber = new HashMap<Integer, Integer>();

		if( latestBuild == null ) return bugCountsByBuildNumber;
		
		int gatheredBuilds = 0;
	    for( AbstractBuild<?,?> build = latestBuild; build != null; build = build.getPreviousBuild() ){
	    	if( gatheredBuilds >= numberOfBuildsToGather ) return bugCountsByBuildNumber;
	    	
	    	ClangScanBuildAction action = build.getAction( ClangScanBuildAction.class );
	        if( action == null ) continue;

            bugCountsByBuildNumber.put( build.getNumber(), action.getBugCount() );
	    }
		
		return bugCountsByBuildNumber;
	}

}
