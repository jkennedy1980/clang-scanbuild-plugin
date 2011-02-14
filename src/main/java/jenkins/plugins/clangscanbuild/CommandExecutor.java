package jenkins.plugins.clangscanbuild;

import java.util.ArrayList;
import java.util.List;

import jenkins.plugins.clangscanbuild.commands.BuildContext;
import jenkins.plugins.clangscanbuild.commands.Command;

public class CommandExecutor {
	
	public static final int SUCCESS = 0;
	public static final int FAILURE = 1;
	
	private List<Command> commands = new ArrayList<Command>();
	
	private CommandExecutor( Command command ){
		commands.add( command );
	}
	
	public static CommandExecutor execute( Command command ){
		return new CommandExecutor( command );
	}
	
	public CommandExecutor and( Command command ){
		commands.add( command );
		return this;
	}
	
	public int withContext( BuildContext context ){
		for( Command command : commands ){
			try{
				return command.execute( context );
			}catch( Exception e ){
				context.log( "Error executing command: " + command + "\n" + e.getMessage() );
			}
		}
		return FAILURE;
	}
	
}
