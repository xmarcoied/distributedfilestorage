import java.io.*;
import java.util.*;

public class ClientSideApplicationMain 
{
	private static Client client;
	private static ArrayList<String> files;
	private static ArrayList<String> directories;
	private static String currentDirectory;
	private static String parentDirectory;
	
	@SuppressWarnings("resource")
	public static void main(String[] args) 
	{
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		Scanner console = new Scanner(System.in);
		int login = 0;
		String username = "";								
		String password = "";								
		String ipAddress = "";								
		int authenticated = 0;								
		int choice = 0; 									
		int parentOrChild = 0; 								
		String childDirectory = "";							
		String newDirectory = "";							
		String result = "";									
		String downloadFile = "";							
		String downloadPath = "";
		String uploadPath = "";
		String uploadName = "";
		long uploadSize;									
		boolean caseCheck;									

		System.out.println("ClIENT/SERVER FILE CLOUD STORAGE APPLICATION");
		System.out.println("============================================");

		try 
		{	
			System.out.println("Enter IP Address of File Server: ");
			ipAddress = stdin.readLine();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		//Create a Client object 
		client = new Client(ipAddress);
		System.out.println("============================================");


		do
		{
			System.out.println("SignUp/Login");
			System.out.println("1. Login to already existing account");
			System.out.println("2. SignUp to a new account");
			login = console.nextInt();
			if (login == 2)
			{
				System.out.println("CREATING A NEW ACCOUNT");
				System.out.println("============================================");
				try 
				{
					System.out.println("Enter Your Email For the New Account: ");
					username = stdin.readLine();
					System.out.println("Enter Your Password For the New Account: ");
					password = stdin.readLine();
					int response = -1;
					response = client.sendNewAccountDetails(username,password);
					if (response == 1)
					{
						System.out.println();
						System.out.println("ACCOUNT CREATED");
	
					}
	
					if (response == 2)
					{
						System.out.println();
						System.out.println("Email already exist - Please Try Another email");
	
					}
	
					if (response == 3)
					{
						System.out.println();
						System.out.println("Invalid Credentials Entered - Please Enter a valid Email");
	
					}
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				continue;	
			} 

			try 
			{	
				System.out.println("LOGIN");
				System.out.println("============================================");
				System.out.println("Enter Your Email: ");
				username = stdin.readLine();
				System.out.println("Enter Your Password: ");
				password = stdin.readLine();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		

			authenticated = client.sendAuthenticationDetails(username, password);
			
			if(authenticated == 0)
			{
				System.out.println();
				System.out.println("Invalid Credentials Entered - Please Try Again");
			}
			else if(authenticated == 1)
			{
				System.out.println();
				System.out.println("Valid User Information - Welcome "+username);
			}
		}
		while(authenticated == 0);
		
		outputCurrentUserInformation();
		
		do
		{
			try
			{
				System.out.println();
				System.out.println("APPLICATION MAIN MENU");
				System.out.println("=====================");
				System.out.println("1. (download) Download File from current directory");
				System.out.println("2. (upload)   Upload File to current directory");
				System.out.println("3. (ls)       List all files in current directory");
				System.out.println("4. (cd)       Move to one of the currently accessible directories");
				System.out.println("5. (mkdir)    Create a new directory inside current directory");
				System.out.println("6. (exit)     Exit the program");
				System.out.println("What would you like to do?");
				choice = console.nextInt();
			}
			catch(InputMismatchException e)
			{
				choice = 6;
				System.out.println();
				System.out.println("You must restart the program. Please enter a number that corresponds to you choice");
				System.out.println("You have not entered your choice in the correct format - Program will end.");
			}
			
			switch(choice)
			{
				case 1:
					InputStream inStream;									
					caseCheck = false;									
					client.sendServerCommand(choice);					
					client.sendMessage(currentDirectory);				
					outputAccessibleFiles();							
					
					try 
					{
						System.out.println();
						System.out.println("Listed above are the files downloadable from this directory");
						System.out.println("Please enter the name of the file you wish to download (Case Sensitive, Include File Extension. eg .txt) ");
						downloadFile = stdin.readLine();
					
						for(int i = 0; i < files.size(); ++i)
						{
							if(files.get(i).equals(downloadFile))
							{
								caseCheck = true;
							}
						}
					
						if(caseCheck == true)
						{
							client.sendMessage(1);					
							client.sendMessage(currentDirectory);	
							client.sendMessage(downloadFile);		
							
							System.out.println("Please specify the path on your computer you want to download your file to: ");
							downloadPath = stdin.readLine();
							downloadPath = downloadPath.replace('\\', '/');		
							if(!downloadPath.endsWith("/"))
							{
								downloadPath += "/";
							}
							
							long fileLength = client.getFileLengthFromServer();		
							String fileName = client.getServerMessage();			
							byte[] fileByteArray = new byte[(int) fileLength];		
							
							inStream = client.clientSocket.getInputStream();								
							FileOutputStream fileOutStream = new FileOutputStream(downloadPath+""+fileName);	
							
							BufferedOutputStream buffedOutStream = new BufferedOutputStream(fileOutStream);	
							int bytesRead = inStream.read(fileByteArray, 0, fileByteArray.length);			
							buffedOutStream.write(fileByteArray, 0, bytesRead);								
							buffedOutStream.close();														
							
							System.out.println();
							System.out.println("File: "+downloadFile+ " download was successful");	
						}
						else	
						{
							System.out.println("You have incorrectly typed the file for download - Please Start Again");
							client.sendMessage(-1);
						}
					} 
					catch(IOException e) 
					{
						System.out.println();
						System.out.println(e.getMessage());
						System.out.println("There appears to be an issue with the download, or possibly the path you have specified does not exist.");
					}
				    break;
				case 2:
					client.sendServerCommand(choice);										
					try 
					{
						System.out.println("Please Specify the entire path of the file you wish to upload - File will be uploaded to current directory");
						System.out.println("Include the file itself and its extension eg .txt in the path. ");
						uploadPath = stdin.readLine();
						uploadPath = uploadPath.replace('\\', '/');			
						File uploadFile = new File(uploadPath);				
						if(uploadFile.exists())								
						{
							client.sendMessage(1);							
							uploadName = uploadFile.getName();				
							uploadSize = uploadFile.length();				
							client.sendMessage(currentDirectory);			
							client.sendMessage(uploadName);					
							client.sendMessage(uploadSize);					
							byte[] fileByteArray = new byte[(int) uploadFile.length()];
							BufferedInputStream bis = new BufferedInputStream(new FileInputStream(uploadFile));	
							bis.read(fileByteArray, 0, fileByteArray.length);			
						    OutputStream os = client.clientSocket.getOutputStream();	
						    os.write(fileByteArray, 0, fileByteArray.length);			
						    os.flush();													
						    
						    result = client.getServerMessage();
						    System.out.println(result);
						}
						else
						{
							System.out.println("This file does not exist");		
							client.sendMessage(-1);								
						}
					}
					catch(IOException e)
					{
						System.out.println();
						System.out.println(e.getMessage());
						System.out.println("There appears to be an issue with the upload, or possibly the path you have specified does not exist.");
					}
					break;
				case 3:
					//RESPONSIBLE FOR RETURNING ALL FILES AND DIRECTORIES IN THE CURRENT DIRECTORY (CLIENT SIDE)
					client.sendServerCommand(choice);			//Send users choice to the server
					client.sendMessage(currentDirectory);		//Send the users current directory to the server
					outputCurrentUserInformation(); 			//Output the result returned from server to the screen
					break;
				case 4:
					//RESPONSIBLE FOR MOVING USER TO DIFFERENT DIRECTORY (CLIENT SIDE)
					caseCheck = false;							//makes sure user types directory in a case sensitive way
					client.sendServerCommand(choice);			//Send users choice to the server
					client.sendMessage(currentDirectory);		//Send the users current directory to the server
					outputAccessibleDirectories();	//Output, parent directory & child directories of current directory
					
					System.out.println();
					System.out.println("Are you accessing parent directory or a child directory?");	//Ask where user wants to navigate to
					System.out.println("1 = Parent");
					System.out.println("2 = Child");
					System.out.println("Enter a number: ");
					parentOrChild = console.nextInt();
					
					if(parentOrChild == 1)//If the user wants to navigate to a parent directory
					{
						if(currentDirectory.equals(username))//make sure we are not in the root and if we are
						{
							//Output appropriate message
							System.out.println();
							System.out.println("You have no parent directory, your in the root directory already.");
							client.sendMessage(-1);//Server do nothing, just loop over & wait for the next command (must be done, server is waiting)
						}
						else// if we are not in the root directory
						{
							client.sendMessage(parentOrChild);	//send appropriate command to server so it can provide the correct functionality
							client.sendMessage(currentDirectory);	//send the server our current directory
							outputCurrentUserInformation();			//when server responds output the results. (defined below)
						}
					}
					else if(parentOrChild == 2)//If the user wants to navigate to a child directory
					{
						try 
						{
							//ask user to enter the name of the child directory
							System.out.println("Please enter the name of the child directory to move into (Case Sensitive)");
							childDirectory = stdin.readLine();
							
							//Make sure one matching what has been typed to the user exists with the correct casing
							for(int i = 0; i < directories.size(); ++i)
							{
								if(directories.get(i).equals(childDirectory))
								{
									caseCheck = true;
								}
							}
							
							//If the child directory the user referenced exists
							if(caseCheck == true)
							{
								client.sendMessage(parentOrChild);		//Send appropriate command to server so it navigates to right position
								client.sendMessage(currentDirectory);	//Send current directory
								client.sendMessage(childDirectory);		//Send directory we want to move into
								outputCurrentUserInformation(); 	//Output the result returned from server to the screen
							}
							else	//If the directory referenced is wrong
							{
								//Output an appropriate message and send a command to the server so it can loop over and start again.
								System.out.println("You have incorrectly typed the child directory - Please Start Again");
								client.sendMessage(-1);//Server do nothing, just loop over and wait for the next command
							}
						} 
						catch(IOException e) 
						{
							System.out.println();
							System.out.println(e.getMessage());
							System.out.println("There is an IO issue, you may have entered some incorrect information - please try again");
						}
					}
					else //if the user inputs a wrong command (not navigating to child or parent directory)
					{
						//Output an appropriate message and send a command to the server so it can loop over and start again.
						System.out.println("Invalid Choice - Please Start Again");
						client.sendMessage(-1);//Server do nothing, just loop over and wait for the next command
					}
					break;
				case 5:
					//RESPONSIBLE FOR CREATING A NEW DIRECTORY INSIDE USERS CURRENT DIRECTORY (CLIENT SIDE)
					try
					{
						client.sendServerCommand(choice);
						System.out.println("What would you like to call the new directory? - Enter a name:");
						newDirectory = stdin.readLine();
						
						client.sendMessage(currentDirectory);	//Send current directory
						client.sendMessage(newDirectory);		//Send name of new directory the user wants
						result = client.getServerMessage();		//Get result back from the server
						System.out.println();
						System.out.println(result);				//Output the result;
						
					} 
					catch(IOException e) 
					{
						System.out.println();
						System.out.println(e.getMessage());
						System.out.println("There is an IO issue, you may have entered some incorrect information - please try again");
					}
					break;
				case 6:
					System.out.println("Bye!");
					client.sendServerCommand(choice);
					break;
				default:
					System.out.println("Invalid Selection - Please Try Again");
					client.sendServerCommand(choice);//Send choice to server so it can log it, loop over and wait once more.
					break;
			}
		}
		while(choice != 6);
		
		client.closeConnections();
	}
	
	public static void outputCurrentUserInformation()
	{
	
		System.out.println();
		System.out.println("CURRENT DIRECTORY");
		System.out.println("==========================================");
		currentDirectory = client.getCurrentDirectory();
		System.out.println(currentDirectory);
		
		outputAccessibleDirectories();
		outputAccessibleFiles();
	}
	
	public static void outputAccessibleDirectories()
	{
		System.out.println();
		System.out.println("CURRENTLY ACCESSIBLE PARENT DIRECTORY OF: "+currentDirectory);
		System.out.println("==========================================");
		parentDirectory = client.getCurrentDirectoriesParent();
		System.out.println(parentDirectory);
		
		//Getting the directories inside users root directory from the server
		System.out.println();
		System.out.println("CURRENTLY ACCESSIBLE CHILD DIRECTORIES INSIDE DIRECTORY: "+currentDirectory);
		System.out.println("==========================================");
		directories = client.getFilesFromServer();	//defined in client class returns all directories in a directory
		if(!directories.isEmpty())//if array list of directories is not empty
		{
			for(int i = 0; i < directories.size(); ++i)
			{
				System.out.println(directories.get(i)); //loop over each directory and output
			}
		}
		else
		{
			System.out.println("No Directories here"); //otherwise the directory does not have any directories
		}
	}//End outputAccessibleDirectories()
	
	//outputs the files in this directory
	public static void outputAccessibleFiles()
	{
		//Getting the files inside the users root directory from the server
		System.out.println();
		System.out.println("CURRENTLY ACCESSIBLE FILES INSIDE DIRECTORY: "+currentDirectory);
		System.out.println("==========================================");
		files = client.getFilesFromServer();//same function can also retrieve all files in the current directory on server
		if(!files.isEmpty())	//if files array list is not empty
		{
			for(int i = 0; i < files.size(); ++i)
			{
				System.out.println(files.get(i)); //loop over and output the files
			}
		}
		else
		{
			System.out.println("No Files here"); //otherwise there are no files in the directory on the server
		}
	}
}
