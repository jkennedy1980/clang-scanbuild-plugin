package jenkins.plugins.clangscanbuild.reports;

import hudson.model.ModelObject;
import hudson.model.AbstractBuild;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;

//TODO: have 2 reports...1 for a single build and 1 for a group of builds
public class ClangScanBuildReport implements ModelObject{

	public AbstractBuild<?,?> build;
	public ClangScanBuildBugSummary summary;
	private boolean isFailed;
	
	public ClangScanBuildReport( ClangScanBuildBugSummary summary, AbstractBuild<?,?> build ){
		super();
		this.summary = summary;
		this.build = build;
	}
	
	@Override
	public String getDisplayName() {
		return "Clang Scan-Build Report";
	}

	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed( boolean isFailed ){
		this.isFailed = isFailed;
	}

}
