package jenkins.plugins.clangscanbuild.publisher;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;


public class ClangScanBuildPublisherDescriptor extends BuildStepDescriptor<Publisher>{

	public ClangScanBuildPublisherDescriptor(){
		super( ClangScanBuildPublisher.class );
		load();
	}
	
	@Override
	public Publisher newInstance(StaplerRequest arg0, JSONObject json ) throws hudson.model.Descriptor.FormException {

		boolean markBuildUnstable = false;
		int unstableBugThreshold = 0;
		
		JSONObject unstableWhenThresholdExceeded = json.optJSONObject( "unstableWhenThresholdExceeded" );
		if( unstableWhenThresholdExceeded != null ){
			markBuildUnstable = true;
			unstableBugThreshold = unstableWhenThresholdExceeded.getInt( "unstableBugThreshold" );
		}
		
		return new ClangScanBuildPublisher(  markBuildUnstable, unstableBugThreshold );
	}
	
	@Override
	public String getDisplayName() {
		return "Publish Clang Scan-Build Results";
	}

	@Override
	public boolean isApplicable( @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType ){
		if( !FreeStyleProject.class.isAssignableFrom( jobType ) ){
			System.err.println( "Clang scan-build ERROR: Expected FreeStyleProject but was: " + jobType + " at Publisher Descriptor" );
		}
		return FreeStyleProject.class.isAssignableFrom( jobType );
	}

}
