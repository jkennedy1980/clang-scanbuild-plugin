package jenkins.plugins.clangscanbuild.history;

import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.List;

import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;
import jenkins.plugins.clangscanbuild.reports.GraphPoint;

public class ClangScanBuildHistoryGathererImpl implements ClangScanBuildHistoryGatherer{

	private int numberOfBuildsToGather = 60;
	
	public ClangScanBuildHistoryGathererImpl(){
		super();
	}
	
	public ClangScanBuildHistoryGathererImpl( int numberOfBuildsToGather ){
		this();
		this.numberOfBuildsToGather = numberOfBuildsToGather;
	}
	
	public List<GraphPoint> gatherHistoryDataSet( AbstractBuild<?,?> latestBuild ){
		List<GraphPoint> points = new ArrayList<GraphPoint>();
		if( latestBuild == null ) return points;
		
		int gatheredBuilds = 0;
	    for( AbstractBuild<?,?> build = latestBuild; build != null; build = build.getPreviousBuild() ){
	    	if( gatheredBuilds >= numberOfBuildsToGather ) return points;
	    	
	    	ClangScanBuildAction action = build.getAction( ClangScanBuildAction.class );
	        if( action == null ) continue;
	        
	        points.add( new GraphPoint( build, action.getBugCount() ) );
	    }
		
	    return points;
	}

}
