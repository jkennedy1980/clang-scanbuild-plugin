package jenkins.plugins.clangscanbuild.publisher;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class ClangScanBuildPublisherTest extends HudsonTestCase{

	@Test
	public void test() throws Exception{
		
		ClangScanBuildPublisher publisher = new ClangScanBuildPublisher( "clang-output", 0, true );
		FreeStyleProject project = createFreeStyleProject();
		
		project.getPublishersList().add( publisher );
		
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		
	}
	
}
