import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("unused")
public class ClientRequestThread extends Thread
{
	private Socket clientSocket;								
	private String message;										
	private String[] userDetails;								
	private String passwords[] = new String[100];				
	private String usernames[] = new String[100];
	private String login;
	private String username;									
	private String password;
	private String currentDirectoriesParent;					
	private File file;											
	private ArrayList<String> files;							
	private ArrayList<String> directories;						
	private String serversFilePath;								
	private String userRootDirectoryPath;						
	private String currentDirectory;							
	private String childDirectory;								
	private String visibleUserDirectory;						
	private String parentDirectory;								
	private String newDirectoryName;							
	private String newDirectoryPath;							
	private String result;										
	private String downloadFile;								
	private String downloadPath;								
	private ObjectOutputStream out;								
	private ObjectInputStream in;
	private boolean authenticated = false;
	private boolean validEmail = false;							
	private volatile boolean running = true;  					
	private int i = 0;											
	private int parentOrChild;									
	private int choice;											
	private int proceedOrNot = 0;								
	private long uploadSize;
	private String uploadName;
	private String uploadPath;
	
	ClientRequestThread(Socket s) 
	{
	   clientSocket = s;
	}
	
	private void sendMessage(Object message)
	{
		try
		{
			out.writeObject(message);	
			out.flush();				
		}
		catch(IOException e)
		{
			System.out.println();
			System.out.println(e.getMessage());
			System.out.println("There is an IO with sending a message, you may have entered some incorrect information - please try again");
		}
	}
	
	private String getServersFilePath()
	{
		String s;
		Path currentRelativePath = Paths.get("");			  								
		s = currentRelativePath.toAbsolutePath().toString(); 								
		s = s.replace('\\', '/');							 								
		System.out.println("Here");
		System.out.println(s);

		return s;
	}
	
	private String createUserDirectory(String path, String username)
	{
		String usersDirectoryPath = path + "/" + username; 						
		File file = new File(usersDirectoryPath);								
		FileWriter writer = null;
		
		if(!file.exists())
		{
			file.mkdir();															
		}
		return usersDirectoryPath;		
	}
	
	public String createNewDirectory(String path, String name)
	{
		File file = new File(path);						
		String result = "";
		
		if(!file.exists())
		{
			file.mkdir();	
			result = "Directory "+name+" has been created successfully";
		}
		else 
		{
			result = "A Directory with the name "+name+" already exists, please try using a different name.";
		}
		
		return result;		
	}
	
	private ArrayList<String> listDirectoriesFiles(File file)
	{
		ArrayList<String> directoryFiles = new ArrayList<String>();		//Create array list of strings to hold the files
		
		for(File f : file.listFiles())									//for each file in the list of files for the directory
		{
			if(f.isFile())
			{
				directoryFiles.add(f.getName());						//Get its name and add it to the array list
			}
		}
		
		return directoryFiles;											//Return the array list
	}
	
	private String getCurrentDirectoriesParent(File file, String username)
	{
		String parentDirectoryName;
		String parentDirectoryPath;
		if(username.equals(file.getName()))
		{
			parentDirectoryName = "You are in your root directory and currently dont have a parent directory";
		}
		else /
		{
			parentDirectoryPath = file.getParent();						//get the path of the current directories parent
			File parentDirectory = new File(parentDirectoryPath);		//use that path to create a file object
			parentDirectoryName = parentDirectory.getName();			//then get the name of that directory
		}
		
		return parentDirectoryName;										//Return the name of the parent directory
	}
	
	//Gets and returns all the directories in a directory
	private ArrayList<String> listDirectoriesDirectories(File file)
	{
		ArrayList<String> directoryDirectories = new ArrayList<String>();			//Array list of strings to hold all the directories
			
		for(File f : file.listFiles())												//For each directory in list of directories
		{
			if(f.isDirectory())
			{
				directoryDirectories.add(f.getName());								//get its name and add it to the array list
			}
		}
			
		return directoryDirectories;												//return the array list
	}
	
	//THE RUN() METHOD, LIKE THE MAIN() METHOD FOR THIS THREAD, ALL METHODS USED IN THIS METHOD ARE DEFINED ABOVE
	//A thread that the server spawns off each time a client connects to the server.
	@SuppressWarnings("resource")
	public void run()
	{
		try 
		{
			//Creating input and output streams for the server
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(clientSocket.getInputStream());
			
			//AUTHENTICATION
			do
			{
				//Create the string sent from the client with user name/password information
				message = (String)in.readObject();	//wait for authentication details
				userDetails = message.split(" "); //splitting the string to grab the two parts.
				login = userDetails[0];
				username = userDetails[1];
				password = userDetails[2];

				if(login.equals("create")){
					validEmail = true;

					Scanner usernameFileReader = new Scanner(new FileReader("UserInformation/Usernames.txt"));

					i = 0;
					while(usernameFileReader.hasNext())
					{
						usernames[i] = usernameFileReader.next();
						if(usernames[i].equals(username))
						{
							validEmail = false;
							break;
						}
						i++;
					}

					usernameFileReader.close();

					try 
					{
						PrintWriter passwordFile = new PrintWriter(new BufferedWriter(new FileWriter("UserInformation/Passwords.txt", true)));
						PrintWriter usernameFile = new PrintWriter(new BufferedWriter(new FileWriter("UserInformation/Usernames.txt", true)));
						
						passwordFile.println(password);
						usernameFile.println(username);
					
						passwordFile.close();
						usernameFile.close();

					}
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					
					if(validEmail)
					{
						sendMessage("1");
					}
					else
					{
						sendMessage("2");

					}
				}

				if(login.equals("login")){
					//Two scanner objects for reading the files for user name and password.
					Scanner passwordFile = new Scanner(new FileReader("UserInformation/Passwords.txt"));
					Scanner usernameFile = new Scanner(new FileReader("UserInformation/Usernames.txt"));
					
					
					//Loop over all user names and passwords to see if any match the ones provided by the user
					i = 0;
					while(passwordFile.hasNext() && usernameFile.hasNext())
					{
						passwords[i] = passwordFile.next();//Get next password/user name from each file
						usernames[i] = usernameFile.next();
						
						//if they match the ones provided
						if(passwords[i].equals(password) && usernames[i].equals(username))
						{
							authenticated = true; //authentication is now true
							break;				  //No need to keep looping if there has been a successful match
						}
						++i;					  //If there wasnt a match increment counter before next iteration(loop)
					}
					
					passwordFile.close(); 
					usernameFile.close();
					
					if(authenticated == true) 
					{
						sendMessage("1");
						running = false;
					}
					else if(authenticated == false)
					{
						sendMessage("0");
					}
				}

			
				

			}
			while(running);	
		} 
		catch (IOException e) 
		{
			System.out.println();
			System.out.println(e.getMessage());
			System.out.println("There is an IO issue with authentication - please try again");
		}
		catch(ClassNotFoundException classnot)
		{
			System.err.println("Data received in unknown format");
		}
		
			serversFilePath = getServersFilePath();
			userRootDirectoryPath = createUserDirectory(serversFilePath, username);
		
			file = new File(userRootDirectoryPath);
			
			currentDirectoriesParent = getCurrentDirectoriesParent(file, username);
			
			files = listDirectoriesFiles(file);
			directories = listDirectoriesDirectories(file);
			sendMessage(file.getName());
			sendMessage(currentDirectoriesParent);
			sendMessage(directories);
			sendMessage(files);
		
		do
		{	
			try 
			{
				choice = (Integer)in.readObject(); //wait on a command from the client
				switch(choice)
				{
					case 1:
						//RESPONSIBLE FOR DOWNLOADING A FILE FROM THE SERVER (SERVER SIDE)
						message = (String)in.readObject();										//Receive users current directory name from the client.
						serversFilePath = getServersFilePath();									//Get file path of the server
						currentDirectory = serversFilePath + "/" + message;						//Create path for current directory
						file = new File(currentDirectory);										//Create a file object set to current directory
						files = listDirectoriesFiles(file); 									//Get directories files
						sendMessage(files);														//Send the files to the client
						
						proceedOrNot = (Integer)in.readObject(); //If we receive 1 we can begin download, otherwise loop over and wait for next command
						
						if(proceedOrNot == 1)
						{
							message = (String)in.readObject();							//Receive users current directory name from the client.
							downloadFile = (String)in.readObject();						//Receive name of file user wants to download
							serversFilePath = getServersFilePath();									//Get file path of the server
							downloadPath = serversFilePath + "/" + message + "/" + downloadFile;	//Make the download path
							File downloadFile = new File(downloadPath);
							sendMessage(downloadFile.length());											//Send its length to the client
							sendMessage(downloadFile.getName());											//Send its name to the client
							byte[] fileByteArray = new byte[(int) downloadFile.length()];					//Make byte array thats file length in size
						    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(downloadFile));	//Create buffered input stream for the file
						    bis.read(fileByteArray, 0, fileByteArray.length);						//Read file into an array of bytes
						    OutputStream os = clientSocket.getOutputStream();					//Get an output stream for the client
						    os.write(fileByteArray, 0, fileByteArray.length);						//Write array of bytes to the client
						    os.flush();															//Clear the stream
						}
					    break;
					case 2:
						//RESPONSIBLE FOR UPLOADING A FILE FROM CLIENT TO SERVER (SERVER SIDE)
						InputStream inStream;
						proceedOrNot = (Integer)in.readObject();
						if(proceedOrNot == 1)
						{
							message = (String)in.readObject();								//Receive users current directory name from the client.
							uploadName = (String)in.readObject();							//Receive name of the uploaded file from the client.
							uploadSize = (Long)in.readObject();								//Receive size of the uploaded file from the client.
							serversFilePath = getServersFilePath();							//Get file path of the server
							uploadPath = serversFilePath + "/" + message + "/" +uploadName;	//Create path for the uploaded file
							byte[] fileByteArray = new byte[(int) uploadSize];			    //Create array of bytes the same length as the file.
							inStream = clientSocket.getInputStream();						//Open an input stream from the client
							FileOutputStream fileOutStream = new FileOutputStream(uploadPath);					//FileOutputStream to write bytes
							BufferedOutputStream buffedOutStream = new BufferedOutputStream(fileOutStream);		//Pass that into BufferedOutputStream (bytes to characters)
							int bytesRead = inStream.read(fileByteArray, 0, fileByteArray.length);				//Read bytes from server and pass into byte array,
							buffedOutStream.write(fileByteArray, 0, bytesRead);									//Write these bytes (buffered) to file.
							buffedOutStream.close();															//Close the BufferedOutputStream
							sendMessage("File: "+uploadName+" has been successfully uploaded to Directory: "+message);
						}
						break;
					case 3:
						//RESPONSIBLE FOR RETURNING ALL FILES AND DIRECTORIES IN THE CURRENT DIRECTORY (SERVER SIDE)
						message = (String)in.readObject();										//Receive users current directory name from the client.
						serversFilePath = getServersFilePath();									//Get file path of the server
						currentDirectory = serversFilePath + "/" + message;						//Create path for current directory
						file = new File(currentDirectory);										//Create a file object set to current directory
						currentDirectoriesParent = getCurrentDirectoriesParent(file, username);	//Get directories parent
						files = listDirectoriesFiles(file); 									//Get directories files
						directories = listDirectoriesDirectories(file);							//Get directories, directories
						
						//Send the current directory, its parent directory, and files and directories it contains
						sendMessage(message);
						sendMessage(currentDirectoriesParent);
						sendMessage(directories);
						sendMessage(files);
						break;
					case 4:
						//RESPONSIBLE FOR MOVING USER TO DIFFERENT DIRECTORY (SERVER SIDE)
						message = (String)in.readObject();						//Receive users current directory name from the client.
						serversFilePath = getServersFilePath();					//Get file path of the server
						currentDirectory = serversFilePath + "/" + message;		//Create path for current directory
						file = new File(currentDirectory);										//Create a file object set to current directory
						currentDirectoriesParent = getCurrentDirectoriesParent(file, username);	//Get directories parent
						directories = listDirectoriesDirectories(file);							//Get directories, directories
						
						//Send back parent directory and child directories
						sendMessage(currentDirectoriesParent);
						sendMessage(directories);
						
						//Wait for the user to make a decision on whether he/she wants to move to a parent or child directory
						parentOrChild = (Integer)in.readObject();
						
						if(parentOrChild == 1)//If the user wants to move up into the parent directory
						{
							message = (String)in.readObject();		
							serversFilePath = getServersFilePath();
							char[] path = message.toCharArray();
							char[] reversedPath;				
							String currentPath = "";			
							int countSlash = 0;						
																
							boolean grabTheRest = false;			

							for(int i = (path.length - 1); i >= 0; --i)//Loop backwards through the path the user can see
							{
								if(path[i] == '/' && countSlash == 0)//When we reach the first slash we know we are passed the current and in parent
								{
									++countSlash;	//increment so this only happens once
									grabTheRest = true;//boolean meaning we can add whats remaining to our string (parent path, will be new current)
									continue; //Move to next iteration, we dont want to include the slash
								}
								
								//Then on next iteration and onwards we take every char and add it to a string (string is now backwards)
								if(grabTheRest == true)
								{
									currentPath += path[i];
								}
							}
							
							reversedPath = currentPath.toCharArray();	//Pass reversed string to char array
							currentPath = "";							//Reset the current path (resetting the reversed string)
							
							for(int i = (reversedPath.length - 1); i >= 0; --i)//loop backwards once more, puts string in correct order
							{
								currentPath += reversedPath[i]; //add each char to the string again
							}
							
							
							currentDirectory = serversFilePath + "/" + currentPath; //Add the path (user see's) to the entire path for the server
							file = new File(currentDirectory);						//Use path to create file object then use object to.......
							currentDirectoriesParent = getCurrentDirectoriesParent(file, username);	//Get directories parent
							files = listDirectoriesFiles(file); //Get directories files
							directories = listDirectoriesDirectories(file);	//Get directories, directories
							
							sendMessage(currentPath);
							sendMessage(currentDirectoriesParent);
							sendMessage(directories);
							sendMessage(files);
						}
						else if(parentOrChild == 2)
						{
							message = (String)in.readObject();								
							childDirectory = (String)in.readObject();						
							serversFilePath = getServersFilePath();							
							currentDirectory = serversFilePath + "/" + message + "/" +childDirectory;	
							file = new File(currentDirectory);
							visibleUserDirectory = message + "/" +childDirectory; 
																				  
							currentDirectoriesParent = getCurrentDirectoriesParent(file, username);
							files = listDirectoriesFiles(file); 									
							directories = listDirectoriesDirectories(file);							
						
							sendMessage(visibleUserDirectory);
							sendMessage(currentDirectoriesParent);
							sendMessage(directories);
							sendMessage(files);
						}
						break;
					case 5:
						
						message = (String)in.readObject();					
						newDirectoryName = (String)in.readObject();			
						serversFilePath = getServersFilePath();				
						newDirectoryPath = serversFilePath + "/" + message + "/" + newDirectoryName;	
						result = createNewDirectory(newDirectoryPath, newDirectoryName);	
																							
						sendMessage(result);			
						break;
					case 6:
						
						System.out.println("Client "+username+" is finished");
						break;
					default:
						System.out.println();	
						break;
				}
			} 
			catch(ClassNotFoundException e) 
			{
				System.out.println();
				System.out.println(e.getMessage());
				System.out.println("There is a class not found issue - please try again");
			} 
			catch(IOException e)
			{
				System.out.println();
				System.out.println(e.getMessage());
				System.out.println("There is an IO issue, - please try again");
			}
		}
		while(choice != 6);		
		
		try 
		{
			clientSocket.close();	
			in.close();
			out.close();
		} 
		catch(IOException e) 
		{
			System.out.println(e.getMessage());
			System.out.println("There appears to have been an issue closing the connection to the server");
			System.out.println("The connection may possibly have been lost already ");
		}
		
	}
}
