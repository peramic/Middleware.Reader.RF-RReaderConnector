package havis.middleware.reader.rf_r.hywear;

import havis.middleware.ale.reader.ReaderConnectorRemoteServer;

public class HyWearRF_RReaderConnectorServer extends ReaderConnectorRemoteServer {
	public HyWearRF_RReaderConnectorServer() {
		super(new HyWearRF_RReaderConnector());
	}
}