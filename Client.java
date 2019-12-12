import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.*;

public class Client 
{	
	public Socket clientSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String serverResponseMsg;	
	private ArrayList<String> files;
	private String currentDirectory;
	private String parentDirectory;
	private String message;
	private Long length;
 	
 	public Client(String ipAddress)
 	{
 		try 
 		{
			clientSocket = new Socket(ipAddress, 1234);	 
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();												
			in = new ObjectInputStream(clientSocket.getInputStream());	
		} 
 		catch(UnknownHostException e)
 		{
			e.printStackTrace();
		} 
 		catch (IOException e) 
		{
			e.printStackTrace();
		}
 	}
	 
	 
	public int sendNewAccountDetails(String userName, String password)
	{
		int answer = -1;

		String regex = "^(.+)@(.+)$";
		Pattern pattern = Pattern.compile(regex);	
		Matcher matcher = pattern.matcher(userName);
		if(matcher.matches() == false)
		{
			answer = 3;
			return answer; 
		}	

		String userDetails = "create " + userName + " " + password; 	
		try
		{
			out.writeObject(userDetails);					
			out.flush();									
			serverResponseMsg = (String)in.readObject();	
			answer = Integer.parseInt(serverResponseMsg);	
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
 		catch(ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
		return answer;
	}
 	//Sends authentication details to the server
 	public int sendAuthenticationDetails(String userName, String password)
 	{
 		String userDetails = "login " + userName + " " + password; 	
 		int answer = -1;
 		try
		{
			out.writeObject(userDetails);					
			out.flush();									
			serverResponseMsg = (String)in.readObject();	
			answer = Integer.parseInt(serverResponseMsg);	
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
 		catch(ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return answer; 
 	}
 	
 	@SuppressWarnings("unchecked")
	public ArrayList<String> getFilesFromServer()
 	{
 		try 
 		{
			files = (ArrayList<String>)in.readObject();
		} 
 		catch(ClassNotFoundException e) 
 		{
			e.printStackTrace();
		}
 		catch(IOException e) 
 		{
			e.printStackTrace();
		}
 		
 		return files; 
 	}
 	
 	public String getCurrentDirectory()
 	{
 		try 
 		{
			currentDirectory = (String)in.readObject();
		} 
 		catch(ClassNotFoundException e) 
 		{
			e.printStackTrace();
		} 
 		catch (IOException e) 
 		{
			e.printStackTrace();
		}
 		
 		return currentDirectory;	
 	}
 	
 	public String getCurrentDirectoriesParent()
 	{
 		try 
 		{
 			parentDirectory = (String)in.readObject();
		} 
 		catch(ClassNotFoundException e) 
 		{
			e.printStackTrace();
		} 
 		catch (IOException e) 
 		{
			e.printStackTrace();
		}
 		
 		return parentDirectory;	
 	}
 	
 	public String getServerMessage()
 	{
 		try 
 		{
 			message = (String)in.readObject();
		} 
 		catch(ClassNotFoundException e) 
 		{
			e.printStackTrace();
		} 
 		catch (IOException e) 
 		{
			e.printStackTrace();
		}
 		
 		return message;	
 	}
 	
 	public long getFileLengthFromServer()
 	{
 		try 
 		{
 			length = (Long)in.readObject();
		} 
 		catch(ClassNotFoundException e) 
 		{
			e.printStackTrace();
		} 
 		catch (IOException e) 
 		{
			e.printStackTrace();
		}
 		
 		return length;	
 	}
 	
 	public void closeConnections()
 	{
 		try 
 		{
			in.close();
			out.close();
			clientSocket.close();
		} 
 		catch(IOException e)
 		{
			e.printStackTrace();
		}
 	}
 	
 	public void sendMessage(Object message)
 	{
 		try 
 		{
			out.writeObject(message);
			out.flush();			 
		} 
 		catch(IOException e)
 		{
			e.printStackTrace();
		}		
 	}
 	
 	public void sendServerCommand(int command)
 	{
 		try 
 		{
			out.writeObject(command); 
			out.flush();			  
		} 
 		catch(IOException e)
 		{
			e.printStackTrace();
		}					
 	}
}