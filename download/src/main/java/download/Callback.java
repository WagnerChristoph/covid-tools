package download;

public interface Callback<T> {
	void onSuccess(T item);
	void onError(Throwable t);
}
