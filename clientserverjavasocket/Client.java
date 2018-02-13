package clientserverjavasocket;

import java.io.IOException;
import java.net.Socket;
/**
 *  Class Client connects to Server and allows user to run through options.
 *  
 *  @author Navid Galt
 *  SWE 622
 * 	Fall 2017
 */

public class Client
{
	public static Socket socket;
	public static SocketHelper socketHelper;

	public static void main(String[] args) throws IOException
	{		
		if (args.length < 1) 
        {
            System.out.println("Client command argument is missing, please retry with an argument.");
            return;
        }
        if (System.getenv("PA1_SERVER") == null) 
        {
            System.out.println("PA1_SERVER environment variable not set.");
            return;
        }
        String[] envVariableFields = System.getenv("PA1_SERVER").split(":");
        try 
        {	// Connect to server (computer name, tcp port #)
             socketHelper = new SocketHelper(connectToServer(envVariableFields[0],Integer.parseInt(envVariableFields[1])));
        }
        catch (Exception e) 
        {
            System.out.println("Error: need client file path and server path" + e.getMessage());
            return;
        }     
        //	handle all command line inputs in our helper & close client input
        socketHelper.handleClientInput(args);
        socketHelper.delay(10000);
	}
	
	//connect to server, terminate program if connection fails
	public static Socket connectToServer(String computerName, int tcpPortNumber)
	{
		try 
		{
			return new Socket(computerName, tcpPortNumber);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
}