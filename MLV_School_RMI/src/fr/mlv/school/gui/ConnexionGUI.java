package fr.mlv.school.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import fr.mlv.school.Library;
import fr.mlv.school.User;

public class ConnexionGUI {
	private final Library					library;

	private final JFrame					frame		  = new JFrame();
	private final JPasswordField			passwordField = new JPasswordField();;
	private final JTextField				loginField	  = new JTextField();

	private final Theme						theme;

	private final ArrayList<Consumer<User>>	consumers	  = new ArrayList<>();

	/**
	 * Create the application.
	 */
	private ConnexionGUI(Theme theme, Library library) {
		this.theme = theme;
		this.library = library;
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @param theme
	 */
	public static ConnexionGUI construct(Theme theme, Library library) {
		ConnexionGUI connexionGUI = new ConnexionGUI(theme, library);

		try {
			connexionGUI.frame.setTitle("Connexion à " + library.getName());
		} catch (RemoteException e) {
			e.printStackTrace(System.err);
			connexionGUI.frame.setTitle("Connexion à une biliothèque inconnue");
		}
		connexionGUI.frame.setSize(320, 200);
		connexionGUI.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		connexionGUI.frame.setLocationRelativeTo(null);
		connexionGUI.frame.setResizable(false);

		JDesktopPane desktopPane = new JDesktopPane();
		desktopPane.setBackground(theme.background);
		connexionGUI.frame.getContentPane().add(desktopPane, BorderLayout.CENTER);

		JLabel lblConnexion = new JLabel("Utilisateur");
		lblConnexion.setBounds(18, 22, 150, 16);
		lblConnexion.setFont(new Font(null, Font.BOLD, 18));
		lblConnexion.setForeground(theme.primary);
		desktopPane.add(lblConnexion);

		JLabel lblPassWord = new JLabel("Mot de passe");
		lblPassWord.setBounds(18, 69, 150, 16);
		lblPassWord.setFont(new Font(null, Font.BOLD, 18));
		lblPassWord.setForeground(theme.primary);
		desktopPane.add(lblPassWord);

		connexionGUI.passwordField.setBounds(155, 63, 150, 28);
		connexionGUI.passwordField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					connexionGUI.connect();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		theme.applyTo(connexionGUI.passwordField);
		connexionGUI.passwordField.setHorizontalAlignment(JTextField.CENTER);
		desktopPane.add(connexionGUI.passwordField);

		connexionGUI.loginField.setBounds(155, 16, 150, 28);
		connexionGUI.loginField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					connexionGUI.connect();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		theme.applyTo(connexionGUI.loginField);
		connexionGUI.loginField.setHorizontalAlignment(JTextField.CENTER);
		desktopPane.add(connexionGUI.loginField);

		JButton btnConnexion = new JButton("Connexion");
		theme.applyTo(btnConnexion);
		btnConnexion.addActionListener(e -> connexionGUI.connect());
		btnConnexion.setBounds(18, 120, 270, 29);
		desktopPane.add(btnConnexion);

		connexionGUI.frame.setVisible(true);
		connexionGUI.loginField.grabFocus();

		return connexionGUI;
	}

	public void connect() {
		String login = loginField.getText();
		char pass[] = passwordField.getPassword();
		try {
			try {
				User user = library.connect(login, pass);
				consumers.parallelStream().forEach(c -> c.accept(user));
				close();
			} catch (IllegalArgumentException e) {
				System.err.println("Cannot authenticate: " + e.getMessage());
				wizz();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
	}

	public void wizz() {
		final int delta = 20;
		final int nbWizz = 4;
		final int speed = 125;

		Point point = frame.getLocation();

		for (int i = 0; i < nbWizz; i++) {
			int pow = (int) Math.pow(-1, i % 2);

			for (int j = 1; j < delta; j++) {
				frame.setLocation(point.x + j * pow, point.y);
			}

			for (int j = delta - 1; j >= 0; j--) {
				frame.setLocation(point.x + j * pow, point.y);
			}

			try {
				Thread.sleep(10000 / speed);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void addConnectedListener(Consumer<User> consumer) {
		consumers.add(consumer);
	}

	public void close() {
		this.frame.dispose();
	}
}
