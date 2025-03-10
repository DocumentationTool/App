package com.wonkglorg.doc.core.response;

import com.wonkglorg.doc.core.objects.RepoId;

import java.util.function.BinaryOperator;

/**
 * A response for a database query operation, indicating its success or failure with an optional message for more information, alongside the data returned by the query
 *
 * @param <T>
 */
public class QueryDatabaseResponse<T> extends Response{
	/**
	 * The data returned by this query
	 */
	private T data;
	
	private QueryDatabaseResponse(RepoId causingRepo, String response, Exception exception, T data) {
		super(causingRepo, response, exception);
		this.data = data;
	}
	
	/**
	 * Constructs a new Sucess response with the attached data
	 *
	 * @param causingRepo the repo this response originated from
	 * @param data the data the query returns
	 * @param <T>
	 * @return the returned response
	 */
	public static <T> QueryDatabaseResponse<T> success(RepoId causingRepo, T data) {
		return new QueryDatabaseResponse<>(causingRepo, null, null, data);
	}
	
	/**
	 * Constructs a new Success response with the attached data and a message
	 *
	 * @param causingRepo the repo this response originated from
	 * @param response the message to respond with
	 * @param data the data provided
	 * @param <T>
	 * @return the constructed response
	 */
	public static <T> QueryDatabaseResponse<T> success(RepoId causingRepo, String response, T data) {
		return new QueryDatabaseResponse<>(causingRepo, response, null, data);
	}
	
	/**
	 * Constructs a new Failed response with the causing exception
	 *
	 * @param causingRepo the repo this response originated from
	 * @param e the exception that causes the error
	 * @param <T>
	 * @return the constructed reponse
	 */
	public static <T> QueryDatabaseResponse<T> fail(RepoId causingRepo, Exception e) {
		return new QueryDatabaseResponse<>(causingRepo, null, e, null);
	}
	
	/**
	 * Combines the data of this response with the data of another response using the provided merger, if the other response is not an error, otherwise sets the error of this response to the error of the given response
	 * @param other the other response to merge with
	 * @param merger the function to merge the data with
	 */
	public void merge(QueryDatabaseResponse<T> other, BinaryOperator<T> merger) {
		if(other.data != null){
			if(other.isError()){
				this.setError(other.getException());
				return;
			}
			data = data == null ? other.data : merger.apply(data, other.data);
		}
	}
	
	public T get() {
		return data;
	}
	
	public T get(T defaultValue) {
		return data == null ? defaultValue : data;
	}
}
