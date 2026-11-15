import javax.swing.*;

/**
 * Handles the Login screen for parking attendants and administrators.
 * Validates credentials, supports password visibility toggling, and
 * transitions to the main SmartParking dashboard upon successful login.
 */
public class LoginForm {
    // Swing UI components automatically bound to LoginForm.form via their field names
    private JPanel mainPanel;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JButton btnClear;
    private JButton btnLogin;

    // Default echo character used to mask password characters (e.g., '•' or '*')
    private final char defaultEchoChar;

    /**
     * Initializes the LoginForm, saves the default password mask character,
     * and attaches event listeners to the checkbox and buttons.
     */
    public LoginForm() {
        // Store the system's default password bullet character so we can restore it later
        defaultEchoChar = txtPassword.getEchoChar();

        // Listener 1: Toggle between plain text and masked bullets when "Show Password" is clicked
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0); // (char) 0 displays actual characters
            } else {
                txtPassword.setEchoChar(defaultEchoChar); // Restore bullet masking
            }
        });

        // Listener 2: Clear both input fields and reset focus back to the username box
        btnClear.addActionListener(e -> {
            txtUsername.setText("");
            txtPassword.setText("");
            chkShowPassword.setSelected(false);
            txtPassword.setEchoChar(defaultEchoChar);
            txtUsername.requestFocus();
        });

        // Listener 3: Validate username and password when "Login" is clicked
        btnLogin.addActionListener(e -> handleLogin());

        // Allow pressing Enter inside the password field to trigger Login directly
        txtPassword.addActionListener(e -> handleLogin());
    }

    /**
     * Checks entered credentials and opens the SmartParking dashboard if valid.
     */
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        // Check for empty inputs first
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Please enter both username and password.",
                    "Missing Credentials",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Verify against authorized system credentials
        boolean isValidAdmin = username.equals("admin") && password.equals("admin123");
        boolean isValidAttendant = username.equals("attendant") && password.equals("park123");

        if (isValidAdmin || isValidAttendant) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Welcome, " + username + "!",
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Close the Login window
            java.awt.Window loginWindow = SwingUtilities.getWindowAncestor(mainPanel);
            if (loginWindow != null) {
                loginWindow.dispose();
            }

            // Launch the main SmartParking dashboard
            JFrame dashboardFrame = new JFrame("Smart Parking Management System");
            dashboardFrame.setContentPane(new SmartParking().getMainPanel());
            dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dashboardFrame.pack();
            dashboardFrame.setLocationRelativeTo(null);
            dashboardFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Invalid username or password. Please try again.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    /**
     * Exposes the root JPanel so Main.java can display the Login screen on startup.
     *
     * @return The root JPanel of LoginForm
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Standalone entry point for testing LoginForm independently.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Smart Parking Login");
            frame.setContentPane(new LoginForm().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}