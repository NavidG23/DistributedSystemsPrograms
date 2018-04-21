import java.rmi.registry.Registry;
import java.rmi.AccessException;
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

public class Server extends ServerImplementation 
{ 
  
   static ServerImplementation obj;
   static RemoteInterface stub;
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
	   obj = new ServerImplementation(this); 
   }
   
   public void setUpAndBind()
   {
	   // Exporting the object of implementation class  
       // (here we are exporting the remote object to the stub) 
       try 
       {
            stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 1099);

            // Binding the remote object (stub) in the registry 
            registry = LocateRegistry.createRegistry(1099); 

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
			UnicastRemoteObject.unexportObject(obj, true);
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
