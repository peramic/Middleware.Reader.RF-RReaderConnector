package havis.middleware.reader.rf_r400;

import havis.middleware.ale.reader.ReaderConnectorRemoteServer;

public class NotificationRF_R400ReaderConnectorServer extends ReaderConnectorRemoteServer {
	public NotificationRF_R400ReaderConnectorServer() {
		super(new NotificationRF_R400ReaderConnector());
	}
}
