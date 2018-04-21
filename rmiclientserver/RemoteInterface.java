import java.rmi.Remote; 
import java.rmi.RemoteException;

/** 
 * 	Interface RemoteInterface abstracts methods for actual   
 *    implementation of remote methods.
 *
 *	@author Navid Galt
 *     SWE 622
 * 	Fall 2017
 *
 */

// Creating Remote interface for application 
public interface RemoteInterface extends Remote 
{  
   public String uploadFile(String filenameToUpload, String outputFile) throws RemoteException;
   
   public String downloadFile(String fileToDownload, String outputFile) throws RemoteException;
   
   public String makeDirectory(String serverPath) throws RemoteException;

   public String listDirectory(String serverPath) throws RemoteException;
   
   public String removeDirectory(String serverPath) throws RemoteException;
   
   public String removeFilename(String serverPath) throws RemoteException;
   
   public void shutdown() throws RemoteException;

} 