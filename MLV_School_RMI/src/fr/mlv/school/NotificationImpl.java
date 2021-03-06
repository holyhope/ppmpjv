package fr.mlv.school;

import java.rmi.RemoteException;
import java.util.Objects;

public class NotificationImpl implements Notification {
	private final Book book;
	private final User user;

	public NotificationImpl(Book book, User user) throws RemoteException {
		this.book = Objects.requireNonNull(book);
		this.user = Objects.requireNonNull(user);
	}

	@Override
	public String getMessage() throws RemoteException {
		return book.getTitle() + " est disponible";
	}

	@Override
	public String getTitle() {
		return "Nouvelle notification";
	}

	@Override
	public Book getBook() throws RemoteException {
		return book;
	}
}
