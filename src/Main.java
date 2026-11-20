import javax.swing.*;

/**
 * Entry point for the Smart Parking Management System.
 * Applies the native system Look & Feel and launches LoginForm on the Swing Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        // Apply native OS styling (buttons, tables, and borders) instead of the legacy Metal theme
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Launch the login screen safely on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Smart Parking System");
            frame.setContentPane(new LoginForm().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null); // Center window on screen
            frame.setVisible(true);
        });
    }
}