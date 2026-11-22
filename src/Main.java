import java.awt.EventQueue;

import dispatch.DispatchManager;
import dispatch.DispatchPolicy;
import gui.MainFrame;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            DispatchManager manager = new DispatchManager(new DispatchPolicy());
            MainFrame frame = new MainFrame(manager);
            frame.setVisible(true);
        });
    }
}