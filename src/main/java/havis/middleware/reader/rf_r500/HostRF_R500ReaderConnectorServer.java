package havis.middleware.reader.rf_r500;

import havis.middleware.ale.reader.ReaderConnectorRemoteServer;

public class HostRF_R500ReaderConnectorServer extends ReaderConnectorRemoteServer {
	public HostRF_R500ReaderConnectorServer() {
		super(new HostRF_R500ReaderConnector());
	}
}
