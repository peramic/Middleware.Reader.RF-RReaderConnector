package havis.middleware.reader.rf_r500;

import havis.middleware.ale.reader.ReaderConnectorRemoteServer;

public class NotificationRF_R500ReaderConnectorServer extends ReaderConnectorRemoteServer {
	public NotificationRF_R500ReaderConnectorServer() {
		super(new NotificationRF_R500ReaderConnector());
	}
}
