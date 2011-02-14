package jenkins.plugins.clangscanbuild.commands;

public interface Command {
	
	public int execute( BuildContext context ) throws Exception;

}
