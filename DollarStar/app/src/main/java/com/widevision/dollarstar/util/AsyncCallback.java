package com.widevision.dollarstar.util;

public interface AsyncCallback<T>
{
	public void onOperationCompleted(T result, Exception e);
}
