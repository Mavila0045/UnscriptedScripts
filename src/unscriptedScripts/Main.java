package unscriptedScripts;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.Properties;

public class Main {
	private static final String DEFAULT_CONFIG_FILE = "config.properties";
	
	// Main method that boots the program indefinitely
	public static void main(String[] args) {
	    try {
	        Properties config = loadConfig();
//	        String showText = config.getProperty("showText");
	        Scanner scanner = new Scanner(System.in);
		
	        System.out.println("Welcome to Unscripted Scripts\n");
	        while(true) {
	        	mainMenu(scanner, config);
	        }
	    } 
	    catch (IOException e) {
	        e.printStackTrace();
	    }
		

	}
	
	// Displays Main Menu
	public static void mainMenu(Scanner scanner, Properties config) {
		System.out.println("Select an option:\n"
				+ "1. Create a Script\n"
				+ "2. Create a Story\n"
				+ "3. Read a Story\n"
				+ "4. Check your Scripts\n"
				+ "5. Delete a Story\n"
				+ "6. Delete a Script\n"
				+ "7. Settings\n"
				+ "8. Exit");
		
		int choice = getInput(scanner, 1, 8);
		System.out.println();
		if(choice == 1) {
			scanner.nextLine();
			createScript(scanner);
		}
		else if(choice == 7) {
			settings(scanner, config);
		}
		else if(choice == 8) {
			System.exit(0);
		}
		else {
			pickFile(scanner, config, choice);
		}
		
	}
	
	// Displays list of Scripts or Stories
	public static void pickFile(Scanner scanner, Properties config, int menuNum) {
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
				createStory(scanner, filePath, config);
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
	
	// Prompts user to create a script
	public static void createScript(Scanner scanner) {
		StringBuilder fileContent = new StringBuilder();
		File directory = new File("Scripts");
		if(!directory.exists()) {
			if(!directory.mkdir()) {
				System.out.println("\nFailed to create " + directory + ". Create folder manually");
				return;
			}
		}
		
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
	public static void createStory(Scanner inputScanner, String filePath, Properties config) {
		try {
			File file = new File(filePath);
			try (Scanner fileScanner = new Scanner(file)) {
				StringBuilder fileContent = new StringBuilder();
				Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
				
				Map<String, String> wordReplacements = new HashMap<>();
				
				while(fileScanner.hasNextLine()) {
					String line = fileScanner.nextLine();
					fileContent.append(line);
					
					if(fileScanner.hasNextLine()) {
						fileContent.append("\n");
					}
				}
				
				if(Boolean.parseBoolean(config.getProperty("showText"))) {
					System.out.println(fileContent + "\n");
				}
				
				Matcher fileMatcher = pattern.matcher(fileContent);
				
				while(fileMatcher.find()) {
					String matchedWord = fileMatcher.group(1);
					boolean hasDigits = matchedWord.matches(".*\\d.*");
					
					if(hasDigits) {
						if(!wordReplacements.containsKey(matchedWord)) {
							String newWord = getUserWord(inputScanner, matchedWord, hasDigits);
							String wordPattern = "\\[" + Pattern.quote(matchedWord) + "\\]";
	                        fileContent = new StringBuilder(fileContent.toString().replaceAll(wordPattern, Matcher.quoteReplacement(newWord)));
	                        wordReplacements.put(matchedWord, newWord);
						}
					}
					else {
						String newWord = getUserWord(inputScanner, matchedWord, hasDigits);
						String wordPattern = "\\[" + Pattern.quote(matchedWord) + "\\]";
						fileContent = new StringBuilder(fileContent.toString().replaceFirst(wordPattern, Matcher.quoteReplacement(newWord)));
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
	public static String getUserWord(Scanner scanner, String word, boolean hasDigits) {
		if(hasDigits) {
			System.out.println("Write a(n) " + word + " (Note: This word choice will be used more than once): ");
		}
		else {
			System.out.println("Write a(n) " + word + ": ");
		}
		
		String input = scanner.nextLine();
		
		return "{" + input + "}";
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
	
	// Creates the file
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
	
	//Loads the config file
	public static Properties loadConfig() throws IOException {
	    Properties properties = new Properties();

	    // Load the config file if it exists
	    try (FileInputStream inputStream = new FileInputStream(DEFAULT_CONFIG_FILE)) {
	        properties.load(inputStream);
	    }
	    catch (IOException e) {
	        // Config file not found, create default config
	        properties.setProperty("showText", "false");

	        // Save the default config to the file
	        try (FileOutputStream outputStream = new FileOutputStream(DEFAULT_CONFIG_FILE)) {
	            properties.store(outputStream, "Default Configuration");
	        }
	    }

	    return properties;
	}
	
    public static void saveConfig(Properties properties) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(DEFAULT_CONFIG_FILE)) {
            properties.store(outputStream, "Updated Configuration");
        }
    }
	
	public static void settings(Scanner scanner, Properties config) {
		boolean showText = Boolean.parseBoolean(config.getProperty("showText"));
		
		System.out.println("Select a setting to change:\n"
				+ "0. Go back to Main Menu\n"
				+ "1. Show script text before creating a story: " + showText);
		int choice = getInput(scanner, 0, 1);
		System.out.println();
		if(choice == 0) {
			return;
		}
		else if(choice == 1) {
			config.setProperty("showText", showText ? "false" : "true");
	        try {
				saveConfig(config);
			}
	        catch (IOException e) {
				e.printStackTrace();
			}
			settings(scanner, config);
		}
	}

	
	
 
}
