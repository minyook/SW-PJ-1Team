
package network;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import login.LoginController;
import login.LoginView;
import login.UserModel;

/**
 *
 * @author rbcks
 */
public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
        }
            LoginView view = new LoginView();
            UserModel model = new UserModel();
            new LoginController(model, view);
            view.setVisible(true);
        });     
    }
}

