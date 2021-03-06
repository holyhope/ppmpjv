package fr.mlv.school;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;

public class CommentImpl extends UnicastRemoteObject implements Comment {
	private static final long serialVersionUID = 1L;
	private final long		  time			   = System.currentTimeMillis();

	private final String	  author;
	private final int		  mark;
	private final String	  content;

	public CommentImpl(String author, int mark, String content) throws RemoteException {
		this.author = Objects.requireNonNull(author);
		this.mark = mark;
		this.content = Objects.requireNonNull(content);
	}

	public CommentImpl() throws RemoteException {
		this("", 0, "");
	}

	public String getAuthor() throws RemoteException {
		return author;
	}

	public int getMark() throws RemoteException {
		return mark;
	}

	public String getContent() throws RemoteException {
		return content;
	}

	public long getTime() throws RemoteException {
		return time;
	}

}
