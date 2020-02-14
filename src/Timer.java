import java.io.File;

public class Timer implements Runnable {
    boolean running, executing, invalid;
    File directory;
    Main tw;

    public Timer(Main tw) {
        this.tw = tw;
        this.running = true;
        this.executing = true;
        this.invalid = false;
    }

    @Override
    public void run() {
        while(running) {
            try {
                if(executing) {
                    directory = tw.latestWorld();
                    if(!invalid) {
                        tw.updateTime(directory);
                    }
                }
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }
}