package fr.mlv.school;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("serial")
public class LibraryImpl extends UnicastRemoteObject implements Library {
	private static final int										WAITING_LIST_SIZE  = 3;

	private final String											name			   = "MLV-School";

	/**
	 * BarCode -> Book
	 */
	private final ConcurrentHashMap<Long, Book>						library			   = new ConcurrentHashMap<>();

	/**
	 * BarCode -> User
	 */
	private final ConcurrentHashMap<Long, User>						borrowers		   = new ConcurrentHashMap<>();

	/**
	 * ISBN -> Users
	 */
	private final ConcurrentHashMap<Long, ArrayBlockingQueue<User>>	waitingList		   = new ConcurrentHashMap<>();

	private final CopyOnWriteArrayList<History>						histories		   = new CopyOnWriteArrayList<>();
	private final ConcurrentHashMap<Long, Long>						bookRegisteredTime = new ConcurrentHashMap<>();

	private final Users												users;

	private final Object											getBookLock		   = new Object();

	public LibraryImpl(Users users) throws RemoteException {
		this.users = Objects.requireNonNull(users);
	}

	public String getName() throws RemoteException {
		return name;
	}

	public boolean addBook(Book book, User user) throws RemoteException {
		if (book == null) {
			throw new IllegalArgumentException("Book is not Valid");
		}
		checkValidUser(user);
		if (!users.userCan(user, Permission.ADD_BOOK)) {
			throw new IllegalArgumentException("User is not allowed");
		}
		bookRegisteredTime.put(book.getBarCode(), System.currentTimeMillis());
		library.put(book.getBarCode(), book);
		return true;
	}

	public boolean deleteBook(Book book, User user) throws RemoteException {
		checkValidBook(book);
		checkValidUser(user);
		if (!users.userCan(user, Permission.REMOVE_BOOK)) {
			throw new IllegalArgumentException("User is not allowed");
		}
		library.remove(book.getBarCode());
		bookRegisteredTime.remove(book.getBarCode());
		return true;
	}

	public boolean forceDeleteBook(Book book) throws RemoteException {
		checkValidBook(book);
		library.remove(book.getISBN());
		bookRegisteredTime.remove(book.getBarCode());
		return true;
	}

	public void checkValidBook(Book book) throws RemoteException {
		if (book == null) {
			throw new IllegalArgumentException("Book is not Valid.");
		}
		if (!library.containsKey(book.getBarCode())) {
			throw new IllegalArgumentException("This book doesn't exist in the library.");
		}
	}

	public void checkValidUser(User user) throws RemoteException {
		if (user == null) {
			throw new IllegalArgumentException("User is not Valid.");
		}
		if (!users.isRegistered(user)) {
			throw new IllegalArgumentException("This user isn't registered.");
		}
	}

	public boolean isBookAvailable(Book book) throws RemoteException {
		checkValidBook(book);
		return borrowers.get(book.getBarCode()) == null ? true : false;
	}

	public boolean getBook(Book book, User user) throws RemoteException {
		checkValidBook(book);
		checkValidUser(user);

		synchronized (getBookLock) {
			if (borrowers.get(book.getBarCode()) == null) {
				ArrayBlockingQueue<User> waitingList = this.waitingList.getOrDefault(book.getISBN(),
						new ArrayBlockingQueue<User>(WAITING_LIST_SIZE));
				if (waitingList.isEmpty() || waitingList.peek().equals(user)) {
					waitingList.poll();
					histories.add(new History(book, user, 1));
					borrowers.put(book.getBarCode(), user);
					return true;
				}
			}
		}

		return false;
	}

	public boolean restoreBook(Book book, User user) throws RemoteException {
		checkValidBook(book);
		checkValidUser(user);
		if (borrowers.get(book.getBarCode()) != null && borrowers.get(book).equals(user)
				&& borrowers.remove(book) != null) {
			histories.add(new History(book, user, 2));
			ArrayBlockingQueue<User> observers = waitingList.get(book.getISBN());
			if (observers != null) {
				observers.peek().addNotification(new NotificationImpl(book, user));
				waitingList.put(book.getISBN(), observers);
			}
			return true;
		}
		return false;

	}

	public boolean subscribeToWaitingList(Book book, User user) throws RemoteException {
		checkValidBook(book);
		checkValidUser(user);
		if (borrowers.get(book.getBarCode()) != null) {
			ArrayBlockingQueue<User> usersWaiting = waitingList.getOrDefault(book.getISBN(),
					new ArrayBlockingQueue<User>(WAITING_LIST_SIZE));

			try {
				usersWaiting.add(user);
			} catch (IllegalStateException ise) {
				return false;
			}
			waitingList.put(book.getISBN(), usersWaiting);
			return true;
		}
		return false;
	}

	public boolean unsubscribeToWaitingList(Book book, User user) throws RemoteException {
		checkValidBook(book);
		checkValidUser(user);
		ArrayBlockingQueue<User> usersWaiting = waitingList.getOrDefault(book.getISBN(),
				new ArrayBlockingQueue<User>(WAITING_LIST_SIZE));
		if (usersWaiting != null) {
			if (usersWaiting.remove(user)) {

				waitingList.put(book.getISBN(), usersWaiting);
				return true;
			}
		}
		return false;
	}

	public Book searchByBarCode(long barCode) throws RemoteException {
		if (barCode <= 0) {
			throw new IllegalArgumentException("barCode must be positive.");
		}
		return library.get(barCode);
	}

	public Book[] searchByISBN(long ISBN) throws RemoteException {
		if (ISBN <= 0) {
			throw new IllegalArgumentException("ISBN must be positive.");
		}
		ArrayList<Book> books = new ArrayList<>();
		for (Book book : library.values()) {
			if (book.getISBN() == ISBN) {
				books.add(book);
			}
		}
		Book[] booksArray = new Book[books.size()];
		booksArray = books.toArray(booksArray);
		return booksArray;
	}

	public Book[] searchByTitle(String title) throws RemoteException {
		if (title == null) {
			throw new IllegalArgumentException("title must not be null.");
		}
		if (title == "") {
			throw new IllegalArgumentException("title must not be empty.");
		}
		ArrayList<Book> books = new ArrayList<>();
		for (Book book : library.values()) {
			if (book.getTitle().contains(title)) {
				books.add(book);
			}
		}
		Book[] booksArray = new Book[books.size()];
		booksArray = books.toArray(booksArray);
		return booksArray;
	}

	public Book[] searchByAuthor(String author) throws RemoteException {
		if (author == null) {
			throw new IllegalArgumentException("author must not be null.");
		}
		if (author == "") {
			throw new IllegalArgumentException("author must not be empty.");
		}
		ArrayList<Book> books = new ArrayList<>();
		for (Book book : library.values()) {
			if (book.getAuthor().contains(author)) {
				books.add(book);
			}
		}
		Book[] booksArray = new Book[books.size()];
		booksArray = books.toArray(booksArray);
		return booksArray;
	}

	public boolean isBuyable(Book book) throws RemoteException {
		checkValidBook(book);
		long timestamp = bookRegisteredTime.get(book.getBarCode());
		Date date = new Date(timestamp);
		Date currentDate = new Date();
		int diffInDays = (int) ((currentDate.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
		if (borrowers.get(book.getBarCode()) == null) {
			if (diffInDays > 365 * 2) {
				for (History history : histories) {
					if (history.getType() == 1 && history.getBook().equals(book)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean buyBook(Book book, User user) throws RemoteException {
		checkValidBook(book);
		checkValidUser(user);
		if (isBuyable(book)) {
			library.remove(book.getBarCode());
			histories.add(new History(book, user, 3));
			return true;
		}
		return false;
	}

	public double getCost(Book book) throws RemoteException {
		checkValidBook(book);
		return book.getCost();
	}

	public Users getUsers() throws RemoteException {
		return users;
	}

	@Override
	public User connect(String username, char password[]) throws RemoteException {
		User user = users.findByUsername(username);
		if (!users.authenticate(user, password)) {
			throw new IllegalArgumentException("Cannot authenticate now.");
		}
		System.out.println("User " + user.getUsername() + " connected.");
		return user;
	}

	@Override
	public boolean isRegistered(User user) throws RemoteException {
		return users.isRegistered(user);
	}

	@Override
	public void disconnect(User user) throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("User " + user.getUsername() + " disconnected.");
	}

	public Book searchByBarCode(Long barCode) {
		return library.get(barCode);
	}

	public Book[] getAllBooks() throws RemoteException {
		return library.values().toArray(new Book[library.size()]);
	}
}
