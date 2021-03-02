package havis.middleware.reader.rf_r400;

import havis.middleware.ale.base.message.Message;
import havis.middleware.ale.exit.Exits;
import havis.middleware.ale.reader.Callback;
import havis.middleware.reader.rf_r.NotificationRF_RReaderConnector;
import havis.util.monitor.Capabilities;
import havis.util.monitor.CapabilityType;
import havis.util.monitor.DeviceCapabilities;

import java.util.ArrayList;
import java.util.List;

import de.feig.FedmIscReaderConst;

public class NotificationRF_R400ReaderConnector extends NotificationRF_RReaderConnector {
	private static final int CONNECTOR_VERSION = 102;

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.Reader.NotificationRF_R400ReaderConnector class.
	 */
	public NotificationRF_R400ReaderConnector() {
		super();
		this.readerConfiguration = new RF_R400Configuration(this.reader, true);
		super.devCaps.setModel("Notification RF-R400");
	}

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.Reader.NotificationRF_R400ReaderConnector class.
	 */
	public NotificationRF_R400ReaderConnector(Callback callback) {
		super(callback);
		this.readerConfiguration = new RF_R400Configuration(this.reader, true);
		super.devCaps.setModel("Notification RF-R400");
	}

	/**
	 * Method to check reader info.
	 */
	@Override
	protected void checkReaderInfo() {
		this.readerLock.lock();
		try {
			this.reader.getReaderInfo();
			if (FedmIscReaderConst.TYPE_ISCLRU1002 != this.reader.getReaderType()) {
				this.clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Connected Reader Type is not RF-R400 Reader!"));
			}

			String verInfo = "";

			try {
				verInfo = this.reader.sendProtocol((byte) 0x66, "00");
			} catch (Exception e) {
			}

			int version;
			try {

				version = (Integer.parseInt(verInfo.substring(0, 2), 16) * 100) + Integer.parseInt(verInfo.substring(2, 4), 16);

				if (verInfo.length() >= 6) {
					StringBuilder firmware = new StringBuilder();
					char[] verChars = verInfo.substring(0, 6).toCharArray();

					/* insert '.' every two chars into the firmware string */
					for (int i = 0; i < verChars.length; i += 2) {
						StringBuilder versionPart = new StringBuilder().append(verChars[i]).append(verChars[i + 1]);
						if (firmware.length() > 0)
							firmware.append('.');
						firmware.append(String.format("%02d", Integer.valueOf(versionPart.toString(), 16)));
					}

					super.devCaps.setFirmware(firmware.toString());

				} else
					super.devCaps.setFirmware(verInfo);

				if (version > CONNECTOR_VERSION) {
					String msg = "Connected Reader has Frimware Version '" + version + "' installed connector was developed for Version '" + CONNECTOR_VERSION
							+ "' if any kind of problems occurred please ask support for Connector update!";
					this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, msg));
					this.notifyFirmwareWarning(msg);
				} else if (version < CONNECTOR_VERSION) {

					String msg = "Connected Reader has Firmware Version '" + version + "' installed connector was developed for Version '" + CONNECTOR_VERSION
							+ "' if any kind of problems occurred please ask support for Firmware Version update!";

					this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, msg));
					this.notifyFirmwareError(msg);
				} else {
					this.notifyFirmwareWarningResolved();
					this.notifyFirmwareErrorResolved();
				}

			} catch (NumberFormatException | StringIndexOutOfBoundsException e) {

			}
		} finally {
			this.readerLock.unlock();
		}
	}

	@Override
	protected void startPortObservation() throws Exception {
		// TODO: support inputs
	}

	@Override
	protected void stopPortObservation() {
	}

	@Override
	public List<Capabilities> getCapabilities(CapabilityType capType) {
		if (isDisposed)
			return null;

		List<Capabilities> ret = new ArrayList<>();
		if (capType == CapabilityType.ALL || capType == CapabilityType.DEVICE_CAPABILITIES) {
			ret.add(new DeviceCapabilities(super.devCaps.getName(), super.devCaps.getManufacturer(), super.devCaps.getModel(),
					super.devCaps.getFirmware()));
		}
		return ret;
	}

	@Override
	protected byte checkAntennas() {
		return 0x0F;
	}
}
