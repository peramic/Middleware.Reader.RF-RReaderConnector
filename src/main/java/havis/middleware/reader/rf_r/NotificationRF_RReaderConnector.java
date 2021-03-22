package havis.middleware.reader.rf_r;

import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.base.message.Message;
import havis.middleware.ale.base.operation.port.PortObservation;
import havis.middleware.ale.base.operation.port.PortOperation;
import havis.middleware.ale.base.operation.tag.Sighting;
import havis.middleware.ale.base.operation.tag.Tag;
import havis.middleware.ale.base.operation.tag.TagOperation;
import havis.middleware.ale.base.operation.tag.result.ReadResult;
import havis.middleware.ale.base.operation.tag.result.Result;
import havis.middleware.ale.base.operation.tag.result.ResultState;
import havis.middleware.ale.exit.Exits;
import havis.middleware.ale.reader.Callback;
import havis.middleware.ale.reader.Property;
import havis.middleware.reader.rf_r.RF_RConfiguration.OperatingModeValue;
import havis.middleware.utils.data.Calculator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import de.feig.FeHexConvert;
import de.feig.FedmBrmTableItem;
import de.feig.FedmException;
import de.feig.FedmIscReaderConst;
import de.feig.FedmIscReaderID;
import de.feig.FedmIscRssiItem;
import de.feig.FedmTaskListener;
import de.feig.FedmTaskOption;
import de.feig.TagHandler.FedmIscTagHandler;
import de.feig.TagHandler.FedmIscTagHandler_EPC_Class1_Gen2;

public abstract class NotificationRF_RReaderConnector extends RF_RReaderConnector {

	private int port;

	private volatile boolean inventoryStarted = false;

	private volatile boolean asyncTaskStarted = false;
	
	private byte lastAntennas = 0;
	
	private AtomicBoolean firstInventory = new AtomicBoolean(true);

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.Reader.NotificationRF_RReaderConnector class.
	 */
	public NotificationRF_RReaderConnector() {
		super();
	}

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.Reader.NotificationRF_RReaderConnector class.
	 * 
	 * @param callback
	 */
	public NotificationRF_RReaderConnector(Callback callback) {
		super(callback);
		this.port = this.clientCallback.getNetworkPort();
	}
	
	@Override
	public void setCallback(Callback callback) {
		super.setCallback(callback);
		this.port = this.clientCallback.getNetworkPort();
	}

	/**
	 * Method to set the operating mode to notification mode.
	 */
	@Override
	protected OperatingModeValue getOperatingMode() {
		return RF_RConfiguration.OperatingModeValue.NotificationMode;
	}

	/**
	 * Method to set the size of the Buffered Read Mode (BRM) table.
	 * 
	 * @return Status code 0 if table size was successfully set else error or
	 *         status code.
	 */
	@Override
	protected int setReaderTableSize() {
		// The integrated tables for Buffered Read Mode (BRM) and ISO Host Mode
		// are not initialized. Before the initial communication, you must set
		// the table size using the method SetTableSize. The size is selected
		// equal to the maximum number of transponders located in the antenna
		// field at the same time. Only the size of the table actually being
		// used needs to be set.
		try {
			readerLock.lock();
			try {
				this.reader.setTableSize(FedmIscReaderConst.BRM_TABLE, this.readerConnection.getConnectionProperties().getTagsInField(),
						this.readerConnection.getConnectionProperties().getBlockCount(), this.readerConnection.getConnectionProperties()
								.getBlockSize(), this.readerConnection.getConnectionProperties().getBlockCount(), this.readerConnection
								.getConnectionProperties().getBlockSize());
				return 0;
			} finally {
				readerLock.unlock();
			}
		} catch (FedmException e) {
			return e.getErrorcode();
		}
	}

	@Override
	public void executePortOperation(long id, PortOperation operation) throws ValidationException, ImplementationException {
		throw new ValidationException("GPIOs are not supported yet");
	}

	@Override
	public void definePortObservation(long id, PortObservation observation) throws ValidationException, ImplementationException {
		throw new ValidationException("GPIOs are not supported yet");
	}

	@Override
	protected Map<String, Object> setDefaultProperties(Map<String, String> originalProperties, Map<String, Object> properties) throws ImplementationException {
		properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.Transmission.Destination.IPv4.IPAddress,
				getLocalAddress(originalProperties.get(Property.Connector.Host), originalProperties.get(Property.Connector.Port)).getAddress());
		properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.Transmission.Destination.PortNumber, Integer.valueOf(this.port));

		// enable reader keep alive to get diagnostic messages
		properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.Transmission.KeepAlive.Enable, (byte) 1);
		properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.Transmission.KeepAlive.IntervalTime, Integer.valueOf(2));

		if (properties.get(de.feig.ReaderConfig.OperatingMode.NotificationMode.Filter.TransponderValidTime) == null) {
			int readerCycleDuration = Math.max(0, this.clientCallback.getReaderCycleDuration());
			properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.Filter.TransponderValidTime, readerCycleDuration / 100);
		}
		properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSelector.UID, (byte) 1);
		properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSelector.AntennaNo, (byte) 0);
		// Support input events - For future use
		properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSelector.InputEvents, (byte) 0);
		properties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSelector.RSSI, (byte) 1);
		return properties;
	}

	@Override
	public void connect() throws ValidationException, ImplementationException {
		boolean retry = false;
		do {
			super.connect();
			try {
				// initially switch off RF
				reader.setData(FedmIscReaderID.FEDM_ISC_TMP_RF_ONOFF, (byte) 0x00);
				reader.sendProtocol((byte) 0x6A);
			} catch (Exception e) {
				throw new ImplementationException("Failed to disable RF: " + e.getMessage());
			}
			try {
				startAsyncTask();
				retry = false;
			} catch (Exception e) {
				// disconnect to avoid broken reader instance
				this.disconnect();
				if (e instanceof FedmException && ((FedmException) e).getErrorcode() == -4086) {
					if (!retry) {
						// do one retry with a different free port
						retry = true;
						int blockedPort = this.port;
						this.port = this.clientCallback.getNetworkPort();
						this.clientCallback.resetNetwortPort(blockedPort);
						this.configurationProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.Transmission.Destination.PortNumber,
								Integer.valueOf(this.port));
					} else
						throw new ImplementationException("Port number to receive reader messages (" + port + ") is already in use, please try again");
				} else
					throw new ImplementationException(e.getMessage());
			}
		} while (retry);
	}

	@Override
	public void disconnect() throws ImplementationException {
		stopAsyncTask();
		// wait for 1s to avoid segmentation fault
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// ignore
		}
		super.disconnect();
	}

	@Override
	protected void updateInventorySettings(RF_RInventoryOperation oldInventoryOperation, RF_RInventoryOperation newInventoryOperation)
			throws ValidationException, ImplementationException {
		super.updateInventorySettings(oldInventoryOperation, newInventoryOperation);
		Map<String, Object> newProperties = new HashMap<String, Object>();
		boolean first = firstInventory.compareAndSet(true, false);
		if (newInventoryOperation.isUser()
				&& (first || !oldInventoryOperation.isUser() || newInventoryOperation.isForceUserComplete() != oldInventoryOperation.isForceUserComplete() || !Arrays
						.equals(newInventoryOperation.getUserDataBlocks(), oldInventoryOperation.getUserDataBlocks()))) {
			newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSelector.Data, (byte) 1);
			newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSource.BankNo, (byte) 3);
			newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSource.ByteOrderOfData, (byte) 0);

			if (newInventoryOperation.isForceUserComplete()) {
				newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSelector.Mode.ReadCompleteBank, (byte) 1);
				// first data block must be reset, otherwise the reader will start reader at that block
				newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSource.FirstDataBlock, 0);
				newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSource.NoOfDataBlocks, 0);
			} else {
				newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSelector.Mode.ReadCompleteBank, (byte) 0);

				boolean[] dataBlocks = newInventoryOperation.getUserDataBlocks();
				int dataBlocksCount = dataBlocks == null ? 0 : dataBlocks.length;

				boolean foundBlock = false;
				int offset = 0;
				int length = 0;

				if (dataBlocks != null && dataBlocksCount > 0) {
					// find first block to read
					for (offset = 0; offset < dataBlocksCount; offset++) {
						if (dataBlocks[offset]) {
							foundBlock = true;
							break;
						}
					}
				}

				if (foundBlock && dataBlocks != null) {
					// count number of blocks to read
					int last;
					for (last = dataBlocksCount - 1; last >= offset; last--)
						if (dataBlocks[last])
							break;

					length = (last - offset) + 1;
				}

				newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSource.FirstDataBlock, offset);
				newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSource.NoOfDataBlocks, length);
			}
		} else if (first || oldInventoryOperation.isUser()) { // is first or was enabled before
			newProperties.put(de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSelector.Data, (byte) 0);
		}

		if (newProperties.size() > 0) {
			this.readerConfiguration.applyReaderConfig(newProperties);
		}
	}

	@Override
	protected void startInventory() throws ValidationException, ImplementationException {
		readerLock.lock();
		try {
			this.setAntennas();
		} finally {
			readerLock.unlock();
		}

		readerLock.lock();
		try {
			if (!inventoryStarted) {
				inventoryStarted = true;
				try {
					// configure multiplexer
					if (this.antennas != this.lastAntennas) {
						this.lastAntennas = this.antennas;
						Map<String, Object> properties = new HashMap<>();
						properties.put(de.feig.ReaderConfig.AirInterface.Multiplexer.Enable, (byte) 1);
						properties.put(de.feig.ReaderConfig.AirInterface.Multiplexer.UHF.Internal.AntennaSelectionMode, (byte) 0x01);
						properties.put(de.feig.ReaderConfig.AirInterface.Multiplexer.UHF.Internal.SelectedAntennas, this.antennas);
						this.readerConfiguration.applyReaderConfig(properties);
					}
					// enable the first configured antenna
					for (int i = 0; i < this.connectTypes.size(); i++) {
						if (((this.antennas & 0xFF) & (1 << i)) != 0) {
							reader.setData(FedmIscReaderID.FEDM_ISC_TMP_RF_ONOFF, (byte) 0x01);
							reader.setData(FedmIscReaderID.FEDM_ISC_TMP_RF_ONOFF_ANT_NR, (byte) (i + 1));
							reader.sendProtocol((byte) 0x6A);
							break;
						}
					}
				} catch (Exception e) {
					clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Failed to configure antennas: " + e.getMessage(), e));
				}
			}
		} finally {
			readerLock.unlock();
		}
	}

	private void startAsyncTask() throws Exception {
		readerLock.lock();
		try {
			if (!asyncTaskStarted) {
				// task initialization class
				FedmTaskOption taskInit = new FedmTaskOption();

				// settings for the listener task
				taskInit.setIpPort(port);
				// total timeout (in seconds after receiving the first bytes of
				// a
				// record) is set internally to 10s
				// This is enough for small local networks
				// can be adapted to the network capabilities (Internet, GPRS,
				// ..)
				// with
				// setInventoryTimeout()
				// TODO: add inventory timeout to RF_RProperties
				taskInit.setInventoryTimeout((byte) 30);
				// Reader waits for an acknowledge, before the next records are
				// transmitted.
				taskInit.setNotifyWithAck(1);

				// When the Ethernet cable gets broken while an active
				// communication,
				// the server-side application (Host) may not indicate an error
				// while he
				// is listening for new transmissions. On the other side, the
				// Reader
				// will run in an error with the next transmission and can close
				// and
				// reopen the socket. But the close and reopen will never be
				// noticed
				// by
				// the Host, as he is listening at a half-closed port.

				// The solution for this very realistic scenario is the
				// activating
				// of
				// the Keep-Alive option on the server-side

				// it is strongly recommended to enable the Keep-Alive option
				taskInit.setKeepAlive(true);
				// TODO: add keep alive idle and interval time and to
				// RF_RProperties
				taskInit.setKeepAliveIdleTime(500);
				taskInit.setKeepAliveIntervalTime(500);
				taskInit.setKeepAliveProbeCount(5); // applicable only for Linux

				reader.startAsyncTask(FedmTaskOption.ID_NOTIFICATION, taskListener, taskInit);
				asyncTaskStarted = true;
			}
		} finally {
			this.readerLock.unlock();
		}
	}

	@Override
	protected void stopInventory() {
		readerLock.lock();
		try {
			if (inventoryStarted) {
				inventoryStarted = false;
				try {
					reader.setData(FedmIscReaderID.FEDM_ISC_TMP_RF_ONOFF, (byte) 0x00);
					reader.sendProtocol((byte) 0x6A);
				} catch (Exception e) {
					clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Failed to disable RF: " + e.getMessage(), e));
				}
			}
		} finally {
			this.readerLock.unlock();
		}
	}

	private void stopAsyncTask() {
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
				// deadlock scenario possible? (See 15.6.2)
				if (asyncTaskStarted && (error = reader.cancelAsyncTask()) == 0) {
					asyncTaskStarted = false;
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
	protected ReadResult readBankFromTag(FedmIscTagHandler_EPC_Class1_Gen2 tag, int bank, RF_RInventoryOperation inventoryOperation) {
		return new ReadResult(ResultState.OP_NOT_POSSIBLE_ERROR);
	}

	@Override
	protected Map<Integer, Result> executeOperationOnTag(FedmIscTagHandler_EPC_Class1_Gen2 executeTag) throws Exception {
		// execute - for future use
		throw new UnsupportedOperationException("Executing operations on tag is not supported");
	}

	private FedmTaskListener taskListener = new FedmTaskListener() {

		@Override
		public void onNewTag(int paramInt) {
			// nothing to do
		}

		/**
		 * Listener method for the transponder data coming with notification
		 * event.
		 * 
		 * @param error
		 *            error code (&lt;0) or OK (0) or reader status byte (&gt;0)
		 * @param remoteIP
		 *            IP-Address of the reader
		 * @param portNr
		 *            port number of the local port which has received the
		 *            notification
		 */
		@Override
		public void onNewNotification(int error, String remoteIP, int portNr) {
			if (!inventoryStarted)
				return;

			if (!readerConnection.getHost().equals(remoteIP) || port != portNr) {
				// ignore
				return;
			}
			if (error != 0) {
				// leave the callback as fast as possible
				logReaderError("Error " + error + " occurred: " + reader.getErrorText(error));
				return;
			}

			// process the notification, but leave the callback as fast as
			// possible
			FedmBrmTableItem[] brmItems = null;
			try {
				brmItems = (FedmBrmTableItem[]) reader.getTable(FedmIscReaderConst.BRM_TABLE);

				if (brmItems.length > 0) {
					for (FedmBrmTableItem tag : brmItems) {
						Tag reportTag = null;

						if (getIdentifierMode() == RF_RConfiguration.TranspoderIdentifierModeValue.AutomaticMode) {
							byte pcHigh = (byte) (getProtocolControl(tag) & 0xFF);
							int length = ((pcHigh & 0xFF) >> 3) * 4;
							String uid = tag.getUid();
							// workaround for ETBv1 tag misconfigured in battery
							// mode
							if (uid.length() < length) {
								try {
									Field field = FedmIscTagHandler.class.getDeclaredField("tabItem");
									field.setAccessible(true);
									FedmBrmTableItem tabItem = (FedmBrmTableItem) field.get(tag);
									byte newLengthBits = (byte) ((uid.length() / 4) << 3);
									byte indicatorBits = (byte) (pcHigh & 7);
									byte newPcHigh = (byte) (indicatorBits | newLengthBits);
									tabItem.class1Gen2PC[0] = newPcHigh;
									logReaderError("Tag \"" + uid + "\" specified an unexpected length of " + length / 2 + " bytes in PC!");
								} catch (Exception e) {
									// skip
								}
							}
						}

						String epc = "";
						try {
							epc = getEpcOfUid(tag);
						} catch (StringIndexOutOfBoundsException ex) {
							/*
							 * HOTFIX for problem that reader reports OK state
							 * on ARM platform although an ISO tag is in the
							 * field
							 */
							logIsoError("An ISO tag is located in the reader field, firmware activation is needed!");
							break;

						}

						reportTag = new Tag(epc.length() == 0 ? new byte[0] : FeHexConvert.hexStringToByteArray(epc));

						if (Tag.isExtended())
							reportTag.setTid("".equals(getTidOfUid(tag)) ? new byte[0] : FeHexConvert.hexStringToByteArray(getTidOfUid(tag)));

						byte[] pc = RFCUtils.shortToBytes((short) getProtocolControl(tag));
						reportTag.setPc(new byte[] { pc[1], pc[0] });
						try {
							if (tag.getRSSI() != null && tag.getRSSI().size() > 0) {
								Entry<Integer, FedmIscRssiItem> rssiEntry = tag.getRSSI().entrySet().iterator().next();
								reportTag.setSighting(new Sighting(readerConnection.toString(), rssiEntry.getValue().antennaNumber, rssiEntry.getValue().RSSI, reportTag.getFirstTime()));
							}
						} catch (Exception e) {
							clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Exception occurred during Inventory: " + e.getMessage(), e));
							continue;
						}

						ReadResult[] readResult = new ReadResult[4];

						boolean readSuccess = true;
						Map<Long, TagOperation> tagOperations = new HashMap<>();
						RF_RInventoryOperation inventoryOperation = getCurrentOperations(tagOperations);

						if (inventoryOperation.isReserved()) {
							// reserved bank - future use
							// readResult[0] = this.readBankFromTag(tag,
							// FedmIscTagHandler_EPC_Class1_Gen2.BANK_RESERVED,
							// inventoryOperation);

							// check if data source is reserved bank
							// int bank =
							// configurationProperties.get(Prefix.Reader +
							// de.feig.ReaderConfig.OperatingMode.NotificationMode.DataSource.BankNo);
							readResult[0] = new ReadResult(ResultState.OP_NOT_POSSIBLE_ERROR, /* epcData */
							new byte[0]);
							// readResult[0] = new
							// ReadResult(ResultState.SUCCESS, /* epcData */new
							// byte[0]);
						}
						if (inventoryOperation.isEpc()) {
							if (readSuccess) {

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

							} else {
								readResult[1] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
							}
						}
						if (inventoryOperation.isTid()) {
							if (readSuccess) {
								if (Tag.isExtended()) {
									if (reportTag.getTid().length > 0) {
										readResult[2] = new ReadResult(ResultState.SUCCESS, reportTag.getTid());
									} else {
										readResult[2] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
										readSuccess = false;
									}
								} else {
									// TID bank in non extended mode - future
									// use
									// check if data source is tid memory bank
									// readResult[2] = this.readBankFromTag(tag,
									// FedmIscTagHandler_EPC_Class1_Gen2.BANK_TID,
									// inventoryOperation);
									// if (readResult[2].getState() !=
									// ResultState.SUCCESS)
									// readSuccess = false;
									readResult[2] = new ReadResult(ResultState.OP_NOT_POSSIBLE_ERROR, new byte[0]);
								}
							} else {
								readResult[2] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
							}
						}
						if (inventoryOperation.isUser()) {
							if (readSuccess) {
								byte[] data = tag.getByteArrayData(FedmIscReaderConst.DATA_RxDB, tag.getBlockAddress(), tag.getBlockCount());
								if (data != null && data.length > 0) {
									boolean[] dataBlocks = inventoryOperation.getUserDataBlocks();
									int dataBlocksCount = dataBlocks == null ? 0 : dataBlocks.length;
									int offset = 0;
									byte[] result = data;
									if (dataBlocks != null && dataBlocksCount > 0) {
										for (offset = 0; offset < dataBlocksCount; offset++) {
											if (dataBlocks[offset]) {
												// found first block
												result = new byte[offset * 2];
												result = Calculator.concat(result, data);
												break;
											}
										}
									}
									readResult[3] = new ReadResult(ResultState.SUCCESS, result);
								} else {
									readResult[3] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
									readSuccess = false;
								}
							} else {
								readResult[3] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
							}
						}

						sendInventoryReport(new InventoryReport(reportTag, readResult), tagOperations);
					}
				} else {
					executeOperation(null, 0);
				}
			} catch (FedmException e) {
				clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Exception occurred during Inventory: " + e.getMessage(), e));
			} catch (Exception e) {
				clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Exception occurred during Inventory: " + e.getMessage(), e));
			}
		}

		/**
		 * Listener method for the reader diagnostic data coming with
		 * notification event.
		 * 
		 * @param error
		 *            code (&lt;0) or OK (0) or reader status byte (&gt;0)
		 * @param ip
		 * @param portNr
		 */
		@Override
		public void onNewReaderDiagnostic(int error, String remoteIP, int portNr) {
			if (!readerConnection.getHost().equals(remoteIP) || port != portNr) {
				// ignore
				return;
			}
			
			// TODO: interpret error here
			if (error != 0) {
				clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Diagnostic: " + error));
				try {
					RF_RStatus state = RF_RStatus.forValue(error);
					clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Diagnostic parsed: " + state.toString()));
				}
				catch (Exception e) {
					clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Diagnostic failed to parse!"));
				}
			}
		}

		/**
		 * @param error
		 * @param input
		 * @param remoteIP
		 * @param portNumber
		 */
		@Override
		public void onNewInputEvent(int error, byte input, String remoteIP, int portNumber) {
			// nothing to do
		}

		/**
		 * this method is called only from Reader working in Antenna Gate and
		 * configured to process People Counter events
		 * 
		 * @param error
		 * @param counter1
		 * @param counter2
		 * @param counter3
		 * @param counter4
		 * @param remoteIP
		 * @param portNumber
		 * @param busAddress
		 */
		@Override
		public void onNewPeopleCounterEvent(int error, int counter1, int counter2, int counter3, int counter4, String remoteIP, int portNumber, int busAddress) {
			// nothing to do
		}

		/**
		 * Listener method for the SAM data received by background process.
		 * 
		 * @param error
		 *            error code (&lt;0) or OK (0)
		 * @param responseData
		 */
		@Override
		public void onNewSAMResponse(int error, byte[] responseData) {
			// nothing to do
		}

		/**
		 * Listener method for the transponder data received by background
		 * Command Queue process.
		 * 
		 * @param error
		 *            error code (&lt;0) or OK (0) or reader status byte (&gt;0)
		 */
		@Override
		public void onNewQueueResponse(int error) {
			// nothing to do
		}

		/**
		 * Listener method for the transponder data received by background Apdu
		 * process.
		 * 
		 * @param error
		 *            error code (&lt;0) or OK (0)
		 */
		@Override
		public void onNewApduResponse(int error) {
			// nothing to do
		}
	};

	@SuppressWarnings("unused")
	private void print(FedmBrmTableItem tag, String epc, Tag reportTag) {
		// <<<<<<<<<<<<<<<<<<<<<<<<<<< test

		System.out.println(configurationProperties.toString());

		System.out.println("EPC: " + epc);
		reportTag.setTid("".equals(getTidOfUid(tag)) ? new byte[0] : FeHexConvert.hexStringToByteArray(getTidOfUid(tag)));
		System.out.println("Tid: " + FeHexConvert.byteArrayToHexString(reportTag.getTid()));

		// UID / serialNumber / epc
		if (tag.isDataValid(FedmIscReaderConst.DATA_SNR)) {
			String serialNumber = tag.getStringData(FedmIscReaderConst.DATA_SNR);
			System.out.println("serialNumber " + serialNumber);
		}

		// Data >> EPC Bank or TID Bank or USER Bank
		if (tag.isDataValid(FedmIscReaderConst.DATA_RxDB)) {
			byte[] b = tag.getByteArrayData(FedmIscReaderConst.DATA_RxDB, tag.getBlockAddress(), tag.getBlockCount());
			String data = FeHexConvert.byteArrayToHexString(b);
			System.out.println("data " + data);
		}

		// AntennaNo / antenna number
		if (tag.isDataValid(FedmIscReaderConst.DATA_ANT_NR)) {
			String antNr = tag.getStringData(FedmIscReaderConst.DATA_ANT_NR);
			System.out.println("antNr " + antNr);
		}

		// transponder type
		if (tag.isDataValid(FedmIscReaderConst.DATA_TRTYPE)) {
			String type = tag.getStringData(FedmIscReaderConst.DATA_TRTYPE);
			System.out.println("type " + type);
		}

		// Timer
		if (tag.isDataValid(FedmIscReaderConst.DATA_TIMER)) {
			String time = tag.getReaderTime().getTime();
			System.out.println("Timer " + time);
		}

		// date
		if (tag.isDataValid(FedmIscReaderConst.DATA_DATE)) {
			String date = tag.getReaderTime().getDate();
			System.out.println("date " + date);
		}

		// RSSI value
		if (tag.isDataValid(FedmIscReaderConst.DATA_IS_RSSI)) {
			HashMap<Integer, FedmIscRssiItem> dicRSSI;
			try {
				dicRSSI = tag.getRSSI();
				System.out.println("RSSI");
				for (Map.Entry<Integer, FedmIscRssiItem> entry : dicRSSI.entrySet()) {
					System.out.println(">>RSSI " + entry.getKey() + " >> " + entry.getValue().toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// input, state
		if (tag.isDataValid(FedmIscReaderConst.DATA_INPUT)) {
			String input = tag.getStringData(FedmIscReaderConst.DATA_INPUT);
			System.out.println("input " + input);
			String state = tag.getStringData(FedmIscReaderConst.DATA_STATE);
			System.out.println("state " + state);
		}

		System.out.println("### ITEM");
		System.out.println("getBlockCount " + tag.getBlockCount());
		System.out.println("getEpcC1G2MaskDesignerID " + tag.getEpcC1G2MaskDesignerID());
		System.out.println("getEpcC1G2MaskDesignerName " + tag.getEpcC1G2MaskDesignerName());
		System.out.println("getEpcC1G2TagModelNumber " + tag.getEpcC1G2TagModelNumber());
		System.out.println("getIdentifier " + tag.getIdentifier());
		System.out.println("getISO15693Manufacturer " + tag.getISO15693Manufacturer());
		System.out.println("getUid " + tag.getUid());
		System.out.println("getBlockAddress " + tag.getBlockAddress());
		System.out.println("getReaderTime " + tag.getReaderTime());
		try {
			System.out.println("getRSSI " + tag.getRSSI());
		} catch (Exception e) {

		}

		System.out.println("AFI " + tag.AFI);
		System.out.println("antennaNumber " + tag.antennaNumber);
		System.out.println("blockCount " + tag.blockCount);
		System.out.println("blockSize " + tag.blockSize);
		System.out.println("class1Gen2PC " + FeHexConvert.byteArrayToHexString(tag.class1Gen2PC));
		System.out.println("class1Gen2XPC_W1 " + FeHexConvert.byteArrayToHexString(tag.class1Gen2XPC_W1));
		System.out.println("dbAddress " + tag.dbAddress);
		System.out.println("direction " + tag.direction);
		System.out.println("DsfID " + tag.DsfID);
		System.out.println("IDDT " + tag.IDDT);
		System.out.println("input " + tag.input);

		System.out.println("isAntNr " + tag.isAntNr);
		System.out.println("isDATE " + tag.isDATE);
		System.out.println("isDB " + tag.isDB);
		System.out.println("isDirection " + tag.isDirection);
		System.out.println("isEpc " + tag.isEpc);
		System.out.println("isInput " + tag.isInput);
		System.out.println("isMacAddress " + tag.isMacAddress);
		System.out.println("isRSSI " + tag.isRSSI);
		System.out.println("isTIMER " + tag.isTIMER);

		System.out.println("isUid " + tag.isUid);
		System.out.println("macAddress " + FeHexConvert.byteArrayToHexString(tag.macAddress));
		System.out.println("rxPubData " + FeHexConvert.byteArrayToHexString(tag.rxPubData));

		System.out.println("state " + tag.state);
		System.out.println("time " + tag.time);
		System.out.println("transponderType " + tag.transponderType);
		System.out.println("uid " + FeHexConvert.byteArrayToHexString(tag.uid));

		// NotificationRF_RReaderConnector.this.clientCallback.notify(id,
		// tag);
	}

	@Override
	public void dispose() throws ImplementationException {
		super.dispose();
		this.clientCallback.resetNetwortPort(this.port);
	}
}
