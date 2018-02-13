package rmiclientserver;

import java.rmi.registry.Registry;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry; 
import java.rmi.server.UnicastRemoteObject; 

/** 
 *   Class Server creates new registry on port 1099 and binds 
 *   object to registry. When Client calls shutdown, server will
 *   unbind name in registry and unexport implementation object.
 * 
 *	@author Navid Galt
 *    SWE 622
 * 	Fall 2017
 *
 */

public class Server extends ImplExample 
{ 
   static ImplExample obj;
   static Hello stub;
   static Registry registry;
   static Server s; 
   
   public static void main(String args[]) 
   { 
	  s = new Server();
	  s.setUpAndBind();
   } 
   
   public Server()
   {
	   super(s);
	   obj = new ImplExample(this); 
   }
   
   public void setUpAndBind()
   {

       try 
       {
    	   	// Exporting the object of implementation class  
	        // (here we are exporting the remote object to the stub) 
			stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);
			
			// Binding the remote object (stub) in the registry 
			registry = LocateRegistry.getRegistry(); 
	       
	 
	       
			System.err.println("Server ready");
	   }
       catch (RemoteException e)
       {
    	   e.printStackTrace();
	   }  
       try 
       {
    	   registry.bind("Interface", stub);
	   }
       catch (RemoteException | AlreadyBoundException e) 
       {
    	   e.printStackTrace();
       } 
      
   }
   public static void stopServer()
   {
	    try
	    {
			UnicastRemoteObject.unexportObject(stub, true);
		}
	    catch (NoSuchObjectException e) 
	    {
			e.printStackTrace();
		}
	   
		try 
		{
			registry.unbind("Interface");
		} 
		catch (RemoteException | NotBoundException e) 
		{
			e.printStackTrace();
		}      
   }
} 
