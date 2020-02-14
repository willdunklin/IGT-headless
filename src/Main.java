import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static File minecraft_directory = null;

    private static boolean running = true, invalid = false;

    private static long TIMEZONE_OFFSET;

    private static String display;

    public static File defaultDirectory() {
        try {
            int os = os();
            if(os == 0)
                return new File(System.getProperty("user.home")+"/AppData/Roaming/.minecraft");
            if(os == 1)
                return new File("/Users/"+System.getProperty("user.name")+"/Library/Application Support/minecraft");
            if(os == 2)
                return new File("/home/"+System.getProperty("user.name")+"/.minecraft");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int os() {
        String systemID = System.getProperty("os.name").toLowerCase();
        if(systemID.contains("win"))
            return 0;
        if(systemID.contains("mac"))
            return 1;
        if(systemID.contains("nix") || systemID.contains("nux") || systemID.contains("aix"))
            return 2;
        return -1;
    }

    public static void chooseDirectory(Scanner input) {
        System.out.println("Please enter the name of your minecraft directory: ");
        do {
            try {
                minecraft_directory = new File(input.nextLine());
            } catch(Exception e) {
                System.err.println("\nAn error occurred: " + e.getMessage());
            }
        } while(minecraft_directory == null);
    }

    public static File latestWorld() {
        try {
            File saves = new File(minecraft_directory.getAbsolutePath()+"/saves");
            if(!saves.exists()) {
                System.err.println("Selected Minecraft Directory: " + minecraft_directory.getAbsolutePath());
                invalid = true;
            } else {
                invalid = false;
                File[] directories = Arrays.stream(Objects.requireNonNull(saves.listFiles())).filter(file -> file.isDirectory()).toArray(File[]::new);
                Arrays.sort(directories, Comparator.comparingLong(File::lastModified));
                return directories[directories.length - 1];
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateLabel(String text) {
        if(!text.equals(display)) {
            System.out.println(text);
            display = text;
        }
    }

    public static void updateTime(File save) {
        try {
            if(save != null && save.exists()) {
                File statsFolder = new File(save.getAbsolutePath() + "/stats");

                if(statsFolder.exists()) {

                    File[] playerStats = statsFolder.listFiles();

                    if(playerStats != null && playerStats.length > 0) {
                        File stats = playerStats[0];
                        String data = Files.readAllLines(stats.toPath()).get(0);
                        Pattern p = Pattern.compile("(inute\":)(\\d+)");
                        Matcher m = p.matcher(data);
                        if(m.find()) {
                            updateLabel(save.getName() + ": " + formatTime(Long.parseLong(m.group(2))));
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String formatTime(long time) {
        time *= 50;
        Date d = new Date(time - TIMEZONE_OFFSET);
        return new SimpleDateFormat("HH:mm:ss.SS").format(d);
    }

    public static void main(String[] args) {
        boolean def;
        Scanner input = new Scanner(System.in);
        try {
            String defaultName = defaultDirectory().getAbsolutePath();
            String answer = null;

            System.out.println("Is \"" + defaultName + "\" your minecraft directory? (y/n)");
            do {
                if(answer != null) {
                    System.out.println("Didn't get that. Is that your minecraft directory? (y/n)");
                }
                answer = input.nextLine().toLowerCase();
            } while(answer == null || !(answer.charAt(0) == 'y' || answer.charAt(0)  == 'n'));

            def = answer.charAt(0) == 'y';
        } catch(Exception e) {
            def = false;
        }

        if(def) {
            minecraft_directory = defaultDirectory();
        } else {
            chooseDirectory(input);
        }

        TimeZone time = TimeZone.getDefault();
        TIMEZONE_OFFSET = time.getRawOffset();

        File directory;
        while(running) {
            try {
                directory = latestWorld();
                if(!invalid) {
                    updateTime(directory);
                } else {
                    System.err.println("Directory is invalid or there is no saves folder.");
                    chooseDirectory(input);
                }
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setInvalid(boolean i) { invalid = i; }
}
