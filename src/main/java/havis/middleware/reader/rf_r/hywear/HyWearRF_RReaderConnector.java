package havis.middleware.reader.rf_r.hywear;

import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.base.message.Message;
import havis.middleware.ale.base.operation.port.PortObservation;
import havis.middleware.ale.base.operation.port.PortOperation;
import havis.middleware.ale.base.operation.tag.Tag;
import havis.middleware.ale.base.operation.tag.TagOperation;
import havis.middleware.ale.base.operation.tag.result.ReadResult;
import havis.middleware.ale.base.operation.tag.result.Result;
import havis.middleware.ale.base.operation.tag.result.ResultState;
import havis.middleware.ale.exit.Exits;
import havis.middleware.ale.reader.Callback;
import havis.middleware.ale.reader.Property;
import havis.middleware.reader.rf_r.RFCUtils;
import havis.middleware.reader.rf_r.RF_RConfiguration.OperatingModeValue;
import havis.middleware.reader.rf_r.RF_RInventoryOperation;
import havis.middleware.reader.rf_r.RF_RReaderConnector;
import havis.middleware.reader.rf_r.RF_RStatus;
import havis.util.monitor.Capabilities;
import havis.util.monitor.CapabilityType;
import havis.util.monitor.ConnectType;
import havis.util.monitor.DeviceCapabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import de.feig.FeHexConvert;
import de.feig.FePortDriverException;
import de.feig.FeReaderDriverException;
import de.feig.FedmBrmTableItem;
import de.feig.FedmException;
import de.feig.FedmIscReaderConst;
import de.feig.FedmIscReaderID;
import de.feig.FEDM.Core.FeException;
import de.feig.FEDM.Core.IBrmTableGroup.BrmTableItem;
import de.feig.FEDM.Core.IScanEventListener;
import de.feig.FEDM.Core.Utility.OutputSetting;
import de.feig.TagHandler.FedmIscTagHandler_EPC_Class1_Gen2;

public class HyWearRF_RReaderConnector extends RF_RReaderConnector {

	private static final int DEFAULT_TID_WORDS = 6;
	private static final int DEFAULT_USER_WORDS = 6;

	private static final int CONNECTOR_VERSION = 101;

	private int port;

	private boolean scanEventTaskStarted = false;

	private boolean inventoryStarted = false;

	public HyWearRF_RReaderConnector() {
		super();
		this.readerConfiguration = new HyWearRF_RConfiguration(this.reader, true);
		super.devCaps.setModel("HyWEAR compact");
	}

	public HyWearRF_RReaderConnector(Callback callback) {
		super(callback);
		this.readerConfiguration = new HyWearRF_RConfiguration(this.reader, true);
		super.devCaps.setModel("HyWEAR compact");
		this.port = this.clientCallback.getNetworkPort();
	}

	@Override
	public void setCallback(Callback callback) {
		super.setCallback(callback);
		this.port = this.clientCallback.getNetworkPort();
	}

	@Override
	protected Map<Short, ConnectType> initAntennas() {
		Map<Short, ConnectType> antennas = new HashMap<>();
		antennas.put((short) 1, ConnectType.TRUE);
		return antennas;
	}


	@Override
	protected void applyInventoryAntennas(Byte inventoryAntennas) {
		// no change required
	}

	@Override
	protected int getDefaultPort() {
		return 10002;
	}

	@Override
	protected OperatingModeValue getOperatingMode() {
		return OperatingModeValue.ScanMode;
	}

	@Override
	protected int setReaderTableSize() {
		try {
			readerLock.lock();
			try {
				this.reader.setTableSize(FedmIscReaderConst.BRM_TABLE, 1);
				return 0;
			} finally {
				readerLock.unlock();
			}
		} catch (FedmException e) {
			return e.getErrorcode();
		}
	}

	@Override
	protected void checkReaderInfo() {
		this.readerLock.lock();
		try {
			this.reader.getReaderInfo();
			if (FedmIscReaderConst.TYPE_HYWEAR_COMPACT != this.reader.getReaderType()) {
				this.clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Connected Reader Type is not HyWEAR compact!"));
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
	protected Map<String, Object> setDefaultProperties(Map<String, String> originalProperties, Map<String, Object> properties) throws ImplementationException {
		properties.put(de.feig.ReaderConfig.OperatingMode.ScanMode.Transmission.Destination.Mode, (byte) 0); // IP mode
		properties.put(de.feig.ReaderConfig.OperatingMode.ScanMode.Transmission.Destination.IPv4.IPAddress,
				getLocalAddress(originalProperties.get(Property.Connector.Host), originalProperties.get(Property.Connector.Port)).getAddress());
		properties.put(de.feig.ReaderConfig.OperatingMode.ScanMode.Transmission.Destination.PortNumber, Integer.valueOf(this.port));
		if (properties.get(de.feig.ReaderConfig.OperatingMode.ScanMode.Filter.TransponderValidTime) == null) {
			int readerCycleDuration = Math.max(0, this.clientCallback.getReaderCycleDuration());
			properties.put(de.feig.ReaderConfig.OperatingMode.ScanMode.Filter.TransponderValidTime, readerCycleDuration / 100);
		}
		properties.put(de.feig.ReaderConfig.OperatingMode.ScanMode.DataSelector.IDD, (byte) 1);
		properties.put(de.feig.ReaderConfig.OperatingMode.ScanMode.DataSelector.ScannerID, (byte) 1);
		properties.put(de.feig.ReaderConfig.OperatingMode.ScanMode.DataSelector.InputEvents, (byte) 0);
		properties.put(de.feig.ReaderConfig.DigitalIO.Signaler.Buzzer.ActivationSources, (byte) 0); // no buzzing
		properties.put(de.feig.ReaderConfig.DigitalIO.Signaler.LED.Mode, (byte) 1); // no automatic LED flashing
		return properties;
	}

	@Override
	public void connect() throws ValidationException, ImplementationException {
		boolean retry = false;
		do {
			super.connect();
			try {
				startScanEventTask();
				retry = false;
			} catch (Exception e) {
				// disconnect to avoid broken reader instance
				this.disconnect();
				if (e instanceof FeException && ((FeException) e).getErrorCode() == -4086) {
					if (!retry) {
						// do one retry with a different free port
						retry = true;
						int blockedPort = this.port;
						this.port = this.clientCallback.getNetworkPort();
						this.clientCallback.resetNetwortPort(blockedPort);
						this.configurationProperties.put(de.feig.ReaderConfig.OperatingMode.ScanMode.Transmission.Destination.PortNumber, Integer.valueOf(this.port));
					} else
						throw new ImplementationException("Port number to receive reader messages (" + port + ") is already in use, please try again");
				} else
					throw new ImplementationException(e.getMessage());
			}
		} while (retry);
	}

	@Override
	protected int resetReader() throws ImplementationException {
		// we have to hard reset the reader to apply configuration
		// hard reset required reconnecting the reader
		try {
			this.reader.sendProtocol((byte) 0x64, "00");
			this.reader.disConnect();
			Thread.sleep(1000);
			connectReader();
		} catch (FePortDriverException | FeReaderDriverException | FedmException | ValidationException | InterruptedException e) {
			throw new ImplementationException(e);
		}
		return 0;
	}

	private IScanEventListener scanEventListener = new IScanEventListener() {
		@Override
		public HashMap<Integer, OutputSetting> onNewScanEvent(BrmTableItem tableItem) {
			boolean success = false;
			if (inventoryStarted) {
				AtomicBoolean inHostMode = new AtomicBoolean(false);
				try {
					FedmBrmTableItem[] brmItems = (FedmBrmTableItem[]) reader.getTable(FedmIscReaderConst.BRM_TABLE);
					for (FedmBrmTableItem tag : brmItems) {
						boolean barcode = isBarcode(tag);
						String epc = "";
						if (barcode) {
							epc = tag.getUid();
						} else {
							try {
								epc = getEpcOfUid(tag);
							} catch (StringIndexOutOfBoundsException ex) {
								/*
								 * HOTFIX for problem that reader reports OK state on ARM platform although an
								 * ISO tag is in the field
								 */
								logIsoError("An ISO tag is located in the reader field, firmware activation is needed!");
								break;

							}
						}
						Tag reportTag = new Tag(epc.length() == 0 ? new byte[0] : FeHexConvert.hexStringToByteArray(epc));

						if (Tag.isExtended()) {
							if (!barcode)
								reportTag.setTid("".equals(getTidOfUid(tag)) ? new byte[0] : FeHexConvert.hexStringToByteArray(getTidOfUid(tag)));
							else {
								// for barcodes also write EPC to TID in extended mode
								reportTag.setTid(reportTag.getEpc());
							}
						}

						if (!barcode) {
							byte[] pc = RFCUtils.shortToBytes((short) getProtocolControl(tag));
							reportTag.setPc(new byte[] { pc[1], pc[0] });
						}

						ReadResult[] readResult = new ReadResult[4];

						Map<Long, TagOperation> tagOperations = new HashMap<>();
						RF_RInventoryOperation inventoryOperation = getCurrentOperations(tagOperations);

						if (inventoryOperation.isReserved()) {
							readResult[0] = new ReadResult(ResultState.OP_NOT_POSSIBLE_ERROR, new byte[0]);
						}
						if (inventoryOperation.isEpc()) {
							if (barcode) {
								readResult[1] = new ReadResult(ResultState.SUCCESS, reportTag.getEpc());
							} else {

								byte[] epcData = new byte[2 + reportTag.getPc().length + reportTag.getEpc().length];
								/* CRC */
								epcData[0] = 0x00;
								epcData[1] = 0x00;

								/* PC */
								for (int i = 0; i < reportTag.getPc().length; i++)
									epcData[2 + i] = reportTag.getPc()[i];

								/* EPC */
								for (int i = 0; i < reportTag.getEpc().length; i++)
									epcData[i + 2 + reportTag.getPc().length] = reportTag.getEpc()[i];

								readResult[1] = new ReadResult(ResultState.SUCCESS, epcData);
							}
						}
						if (inventoryOperation.isTid()) {
							if (!barcode) {
								if (Tag.isExtended()) {
									if (reportTag.getTid().length > 0) {
										readResult[2] = new ReadResult(ResultState.SUCCESS, reportTag.getTid());
									} else {
										try {
											reportTag.setTid(readBankWhileScanning(reportTag.getEpc(), FedmIscTagHandler_EPC_Class1_Gen2.BANK_TID, 0, DEFAULT_TID_WORDS, "", inHostMode));
											readResult[2] = new ReadResult(ResultState.SUCCESS, reportTag.getTid());
										} catch (FeException e) {
											clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Failed to read TID bank: " + e.getMessage(), e));
											readResult[2] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
										}
									}
								} else {
									try {
										reportTag.setTid(readBankWhileScanning(reportTag.getEpc(), FedmIscTagHandler_EPC_Class1_Gen2.BANK_TID, 0, DEFAULT_TID_WORDS, "", inHostMode));
										readResult[2] = new ReadResult(ResultState.SUCCESS, reportTag.getTid());
									} catch (FeException e) {
										clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Failed to read TID bank: " + e.getMessage(), e));
										readResult[2] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
									}
								}
							} else {
								readResult[2] = new ReadResult(ResultState.SUCCESS, reportTag.getEpc());
							}
						}
						if (inventoryOperation.isUser()) {
							if (!barcode)
								readResult[3] = readUserBankWhileScanning(reportTag.getEpc(), inventoryOperation, "", inHostMode);
							else
								readResult[3] = new ReadResult(ResultState.OP_NOT_POSSIBLE_ERROR, new byte[0]);
						}

						sendInventoryReport(new InventoryReport(reportTag, readResult), tagOperations);
						success = true;
					}
				} catch (Exception e) {
					clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Exception occurred during Inventory: " + e.getMessage(), e));
				} finally {
					if (inHostMode.compareAndSet(true, false)) {
						try {
							// back to scan mode
							readerModule.ICmd.sendProtocol((byte) 0x6A, "01");
						} catch (FeException e) {
							clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Failed to switch back to scan mode: " + e.getMessage(), e));
						}
					}
				}
			}
			// return output to reader
			HashMap<Integer, OutputSetting> outputSets = new HashMap<Integer, OutputSetting>();
			if (success) {
				outputSets.put(Integer.valueOf(0), new OutputSetting(1, 2, 1, 0, 3)); // buzzer 300ms
				outputSets.put(Integer.valueOf(1), new OutputSetting(1, 1, 1, 0, 3)); // green LED 300ms
			}
			return outputSets;
		}
	};

	protected ReadResult readUserBankWhileScanning(byte[] epc, RF_RInventoryOperation inventoryOperation, final String password, AtomicBoolean inHostMode) {
		byte[] readData = null;
		RF_RStatus readResultCode = RF_RStatus.RF_communication_error;

		try {
			boolean[] dataBlocks = inventoryOperation.getUserDataBlocks();
			int dataBlocksCount = dataBlocks == null ? 0 : dataBlocks.length;

			if (dataBlocksCount == 0 || inventoryOperation.isForceUserComplete()) {
				readResultCode = RF_RStatus.OK;
				readData = readBankWhileScanning(epc, FedmIscTagHandler_EPC_Class1_Gen2.BANK_USER, 0, DEFAULT_USER_WORDS, password, inHostMode);
			} else if (dataBlocks != null && dataBlocksCount > 0) {
				byte[] readDataBlocks;
				readData = new byte[dataBlocksCount * 2];

				for (int offset = 0; offset < dataBlocksCount; offset++) {
					int length = 0;
					// Skip not required data blocks
					while (!dataBlocks[offset] && (offset < dataBlocksCount)) {
						offset++;
					}

					if (offset < dataBlocksCount) {
						// Count number of blocks that must be read
						while ((offset + length < dataBlocksCount) && dataBlocks[offset + length]) {
							length++;
						}

						// execute read operation
						readDataBlocks = readBankWhileScanning(epc, FedmIscTagHandler_EPC_Class1_Gen2.BANK_USER, offset, length, password, inHostMode);
						readResultCode = RF_RStatus.OK;

						// if read operation was successful the data will be
						// added to the array
						if (readResultCode == RF_RStatus.OK) {
							System.arraycopy(readDataBlocks, 0, readData, offset * 2, readDataBlocks.length);
							offset += length;
						}
					}
				}
			}
		} catch (Exception e) {
			this.logReaderError("Error occurred during read bank from tag '" + FeHexConvert.byteArrayToHexString(epc) + "'!" + " \r\n" + "Exception: " + e.getMessage());
			readData = null;
			readResultCode = RF_RStatus.RF_communication_error;
		}

		ReadResult readResult;
		if (readData != null) {
			// for (int i = 0; i < readData.length; i += 2)
			// RFCUtils.reverseByteArray(readData, i, 2);

			readResult = new ReadResult();
			readResult.setData(readData);
		} else {
			readResult = new ReadResult();
			readResult.setData(new byte[0]);
		}

		if (readResultCode == RF_RStatus.OK)
			readResult.setState(ResultState.SUCCESS);

		else if (readResultCode == RF_RStatus.Authent_Error)
			readResult.setState(ResultState.PASSWORD_ERROR);

		else if (readResultCode == RF_RStatus.Tag_Error) {
			byte isoError = this.reader.getByteData(FedmIscReaderID.FEDM_ISC_TMP_B0_ISO_ERROR);
			switch (isoError) {
			case 0x03:
				readResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
				break;
			case 0x04:
				readResult.setState(ResultState.PERMISSION_ERROR);
				break;
			default:
				readResult.setState(ResultState.MISC_ERROR_TOTAL);
				break;
			}
		} else
			readResult.setState(ResultState.MISC_ERROR_TOTAL);

		return readResult;
	}

	protected byte[] readBankWhileScanning(byte[] epc, final int bank, final int offset, final int length, final String password, AtomicBoolean inHostMode) throws FeException {
		// TODO: support password
		if (inHostMode.compareAndSet(false, true)) {
			readerModule.ICmd.sendProtocol((byte) 0x6A, "80"); // switch to Maintain Host Mode
		}
		String response = readerModule.ICmd.sendProtocol((byte) 0xB0, "2331" + FeHexConvert.byteToHexString((byte) epc.length) + FeHexConvert.byteArrayToHexString(epc)
				+ FeHexConvert.byteToHexString((byte) bank) + "00" + FeHexConvert.byteToHexString((byte) offset) + FeHexConvert.byteToHexString((byte) length));
		if (response != null && response.length() > 4) {
			int blockCount = FeHexConvert.hexStringToByte(response.substring(0, 2)) & 0xFF;
			int blockSize = FeHexConvert.hexStringToByte(response.substring(2, 4)) & 0xFF;
			int len = (blockSize * 2 * blockCount) + (blockCount * 2) + 4;
			int index = 4;
			int byteIndex = 0;
			byte[] result = new byte[blockCount * blockSize];
			while (index < len) {
				index += 2; // skip gap
				byte[] block = FeHexConvert.hexStringToByteArray(response.substring(index, (index += (blockSize * 2))));
				for (int i = 0; i < block.length; i++)
					result[byteIndex++] = block[i];
			}
			return result;
		}

		return new byte[0];
	}

	@Override
	public void disconnect() throws ImplementationException {
		stopScanEventTask();
		// wait for 1s to avoid possible issues
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// ignore
		}
		super.disconnect();
	}

	private void startScanEventTask() throws Exception {
		readerLock.lock();
		try {
			if (!scanEventTaskStarted) {
				readerModule.IAsync.startScanEventTask(this.port, this.scanEventListener);
				scanEventTaskStarted = true;
			}
		} finally {
			readerLock.unlock();
		}
	}

	private void stopScanEventTask() {
		// Internally, CancelAsyncTask() sets a flag for the listener thread to
		// stop the process and to force immediately finishing.
		// CancelAsyncTask() is waiting up to 3 seconds for the thread finish
		// event.

		// If the listener thread is just calling the callback function,
		// CancelAsyncTask() returns immediately with the error code -4084
		// ("FEISC: asynchronous task is busy") and CancelAsyncTask() has to be
		// called again, until the return value is 0.

		int error = 0;
		do {
			readerLock.lock();
			try {
				if (scanEventTaskStarted && (error = readerModule.IAsync.stop()) == 0) {
					scanEventTaskStarted = false;
				}
			} finally {
				this.readerLock.unlock();
			}
			if (error == -4804) {
				// FEISC: asynchronous task is busy
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					break;
				}
			}
		} while (error == -4084);
	}

	@Override
	protected void startInventory() throws ValidationException, ImplementationException {
		readerLock.lock();
		try {
			if (!inventoryStarted) {
				inventoryStarted = true;
			}
		} finally {
			readerLock.unlock();
		}
	}

	@Override
	protected void stopInventory() {
		readerLock.lock();
		try {
			if (inventoryStarted) {
				inventoryStarted = false;
			}
		} finally {
			this.readerLock.unlock();
		}
	}

	@Override
	protected ReadResult readBankFromTag(FedmIscTagHandler_EPC_Class1_Gen2 tag, int bank, RF_RInventoryOperation inventoryOperation) {
		return new ReadResult(ResultState.OP_NOT_POSSIBLE_ERROR);
	}

	@Override
	protected Map<Integer, Result> executeOperationOnTag(FedmIscTagHandler_EPC_Class1_Gen2 executeTag) throws Exception {
		throw new UnsupportedOperationException("Executing operations on tag is not supported");
	}

	@Override
	public void executePortOperation(long id, PortOperation operation) throws ValidationException, ImplementationException {
		throw new ValidationException("GPIOs are not supported");
	}

	@Override
	public void definePortObservation(long id, PortObservation observation) throws ValidationException, ImplementationException {
		throw new ValidationException("GPIOs are not supported");
	}

	@Override
	protected void startPortObservation() throws Exception {
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
			ret.add(new DeviceCapabilities(super.devCaps.getName(), super.devCaps.getManufacturer(), super.devCaps.getModel(), super.devCaps.getFirmware()));
		}
		return ret;
	}

	@Override
	protected byte checkAntennas() {
		return 0;
	}

}
