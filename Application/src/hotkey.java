
/**
 * hotkey is a class which will allow global hotkeys by using C++ Native Calls
 *
 * @author Amir Raminfar
 */
public class hotkey implements Runnable {
    private Thread mythread = null;
    private UserWin window = null;

    static {
        System.loadLibrary("hotkey");
    }

    /**
     * Creates a new instance of Hotkey with window being the owner
     *
     * @param window Owner of the hotkey
     */
    public hotkey(UserWin window) {
        this.window = window;
    }

    /**
     * Start the operating system call
     */
    public native void startloop();

    /**
     * Starts the thread for hotkey listener
     */
    public void start() {
        if (mythread == null) {
            mythread = new Thread(this, "HotKey Listener");
            mythread.start();
        }
    }


    /**
     * Called when any hotkey gets triggered
     *
     * @param wParam parameter of hotkey
     */
    public void trigger(int wParam) {
        window.hotkeyEvent(wParam);
    }

    /**
     * Starts the hotkey loop
     */
    public void run() {
        startloop();
    }
}
