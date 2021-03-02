package havis.middleware.reader.rf_r400;

import havis.middleware.ale.reader.ReaderConnectorRemoteServer;

public class HostRF_R400ReaderConnectorServer extends ReaderConnectorRemoteServer {
	public HostRF_R400ReaderConnectorServer() {
		super(new HostRF_R400ReaderConnector());
	}
}
