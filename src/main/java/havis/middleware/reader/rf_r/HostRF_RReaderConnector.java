package havis.middleware.reader.rf_r;

import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.message.Message;
import havis.middleware.ale.base.operation.tag.LockType;
import havis.middleware.ale.base.operation.tag.Operation;
import havis.middleware.ale.base.operation.tag.Sighting;
import havis.middleware.ale.base.operation.tag.Tag;
import havis.middleware.ale.base.operation.tag.TagOperation;
import havis.middleware.ale.base.operation.tag.result.CustomResult;
import havis.middleware.ale.base.operation.tag.result.KillResult;
import havis.middleware.ale.base.operation.tag.result.LockResult;
import havis.middleware.ale.base.operation.tag.result.PasswordResult;
import havis.middleware.ale.base.operation.tag.result.ReadResult;
import havis.middleware.ale.base.operation.tag.result.Result;
import havis.middleware.ale.base.operation.tag.result.ResultState;
import havis.middleware.ale.base.operation.tag.result.WriteResult;
import havis.middleware.ale.exit.Exits;
import havis.middleware.ale.reader.Callback;
import havis.middleware.reader.rf_r.RF_RConfiguration.OperatingModeValue;
import havis.middleware.utils.data.Calculator;
import havis.middleware.utils.data.Comparison;
import havis.middleware.utils.data.Converter;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.feig.FeHexConvert;
import de.feig.FePortDriverException;
import de.feig.FeReaderDriverException;
import de.feig.FedmException;
import de.feig.FedmIscReaderConst;
import de.feig.FedmIscReaderID;
import de.feig.FedmIscRssiItem;
import de.feig.FedmIsoTableItem;
import de.feig.TagHandler.FedmIscTagHandler;
import de.feig.TagHandler.FedmIscTagHandler_EPC_Class1_Gen2;
import de.feig.TagHandler.FedmIscTagHandler_Result;

public abstract class HostRF_RReaderConnector extends RF_RReaderConnector {

	private static final byte DEFAULT_MODEL_OFFSET = 0x14;

	private static final byte EPC_CUSTOM_COMMAND = (byte) 0xB4;

	private static final byte[] RFMICRON_VENDOR_ID = new byte[] { 0x00, 0x24 };
	private static final byte[] FARSENS_VENDOR_ID = new byte[] { 0x08, 0x28 };

	private static final byte RFMICRON_MODEL_OFFSET = 0x10;
	private static final byte[] RFMICRON_COMMAND_PREFIX = new byte[] { 0x24, 0x01, 0x11 };
	private static final byte RFMICRON_CODE_TEMP = 0x04;
	private static final int RFMICRON_TEMP_CALIBRATION_BANK = 3;
	private static final int RFMICRON_TEMP_CALIBRATION_OFFSET = 0x80;
	private static final int RFMICRON_TEMP_CALIBRATION_LENGTH = 64;
	private static final int RFMICRON_TEMP_CALIBRATION_CODE_LENGTH = 12;
	private static final int RFMICRON_TEMP_CALIBRATION_TEMP_LENGTH = 11;
	private static final int RFMICRON_TEMP_CALIBRATION_CODE1_OFFSET = 0x90;
	private static final int RFMICRON_TEMP_CALIBRATION_TEMP1_OFFSET = 0x9C;
	private static final int RFMICRON_TEMP_CALIBRATION_CODE2_OFFSET = 0xA7;
	private static final int RFMICRON_TEMP_CALIBRATION_TEMP2_OFFSET = 0xB3;

	private static final int FARSENS_DATA_BANK = 3;
	private static final int FARSENS_DATA_OFFSET = 0x100 * 16;
	private static final int FARSENS_BLINK_OFFSET = 0x91 * 16;
	private static final byte[] FARSENS_BLINK_DATA = new byte[] { 0x01, 0x00 };
	private static final byte FARSENS_COMMAND_BLINK = 0x01;
	private static final byte FARSENS_COMMAND_SHORT = 0x02;
	private static final byte FARSENS_COMMAND_FLOAT = 0x04;
	private static final byte FARSENS_COMMAND_THREE_SHORT = 0x06;
	private static final byte FARSENS_COMMAND_TWO_FLOAT = 0x08;
	private static final int FARSENS_DATA_FRAME_LENGTH = 32;
	private static final int FARSENS_DATA_VALUE_OFFSET = 16;
	private static final byte FARSENS_DATA_HEADER_OK = (byte) 0xAA;
	private static final byte FARSENS_DATA_QOS_BEST = (byte) 0xFF;
	private static final byte FARSENS_DATA_QOS_GOOD = (byte) 0xEE;

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.Reader.HostRF_RReaderConnector class.
	 */
	public HostRF_RReaderConnector() {
		super();
	}

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.Reader.HostRF_RReaderConnector class.
	 * 
	 * @param callback
	 */
	public HostRF_RReaderConnector(Callback callback) {
		super(callback);
	}

	/**
	 * Method to set the operating mode to host mode.
	 */
	@Override
	protected OperatingModeValue getOperatingMode() {
		return RF_RConfiguration.OperatingModeValue.HostMode;
	}

	/**
	 * Method to set the size of the ISO table.
	 * 
	 * @return Status code 0 if table size was successfully set else error or
	 *         status code.
	 */
	@Override
	protected int setReaderTableSize() {
		try {
			readerLock.lock();
			try {
				this.reader.setTableSize(FedmIscReaderConst.ISO_TABLE, (int) this.readerConnection.getConnectionProperties().getTagsInField(),
						(int) this.readerConnection.getConnectionProperties().getBlockCount(), (int) this.readerConnection.getConnectionProperties()
								.getBlockSize(), (int) this.readerConnection.getConnectionProperties().getBlockCount(), (int) this.readerConnection
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
	protected Map<String, Object> setAntennaProperties(Map<String, Object> properties) throws ImplementationException {
		properties.put(de.feig.ReaderConfig.AirInterface.Multiplexer.Enable, (byte) 0x00);
		return properties;
	}

	private Lock inventoryLock = new ReentrantLock();
	private Condition inventoryCondition = inventoryLock.newCondition();

	/**
	 * Method to start the inventory using the host mode.
	 */
	@Override
	protected void startInventory() {
		readerLock.lock();
		try {
			this.setAntennas();
		} finally {
			readerLock.unlock();
		}

		inventoryLock.lock();
		try {
			this.doInventory = true;
			if (this.inventoryThread == null) {
				this.inventoryThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							inventory();
						} catch (Exception e) {
							// FIXME: exception handling
							e.printStackTrace();
							inventoryThread = null;
						}
					}
				}, "inventoryThread");

				this.inventoryThread.start();
			} else {
				this.inventoryCondition.signal();
			}
		} finally {
			inventoryLock.unlock();
		}

	}

	private Thread inventoryThread;
	private volatile boolean doInventory;

	@Override
	/**
	 * Method to stop the inventory.
	 */
	protected void stopInventory() {
		inventoryLock.lock();
		try {
			this.doInventory = false;
			this.inventoryCondition.signal(); // to check if we are disconnected
		} finally {
			inventoryLock.unlock();
		}
	}

	/**
	 * Method to read a complete bankfrom a tag.
	 * 
	 * @param tag
	 *            The tag to read a bank from.
	 * @param bank
	 *            The bank to read.
	 * @return {@link ReadResult}
	 */
	@Override
	protected ReadResult readBankFromTag(FedmIscTagHandler_EPC_Class1_Gen2 tag, int bank, RF_RInventoryOperation inventoryOperation) {
		byte[] readData = null;
		RF_RStatus readResultCode = RF_RStatus.RF_communication_error;

		try {

			reader.setData(FedmIscReaderID.FEDM_ISC_TMP_RF_ONOFF, (byte) 0x01);
			if (tag.getRSSI() != null && tag.getRSSI().size() > 0) {
				Entry<Integer, FedmIscRssiItem> rssiEntry = tag.getRSSI().entrySet().iterator().next();
				reader.setData(FedmIscReaderID.FEDM_ISC_TMP_RF_ONOFF_ANT_NR, rssiEntry.getValue().antennaNumber);
			}
			reader.sendProtocol((byte) 0x6a);

			boolean[] dataBlocks = inventoryOperation.getUserDataBlocks();
			int dataBlocksCount = dataBlocks == null ? 0 : dataBlocks.length;

			if ((bank != FedmIscTagHandler_EPC_Class1_Gen2.BANK_USER) || (dataBlocksCount == 0 || inventoryOperation.isForceUserComplete())) {
				FedmIscTagHandler_Result result = new FedmIscTagHandler_Result();

				readResultCode = RF_RStatus.forValue(tag.readCompleteBank(bank, "", result));
				readData = result.data;
			}

			if (bank == FedmIscTagHandler_EPC_Class1_Gen2.BANK_USER && dataBlocks != null && dataBlocksCount > 0) {
				byte[] readDataBlocks;

				// merge already read data, if any
				if (readData != null) {
					byte[] data = new byte[Math.max(dataBlocksCount * 2, readData.length)];
					System.arraycopy(readData, 0, data, 0, readData.length);
					readData = data;
				} else {
					readData = new byte[dataBlocksCount * 2];
				}

				// Starts at the last read data block
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
						FedmIscTagHandler_Result result = new FedmIscTagHandler_Result();
						int readMultipleBlocks = tag.readMultipleBlocks(bank, offset, length, "", result);
						readResultCode = (RF_RStatus.forValue(readMultipleBlocks));
						readDataBlocks = result.data;

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
			this.logReaderError("Error occurred during read complete bank from tag '" + tag.getEpcOfUid() + "'!" + " \r\n" + "Exception: " + e.getMessage());
			readData = null;
			readResultCode = RF_RStatus.RF_communication_error;
		}

		ReadResult readResult;
		if (readData != null) {
			for (int i = 0; i < readData.length; i += 2)
				RFCUtils.reverseByteArray(readData, i, 2);

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

	/**
	 * Method to execute operations on matching tags from the <paramref
	 * name="executeTag"/> List.
	 * 
	 * @param executeTag
	 *            List of tags to execute operations on.
	 * @return A list with results for every operation.
	 * @throws Exception
	 */
	@Override
	protected Map<Integer, Result> executeOperationOnTag(FedmIscTagHandler_EPC_Class1_Gen2 executeTag) throws Exception {

		reader.setData(FedmIscReaderID.FEDM_ISC_TMP_RF_ONOFF, (byte) 0x01);
		if (executeTag.getRSSI() != null && executeTag.getRSSI().size() > 0) {
			Entry<Integer, FedmIscRssiItem> rssiEntry = executeTag.getRSSI().entrySet().iterator().next();
			reader.setData(FedmIscReaderID.FEDM_ISC_TMP_RF_ONOFF_ANT_NR, rssiEntry.getValue().antennaNumber);
		}
		reader.sendProtocol((byte) 0x6A);

		boolean errorOccurred = false;

		Map<Integer, Result> result = new HashMap<>();
		String password = "";
		for (Operation op : this.executeTagOperation.getValue().getOperations()) {
			if (errorOccurred) {
				Result errorResult = null;
				switch (op.getType()) {
				case KILL:
					errorResult = new KillResult(ResultState.MISC_ERROR_TOTAL);
					break;
				case LOCK:
					errorResult = new LockResult(ResultState.MISC_ERROR_TOTAL);
					break;
				case PASSWORD:
					errorResult = new PasswordResult(ResultState.MISC_ERROR_TOTAL);
					break;
				case READ:
					errorResult = new ReadResult(ResultState.MISC_ERROR_TOTAL);
					break;
				case WRITE:
					errorResult = new WriteResult(ResultState.MISC_ERROR_TOTAL);
					break;
				case CUSTOM:
					errorResult = new CustomResult(ResultState.MISC_ERROR_TOTAL);
					break;
				default:
					break;
				}

				result.put(op.getId(), errorResult);
				continue;
			}

			switch (op.getType()) {
			case KILL:
				RF_RStatus killResultCode;
				try {
					killResultCode = RF_RStatus.forValue(executeTag.kill(FeHexConvert.byteArrayToHexString(op.getData())));
				} catch (Exception e) {
					this.logReaderError("Error occurred during kill tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: " + e.getMessage());
					killResultCode = RF_RStatus.RF_communication_error;
				}
				KillResult killResult = new KillResult();
				// OK or write error is a success
				if (killResultCode == RF_RStatus.OK || killResultCode == RF_RStatus.Write_Error)
					killResult.setState(ResultState.SUCCESS);
				else if (killResultCode == RF_RStatus.Authent_Error) {
					killResult.setState(ResultState.PASSWORD_ERROR);
					errorOccurred = true;
				} else {
					killResult.setState(ResultState.MISC_ERROR_TOTAL);
					errorOccurred = true;
				}
				result.put(op.getId(), killResult);
				break;

			case LOCK:
				byte killmask = 0x00;
				byte killaction = 0x00;
				byte accessmask = 0x00;
				byte accessaction = 0x00;
				byte epcmask = 0x00;
				byte epcaction = 0x00;
				byte tidmask = 0x00;
				byte tidaction = 0x00;
				byte usermask = 0x00;
				byte useraction = 0x00;

				LockType lockType = LockType.values()[op.getData()[0]]; // TODO:
																		// risky!
				byte lockmask = 0x02;
				byte lockaction = 0x00;
				switch (lockType) {
				case LOCK:
					lockaction = 0x02;
					break;
				case PERMALOCK:
					lockmask = 0x03;
					lockaction = 0x03;
					break;
				case PERMAUNLOCK:
					lockmask = 0x03;
					lockaction = 0x01;
					break;
				case UNLOCK:
					break;
				default:
					break;
				}

				switch (op.getField().getBank()) {
				case 0:
					if (op.getField().getOffset() == 0 && op.getField().getLength() == 32) {
						killmask = lockmask;
						killaction = lockaction;
					} else if (op.getField().getOffset() == 32 && op.getField().getLength() == 32) {
						accessmask = lockmask;
						accessaction = lockaction;
					}
					break;
				case 1:
					epcmask = lockmask;
					epcaction = lockaction;
					break;
				case 2:
					tidmask = lockmask;
					tidaction = lockaction;
					break;
				case 3:
					usermask = lockmask;
					useraction = lockaction;
					break;
				default:
					break;
				}
				RF_RStatus lockResultCode;
				try {
					lockResultCode = RF_RStatus.forValue(executeTag.lock(password, killmask, killaction, accessmask, accessaction, epcmask, epcaction, tidmask,
							tidaction, usermask, useraction));
				} catch (Exception e) {
					this.logReaderError("Error occurred during lock tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: " + e.getMessage());
					lockResultCode = RF_RStatus.RF_communication_error;
				}
				LockResult lockResult = new LockResult();
				if (lockResultCode == RF_RStatus.OK)
					lockResult.setState(ResultState.SUCCESS);

				else if (lockResultCode == RF_RStatus.Authent_Error) {
					lockResult.setState(ResultState.PASSWORD_ERROR);
					errorOccurred = true;
				}

				else if (lockResultCode == RF_RStatus.Tag_Error) {
					byte isoError = this.reader.getByteData(FedmIscReaderID.FEDM_ISC_TMP_B0_ISO_ERROR);
					switch (isoError) {
					case 0x03:
						lockResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
						errorOccurred = true;
						break;
					case 0x04:
						lockResult.setState(ResultState.PERMISSION_ERROR);
						errorOccurred = true;
						break;
					default:
						lockResult.setState(ResultState.MISC_ERROR_TOTAL);
						errorOccurred = true;
						break;
					}
				} else {
					lockResult.setState(ResultState.MISC_ERROR_TOTAL);
					errorOccurred = true;
				}
				result.put(op.getId(), lockResult);
				break;

			case PASSWORD:
				password = FeHexConvert.byteArrayToHexString(op.getData());
				result.put(op.getId(), new PasswordResult(ResultState.SUCCESS));
				break;

			case READ:

				byte[] readData;
				RF_RStatus readResultCode;
				if (op.getField().getLength() == 0) {
					try {
						FedmIscTagHandler_Result rdRes = new FedmIscTagHandler_Result();
						readResultCode = RF_RStatus.forValue(executeTag.readCompleteBank(op.getField().getBank(), password, rdRes));
						readData = rdRes.data;
					} catch (Exception e) {
						this.logReaderError("Error occurred during read complete bank from tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: "
								+ e.getMessage());
						readData = null;
						readResultCode = RF_RStatus.RF_communication_error;
					}
					if ((readResultCode == RF_RStatus.OK) && (op.getField().getOffset() > 0)) {
						readData = Calculator.shift(readData, op.getField().getOffset());
					}
				} else if (op.getField().getOffset() % 16 != 0 || op.getField().getLength() % 16 != 0) {
					try {
						FedmIscTagHandler_Result tagRes = new FedmIscTagHandler_Result();
						readResultCode = RF_RStatus.forValue(executeTag.readMultipleBlocks((int) op.getField().getBank(), (int) op.getField().getOffset() / 16,
								(int) (Calculator.size(op.getField().getLength() + op.getField().getOffset(), 16) - (op.getField().getOffset() / 16)),
								password, tagRes));

						readData = tagRes.data;
					} catch (Exception e) {
						this.logReaderError("Error occurred during read multiple blocks from tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: "
								+ e.getMessage());
						readData = null;
						readResultCode = RF_RStatus.RF_communication_error;
					}
					if (readResultCode == RF_RStatus.OK)
						readData = Calculator.shift(readData, op.getField().getOffset() % 16, 8);
				} else {
					try {
						FedmIscTagHandler_Result tagRes = new FedmIscTagHandler_Result();
						readResultCode = RF_RStatus.forValue(executeTag.readMultipleBlocks((int) op.getField().getBank(), (int) op.getField().getOffset() / 16,
								(int) op.getField().getLength() / 16, password, tagRes));
						readData = tagRes.data;
					} catch (Exception e) {
						this.logReaderError("Error occurred during read multiple blocks from tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: "
								+ e.getMessage());
						readData = null;
						readResultCode = RF_RStatus.RF_communication_error;
					}
				}

				ReadResult readResult;
				if (readData != null) {
					for (int i = 0; i < readData.length; i += 2) {
						if (readData.length > i + 1)
							RFCUtils.reverseByteArray(readData, i, 2);
					}
					readResult = new ReadResult();
					readResult.setData(readData);
				} else {
					readResult = new ReadResult();
					readResult.setData(new byte[0]);
				}
				if (readResultCode == RF_RStatus.OK)
					readResult.setState(ResultState.SUCCESS);

				else if (readResultCode == RF_RStatus.Authent_Error) {
					readResult.setState(ResultState.PASSWORD_ERROR);
					errorOccurred = true;
				} else if (readResultCode == RF_RStatus.Tag_Error) {
					byte isoError = this.reader.getByteData(FedmIscReaderID.FEDM_ISC_TMP_B0_ISO_ERROR);
					switch (isoError) {
					case 0x03:
						readResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
						errorOccurred = true;
						break;
					case 0x04:
						readResult.setState(ResultState.PERMISSION_ERROR);
						errorOccurred = true;
						break;
					default:
						readResult.setState(ResultState.MISC_ERROR_TOTAL);
						errorOccurred = true;
						break;
					}
				} else {
					readResult.setState(ResultState.MISC_ERROR_TOTAL);
					errorOccurred = true;
				}

				result.put(op.getId(), readResult);

				break;

			case WRITE:
				byte[] writeData = (byte[]) op.getData().clone();
				for (int i = 0; i < writeData.length; i += 2)
					RFCUtils.reverseByteArray(writeData, i, 2);

				int length = (int) (op.getField().getLength() > 0 ? op.getField().getLength() : writeData.length * 8);
				RF_RStatus writeResultCode;
				if (op.getField().getBank() == 1 && op.getField().getOffset() < 16) {
					// Shift Write Data Outside CRC
					byte[] trunkWriteData = new byte[writeData.length - 2];
					trunkWriteData = Arrays.copyOfRange(writeData, 2, writeData.length - 2);
					try {
						writeResultCode = RF_RStatus.forValue(executeTag.writeMultipleBlocks((int) op.getField().getBank(), 1, (length - 16) / 16, password,
								trunkWriteData));
					} catch (Exception e) {
						this.logReaderError("Error occurred during write multiple blocks on tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: "
								+ e.getMessage());
						writeResultCode = RF_RStatus.RF_communication_error;
					}
				} else {
					try {
						writeResultCode = RF_RStatus.forValue(executeTag.writeMultipleBlocks((int) op.getField().getBank(),
								(int) op.getField().getOffset() / 16, length / 16, password, writeData));
					} catch (Exception e) {
						this.logReaderError("Error occurred during write multiple blocks on tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: "
								+ e.getMessage());
						writeResultCode = RF_RStatus.RF_communication_error;
					}
				}
				WriteResult writeResult;
				if (writeResultCode == RF_RStatus.OK) {
					writeResult = new WriteResult(ResultState.SUCCESS, length);
				}

				else if (writeResultCode == RF_RStatus.Authent_Error) {
					writeResult = new WriteResult(ResultState.PASSWORD_ERROR);
					errorOccurred = true;
				}

				else if (writeResultCode == RF_RStatus.Tag_Error) {
					writeResult = new WriteResult();
					byte isoError = this.reader.getByteData(FedmIscReaderID.FEDM_ISC_TMP_B0_ISO_ERROR);
					switch (isoError) {
					case 0x03:
						writeResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
						errorOccurred = true;
						break;
					case 0x04:
						writeResult.setState(ResultState.PERMISSION_ERROR);
						errorOccurred = true;
						break;
					default:
						writeResult.setState(ResultState.MISC_ERROR_TOTAL);
						errorOccurred = true;
						break;
					}
				} else {
					writeResult = new WriteResult();
					writeResult.setState(ResultState.MISC_ERROR_TOTAL);
					errorOccurred = true;
				}
				result.put(op.getId(), writeResult);
				break;

			case CUSTOM:
				CustomResult customResult = null;
				byte[] tid = "".equals(executeTag.getTidOfUid()) ? new byte[0] : FeHexConvert.hexStringToByteArray(executeTag.getTidOfUid());

				if (op.getData().length >= 2) {
					// TODO: move code into separate classes implementing
					// methods matches() and execute()
					if (op.getData().length > 4 && op.getData()[0] == RFMICRON_VENDOR_ID[0] && op.getData()[1] == RFMICRON_VENDOR_ID[1]
							&& matchesVendorAndModel(tid, RFMICRON_VENDOR_ID, new byte[] { op.getData()[2], op.getData()[3] }, RFMICRON_MODEL_OFFSET)) {
						customResult = executeRfMicronCustomCommand(executeTag, op.getData(), password);
					} else if (op.getData().length > 4 && op.getData()[0] == FARSENS_VENDOR_ID[0] && op.getData()[1] == FARSENS_VENDOR_ID[1]
							&& matchesVendorAndModel(tid, FARSENS_VENDOR_ID, new byte[] { op.getData()[2], op.getData()[3] })) {
						customResult = executeFarsensCustomCommand(executeTag, op.getData(), password);
					}
				}

				if (customResult == null) {
					customResult = new CustomResult();
					customResult.setData(new byte[0]);
					customResult.setState(ResultState.OP_NOT_POSSIBLE_ERROR);
					errorOccurred = true;
				} else if (customResult.getState() != ResultState.SUCCESS) {
					errorOccurred = true;
				}

				result.put(op.getId(), customResult);

				break;

			default:
				break;
			}
		}
		return result;
	}

	private boolean matchesVendorAndModel(byte[] tid, byte[] vendor, byte[] model, int modelOffset) {
		if (modelOffset < 0x10 || modelOffset > DEFAULT_MODEL_OFFSET)
			throw new IllegalArgumentException("Model offset must be between 0x10 and 0x16");
		if (tid.length == 0)
			return true; // always match if no TID

		byte[] tidModel = new byte[] { tid[2], tid[3] };
		if (modelOffset < DEFAULT_MODEL_OFFSET)
			tidModel = Calculator.shift(tidModel, modelOffset - DEFAULT_MODEL_OFFSET);
		return Comparison.equal(vendor, Calculator.shift(new byte[] { tid[1], tid[2] }, -4), 9, 7) && Comparison.equal(model, tidModel, 12, 4);
	}

	boolean matchesVendorAndModel(byte[] tid, byte[] vendor, byte[] model) {
		return matchesVendorAndModel(tid, vendor, model, DEFAULT_MODEL_OFFSET);
	}

	private CustomResult executeRfMicronCustomCommand(FedmIscTagHandler_EPC_Class1_Gen2 executeTag, byte[] data, String password) {
		byte[] customData = null;
		RF_RStatus customResultCode;

		// send the TID if in extended mode, otherwise send the EPC,
		// see readMultipleBlocks()
		String id = Tag.isExtended() ? executeTag.getTidOfUid() : executeTag.getEpcOfUid();
		String idLength = FeHexConvert.byteToHexString((byte) (id.length() / 2));
		byte[] command = new byte[data.length - 2];
		for (int i = 0; i < command.length; i++)
			command[i] = data[i + 2];

		try {
			String rawDataString = this.reader.sendProtocol(EPC_CUSTOM_COMMAND, FeHexConvert.byteArrayToHexString(RFMICRON_COMMAND_PREFIX) + idLength + id
					+ FeHexConvert.byteArrayToHexString(command));
			// at least 7 bytes
			if (rawDataString != null && rawDataString.length() > (7 * 2)) {
				byte[] rawData = FeHexConvert.hexStringToByteArray(rawDataString);
				// cut the response header (region, freq, db_n, db_size)
				customData = new byte[rawData.length - 7];
				for (int i = 0; i < customData.length; i++)
					customData[i] = rawData[i + 7];
				customResultCode = RF_RStatus.OK;
			} else
				customResultCode = RF_RStatus.No_valid_Data;
		} catch (Exception e) {
			this.logReaderError("Error occurred during execute of custom command for tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: "
					+ e.getMessage());
			customResultCode = RF_RStatus.RF_communication_error;
		}

		if (data[4] == RFMICRON_CODE_TEMP && customData != null) {
			if (customData.length >= 2) {
				// read calibration data and calculate temperature
				byte[] calibrationData = null;
				try {
					FedmIscTagHandler_Result calibrationDataResult = new FedmIscTagHandler_Result();
					customResultCode = RF_RStatus.forValue(executeTag.readMultipleBlocks(RFMICRON_TEMP_CALIBRATION_BANK, RFMICRON_TEMP_CALIBRATION_OFFSET / 16,
							RFMICRON_TEMP_CALIBRATION_LENGTH / 16, password, calibrationDataResult));
					calibrationData = calibrationDataResult.data;
					for (int i = 0; i < calibrationData.length; i += 2) {
						if (calibrationData.length > i + 1)
							RFCUtils.reverseByteArray(calibrationData, i, 2);
					}

				} catch (Exception e) {
					this.logReaderError("Error occurred during execute of read command for tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: "
							+ e.getMessage());
					customResultCode = RF_RStatus.RF_communication_error;
				}
				if (calibrationData != null && calibrationData.length >= 8) {
					double code1 = Converter.toLong(Calculator.strip(calibrationData,
							RFMICRON_TEMP_CALIBRATION_CODE1_OFFSET - RFMICRON_TEMP_CALIBRATION_OFFSET, RFMICRON_TEMP_CALIBRATION_CODE_LENGTH),
							RFMICRON_TEMP_CALIBRATION_CODE_LENGTH);
					double temp1 = Converter.toLong(Calculator.strip(calibrationData,
							RFMICRON_TEMP_CALIBRATION_TEMP1_OFFSET - RFMICRON_TEMP_CALIBRATION_OFFSET, RFMICRON_TEMP_CALIBRATION_TEMP_LENGTH),
							RFMICRON_TEMP_CALIBRATION_TEMP_LENGTH);
					double code2 = Converter.toLong(Calculator.strip(calibrationData,
							RFMICRON_TEMP_CALIBRATION_CODE2_OFFSET - RFMICRON_TEMP_CALIBRATION_OFFSET, RFMICRON_TEMP_CALIBRATION_CODE_LENGTH),
							RFMICRON_TEMP_CALIBRATION_CODE_LENGTH);
					double temp2 = Converter.toLong(Calculator.strip(calibrationData,
							RFMICRON_TEMP_CALIBRATION_TEMP2_OFFSET - RFMICRON_TEMP_CALIBRATION_OFFSET, RFMICRON_TEMP_CALIBRATION_TEMP_LENGTH),
							RFMICRON_TEMP_CALIBRATION_TEMP_LENGTH);

					double temperatureCode = (customData[0] & 0xFF) * 256 + (customData[1] & 0xFF);
					// use plausible values only (from RFMicron documentation)
					if (temperatureCode > 1000 && temperatureCode < 3500) {
						// calculation from RFMicron documentation
						double temperature = 1.0 / 10.0 * ((((temp2 - temp1) / (code2 - code1)) * (temperatureCode - code1)) + temp1 - 800.0);
						customData = Converter.toByteArray(temperature);
					} else {
						customResultCode = RF_RStatus.No_valid_Data;
					}
				}
			} else
				customResultCode = RF_RStatus.No_valid_Data;
		}

		CustomResult customResult = new CustomResult();
		customResult.setData(customData != null ? customData : new byte[0]);

		if (customResultCode == RF_RStatus.OK)
			customResult.setState(ResultState.SUCCESS);
		else if (customResultCode == RF_RStatus.Authent_Error) {
			customResult.setState(ResultState.PASSWORD_ERROR);
		} else if (customResultCode == RF_RStatus.Command_not_available) {
			customResult.setState(ResultState.OP_NOT_POSSIBLE_ERROR);
		} else if (customResultCode == RF_RStatus.Tag_Error) {
			byte isoError = this.reader.getByteData(FedmIscReaderID.FEDM_ISC_TMP_B0_ISO_ERROR);
			switch (isoError) {
			case 0x03:
				customResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
				break;
			case 0x04:
				customResult.setState(ResultState.PERMISSION_ERROR);
				break;
			default:
				customResult.setState(ResultState.MISC_ERROR_TOTAL);
				break;
			}
		} else
			customResult.setState(ResultState.MISC_ERROR_TOTAL);

		return customResult;
	}

	private CustomResult executeFarsensCustomCommand(FedmIscTagHandler_EPC_Class1_Gen2 executeTag, byte[] data, String password) {
		byte[] customData = null;
		RF_RStatus customResultCode = null;

		byte[] rawData = null;
		byte command = data[4];
		FedmIscTagHandler_Result rawResult = new FedmIscTagHandler_Result();

		try {
			switch (command) {
			case FARSENS_COMMAND_BLINK:
				customResultCode = RF_RStatus.forValue(executeTag.writeMultipleBlocks(FARSENS_DATA_BANK, FARSENS_BLINK_OFFSET / 16, 1, password,
						FARSENS_BLINK_DATA));
				break;
			default:
				customResultCode = RF_RStatus.forValue(executeTag.readMultipleBlocks(FARSENS_DATA_BANK, FARSENS_DATA_OFFSET / 16,
						((command * 8) + FARSENS_DATA_FRAME_LENGTH) / 16, password, rawResult));
				break;
			}
			rawData = rawResult.data;
			for (int i = 0; rawData != null && i < rawData.length; i += 2) {
				if (rawData.length > i + 1)
					RFCUtils.reverseByteArray(rawData, i, 2);
			}
		} catch (Exception e) {
			this.logReaderError("Error occurred during execute of read command for tag '" + executeTag.getEpcOfUid() + "'!" + " \r\n" + "Exception: "
					+ e.getMessage());
			customResultCode = RF_RStatus.RF_communication_error;
		}

		if (customResultCode == RF_RStatus.OK && command > FARSENS_COMMAND_BLINK) {
			if (rawData != null && rawData.length == command + (FARSENS_DATA_FRAME_LENGTH / 8)) {
				byte header = rawData[0];
				byte qos = rawData[rawData.length - 1];
				byte[] data1, data2, data3;

				if (header == FARSENS_DATA_HEADER_OK && (qos == FARSENS_DATA_QOS_GOOD || qos == FARSENS_DATA_QOS_BEST)) {
					switch (command) {
					case FARSENS_COMMAND_SHORT:
						customData = Converter.toByteArray((double) ByteBuffer.wrap(Calculator.strip(rawData, FARSENS_DATA_VALUE_OFFSET, command * 8))
								.order(ByteOrder.LITTLE_ENDIAN).getShort());
						break;
					case FARSENS_COMMAND_FLOAT:
						customData = Converter.toByteArray((double) ByteBuffer.wrap(Calculator.strip(rawData, FARSENS_DATA_VALUE_OFFSET, command * 8))
								.order(ByteOrder.LITTLE_ENDIAN).getFloat());
						break;
					case FARSENS_COMMAND_THREE_SHORT:
						data1 = Converter.toByteArray((double) ByteBuffer.wrap(Calculator.strip(rawData, FARSENS_DATA_VALUE_OFFSET, Short.BYTES * 8))
								.order(ByteOrder.LITTLE_ENDIAN).getShort());
						data2 = Converter.toByteArray((double) ByteBuffer
								.wrap(Calculator.strip(rawData, FARSENS_DATA_VALUE_OFFSET + Short.BYTES * 8, Short.BYTES * 8)).order(ByteOrder.LITTLE_ENDIAN)
								.getShort());
						data3 = Converter.toByteArray((double) ByteBuffer
								.wrap(Calculator.strip(rawData, FARSENS_DATA_VALUE_OFFSET + (Short.BYTES * 2 * 8), Short.BYTES * 8))
								.order(ByteOrder.LITTLE_ENDIAN).getShort());
						customData = Calculator.concat(Calculator.concat(data1, data2), data3);
					case FARSENS_COMMAND_TWO_FLOAT:
						data1 = Converter.toByteArray((double) ByteBuffer.wrap(Calculator.strip(rawData, FARSENS_DATA_VALUE_OFFSET, Float.BYTES * 8))
								.order(ByteOrder.LITTLE_ENDIAN).getFloat());
						data2 = Converter.toByteArray((double) ByteBuffer
								.wrap(Calculator.strip(rawData, FARSENS_DATA_VALUE_OFFSET + Short.BYTES * 8, Float.BYTES * 8)).order(ByteOrder.LITTLE_ENDIAN)
								.getFloat());
						customData = Calculator.concat(data1, data2);
						break;
					}
				}
			}

			if (customData == null)
				customResultCode = RF_RStatus.No_valid_Data;
		}

		CustomResult customResult = new CustomResult();
		customResult.setData(customData != null ? customData : new byte[0]);

		if (customResultCode == RF_RStatus.OK)
			customResult.setState(ResultState.SUCCESS);
		else if (customResultCode == RF_RStatus.Authent_Error) {
			customResult.setState(ResultState.PASSWORD_ERROR);
		} else if (customResultCode == RF_RStatus.Command_not_available) {
			customResult.setState(ResultState.OP_NOT_POSSIBLE_ERROR);
		} else if (customResultCode == RF_RStatus.Tag_Error) {
			byte isoError = this.reader.getByteData(FedmIscReaderID.FEDM_ISC_TMP_B0_ISO_ERROR);
			switch (isoError) {
			case 0x03:
				customResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
				break;
			case 0x04:
				customResult.setState(ResultState.PERMISSION_ERROR);
				break;
			default:
				customResult.setState(ResultState.MISC_ERROR_TOTAL);
				break;
			}
		} else if (customResultCode == RF_RStatus.Array_Boundary_Error) {
			customResult.setState(ResultState.OUT_OF_RANGE_ERROR);
		} else
			customResult.setState(ResultState.MISC_ERROR_TOTAL);

		return customResult;
	}

	private Map<String, FedmIscTagHandler> tagInventory(boolean moreData) throws FedmException, FePortDriverException, FeReaderDriverException {
		if (moreData)
			return this.reader.tagInventory(true, (byte) 0x90, this.antennas);
		else
			return this.reader.tagInventory(true, (byte) 0x10, this.antennas);
	}

	private void inventory() throws Exception {
		while (true) {
			while (this.doInventory) {
				if (!runInventory()) {
					// inventory failed, return immediately
					inventoryThread = null;
					return;
				}
				Thread.yield(); // to enable other threads to process
			}

			// wait for work
			inventoryLock.lock();
			try {
				while (!this.doInventory) {
					inventoryCondition.await();

					if (!isConnected()) {
						// end this thread if we are not connected anymore
						inventoryThread = null;
						return;
					}
				}
			} finally {
				inventoryLock.unlock();
			}
		}
	}

	private boolean runInventory() throws ImplementationException, Exception, InterruptedException {
		try {
			readerLock.lock();
			try {
				Map<String, FedmIscTagHandler> inventoryTagList = this.tagInventory(false);
				RF_RStatus state = RF_RStatus.forValue(this.reader.getLastStatus());
				if (state == RF_RStatus.Firmware_activation_required)
					this.logIsoError("An ISO tag is located in the reader field, firmware activation is needed!");
				else if (state == RF_RStatus.RF_Warning)
					this.logAntennaError("An antenna is not connected correctly to the reader please check all antenna cables!");
				else if (state == RF_RStatus.Parameter_Range_Error)
					this.logParameterRangeError("The property '" + RF_RProperties.PropertyName.InventoryAntennas + "' was invalid specified!");
				else if (state == RF_RStatus.OK || state == RF_RStatus.No_Transponder) {

					if (this.isoErrorCount > 0)
						this.isoErrorCount--;
					if (this.antennaErrorCount > 0)
						this.antennaErrorCount--;
					this.parameterRangeErrorOccurred = false;
					this.readerErrorOccurred = false;

					if (isoErrorCount == 0)
						notifyIsoErrorResolved();

					if (antennaErrorCount == 0)
						notifyAntennaErrorResolved();

				}
				while (RF_RStatus.forValue(this.reader.getLastStatus()) == RF_RStatus.More_Data) {
					Map<String, FedmIscTagHandler> moreTagList = this.tagInventory(true);
					for (Entry<String, FedmIscTagHandler> moreEntry : moreTagList.entrySet()) {
						if (!inventoryTagList.containsKey(moreEntry.getKey()))
							inventoryTagList.put(moreEntry.getKey(), moreEntry.getValue());
					}
				}
				if (inventoryTagList.size() > 0) {
					int tagCount = 0;

					for (Entry<String, FedmIscTagHandler> inventoryEntry : inventoryTagList.entrySet()) {
						tagCount++;
						Tag reportTag = null;
						if (inventoryEntry.getValue().getTagHandlerType() == FedmIscTagHandler.TYPE_EPC_CLASS1_GEN2) {
							FedmIscTagHandler_EPC_Class1_Gen2 tag = (FedmIscTagHandler_EPC_Class1_Gen2) inventoryEntry.getValue();

							if (this.getIdentifierMode() == RF_RConfiguration.TranspoderIdentifierModeValue.AutomaticMode) {
								byte pcHigh = (byte) (tag.getProtocolControl() & 0xFF);
								int length = ((pcHigh & 0xFF) >> 3) * 4;
								String uid = tag.getUid();
								// workaround for ETBv1 tag misconfigured in
								// battery mode
								if (uid.length() < length) {
									try {
										Field field = FedmIscTagHandler.class.getDeclaredField("tabItem");
										field.setAccessible(true);
										FedmIsoTableItem tabItem = (FedmIsoTableItem) field.get(tag);
										byte newLengthBits = (byte) ((uid.length() / 4) << 3);
										byte indicatorBits = (byte) (pcHigh & 7);
										byte newPcHigh = (byte) (indicatorBits | newLengthBits);
										tabItem.class1Gen2PC[0] = newPcHigh;
										this.logReaderError("Tag \"" + uid + "\" specified an unexpected length of " + length / 2 + " bytes in PC!");
									} catch (Exception e) {
										// skip
									}
								}
							}

							String epc = "";
							try {
								epc = tag.getEpcOfUid();
							} catch (StringIndexOutOfBoundsException ex) {

								/*
								 * HOTFIX for problem that reader reports OK
								 * state on ARM platform although an ISO tag is
								 * in the field
								 */
								inventoryTagList.clear();
								state = RF_RStatus.Firmware_activation_required;
								this.logIsoError("An ISO tag is located in the reader field, firmware activation is needed!");
								break;

							}

							reportTag = new Tag(epc.length() == 0 ? new byte[0] : FeHexConvert.hexStringToByteArray(epc));

							if (Tag.isExtended())
								reportTag.setTid("".equals(tag.getTidOfUid()) ? new byte[0] : FeHexConvert.hexStringToByteArray(tag.getTidOfUid()));

							byte[] pc = RFCUtils.shortToBytes((short) tag.getProtocolControl());
							reportTag.setPc(new byte[] { pc[1], pc[0] });

							if (tag.getRSSI() != null && tag.getRSSI().size() > 0) {
								Entry<Integer, FedmIscRssiItem> rssiEntry = tag.getRSSI().entrySet().iterator().next();
								reportTag.setSighting(new Sighting(this.readerConnection.toString(), rssiEntry.getValue().antennaNumber,
										rssiEntry.getValue().RSSI, reportTag.getFirstTime()));
							}

							ReadResult[] readResult = new ReadResult[4];

							boolean readSuccess = true;
							Map<Long, TagOperation> tagOperations = new HashMap<>();
							RF_RInventoryOperation inventoryOperation = getCurrentOperations(tagOperations);

							if (inventoryOperation.isReserved()) {
								readResult[0] = this.readBankFromTag(tag, FedmIscTagHandler_EPC_Class1_Gen2.BANK_RESERVED, inventoryOperation);
								if (readResult[0].getState() != ResultState.SUCCESS)
									readSuccess = false;
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
										readResult[2] = this.readBankFromTag(tag, FedmIscTagHandler_EPC_Class1_Gen2.BANK_TID, inventoryOperation);
										if (readResult[2].getState() != ResultState.SUCCESS)
											readSuccess = false;
									}
								} else {
									readResult[2] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
								}
							}
							if (inventoryOperation.isUser()) {
								if (readSuccess) {
									readResult[3] = this.readBankFromTag(tag, FedmIscTagHandler_EPC_Class1_Gen2.BANK_USER, inventoryOperation);
									if (readResult[3].getState() != ResultState.SUCCESS)
										readSuccess = false;
								} else {
									readResult[3] = new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]);
								}
							}

							if (!this.executeOperation(tag, inventoryTagList.size() - tagCount))
								sendInventoryReport(new InventoryReport(reportTag, readResult), tagOperations);
						}
					}
					if (!this.readerErrorOccurred && this.readerErrorCount > 0)
						this.readerErrorCount--;
				} else {
					this.executeOperation(null, 0);
				}
				notifyConnectionErrorResolved();
			} finally {
				readerLock.unlock();
			}
		} catch (FePortDriverException e) {
			if (e.getErrorCode() <= -1200 && e.getErrorCode() >= -1299) {
				this.disconnect();
				this.clientCallback.notify(new Message(Exits.Reader.Controller.ConnectionLost, "Connection lost to " + this.devCaps.getModel() + "!"));
				notifyConnectionError("Connection lost to " + this.devCaps.getModel() + "!");

				return false;
			} else {
				this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, "Exception occurred during inventory: " + e.getMessage(), e));
			}
		} catch (FeReaderDriverException e) {
			if (e.getErrorCode() == -4035) {
				this.disconnect();
				this.clientCallback.notify(new Message(Exits.Reader.Controller.ConnectionLost, "Asynchronous connection to " + this.devCaps.getModel() + "!"));

				notifyConnectionError("Asynchronous connection to " + this.devCaps.getModel() + "!");

				return false;
			} else {
				this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, "Exception occurred during inventory: " + e.getMessage(), e));
			}
		} catch (FedmException e) {
			if (e.getErrorcode() == -105) {
				this.disconnect();
				this.clientCallback.notify(new Message(Exits.Reader.Controller.ConnectionLost, "Connection to " + this.devCaps.getModel()
						+ " was disconnected due to buffer insufficiency!"));

				notifyConnectionError("Connection to " + this.devCaps.getModel() + " was disconnected due to buffer insufficiency!");

				return false;
			} else {
				this.clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Exception occurred during inventory: " + e.getMessage(), e));
			}
		} catch (Exception e) {
			this.clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Exception occurred during inventory: " + e.getMessage(), e));
		}

		return true;
	}
}
