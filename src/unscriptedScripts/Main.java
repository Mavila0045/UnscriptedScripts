package unscriptedScripts;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

public class Main {
	
	// Main method that boots the program indefinitely
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Welcome to Unscripted Scripts\n");
		while(true) {
			mainMenu(scanner);
		}
	}
	
	// Displays Main Menu
	public static void mainMenu(Scanner scanner) {
		System.out.println("Select an option:\n"
				+ "1. Create a Script\n"
				+ "2. Create a Story\n"
				+ "3. Read a Story\n"
				+ "4. Check your Scripts\n"
				+ "5. Delete a Story\n"
				+ "6. Delete a Script\n"
				+ "7. Exit");
		
		int choice = getInput(scanner, 1, 7);
		System.out.println();
		if(choice == 1) {
			scanner.nextLine();
			createScript(scanner);
		}
		else if(choice == 7) {
			System.exit(0);
		}
		else {
			pickFile(scanner, choice);
		}
		
	}
	
	// Displays list of Scripts or Stories
	public static void pickFile(Scanner scanner, int menuNum) {
		File directory;
		if(menuNum == 2 || menuNum == 4 || menuNum == 6) {
			System.out.println("Pick a Script:");
			directory = new File("Scripts");
		}
		else {
			System.out.println("Pick a Story:");
			directory = new File("Stories");
		}
		
		System.out.println("0. Go back to Main Menu");
		
		if(directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles();
			
			if(files != null) {
				for(int i = 0; i < files.length; i++) {
					if(files[i].isFile()) {
						System.out.println((i + 1) + ". " + files[i].getName());
					}
				}
			}
			
			int choice = getInput(scanner, 0, files.length);
			scanner.nextLine();
			System.out.println();
			
			if(choice == 0) {
				return;
			}
			
			String filePath = directory + "/" + files[choice - 1].getName();
			if(menuNum == 2) {
				createStory(scanner, filePath);
			}
			else if(menuNum == 3 || menuNum == 4) {
				readFile(filePath);
			}
			else if(menuNum == 5 || menuNum == 6) {
				deleteFile(scanner, filePath);
			}
		}
		else {
			boolean createDirectory = directory.mkdir();
			if(createDirectory) {
				getInput(scanner, 0, 0);
				scanner.nextLine();
				System.out.println();
				
				return;
			}
			else {
				System.out.println("\nFailed to create " + directory + ". Create folder manually");
			}
		}
	}
	
	// TODO Prompts user to create a script
	public static void createScript(Scanner scanner) {
		StringBuilder fileContent = new StringBuilder();
		
		System.out.println("Create your script. Remember to enclose words you want to replace in brackets (Ex. [NOUN]):");
		fileContent.append(scanner.nextLine());
		
		System.out.println("Add another line to the script [Y/N]?");
		
		while(getConfirmation(scanner)) {
			fileContent.append("\n" + scanner.nextLine());
			System.out.println("Add another line to the script [Y/N]?");
		}
		
		saveFile(scanner, "Scripts/", fileContent);

	}
	
	// Prompts user to create a story with the chosen script
	public static void createStory(Scanner inputScanner, String filePath) {
		try {
			File file = new File(filePath);
			try (Scanner fileScanner = new Scanner(file)) {
				StringBuilder fileContent = new StringBuilder();
				Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
				
				
				while(fileScanner.hasNextLine()) {
					String line = fileScanner.nextLine();
					Matcher matcher = pattern.matcher(line);
					
					while(matcher.find()) {
						String newWord = getUserWord(inputScanner, matcher.group() + ": ");
						line = line.replaceFirst("\\[([^\\]]+)\\]", newWord);
						matcher.appendReplacement(fileContent, newWord);
					}
					matcher.appendTail(fileContent);
					if(fileScanner.hasNextLine()) {
						fileContent.append("\n");
					}
				}
				
				saveFile(inputScanner, "Stories/", fileContent);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// Prompts user to write a specific word
	public static String getUserWord(Scanner scanner, String word) {
		System.out.println("Write a(n) " + word);
		String input = scanner.nextLine();
		
		return "[" + input + "]";
	}
	
	// Print the chosen File
	public static void readFile(String filePath) {
		File file = new File(filePath);
		try (Scanner fileScanner = new Scanner(file)) {
			System.out.println("Text:");
			while(fileScanner.hasNextLine()) {
				System.out.println(fileScanner.nextLine());
			}
			System.out.println();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	// TODO Creates the file
	public static void saveFile(Scanner scanner, String directoryPath, StringBuilder fileContent) {
		System.out.println("\nText:\n" + fileContent);
		System.out.println("\nSave this as a file [Y/N]?");
		
		if(getConfirmation(scanner)) {
			System.out.println("\nGive this file a name (without extension): ");
			String output = directoryPath + scanner.nextLine() + ".txt";
			File checkFile = new File(output);
			boolean abort = false;
		
			while(checkFile.exists()) {
				System.out.println("\nFilename already exists.\n"
						+ "1. Overwrite\n"
						+ "2. Rename\n"
						+ "3. Abort");
			
				int choice = getInput(scanner, 1, 3);
				scanner.nextLine();
				if(choice == 1) {
					try(BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
						writer.write(fileContent.toString());
					}
					catch (IOException e) {
						System.out.println("An error occured while writing to the file: " + e.getMessage());
					}
					break;
				}
				else if(choice == 2) {
					System.out.println("\nGive this file a name (without extension): ");
					output = directoryPath + scanner.nextLine() + ".txt";
					checkFile = new File(output);
					continue;
				}
				else {
					abort = true;
					break;
				}
			
			}
			if(!abort) {
				try(BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
					writer.write(fileContent.toString());
				}
				catch (IOException e) {
					System.out.println("An error occured while writing to the file: " + e.getMessage());
				}
			}
		}
		
		System.out.println();
	}
	
	// Delete the chosen File
	public static void deleteFile(Scanner scanner, String filePath) {
		System.out.println("Are you sure you want to delete the file at \"" + filePath + "\" [Y/N]?");
		
		if(getConfirmation(scanner)) {
			File file = new File(filePath);
			file.delete();
		}
		
		System.out.println();
	}
	
	// Checks if integer input is a valid choice
	public static int getInput(Scanner scanner, int start, int end) {
		while(!scanner.hasNextInt()) {
			System.out.println("Not a valid choice. Pick a number from the list.");
			scanner.next();
		}
		
		int choice = scanner.nextInt();
		
		while(!(start <= choice && choice <= end)) {
			System.out.println("Not a valid choice. Pick a number from the list.");
			
			while(!scanner.hasNextInt()) {
				System.out.println("Not a valid choice. Pick a number from the list.");
				scanner.next();
			}
			
			choice = scanner.nextInt();
		}
		
		return choice;
	}
	
	// Checks if String input is a valid choice
	public static boolean getConfirmation(Scanner scanner) {
		String input = scanner.nextLine().toUpperCase();
		
		if(input.equals("Y") || input.equals("YES") || input.equals("1")) {
			return true;
		}
		else if(input.equals("N") || input.equals("NO") || input.equals("0")) {
			return false;
		}
		else {
			System.out.println("Type yes or no, or simply Y or N, or even 1 or 0.");
			return getConfirmation(scanner);
		}
	}
	
	
 
}
