package fr.mlv.school.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import fr.mlv.school.Book;
import fr.mlv.school.Library;
import fr.mlv.school.User;

public class BiblioGUI {
	private final JFrame						   frame	 = new JFrame();
	private final JTextField					   textField = new JTextField();;
	private final Library						   library;
	private final User							   user;

	private final ArrayList<Consumer<WindowEvent>> consumers = new ArrayList<>();

	/**
	 * Launch the application.
	 */
	// public static void main(String[] args) {
	// EventQueue.invokeLater(new Runnable() {
	// public void run() {
	// try {
	// BiblioGUI window = new BiblioGUI();
	// window.frame.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	// }

	/**
	 * Create the application.
	 * 
	 * @param library
	 * 
	 * @param user
	 * 
	 * @throws RemoteException
	 */
	public BiblioGUI(Library library, User user) {
		this.library = library;
		this.user = user;
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws RemoteException
	 */
	public static BiblioGUI construct(Library library, User user) throws RemoteException {
		BiblioGUI biblioGUI = new BiblioGUI(library, user);

		int frameWidth = 900;
		int frameHeight = 600;
		Color myColor = new Color(218, 165, 32);

		// Fenetre principale
		try {
			biblioGUI.frame.setTitle(library.getName());
		} catch (RemoteException e1) {
			e1.printStackTrace(System.err);
			biblioGUI.frame.setTitle("Bibliothèque inconnue");
		}
		// frame.setSize(frameWidth, frameHeight);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		biblioGUI.frame.setBounds((int) screenSize.getWidth() - frameWidth, 0, frameWidth, frameHeight);
		biblioGUI.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		biblioGUI.frame.setResizable(false);
		biblioGUI.frame.setLocationRelativeTo(null);

		// Up
		JPanel panelTop = new JPanel();
		biblioGUI.frame.getContentPane().add(panelTop, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel(" ");
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		ImageIcon imageBiblio = new ImageIcon(
				new ImageIcon(BiblioGUI.class.getResource("/fr/mlv/school/gui/biblio.jpg")).getImage()
						.getScaledInstance(biblioGUI.frame.getWidth(), 150, Image.SCALE_DEFAULT));
		panelTop.setLayout(new GridLayout(0, 1, 0, 300));

		lblNewLabel.setIcon(imageBiblio);
		panelTop.add(lblNewLabel);

		// Center
		JSplitPane splitPaneCenter = new JSplitPane();
		splitPaneCenter.setResizeWeight(0.05);
		biblioGUI.frame.getContentPane().add(splitPaneCenter, BorderLayout.CENTER);

		JPanel panelRight = new JPanel();
		splitPaneCenter.setRightComponent(panelRight);
		panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.X_AXIS));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setBackground(myColor);
		panelRight.add(scrollPane);

		Vector<Vector<Object>> data = new Vector<>();
		Vector<String> headers = new Vector<>();
		headers.addElement("Isbn");
		headers.addElement("Title");
		headers.addElement("Author");
		headers.addElement("Summary");
		headers.addElement("Publisher");
		headers.addElement("");
		headers.addElement("");

		DefaultTableModel model = new DefaultTableModel(data, headers);
		JTable table = new JTable(model);
		table.setRowSelectionAllowed(false);

		ButtonEditor buttonAddPanier = new ButtonEditor(new JCheckBox());
		buttonAddPanier.addTableModel(model);

		TableColumn columnKart = table.getColumn(headers.lastElement());
		columnKart.setCellRenderer(new ButtonRenderer());
		columnKart.setCellEditor(buttonAddPanier);
		scrollPane.setViewportView(table);

		JDesktopPane desktopPaneLeft = new JDesktopPane();
		desktopPaneLeft.setBackground(new Color(218, 165, 32));
		splitPaneCenter.setLeftComponent(desktopPaneLeft);
		GridBagLayout gbl_desktopPaneLeft = new GridBagLayout();
		gbl_desktopPaneLeft.columnWidths = new int[] { 183, 0 };
		gbl_desktopPaneLeft.rowHeights = new int[] { 22, 23, 23, 160, 16, 28, 1, 29, 0 };
		gbl_desktopPaneLeft.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_desktopPaneLeft.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		desktopPaneLeft.setLayout(gbl_desktopPaneLeft);

		GridBagConstraints gbc_btnConnexion = new GridBagConstraints();
		gbc_btnConnexion.anchor = GridBagConstraints.NORTH;
		gbc_btnConnexion.insets = new Insets(0, 0, 5, 0);
		gbc_btnConnexion.gridx = 0;
		gbc_btnConnexion.gridy = 1;

		JButton btnPanier = new JButton("Mon panier");
		btnPanier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				PanierGUI panierGUI = new PanierGUI();
			}
		});
		GridBagConstraints gbc_btnPanier = new GridBagConstraints();
		gbc_btnPanier.anchor = GridBagConstraints.NORTH;
		gbc_btnPanier.insets = new Insets(0, 0, 5, 0);
		gbc_btnPanier.gridx = 0;
		gbc_btnPanier.gridy = 2;
		desktopPaneLeft.add(btnPanier, gbc_btnPanier);

		JLabel lblFindLivre = new JLabel("Rechercher un livre :");
		GridBagConstraints gbc_lblFindLivre = new GridBagConstraints();
		gbc_lblFindLivre.fill = GridBagConstraints.VERTICAL;
		gbc_lblFindLivre.insets = new Insets(0, 0, 5, 0);
		gbc_lblFindLivre.gridx = 0;
		gbc_lblFindLivre.gridy = 4;
		desktopPaneLeft.add(lblFindLivre, gbc_lblFindLivre);

		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 5;
		desktopPaneLeft.add(biblioGUI.textField, gbc_textField);
		biblioGUI.textField.setColumns(10);

		JButton btnFindBook = new JButton("Rechercher");
		btnFindBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Book[] books;
				try {
					books = library.searchByTitle(biblioGUI.textField.getText());
					ArrayList<Number> isbns = new ArrayList<>();

					for (Book book : books) {
						if (!isbns.contains(book.getISBN())) {
							Vector<Object> vectorBook = new Vector<>();
							vectorBook.addElement(book.getISBN());
							vectorBook.addElement(book.getTitle());
							vectorBook.addElement(book.getAuthor());
							vectorBook.addElement(book.getSummary());
							vectorBook.addElement(book.getPublisher());
							vectorBook.addElement("Ajouter au panier");
							data.addElement(vectorBook);
						}
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(System.err);
				}
			}
		});

		GridBagConstraints gbc_btnFindBook = new GridBagConstraints();
		gbc_btnFindBook.insets = new Insets(0, 0, 5, 0);
		gbc_btnFindBook.anchor = GridBagConstraints.SOUTH;
		gbc_btnFindBook.gridx = 0;
		gbc_btnFindBook.gridy = 6;
		desktopPaneLeft.add(btnFindBook, gbc_btnFindBook);

		// Bottom
		JPanel panelDown = new JPanel();
		biblioGUI.frame.getContentPane().add(panelDown, BorderLayout.SOUTH);

		biblioGUI.frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				biblioGUI.consumers.parallelStream().forEach(consumer -> consumer.accept(e));
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		biblioGUI.frame.setVisible(true);

		return biblioGUI;
	}

	public void close() {
		frame.dispose();
	}

	public void addCloseListener(Consumer<WindowEvent> consumer) {
		consumers.add(consumer);
	}
}