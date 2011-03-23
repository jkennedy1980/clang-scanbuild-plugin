package jenkins.plugins.clangscanbuild.reports;

import hudson.model.AbstractBuild;

public class GraphPoint {

	private AbstractBuild<?,?> build;
	private int bugCount;
	
	public GraphPoint(AbstractBuild<?, ?> build, int bugCount) {
		super();
		this.build = build;
		this.bugCount = bugCount;
	}
	
	public AbstractBuild<?, ?> getBuild() {
		return build;
	}
	public void setBuild(AbstractBuild<?, ?> build) {
		this.build = build;
	}
	public int getBugCount() {
		return bugCount;
	}
	public void setBugCount(int bugCount) {
		this.bugCount = bugCount;
	}

}
