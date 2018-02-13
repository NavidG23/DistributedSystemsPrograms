package clientserverjavasocket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/** 
 * 	Class SocketHelper houses most of the workload of the Client and Server communication.
 * 	It only exposes what needs to be shown at the surface level of communication and abstracts 
 * 	the rest to help keep Client and Server class clean and hide important implementation details.
 * 
 *	@author Navid Galt
 *  SWE 622
 * 	Fall 2017
 *
 */
public class SocketHelper 
{	
	private final Object lock = new Object();
	private Socket socket;
	private DataInputStream incomingData;
	private DataOutputStream outgoingData;
	private boolean inUse;	
	public SocketHelper(Socket s) throws IOException
	{		
		this.socket = s;   this.initializeStreams();
	}	
/**	SERVER STUFF
 * -------------------------------------------------------------
 */
	private void doUpload() throws IOException	//	Server performs upload functionality
	{
		String filename = this.getSourceFilename();	
    	String contents = this.readFile(filename);
        long bytesToUpload = this.getSourceFileSize();        

        try 
        {            			                
            long bytesUploaded = (long)contents.length();	
            this.writeCurrentFileSize(bytesUploaded);      // Send to the client the starting point of the upload data
            System.out.println("Recieving " + filename + " from a client..."+
            				   "\nCurrent size -> " + bytesUploaded + "   Size on client  -> " + bytesToUpload);            
            if(bytesUploaded - bytesToUpload == 0)
            {
            	System.out.println("Same size.");
            }
            else 
            {
            	this.writeRemainingBytesToFile(bytesToUpload, filename);
            	System.out.println("Uploaded Successfully");
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
	}	
	//	Server performs download functionality when requested by client
	private void doDownload() throws IOException
	{		
		String fn = this.getSourceFilename();				// read the path of the file that is desired		
		this.writeFileSize(fn);								// write the size on server
		long bytesToDownload = this.getSourceFileSize();	// read the current size on client
		String contents = this.readFile(fn);				// get file contents of server file
		System.out.println("Starting Sending of "+ fn +" ..."+
						   "  Size on client -> " + bytesToDownload + "\nSize on server -> " + contents.length());
		if(!isFileChanged((long)this.readFile(fn).length(), bytesToDownload))
        {
        	System.out.println("The file has not changed.");
        }       
		else
        {
        	System.out.print("The file has changed: Resuming download at " + 
        					 ((float)bytesToDownload/(float)contents.length())*100 + "% (of total bytes)");
        	this.writeData(contents.substring((int)bytesToDownload, contents.length())); 
        }        
		this.haltOutboundStream();  this.haltInboundStream();	// stop streams		
        System.out.println("\nComplete!");
	}
	//Server sends info of directory
	private void doDirectory() throws IOException
	{
		String path = getSourceFilename(); //get path to look for in server
		// Make sure path is a directory
        if (!(new File(path).exists()) || !(new File(path).isDirectory()))
        {
            outgoingData.writeBoolean(false);
            return;
        }

        // Signal client directory is good
        outgoingData.writeBoolean(true);
        
        File folder = new File(path);
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
        outgoingData.writeUTF(Arrays.toString(listOfFiles));

        System.out.println("complete!");
    } 
	//Server makes new directory
	public void doMakeDirectory() throws IOException
	{
	    // Get the directory to create and create it
        String path = getSourceFilename();
        outgoingData.writeBoolean(new File(path).mkdir());
	}
	//Server removes directory requested by Client
	public void doRemoveDirectory() throws IOException
	{
		 // Get the directory to remove and remove it
        String path = getSourceFilename();

        if (!(new File(path).exists()) || !(new File(path).isDirectory())) 
        {
            outgoingData.writeBoolean(false);
            return;
        }
        outgoingData.writeBoolean(new File(path).delete());
	}
	//Server removes file requested by Client
	public void doRemoveFile() throws IOException
	{
		String path = getSourceFilename();
		if (!(new File(path).exists()) || (new File(path).isDirectory())) 
		{
			outgoingData.writeBoolean(false);
            return;
        }
        outgoingData.writeBoolean(new File(path).delete());
    }
/**CLIENT STUFF	
 * -------------------------------------------------------------
 */
	// client requests server to shut down, then exits program
	private void requestShutdown() throws IOException
	{
		this.initializeStreams();								// subscribe to new streams
		this.setArgument("shutdown");							// set mode
		this.haltOutboundStream(); this.haltInboundStream();	// send mode then stop streams
		this.closeClient();
        System.out.println("good bye!");
        System.exit(0);
	}	
	// client requests the server to engage in download
	private void requestDownload(String serverPath, String fileName) throws IOException
	{	
		System.out.println("REQUESTING DOWNLOAD");
		try
		{
			this.initializeStreams(); 							//Ensure we subscribe to a new in/outbound stream     
			this.setArgument("download");						// get current file and its size			
			this.writePath(serverPath);							// tell server what file you want			
			long serverFileSize = this.getSourceFileSize();		// get the size of it on server			
			String clientFile = this.readFile(fileName);		// check if file exists on client			
			long clientFileSize = (long)clientFile.length();	// get its length			
			this.writeCurrentFileSize(clientFileSize);			// pass current length of client file to server	
			
			
			System.out.println("Starting Download of "+ serverPath +" ..."+
					   "\nSize on server -> " + clientFile.length() + "  Size on client -> " + serverFileSize);
			
			
            if(clientFileSize - serverFileSize == 0)
            {
            	System.out.println("File size unchanged.");
            }
            else 
            {
            	this.writeRemainingBytesToFile(serverFileSize, fileName);	// write contents from server
            }
            this.haltInboundStream();						// halt stream
            System.out.println("\nFile Downloaded.");
            
		}
		catch (Exception e)
        {
            System.out.println(e.getMessage());
        }		
	}
	// client requests the server to engage in upload
	private void requestUpload(String fileName, String serverPath) throws IOException
	{
		//Get file contents
		String contents = this.readFile(fileName);	
        try 
        {
        	this.initializeStreams(); 								//Ensure we subscribe to a new in/outbound stream      
            this.setArgument("upload");            
            this.writePath(serverPath);								//Signal server of the file to upload and its properties
            this.writeCurrentFileSize(contents.length());
            long currentServerSize = this.getSourceFileSize();     	// Check the starting point of upload     
            
            if(!isFileChanged((long)contents.length(), currentServerSize))
            {
            	System.out.println("The file has not changed.");
            }            
            else
            {
            	System.out.print("The file has changed: Resuming upload at " + 
            					 ((float)currentServerSize/(float)contents.length())*100 + "% (of total bytes)");
            	this.writeData(contents.substring((int)currentServerSize, contents.length()));        // write data from client
            }            
            this.haltOutboundStream();   this.haltInboundStream();							// stop streams
            System.out.println("\nFile Uploaded.");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
	}
	//client requests the server to list directory
	private void requestDirectory(String serverPath)
	{
		try
		{
			//Ensure we subscribe to a new in/outbound stream   
			this.initializeStreams();								
			// Send to server the file to be downloaded
			this.setArgument("dir");
            writePath(serverPath);

            if (!incomingData.readBoolean()) 
            {
                System.out.println("The path " + serverPath + " does not exist or is not a directory in the server.");
                return;
            }
            System.out.println(incomingData.readUTF().trim());
            System.out.println("\nComplete!");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	//client requests to make a directory on server
	private void requestMakeDirectory(String serverPath)
	{
		try 
		{
			this.initializeStreams(); 

            System.out.print("Creating directory " + serverPath + "...");

            // Send to server the folder to be made
            this.setArgument("mkdir");
            writePath(serverPath);

            if (!incomingData.readBoolean()) 
            {
                System.out.println("The path " + serverPath + " does not exist or is not a directory in the server.");
                return;
            }

            System.out.println("\nComplete!");
        }
		catch (Exception e)
		{
            System.out.println(e.getMessage());
        }
	}
	//client requests to remove a directory on server
	private void requestRemoveDirectory(String serverPath) throws IOException
	{
		try
		{
			this.initializeStreams(); 
		    System.out.print("Deleting directory " + serverPath + "...");
		    //Send to server the folder to be deleted
		    this.setArgument("rmdir");
            writePath(serverPath);
		    if (!incomingData.readBoolean())
		    {
		        System.out.println("The path " + serverPath + " does not exist, is not empty or is not a directory in the server.");
		        return;
		    }
		    System.out.println("\nComplete!");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	//client requests to remove a file on server
	public void requestRemoveFilename(String serverPath)
	{
		try
		{
			this.initializeStreams(); 
			System.out.print("Deleting file " + serverPath + "...");
			// Send to server the file to be deleted
            this.setArgument("rm");
            writePath(serverPath);
            if (!incomingData.readBoolean()) 
            {
                System.out.println("The path " + serverPath + " does not exist or is not a file in the server.");
                return;
            }
            System.out.println("\nComplete!");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
/**	FILE STUFF
 * --------------------------------------------------------------
 */
	private String readFile(String path)
    {        
        File fileToUpload = new File(path);	
        String result = "";        
        if (!fileToUpload.exists()) 		// Stop if file to upload doesn't exist
        {
            System.out.println(path + " does not exist.");
            return "";
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
	private static boolean isFileChanged(long clientSize, long serverSize)
	{		
		return ((clientSize-serverSize)==0) ?  false :  true;
	}	
	private void setArgument(String arg) throws IOException
	{
		this.outgoingData.writeUTF(arg);		
	}	
	public String getArgument() throws IOException	
	{	
        return this.incomingData.readUTF();
	}	
	private String getSourceFilename() throws IOException 
	{
		return this.incomingData.readUTF().trim();
	}	
	private long getSourceFileSize() throws IOException 
	{
		return this.incomingData.readLong();
	}	
	private void writeCurrentFileSize(long byteSize) throws IOException
	{
		this.outgoingData.writeLong(byteSize);
	}		
	private void writeData(String data) throws IOException
	{
		this.outgoingData.write(data.getBytes());
	}
	private void writePath(String path) throws IOException
	{
		this.outgoingData.writeUTF(path);
	}	
	private void writeFileSize(String path) throws IOException
	{
		this.outgoingData.writeLong(this.readFile(path).length());
	}	
	private int getFileSizeOffset(String filename) throws IOException 
	{
        File fileBeingUploaded = new File(filename);    // Create the file and ready to stream data to it
        int offset = 0;        
        if (fileBeingUploaded.isFile()) 
        {
        	offset = this.readFile(filename).length();                        
        }
        else
        {
        	this.createFile(filename);
        }	
        return offset;
	}
	private void createFile(String filename) throws IOException
	{
		new File(filename).createNewFile();
	}
	private void writeRemainingBytesToFile(long bytesToUpload, String filename) throws IOException
	{
		int offset = this.getFileSizeOffset(filename);
		FileOutputStream fos = new FileOutputStream(filename, true);		
        String currentContents = "";
        byte[] dataBits = new byte[(int)bytesToUpload-offset];
        this.inUse = true;
        try 
        {
            while (this.inUse) 
            {
            	int data = this.incomingData.read(dataBits, 0, (int)bytesToUpload-offset);
            	if(data >= 0)
            	{
            		currentContents += new String(dataBits);            		
            		byte[] dbitz = new byte[currentContents.length()];
            		dbitz = currentContents.getBytes();            		
            		fos.write(dbitz, 0, currentContents.length());
            		fos.flush(); fos.close();
            		this.inUse = false;
            		break;
            	}            	
            }
        }
        catch (Exception e)
        {
            fos.flush();  fos.close();
        }
	}
/**	INPUT HANDLING STUFF
 * -----------------------------------------------------------------
 */	
	public void handleClientInput(String[] args) throws IOException
	{
		switch(args[0].toLowerCase())
		{		
	        case "upload":   this.requestUpload(args[1], args[2]);   break;        	
	        case "download": this.requestDownload(args[1], args[2]); break;
	        case "dir":		 this.requestDirectory(args[1]);         break;
	        case "mkdir":    this.requestMakeDirectory(args[1]);     break;
	        case "rmdir":	 this.requestRemoveDirectory(args[1]);   break;
	        case "rm":       this.requestRemoveFilename(args[1]);    break;
	        case "shutdown": this.requestShutdown();                 break;
	    	default:         System.out.println("invalid input");        	
		}		
		this.closeClient();
        return ;
	}	
	public boolean handleServerInput(String arg) throws IOException
	{
		boolean flag = true;
		switch(arg.toLowerCase())
		{		
			case "upload":   this.doUpload();                    break;								
			case "download": this.doDownload();	                 break;
			case "dir":		 this.doDirectory();                 break;
			case "mkdir":    this.doMakeDirectory();             break;
			case "rmdir":    this.doRemoveDirectory();           break;
			case "rm":       this.doRemoveFile();                break;
			case "shutdown": this.requestShutdown();flag=false;  break;
			default:        System.out.println("invalid");					
		}
		return flag;
	}	
/**	APPLICATION STATE STUFF	
 * -----------------------------------------------------------------
 */
	private void initializeStreams() throws IOException
	{
		this.outgoingData = new DataOutputStream(this.socket.getOutputStream());
    	this.incomingData = new DataInputStream(this.socket.getInputStream());   
	}	
	private void haltInboundStream() throws IOException
	{
		this.incomingData.close();
	}	
	private void haltOutboundStream() throws IOException
	{
		this.outgoingData.flush();   this.outgoingData.close();
	}	
	public void delay(int t)
	{
       synchronized(this.lock){try{this.lock.wait(t);}catch(InterruptedException e){e.printStackTrace();}this.lock.notify();} 
    }	
	private void closeClient() throws IOException
	{
		try{this.socket.close();}catch(IOException e){e.printStackTrace();}
	}	
}