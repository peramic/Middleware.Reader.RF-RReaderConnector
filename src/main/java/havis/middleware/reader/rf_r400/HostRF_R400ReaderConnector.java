package havis.middleware.reader.rf_r400;

import havis.middleware.ale.base.message.Message;
import havis.middleware.ale.base.operation.port.Pin;
import havis.middleware.ale.base.operation.port.Pin.Type;
import havis.middleware.ale.base.operation.port.Port;
import havis.middleware.ale.base.operation.port.result.ReadResult;
import havis.middleware.ale.base.operation.port.result.Result;
import havis.middleware.ale.base.operation.port.result.Result.State;
import havis.middleware.ale.exit.Exits;
import havis.middleware.ale.reader.Callback;
import havis.middleware.reader.rf_r.HostRF_RReaderConnector;
import havis.util.monitor.Capabilities;
import havis.util.monitor.CapabilityType;
import havis.util.monitor.DeviceCapabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.feig.FePortDriverException;
import de.feig.FeReaderDriverException;
import de.feig.FedmIscReaderConst;
import de.feig.FedmIscReaderID;

/**
 * Class that provide all functionality for communicating with an RF_R400 reader
 * with host mode.
 */
public class HostRF_R400ReaderConnector extends HostRF_RReaderConnector {	
	private static final int CONNECTOR_VERSION = 102;

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.Reader.HostRF_R400ReaderConnector class.
	 */
	public HostRF_R400ReaderConnector() {
		super();
		this.readerConfiguration = new RF_R400Configuration(this.reader, true);
		super.devCaps.setModel("Host RF-R400");
	}

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.Reader.HostRF_R400ReaderConnector class.
	 */
	public HostRF_R400ReaderConnector(Callback callback) {
		super(callback);
		this.readerConfiguration = new RF_R400Configuration(this.reader, true);
		super.devCaps.setModel("Host RF-R400");				
	}

	/**
	 * Method to check reader info.
	 */
	@Override
	protected void checkReaderInfo() {
		this.readerLock.lock();
		try {
			this.reader.getReaderInfo();
			if (FedmIscReaderConst.TYPE_ISCLRU1002 != this.reader
					.getReaderType()) {
				this.clientCallback.notify(new Message(
						Exits.Reader.Controller.Error,
						"Connected Reader Type is not RF-R400 Reader!"));
			}

			String verInfo = "";

			try {
				verInfo = this.reader.sendProtocol((byte) 0x66, "00");
			} catch (Exception e) { }

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

	/**
	 * Method to check connected antennas.
	 * 
	 * @return Byte code that indicates which antennas are connected.
	 */
	@Override
	protected byte checkAntennas() {
		return 0x0F; // all 4 antennas: 0000 1111
	}

	private Object syncObservationThread = new Object();
	private Thread observationThread;
	private boolean doObservation;
	
	/**
	 * Method to start the port observation using host mode.
	 */
	@Override
	protected void startPortObservation() throws Exception {
		synchronized (this.syncObservationThread) {
			this.getGPIOInitialState();
			this.observationThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						observation();
					} catch (InterruptedException e) {
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, "observationThread");

			this.doObservation = true;
			this.observationThread.start();
		}
	}

	@Override
	public List<Capabilities> getCapabilities(CapabilityType capabilityType) {
		if (isDisposed) return null;
		
		List<Capabilities> ret = new ArrayList<>();		
		if (capabilityType == CapabilityType.ALL || capabilityType == CapabilityType.DEVICE_CAPABILITIES) {
			ret.add(new DeviceCapabilities(
				super.devCaps.getName(),
				super.devCaps.getManufacturer(),
				super.devCaps.getModel(),
				super.devCaps.getFirmware()));
		}		
		return ret;		
	}
	
	/**
	 * Method to stop the port observation.
	 */
	@Override
	protected void stopPortObservation() {
		synchronized (this.syncObservationThread) {
			this.doObservation = false;
		}
	}

	private void observation() throws Exception {
		int status;
		Map<Integer, Result> result;
		while (this.doObservation) {
			try {
				this.readerLock.lock();
				try {
					boolean data;
					status = this.reader.sendProtocol((byte) 0x74);
					if (status == 0) {
						data = this.reader
								.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN1);
						if (data != this.inputPortState[0]) {
							result = new HashMap<>();
							result.put(
									0,
									new ReadResult(
											State.SUCCESS,
											(byte) ((inputPortState[0] = data) ? 1
													: 0)));
							Port port = new Port(new Pin(1, Type.INPUT),
									this.reader.getReaderName(), result);
							sendObservationReport(port);
						}

						data = this.reader
								.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN2);
						if (data != this.inputPortState[1]) {
							result = new HashMap<>();
							result.put(
									0,
									new ReadResult(
											State.SUCCESS,
											(byte) ((inputPortState[1] = data) ? 1
													: 0)));
							Port port = new Port(new Pin(2, Type.INPUT),
									this.reader.getReaderName(), result);
							sendObservationReport(port);

						}
					}
					notifyConnectionErrorResolved();
				} finally {
					this.readerLock.unlock();
				}
			} catch (FePortDriverException e) {
				if (e.getErrorCode() <= -1200 && e.getErrorCode() >= -1299) {
					this.disconnect();
					this.clientCallback.notify(new Message(
							Exits.Reader.Controller.ConnectionLost,
							"Connection lost to " + super.devCaps.getModel() + "!"));
					this.notifyConnectionError("Connection lost to " + super.devCaps.getModel() + "!");
					break;
				} else {
					this.clientCallback
							.notify(new Message(
									Exits.Reader.Controller.Warning,
									"Port exception occurred during observation: " + e.getMessage(), e));
				}
			} catch (FeReaderDriverException e) {
				if (e.getErrorCode() == -4035) {
					this.disconnect();
					this.clientCallback
							.notify(new Message(
									Exits.Reader.Controller.ConnectionLost,
									"Asynchron connection to "
											+ super.devCaps.getModel() + "!"));					
					this.notifyConnectionError("Asynchron connection to " + super.devCaps.getModel() + "!");					
					break;
				} else {
					this.clientCallback
							.notify(new Message(
									Exits.Reader.Controller.Warning,
									"Driver exception occurred during observation: " + e.getMessage(), e));
				}
			} catch (Exception e) {
				this.clientCallback.notify(new Message(
						Exits.Reader.Controller.Error,
						"Exception occurred during observation: " + e.getMessage(), e));
				throw e;
			}
			Thread.sleep(this.readerConnection.getConnectionProperties().getInputDelay());
		}
	}
}
