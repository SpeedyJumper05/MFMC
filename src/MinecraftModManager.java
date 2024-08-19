import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftModManager {

    public static void main(String[] args) {
        String modsFolderPath = System.getenv("APPDATA") + "\\.minecraft\\mods\\";
        String oldModsBasePath = System.getenv("APPDATA") + "\\.minecraft\\old mods\\Fabric\\";
        String version = null;

        // Step 1: Find the fabric-api mod and extract the version number
        File modsFolder = new File(modsFolderPath);
        File[] files = modsFolder.listFiles();
        if (files != null) {
            Pattern pattern = Pattern.compile("fabric-api-.*?\\+(.*?)\\.jar");

            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.find()) {
                    version = matcher.group(1).replace(".", "-");
                    break;
                }
            }
        }

        // Step 1a: Fallback if no Fabric API mod is found
        if (version == null) {
            System.out.println("No Fabric API mod found.");
            System.out.println("This program only works with the Fabric mod loader and Fabric API installed.");
            waitForUser();
            return;
        }

        System.out.println("Detected version: " + version);

        // Step 2: Create necessary directories
        File oldModsFolder = new File(oldModsBasePath);
        if (!oldModsFolder.exists()) {
            oldModsFolder.mkdirs();
        }

        File versionFolder = new File(oldModsFolder, version);
        if (!versionFolder.exists()) {
            versionFolder.mkdirs();
        }

        // Step 3: Copy files to the version folder and delete from mods folder
        for (File file : files) {
            File destinationFile = new File(versionFolder, file.getName());
            if (!destinationFile.exists()) {
                try {
                    Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            file.delete();
        }

        // Notify the user that mods have been saved
        System.out.println("All mods have been saved to the folder: " + versionFolder.getAbsolutePath());

        // Step 4: Prompt the user to select a version and copy mods back
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select a version to load into the mods folder from the following options:");
        File[] versionFolders = oldModsFolder.listFiles(File::isDirectory);

        if (versionFolders != null) {
            for (int i = 0; i < versionFolders.length; i++) {
                System.out.println((i + 1) + ": " + versionFolders[i].getName());
            }

            int choice = scanner.nextInt();
            if (choice < 1 || choice > versionFolders.length) {
                System.out.println("Invalid selection.");
                waitForUser();
                return;
            }

            File selectedVersionFolder = versionFolders[choice - 1];
            File[] selectedFiles = selectedVersionFolder.listFiles();

            if (selectedFiles != null) {
                for (File file : selectedFiles) {
                    File destinationFile = new File(modsFolder, file.getName());
                    try {
                        Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("Mods for version " + selectedVersionFolder.getName() + " have been loaded into the mods folder.");
        } else {
            System.out.println("No version folders found.");
        }

        // Wait for user input before closing
        waitForUser();
        scanner.close();
    }

    // Method to wait for user input before closing the console
    private static void waitForUser() {
        System.out.println("\nPress ENTER to exit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
