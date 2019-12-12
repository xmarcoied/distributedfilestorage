import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;

public class ServerSideApplicationMain 
{
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException 
	{
		ServerSocket appServerSocket = new ServerSocket(1234,10);	

		File dir = new File("UserInformation");
		if (!dir.exists()) dir.mkdirs();

		File emailsFile = new File("UserInformation/Usernames.txt");
		emailsFile.createNewFile(); 

		File passwordFile = new File("UserInformation/Passwords.txt");
		passwordFile.createNewFile(); 
	    
	    while(true) 
	    {
	      Socket clientSocket = appServerSocket.accept(); 
	      ClientRequestThread clientThread = new ClientRequestThread(clientSocket);
	      clientThread.start(); 
	    }
	}
}
