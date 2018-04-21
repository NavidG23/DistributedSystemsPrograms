import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

/** 
 * 	Class Client retrieves object from registry and uses   
 *   reference of object to call methods from remote location.
 * 
 *	@author Navid Galt
 *     SWE 622
 * 	Fall 2017
 *
 */
public class Client 
{ 
   public static void main(String[] args) 
   {  
      try 
      {                    
         // Getting the registry
         Registry registry = LocateRegistry.getRegistry(null);
         // Looking up the registry for the remote object
         RemoteInterface stub = (RemoteInterface) registry.lookup("Interface"); 
          
        if (args.length < 1) 
        {
             System.out.println("Client command argument is missing, please retry with an argument.");
             return;
        }	 	 
        switch(args[0].toLowerCase())
        {
     	  case "upload": 
                String responseNewUpload = stub.uploadFile(args[1], args[2]);
                System.out.println(responseNewUpload);
                break;              	 
          case "download":
     	        String responseDownloaded = stub.downloadFile(args[1], args[2]);
                System.out.println(responseDownloaded);
     	        break;
     	  case "dir":		 
     	      String answer = stub.listDirectory(args[1]);  
     	      List<String> listOfFiles = Arrays.asList(answer.split(","));
     	      System.out.println("The list of files in the requested directory is... ");
     	      for(int i = 0; i < listOfFiles.size(); i++)
     	      {
     	          System.out.println(listOfFiles.get(i) + "\n");
     	      }
     	      break;
     	  case "mkdir":    
              String responseMkDir = stub.makeDirectory(args[1]); 
              System.out.println(responseMkDir);
              break;
     	  case "rmdir":	   
              String responseRmDir = stub.removeDirectory(args[1]); 
              System.out.println(responseRmDir);
              break;
     	  case "rm":       
              String responseRm = stub.removeFilename(args[1]);
              System.out.println(responseRm);
              break;
     	  case "shutdown":
              System.out.println("Bye bye bye");
              stub.shutdown();
              break;
     	  default:         
              System.out.println("invalid input");        	
          }
      }
      catch (Exception e)
      {
         System.err.println("Client exception: " + e.toString()); 
         e.printStackTrace(); 
      } 
   } 
}