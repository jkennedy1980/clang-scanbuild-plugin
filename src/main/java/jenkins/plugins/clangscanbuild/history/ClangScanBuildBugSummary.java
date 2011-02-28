package jenkins.plugins.clangscanbuild.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClangScanBuildBugSummary {

	private int buildNumber;
	public Set<ClangScanBuildBug> bugs = new HashSet<ClangScanBuildBug>();
	
	public boolean contains( ClangScanBuildBug bug ){
		for( ClangScanBuildBug candidate : bugs ){
			if( bug.getBugDescription().equals( candidate.getBugDescription() ) ) return true;
		}
		return false;
	}
	
	public ClangScanBuildBugSummary( int buildNumber ){
		this.buildNumber = buildNumber;
	}
	
	public boolean add( ClangScanBuildBug bug ){
		return bugs.add( bug );
	}
	
	public int getBugCount(){
		return bugs.size();
	}

	public List<ClangScanBuildBug> getBugs() {
		return new ArrayList<ClangScanBuildBug>( bugs );
	}

	public void addBugs( Collection<ClangScanBuildBug> bugs ) {
		this.bugs.addAll( bugs );
	}

	public int getBuildNumber() {
		return buildNumber;
	}

}
