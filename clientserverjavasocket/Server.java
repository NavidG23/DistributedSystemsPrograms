package clientserverjavasocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 *  Class Server allows multiple clients to connect and service their commands.
 *  
 *  @author Navid Galt
 *  SWE 622
 * 	Fall 2017
 */

public class Server
{
	public static ServerSocket serverSocket;
	public static Socket socket;
	public static SocketHelper socketHelper;
	public static int portNumber;
	
	public static void main(String[] args) throws IOException
	{		
		serverSocket = null; socket = null;
		Server server = new Server();
		if(args.length < 1)
		{
			System.out.println("Error: server 'start' argument is missing...");
			return;
		}
		if(args[0].equalsIgnoreCase("start"))
		{
			portNumber = Integer.parseInt(args[1]);
			server.createServer(portNumber);
			try
			{
				server.start(portNumber);
			}
			catch(IOException e)
			{
				// creates a new server if it crashes
				server.createServer(portNumber); server.start(portNumber);
			}
		}
	}
	
	public void createServer(int pn)
	{		
		try 
		{
			serverSocket = new ServerSocket(pn);			
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}			
	}
	
	public void start(int port) throws IOException
	{
		System.out.println("server is running at port" + port);
		while(true)
		{
			System.out.println("Waiting for request...");
			try
			{
				socketHelper = new SocketHelper(serverSocket.accept());
				String arg = socketHelper.getArgument();
				while(socketHelper.handleServerInput(arg)) break;
			}
			catch(Exception e)
			{
				System.out.println("Failed to connect "+ e.getMessage());
			}			
		}		
	}	
}