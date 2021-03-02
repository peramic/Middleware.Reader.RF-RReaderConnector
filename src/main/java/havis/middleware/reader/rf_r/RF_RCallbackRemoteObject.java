package havis.middleware.reader.rf_r;

import havis.middleware.ale.reader.Callback;
import havis.middleware.ale.reader.CallbackRemoteObject;

import java.rmi.RemoteException;

/**
 * Callback object must be created in the class loader of the reader to enable
 * loading of all necessary classes.
 */
public class RF_RCallbackRemoteObject extends CallbackRemoteObject {

	private static final long serialVersionUID = 1L;

	public RF_RCallbackRemoteObject(Callback callback) throws RemoteException {
		super(callback);
	}
}
