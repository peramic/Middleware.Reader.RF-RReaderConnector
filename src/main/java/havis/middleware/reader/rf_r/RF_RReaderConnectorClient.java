package havis.middleware.reader.rf_r;

import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.reader.Callback;
import havis.middleware.ale.reader.CallbackRemoteObject;
import havis.middleware.ale.reader.ReaderConnectorProcess;
import havis.middleware.ale.reader.ReaderConnectorRemoteClient;

import java.rmi.RemoteException;

public class RF_RReaderConnectorClient extends ReaderConnectorRemoteClient {

	public RF_RReaderConnectorClient(ReaderConnectorProcess process) {
		super(process);
	}

	@Override
	protected void onRedefineState() throws ImplementationException {
		// nothing to do
	}

	@Override
	protected CallbackRemoteObject newCallbackRemoteObject(Callback callback) throws RemoteException {
		return new RF_RCallbackRemoteObject(callback);
	}
}
