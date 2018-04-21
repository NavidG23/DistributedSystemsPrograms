import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;


/** 
 *   Class ServerImplementation is the meat of this program and
 *   contains all implementation for RemoteInterface.
 *   This allows the client to access functions to execute on
 *   remote server. 
 * 
 *   @author Navid Galt
 *   SWE 622
 *   Fall 2017
 *
 */

public class ServerImplementation implements RemoteInterface 
{  
    Server sptr;
	
	public ServerImplementation(Server s)
	{
		sptr=s;
	}
    public String uploadFile(String filenameToUpload, String outputFile) throws RemoteException
	{
    	if(readFile(filenameToUpload) == null)
		{
			return "file does not exist at specified location";
		}
    	byte[] full_file = this.getBytes(filenameToUpload);
    	String msg ="";
    	int full_length = full_file.length;
    	
    	byte[] partial_file;
    	int partial_length = 0;
    	
    	boolean fileExists = this.doesFileExist(outputFile);
    	
    	if(fileExists)
    	{
    	    partial_file = this.getBytes(outputFile);
    	    partial_length = partial_file.length;
    	}
    	else
    	{
    	    partial_file = new byte[full_length];
    	    
    	}

    	int offset = full_length - partial_length;    	
    	int out_size = offset;    	    	
    	byte[] full_part = new byte[offset];    	 	
    	
    	for (int i=0; i<offset;i++)
        {
    	    full_part[i] = full_file[(partial_length)+i];
    	}    	
    	
        try 
        {   
           
        	if(full_length == partial_length)
        	{
        		return "files are of same size";
        	}
        	else
        	{
            	FileOutputStream fos = new FileOutputStream(outputFile, fileExists);           
                fos.write(full_part, 0, out_size);
                fos.close();
             
                if(fileExists)
                {
                    msg = "Starting at " + ((float)partial_length/(float)full_length)*100 + "%" + "\nFile Uploaded Successfully";
                }
                else
                {
                    msg = "Creating new file... \nFile Uploaded Successfully";
                }
            }    
        }
        catch(Exception e)
        {
        	System.out.println(e.getMessage());
        }
        return msg;
	}
    
    private byte[] getBytes(String filename)
    {
    	if(this.doesFileExist(filename)==true)
    	{
    		return this.readFile(filename).getBytes();
    	}
    	return null;
    }
    
    private boolean doesFileExist(String filename)
    {
       File file = new File(filename);
       
       if (file.exists())
       {
           return true;
       } 
       return false;
    }
    
    private String readFile(String path)
    {        
        File fileToUpload = new File(path);	
        String result = "";        
        if (!fileToUpload.exists()) 		// Stop if file to upload doesn't exist
        {
            System.out.println(path + " does not exist.");
            return null;
        } 
    	   try(BufferedReader br = new BufferedReader(new FileReader(fileToUpload))) 
    	   {
    	        String line;
    	        while ((line = br.readLine()) != null) 
    	        {
    	            result += line;
    	        }
    	    }
    	    catch(Exception e)
 		{
    		   System.out.println(e.getMessage());
 		}
    	return result;
    }
    
    public String downloadFile(String filenameToDownload, String outputFile) throws RemoteException
	{
		File fileToDownload = new File(filenameToDownload);	      
	    if (!fileToDownload.exists()) 		// Stop if file to upload doesn't exist
	    {
	    	return filenameToDownload + " does not exist on Server machine.";
	    } 
	    byte[] full_file = this.getBytes(filenameToDownload);
    	String msg ="";
    	int full_length = full_file.length;
    	
    	byte[] partial_file;
    	int partial_length = 0;
    	
    	boolean fileExists = this.doesFileExist(outputFile);
    	
    	if(fileExists)
    	{
    	    partial_file = this.getBytes(outputFile);
    	    partial_length = partial_file.length;
    	}
    	else
    	{
    	    partial_file = new byte[full_length];
    	    
    	}

    	int offset = full_length - partial_length;    	
    	int out_size = offset;    	    	
    	byte[] full_part = new byte[offset];    	 	
    	
    	for (int i=0; i<offset;i++)
        {
    	    full_part[i] = full_file[(partial_length)+i];
    	}    	
    	
        try 
        {   
           
        	if(full_length == partial_length)
        	{
        		return "files are of same size";
        	}
        	else
        	{
            	FileOutputStream fos = new FileOutputStream(outputFile, fileExists);           
                fos.write(full_part, 0, out_size);
                fos.close();
             
                if(fileExists)
                {
                    msg = "Starting download at... " + ((float)partial_length/(float)full_length)*100 + "%" + "\nFile Downloaded Successfully";
                }
                else
                {
                    msg = "Downloading new file... \nFile Downloaded Successfully";
                }
            }
        }
        catch(Exception e)
        {
        	System.out.println(e.getMessage());
        }
        return msg;		
	}
	
	public String makeDirectory(String serverPath) throws RemoteException
	{
		File file = new File(serverPath);
		if(file.exists())
		{
			return "Folder already exists in directory";
		}
		file.mkdir();
		return "Folder created";
	}

	public String listDirectory(String serverPath) throws RemoteException
	{
		// Make sure path is a directory
        if (!(new File(serverPath).exists()) || !(new File(serverPath).isDirectory()))
        {
        	String error = "No such directory exists";
            return error;
        }
        
        File folder = new File(serverPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) 
        {
          if (listOfFiles[i].isFile()) 
          {
            System.out.println("File " + listOfFiles[i].getName() + "\n");
          }
          else if (listOfFiles[i].isDirectory())
          {
            System.out.println("Directory " + listOfFiles[i].getName() + "\n");
          }
        }
        return Arrays.toString(listOfFiles);
     }
     
     public String removeDirectory(String serverPath) throws RemoteException
	 {
	        if (!(new File(serverPath).exists()) || !(new File(serverPath).isDirectory())) 
	        {
	        	String error = "No such directory exists";
	            return error;
	        }
	        new File(serverPath).delete();
	        return "Directory successfully deleted";
	 }
     
     public String removeFilename(String fileName) throws RemoteException
	 {
            File file = new File(fileName);
	        if (!(file.exists())) 
	        {
	        	String error = "No such file exists in directory, please retry";
	            return error;
	        }
	        file.delete();
	        return file + " successfully deleted";
	 }
     
    @SuppressWarnings("static-access")
    public void shutdown() throws RemoteException
    {
        sptr.stopServer();
    }
} 
