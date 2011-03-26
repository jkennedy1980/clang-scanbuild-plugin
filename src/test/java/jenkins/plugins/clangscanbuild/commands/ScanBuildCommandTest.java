package jenkins.plugins.clangscanbuild.commands;

import hudson.FilePath;
import hudson.util.ArgumentListBuilder;

import java.io.File;

import junit.framework.Assert;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

public class ScanBuildCommandTest{

	private BuildContext context = EasyMock.createMock( BuildContext.class ); 
	
	@Test
	public void onlyRequiredOptionsSet() throws Exception{
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "/OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );

		String actual = buildCommandAndReturn( command );
		
		String expected = "/ScanBuild -k -v -v -o /OutputFolder xcodebuild -activetarget -configuration Debug clean build";
		Assert.assertEquals( expected, actual );
	}
	
	@Test
	public void xcode4WorkspaceSet() throws Exception{
		// XCode 4 workspace/scheme should override unnecessary target
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "/OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setConfig( "myConfig" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setScheme( "myScheme" );
		command.setTarget( "myTarget" );
		command.setTargetSdk( "myTargetSdk" );
		command.setWorkspace( "myWorkspace" );
		
		String actual = buildCommandAndReturn( command );
		
		String expected = "/ScanBuild -k -v -v -o /OutputFolder xcodebuild -workspace myWorkspace -scheme myScheme -configuration myConfig -sdk myTargetSdk clean build";
		Assert.assertEquals( expected, actual );
	}
	
	@Test
	public void xcode3TargetSet() throws Exception{
		// XCode 4 workspace/scheme should override unnecessary target
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "/OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setConfig( "myConfig" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setTarget( "myTarget" );
		command.setTargetSdk( "myTargetSdk" );
		
		String actual = buildCommandAndReturn( command );
		
		String expected = "/ScanBuild -k -v -v -o /OutputFolder xcodebuild -target myTarget -configuration myConfig -sdk myTargetSdk clean build";
		Assert.assertEquals( expected, actual );
	}
	
	private String buildCommandAndReturn( ScanBuildCommand command ) throws Exception{
		context.log( (String) EasyMock.notNull() );
		EasyMock.expectLastCall().anyTimes();
		
		Capture<ArgumentListBuilder> argumentListCapture = new Capture<ArgumentListBuilder>();
		EasyMock.expect( context.waitForProcess( EasyMock.same( command.getProjectDirectory() ), EasyMock.capture( argumentListCapture ) ) ).andReturn( 0 );
		
		EasyMock.replay( context );
		command.execute( context );
		EasyMock.verify( context );
		
		return argumentListCapture.getValue().toStringWithQuote();
	}
	
}
