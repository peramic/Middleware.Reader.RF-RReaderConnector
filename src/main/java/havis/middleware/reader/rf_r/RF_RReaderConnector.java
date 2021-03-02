package havis.middleware.reader.rf_r;

import havis.middleware.ale.base.KeyValuePair;
import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.base.message.Message;
import havis.middleware.ale.base.operation.port.Pin;
import havis.middleware.ale.base.operation.port.Pin.Type;
import havis.middleware.ale.base.operation.port.Port;
import havis.middleware.ale.base.operation.port.PortObservation;
import havis.middleware.ale.base.operation.port.PortOperation;
import havis.middleware.ale.base.operation.port.result.Result.State;
import havis.middleware.ale.base.operation.port.result.WriteResult;
import havis.middleware.ale.base.operation.tag.Filter;
import havis.middleware.ale.base.operation.tag.LockType;
import havis.middleware.ale.base.operation.tag.Sighting;
import havis.middleware.ale.base.operation.tag.Tag;
import havis.middleware.ale.base.operation.tag.TagOperation;
import havis.middleware.ale.exit.Exits;
import havis.middleware.ale.reader.Callback;
import havis.middleware.ale.reader.Capability;
import havis.middleware.ale.reader.Prefix;
import havis.middleware.ale.reader.Property;
import havis.middleware.ale.reader.ReaderConnector;
import havis.middleware.ale.reader.ReaderUtils;
import havis.middleware.ale.service.rc.RCConfig;
import havis.middleware.reader.rf_r.RF_RConfiguration.ReaderCall;
import havis.middleware.reader.rf_r.RF_RConfiguration.TranspoderIdentifierModeValue;
import havis.middleware.reader.rf_r.RF_RInventoryOperation.UserReadMode;
import havis.middleware.utils.data.Calculator;
import havis.util.monitor.AntennaConfiguration;
import havis.util.monitor.AntennaError;
import havis.util.monitor.Capabilities;
import havis.util.monitor.CapabilityType;
import havis.util.monitor.Configuration;
import havis.util.monitor.ConfigurationType;
import havis.util.monitor.ConnectType;
import havis.util.monitor.ConnectionError;
import havis.util.monitor.DeviceCapabilities;
import havis.util.monitor.FirmwareError;
import havis.util.monitor.FirmwareWarning;
import havis.util.monitor.ReaderEvent;
import havis.util.monitor.TagError;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.feig.FeHexConvert;
import de.feig.FePortDriverException;
import de.feig.FeReaderDriverException;
import de.feig.FedmBrmTableItem;
import de.feig.FedmException;
import de.feig.FedmIscReader;
import de.feig.FedmIscReaderID;
import de.feig.FedmIscReaderInfo;
import de.feig.FedmIscRssiItem;
import de.feig.FEDM.Core.ReaderModule;
import de.feig.ReaderConfig.DigitalIO;
import de.feig.TagHandler.FedmIscTagHandler_EPC_Class1_Gen2;

/**
 * Abstract class that provides base functionality for communicating with all
 * RF-R readers.
 *
 */

public abstract class RF_RReaderConnector implements ReaderConnector {

	private volatile boolean isInitialize = false;
	private volatile boolean isConnected = false;

	protected ReaderModule readerModule;

	/**
	 * Represents the physical reader.
	 */
	protected FedmIscReader reader;

	/**
	 * Lock object to sync Reader access.
	 */
	protected Lock readerLock = new ReentrantLock();

	/**
	 * Holds all connection informations.
	 */
	protected RF_RConnection readerConnection;

	/**
	 * List of reader configuration parameters.
	 */
	protected Map<String, Object> configurationProperties;

	/**
	 * Provides access to reader configuration.
	 */
	protected RF_RConfiguration readerConfiguration;

	/**
	 * Holds all inventory informations.
	 */
	protected RF_RInventoryOperation inventoryOperation;

	/**
	 * The client callback class.
	 */
	protected Callback clientCallback;

	protected boolean isDisposed = false;
	
	protected volatile byte antennas;

	// Properties for TagOperations
	private Object syncTagOperationList = new Object();
	private Map<Long, TagOperation> tagOperationList = new Hashtable<>();
	private Object syncTagObserverList = new Object();
	private List<Long> tagObserverList = new ArrayList<>();

	// Properties for PortOperations
	private Object syncPortObservationList = new Object();
	private Map<Long, PortObservation> portObservationList = new Hashtable<>();
	private Object syncPortObserverList = new Object();
	private List<Long> portObserverList = new ArrayList<>();

	/**
	 * Lock object to sync execute operations.
	 */
	private Lock syncExecuteTagOperation = new ReentrantLock(true);
	private boolean executeAbort = false;
	private Semaphore executeEvent = new Semaphore(1);

	/**
	 * Represents the current execute reader operation.
	 */
	protected KeyValuePair<Long, TagOperation> executeTagOperation = new KeyValuePair<>();

	/**
	 * A counter which represents the number of inventory attempts for the
	 * current <see cref="executeTagOperation"/>.
	 */
	private int inventoryAttempts;

	/**
	 * The device capabilities containing the name, type, manufacturer and firmware version of the reader. 
	 */
	protected DeviceCapabilities devCaps = new DeviceCapabilities(null, "HARTING Software Development GmbH & Co.KG", null, null);
	
	/**
	 * A map representing the connect type for each antenna.
	 */
	protected Map<Short, ConnectType> connectTypes;

	/**
	 * Initializes a new instance of the Havis.Middleware.Reader.RF_RReaderConnector
	 * class.
	 */
	public RF_RReaderConnector() {
		
		this.connectTypes = initAntennas();

		readerLock.lock();
		try {
			this.inventoryOperation = new RF_RInventoryOperation();
		} finally {
			readerLock.unlock();
		}
	}

	protected Map<Short, ConnectType> initAntennas() {
		Map<Short, ConnectType> antennas = new HashMap<>();
		antennas.put((short) 1, ConnectType.AUTO);
		antennas.put((short) 2, ConnectType.AUTO);
		antennas.put((short) 3, ConnectType.AUTO);
		antennas.put((short) 4, ConnectType.AUTO);
		return antennas;
	}

	/**
	 * Initializes a new instance of the {@link RF_RReaderConnector} class.
	 *
	 * @param callback
	 */
	public RF_RReaderConnector(Callback callback) {
		this();
		this.clientCallback = callback;
		
		this.connectTypes = initAntennas();
		
		this.devCaps.setName(callback.getName());	
	}

	protected InetAddress getLocalAddress(String remoteHost, String port) throws ImplementationException {
		try (final DatagramSocket socket = new DatagramSocket()) {
			int remotePort = port != null ? Integer.parseInt(port) : getDefaultPort();
			socket.connect(InetAddress.getByName(remoteHost), remotePort);
			return socket.getLocalAddress();
		} catch (NumberFormatException e) {
			throw new ImplementationException("Failed to retrieve local IP address for reader at " + remoteHost + ":" + port + ": invalid port");
		} catch (Exception e) {
			throw new ImplementationException("Failed to retrieve local IP address for reader at " + remoteHost + ":" + port + ": " + e.getMessage());
		}
	}

	/**
	 * Sets the list of properties, that are used to set up the reader.
	 *
	 * @param properties
	 * @throws ValidationException
	 *             if value did not contains all necessary properties or
	 *             properties have invalid values
	 * @throws ImplementationException
	 *             if any complication occurred during communication with the
	 *             reader.
	 */
	@Override
	public void setProperties(Map<String, String> properties) throws ValidationException, ImplementationException {

		try {
			this.validateConnectorProperties(properties);
			this.configurationProperties = setDefaultProperties(properties, this.readerConfiguration.validateConfigurationProperties(properties));
			
			if (!this.isInitialize) {
				this.readerConnection = getConnectionObject(properties);
				this.isInitialize = true;
			} else {
				if (!this.isConnected) {
					this.readerConnection = getConnectionObject(properties);
				} else {
					RF_RConnection newConnection = getConnectionObject(properties);
					if (this.readerConnection.equals(newConnection)) {
						applyInventoryAntennas(this.readerConnection.getConnectionProperties().getInventoryAntennas());
						readerLock.lock();
						try {
							this.readerConfiguration.applyCompleteReaderConfig(this.configurationProperties, this.getOperatingMode().getValue(),
									(byte) this.getIdentifierMode().ordinal(), new ReaderCall() {
										@Override
										public int call() throws ImplementationException {
											return resetReader();
										}
									});
						} finally {
							readerLock.unlock();
						}
					} else {
						this.readerConnection = newConnection;
						applyInventoryAntennas(this.readerConnection.getConnectionProperties().getInventoryAntennas());
						this.disconnect();
						this.connect();
					}
				}
			}
		} catch (ValidationException e) {
			throw e;
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.readerConnection.toString() + ")!");
		}
	}

	protected int resetReader() throws ImplementationException {
		try {
			return reader.sendProtocol((byte) 0x63);
		} catch (FePortDriverException | FeReaderDriverException | FedmException e) {
			throw new ImplementationException(e);
		}
	}

	protected Map<String, Object> setDefaultProperties(Map<String, String> originalProperties, Map<String, Object> properties) throws ImplementationException {
		return properties;
	}

	protected Map<String, Object> setAntennaProperties(Map<String, Object> properties) throws ImplementationException {
		return properties;
	}

	protected void applyInventoryAntennas(Byte inventoryAntennas) {
		if (inventoryAntennas != null) {
			for (short s = 0; s < connectTypes.size(); s++)
				connectTypes.put((short) (s + 1), ((inventoryAntennas >> s) & 1) == 1 ? ConnectType.TRUE : ConnectType.FALSE);
		} else {
			for (Short key : connectTypes.keySet()) {
				connectTypes.put(key, ConnectType.AUTO);
			}
		}
	}

	/**
	 * Gets a capability by name.
	 *
	 * @param name
	 *            request capability name
	 * @return The requested capability value
	 * @throws ValidationException
	 * @throws ImplementationException
	 */
	@Override
	public String getCapability(String name) throws ValidationException, ImplementationException {
		switch (name) {
		case Capability.LostEPCOnWrite:
			return "false";
		default:
			throw new ValidationException("Unkown capabilty name '" + name + "' for " + this.devCaps.getName() + "!");
		}
	}

	/**
	 * @return true if we are connected, false otherwise
	 */
	public boolean isConnected() {
		return this.isConnected;
	}

	/**
	 * Method to establish the connection to the reader. The method opens the
	 * connection, sets the reader configuration, and start the reader working
	 * thread.
	 *
	 * @throws ValidationException
	 *             if setting up the reader connection failed.
	 * @throws ImplementationException
	 *             if Properties were not set or connection to the reader failed
	 *             for any reason.
	 */
	@Override
	public void connect() throws ValidationException, ImplementationException {
		if (!this.isInitialize)
			throw new ImplementationException(this.devCaps.getModel() + " ReaderConnector was not initialized!");
		readerLock.lock();
		try {
			try {
				this.readerModule = new ReaderModule();
				this.reader = this.readerModule.getReaderImpl();
				this.readerConfiguration.setReader(this.reader);
				this.connectReader();

				this.checkReaderInfo();

				this.applyInventoryAntennas(this.readerConnection.getConnectionProperties().getInventoryAntennas());
				this.readerConfiguration.applyCompleteReaderConfig(this.configurationProperties, this.getOperatingMode().getValue(),
						(byte) this.getIdentifierMode().ordinal() /* TODO: ordinal OK? */, new ReaderCall() {
							@Override
							public int call() throws ImplementationException {
								return resetReader();
							}
						});

				int readerTableSizeResult = this.setReaderTableSize();
				if (readerTableSizeResult != 0) {
					if (readerTableSizeResult > 0)
						throw new ValidationException(reader.getStatusText((byte) readerTableSizeResult) + " ("
								+ this.readerConnection.toString() + ")!");
					else
						throw new ImplementationException(reader.getErrorText(readerTableSizeResult) + " ("
								+ this.readerConnection.toString() + ")!");
				}

				synchronized (syncTagObserverList) {
					if (this.tagObserverList.size() > 0)
						this.startInventory();
				}

				synchronized (syncPortObserverList) {
					if (this.portObservationList.size() > 0)
						this.startPortObservation();
				}

				this.isConnected = true;
				notifyConnectionErrorResolved();
			} catch (ValidationException | ImplementationException e) {
				notifyConnectionError(e.getMessage());
				throw e;
			} catch (Exception e) {
				notifyConnectionError(e.getMessage() + " (" + this.devCaps.getName() + ")!");
				throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
			}
		} finally {
			readerLock.unlock();
		}
	}

	protected void connectReader() throws FedmException, FeReaderDriverException, InterruptedException, FePortDriverException, ValidationException, ImplementationException {
		switch (this.readerConnection.getConnectionType()) {
		case TCP:
			try {
				this.reader.connectTCP(this.readerConnection.getHost(), this.readerConnection.getPort());
			} catch (FePortDriverException fex) {
				if (fex.getErrorCode() == -1211) {
					// maybe we connected to quickly after a disconnect,
					// retry once after 500ms
					Thread.sleep(500);
					this.reader.connectTCP(this.readerConnection.getHost(), this.readerConnection.getPort());
				}
			}
			break;
		case USB:
			this.reader.connectUSB(this.readerConnection.getDeviceID());
			break;
		case COMM:
			this.reader.connectCOMM(this.readerConnection.getPort(), true);
			this.reader.findBaudRate();
			break;
		default:
			throw new ValidationException("Unsupported connection type for " + this.devCaps.getModel() + "!" + " ("
					+ this.readerConnection.toString() + ")!");
		}

		this.reader.setPortPara("Timeout", "" + this.readerConnection.getTimeout());

		if (!this.reader.isConnected())
			throw new ImplementationException("Unable to connect to " + this.devCaps.getModel() + "!" + " ("
					+ this.readerConnection.toString() + ")!");
		this.reader.setProtocolFrameSupport(0x02);
	}

	/**
	 * Method to disconnect from the reader.
	 *
	 * @throws ImplementationException
	 */
	@Override
	public void disconnect() throws ImplementationException {
		if (!this.isConnected)
			return;

		try {
			this.isConnected = false;

			this.stopInventory();

			if (this.portObserverList.size() > 0)
				this.stopPortObservation();

			readerLock.lock();
			try {
				while (this.reader.isConnected())
					this.reader.disConnect();
				
				this.reader.destroy();
			} finally {
				readerLock.unlock();
			}
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
		}
	}

	/**
	 * The inventory operation has changed, if necessary, update the inventory
	 * settings accordingly
	 * 
	 * @param oldInventoryOperation
	 *            the old inventory settings, can be null
	 * @param newInventoryOperation
	 *            the new inventory settings
	 * @throws ValidationException
	 * @throws ImplementationException
	 */
	protected void updateInventorySettings(RF_RInventoryOperation oldInventoryOperation, RF_RInventoryOperation newInventoryOperation)
			throws ValidationException, ImplementationException {
		// nothing to do by default
	}

	/**
	 * Method to define new tag operation on the reader.
	 * 
	 * @param id
	 *            The unique id to identify the tag operation
	 * @param operation
	 *            The reader operation to define
	 * @throws ImplementationException
	 */
	@Override
	public void defineTagOperation(long id, TagOperation operation) throws ImplementationException {
		synchronized (this.syncTagOperationList) {
			try {
				if (!this.tagOperationList.containsKey(id))
					this.tagOperationList.put(id, operation);
				else
					throw new ImplementationException("Tag operation with id '" + id + "' was already defined for "
							+ this.devCaps.getModel() + " (" + this.devCaps.getName() + ")!");

			} catch (ImplementationException e) {
				throw e;
			} catch (Exception e) {
				throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
			}
		}
	}

	/**
	 * Method to undefine a reader operation on the reader.
	 *
	 * @param id
	 *            The unique id to identify the reader operation
	 * @throws ValidationException
	 *             if the given id <paramref name="id"/> is not a know id
	 */
	@Override
	public void undefineTagOperation(long id) throws ImplementationException {
		try {
			if (!this.tagOperationList.containsKey(id))
				throw new ImplementationException("Unkown tag operation ID '" + id + "' for " + this.devCaps.getModel() + " ("
						+ this.readerConnection.toString() + ")!");

			if (this.tagObserverList.contains(id))
				this.disableTagOperation(id);

			synchronized (this.syncTagOperationList) {
				this.tagOperationList.remove(id);
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
		}
	}
	
	protected RF_RInventoryOperation getCurrentOperations(Map<Long, TagOperation> tagOperations) {
		RF_RInventoryOperation inventoryOperation;
		synchronized (this.syncTagObserverList) {
			inventoryOperation = this.inventoryOperation.clone();
			synchronized (this.syncTagOperationList) {
				for (Long id : this.tagObserverList) {
					TagOperation op = tagOperationList.get(id);
					if (op != null) {
						tagOperations.put(id, op);
					}
				}
			}
		}
		return inventoryOperation;
	}

	/**
	 * Method to enable a reader operation on the reader. After this method the
	 * reader connector will report result using the given callback method.
	 *
	 * @param id
	 *            The unique id to identify the reader operation
	 * @throws ImplementationException
	 *             if create inventory operation failed
	 */
	@Override
	public void enableTagOperation(long id) throws ImplementationException {
		try {
			synchronized (this.syncTagObserverList) {
				if (this.tagOperationList.containsKey(id))
					this.tagObserverList.add(id);
				else
					throw new ImplementationException("Unkown reader operation ID '" + id + "' for " + this.devCaps.getModel()
							+ " (" + this.readerConnection.toString() + ")!");

				RF_RInventoryOperation newInventoryOperation = this.getInventoryOperation();
				if (!newInventoryOperation.equals(this.inventoryOperation)) {
					updateInventorySettings(this.inventoryOperation, newInventoryOperation);
					this.inventoryOperation = newInventoryOperation;
				}
			}

			if (this.isConnected && this.tagObserverList.size() == 1) {
				this.startInventory();
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			this.clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Enable tag operation failed: " + e.getMessage(), e));
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
			
		}
	}

	/**
	 * Method to disable a reader operation on the reader. After this method the
	 * reader connector will no longer report results.
	 *
	 * @param id
	 *            The unique id to identify the reader operation.
	 * @throws ImplementationException
	 *             if create inventory operation failed.
	 */
	@Override
	public void disableTagOperation(long id) throws ImplementationException {
		try {
			synchronized (this.syncTagObserverList) {
				if (this.tagObserverList.contains(id))
					this.tagObserverList.remove(id);
				else
					throw new ImplementationException("Reader operation ID '" + id + "' was not active for "
							+ this.devCaps.getModel() + " (" + this.devCaps.getName() + ")!");

				RF_RInventoryOperation newInventoryOperation = this.getInventoryOperation();
				if (!newInventoryOperation.equals(this.inventoryOperation)) {
					updateInventorySettings(this.inventoryOperation, newInventoryOperation);
					this.inventoryOperation = newInventoryOperation;
				}
			}
			if (this.isConnected && this.tagObserverList.size() == 0)
				this.stopInventory();
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
		}
	}

	/**
	 * Method to define a reader operation and execute it once.
	 *
	 * @param id
	 *            The unique id to identify the operation
	 * @param operation
	 *            The reader operation to execute
	 * @throws ValidationException
	 * @throws ImplementationException
	 *             if connector was not connected to the reader.
	 */
	@Override
	public void executeTagOperation(long id, TagOperation operation)
			throws ValidationException, ImplementationException {
		if (!this.isConnected)
			throw new ValidationException("ReaderConnector was not connected to " + this.devCaps.getModel() + " ("
					+ devCaps.getName() + ")!");

		try {
			executeEvent.acquire();
			boolean validExecute = true;

			this.syncExecuteTagOperation.lock();
			try {
				this.executeAbort = false;

				this.executeTagOperation = new KeyValuePair<Long, TagOperation>(id, operation);
				this.inventoryAttempts = 0;

				Map<Integer, havis.middleware.ale.base.operation.tag.result.Result> errorList = this.validateExecuteOperation(operation);
				if (errorList.size() > 0) {
					validExecute = false;
					this.sendExecuteErrorReport(errorList);
				}
			} finally {
				this.syncExecuteTagOperation.unlock();
			}

			if (validExecute && this.tagObserverList.size() == 0)
				this.startInventory();

		} catch (Exception e) {
			e.printStackTrace();
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
		}
	}

	/**
	 * Method to abort a running reader operation.
	 *
	 * @param id
	 *            The unique id of the reader operation.
	 * @throws ImplementationException
	 */
	@Override
	public void abortTagOperation(long id) throws ImplementationException {
		this.syncExecuteTagOperation.lock();
		try {
			this.executeAbort = true;
		} finally {
			this.syncExecuteTagOperation.unlock();
		}
	}

	/**
	 * Method to define new port observation on the reader.
	 *
	 * @param id
	 *            The unique id to identify the port observation
	 * @param observation
	 *            The observation
	 * @throws ValidationException
	 * @throws ImplementationException
	 */
	@Override
	public void definePortObservation(long id, PortObservation observation)
			throws ValidationException, ImplementationException {

		try {
			if (this.isConnected && this.portObserverList.size() == 0)
				this.getGPIOInitialState();

			this.validateObservation(observation);

			synchronized (this.syncPortObservationList) {
				if (!this.portObservationList.containsKey(id))
					this.portObservationList.put(id, observation);
				else
					throw new ImplementationException("Port observation id with '" + id + "' was already defined for "
							+ this.devCaps.getModel() + " (" + this.devCaps.getName() + ")!");
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.readerConnection.toString() + ")!");
		}
	}

	/**
	 * Method to undefine a port observation on the reader.
	 *
	 * @param id
	 *            The unique id to identify the port observation
	 * @throws ImplementationException
	 */
	@Override
	public void undefinePortObservation(long id) throws ImplementationException {
		try {
			if (!this.portObservationList.containsKey(id))
				throw new ImplementationException("Unkown port observation ID '" + id + "' for " + this.devCaps.getModel()
						+ " (" + this.readerConnection.toString() + ")!");

			if (this.portObserverList.contains(id))
				this.disablePortObservation(id);

			synchronized (syncPortObservationList) {
				this.portObservationList.remove(id);
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
		}
	}

	/**
	 * Method to enable a port observation on the reader. After this method the
	 * reader connector will report result using the given callback method.
	 *
	 * @param id
	 *            The unique id to identify the port observation
	 * @throws ImplementationException
	 */
	@Override
	public void enablePortObservation(long id) throws ImplementationException {
		try {
			synchronized (syncPortObserverList) {
				if (this.portObservationList.containsKey(id))
					this.portObserverList.add(id);
				else
					throw new ImplementationException("Unkown port observation ID '" + id + "' for " + this.devCaps.getModel()
							+ " (" + this.devCaps.getName() + ")!");

				if (this.isConnected && this.portObserverList.size() == 1)
					this.startPortObservation();
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
		}
	}

	/**
	 * Method to disable a port observation on the reader. After this method the
	 * reader connector will no longer report results.
	 *
	 * @param id
	 *            The unique id to identify the port observation
	 * @throws ImplementationException
	 */
	@Override
	public void disablePortObservation(long id) throws ImplementationException {
		try {
			synchronized (this.syncPortObserverList) {
				if (this.portObserverList.contains(id))
					this.portObserverList.remove(id);
				else
					throw new ImplementationException("Port observation ID '" + id + "' was not active for "
							+ this.devCaps.getModel() + " (" + this.devCaps.getName() + ")!");

				if (this.isConnected && this.portObserverList.size() == 0)
					this.stopPortObservation();
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
		}
	}

	/**
	 * Method to execute a port operation.
	 *
	 * @param id
	 *            The unique id to identify the port operation
	 * @param operation
	 *            The pin operation to execute
	 * @throws ImplementationException
	 */
	@Override
	public void executePortOperation(long id, PortOperation operation) throws ValidationException, ImplementationException {
		if (!this.isConnected)
			throw new ValidationException("ReaderConnector was not connected to " + this.devCaps.getModel() + " ("
					+ readerConnection.toString() + ")!");
		try {
			if (this.isConnected && this.portObserverList.size() == 0)
				this.getGPIOInitialState();

			Map<Integer, havis.middleware.ale.base.operation.port.result.Result> result = new Hashtable<>();

			boolean error = false;

			for (havis.middleware.ale.base.operation.port.Operation o : operation.getOperations()) {
				switch (o.getType()) {
				case READ:
					if (error) {
						result.put(o.getId(), new havis.middleware.ale.base.operation.port.result.ReadResult(
								havis.middleware.ale.base.operation.port.result.Result.State.MISC_ERROR_TOTAL));
					} else {
						if (o.getPin().getId() >= 1 && ((o.getPin().getType() == Type.INPUT
								&& o.getPin().getId() <= this.inputPortState.length)
								|| (o.getPin().getType() == Type.OUTPUT && o.getPin()
										.getId() <= (this.outputPortState.length + this.relayPortState.length))))
							result.put(o.getId(), this.getPortReadResult(o));
						else {
							result.put(o.getId(), new havis.middleware.ale.base.operation.port.result.ReadResult(
									havis.middleware.ale.base.operation.port.result.Result.State.PORT_NOT_FOUND_ERROR));
							error = true;
						}
					}
					break;
				case WRITE:
					if (error) {
						result.put(o.getId(), new havis.middleware.ale.base.operation.port.result.WriteResult(
								havis.middleware.ale.base.operation.port.result.Result.State.MISC_ERROR_TOTAL));
					} else {
						if (o.getPin().getType() == Type.OUTPUT)
							if (o.getPin().getId() >= 1
									&& o.getPin().getId() <= (this.outputPortState.length + this.relayPortState.length))
								result.put(o.getId(), this.getPortWriteResult(o));
							else {
								result.put(o.getId(), new havis.middleware.ale.base.operation.port.result.WriteResult(
										havis.middleware.ale.base.operation.port.result.Result.State.PORT_NOT_FOUND_ERROR));
								error = true;
							}
						else {
							result.put(o.getId(), new havis.middleware.ale.base.operation.port.result.WriteResult(
									havis.middleware.ale.base.operation.port.result.Result.State.OP_NOT_POSSIBLE_ERROR));
							error = true;
						}
					}
					break;
				default:
					result.put(o.getId(), new havis.middleware.ale.base.operation.port.result.Result(
							havis.middleware.ale.base.operation.port.result.Result.State.OP_NOT_POSSIBLE_ERROR));
					error = true;
					break;
				}
			}
			this.clientCallback.notify(id, new Port(result));
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.devCaps.getName() + ")!");
		}
	}

	/**
	 * Disposes this instance.
	 */
	@Override
	public void dispose() throws ImplementationException {
		this.dispose(true);

	}

	/**
	 * Disposes this instance. According to <paramref name="disposing"/> also
	 * managed resources will be disposed.
	 *
	 * @param disposing
	 *            Indicator if also managed resources should be disposed.
	 * @throws ImplementationException
	 */
	protected void dispose(boolean disposing) throws ImplementationException {
		if (!this.isDisposed) {
			if (this.isConnected) {
				this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, "Disconnect on Dispose!"));
				this.disconnect();
			}
			if (this.reader != null) {
				readerLock.lock();
				try {
					this.reader.destroy();
					this.reader = null;
				} finally {
					readerLock.unlock();
				}
			}
			this.isDisposed = true;
		}
	}

	/**
	 * Returns the reader configuration
	 *
	 * @return The reader configuration
	 * @throws ImplementationException
	 */
	@Override
	public RCConfig getConfig() throws ImplementationException {
		readerLock.lock();
		try {
			return this.readerConfiguration.getReaderConfig();
		} finally {
			readerLock.unlock();
		}
	}

	@Override
	public void setCallback(Callback callback) {
		this.clientCallback = callback;
		this.devCaps.setName(callback.getName());
	}

	protected int readerErrorCount = 0;
	protected boolean readerErrorOccurred = false;
	protected int isoErrorCount = 0;
	protected int antennaErrorCount = 0;
	protected boolean parameterRangeErrorOccurred = false;

	protected void logReaderError(String message) {
		this.readerErrorOccurred = true;
		if (this.readerErrorCount <= 0) {
			this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, message));					
		}
		this.readerErrorCount = this.readerConnection.getConnectionProperties().getReaderErrorCount();
	}

	protected void logIsoError(String message) {
		if (this.isoErrorCount <= 0) {
			this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, message));
			this.notifyIsoError(message);			
		}
		this.isoErrorCount = this.readerConnection.getConnectionProperties().getIsoErrorCount();
	}

	protected void logAntennaError(String message) {
		if (this.antennaErrorCount <= 0) {
			this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, message));
			this.notifyAntennaError(message, (short)0);			
		}
		this.antennaErrorCount = this.readerConnection.getConnectionProperties().getAntennaErrorCount();		
	}

	protected TagError lastIsoError;
	protected FirmwareError lastFwError;
	protected FirmwareWarning lastFwWarning;
	protected AntennaError lastAntError;
	protected ConnectionError lastConError;
	
	private void notifyEvent(ReaderEvent event) {
		this.clientCallback.notify(event);
	}

	protected void notifyIsoError(String msg) {
		this.lastIsoError = new TagError(new Date(), true, msg);
		this.notifyEvent(this.lastIsoError);
	}

	protected void notifyAntennaError(String msg, short antennaId) {
		this.lastAntError = new AntennaError(new Date(), true, msg, antennaId);
		this.notifyEvent(this.lastAntError);
	}

	protected void notifyConnectionError(String msg) {
		this.lastConError = new ConnectionError(new Date(), true, msg);
		this.notifyEvent(this.lastConError);
	}

	protected void notifyFirmwareError(String msg) {
		this.lastFwError = new FirmwareError(new Date(), true, msg);
		this.notifyEvent(this.lastFwError);
	}

	protected void notifyFirmwareWarning(String msg) {
		this.lastFwWarning = new FirmwareWarning(new Date(), true, msg);
		this.notifyEvent(this.lastFwWarning);
	}

	protected void notifyIsoErrorResolved() {
		if (this.lastIsoError != null) {
			this.lastIsoError.setTimestamp(new Date());
			this.lastIsoError.setState(false);
			notifyEvent(this.lastIsoError);
			this.lastIsoError = null;
		}
	}

	protected void notifyAntennaErrorResolved() {
		if (this.lastAntError != null) {
			this.lastAntError.setTimestamp(new Date());
			this.lastAntError.setState(false);
			notifyEvent(this.lastAntError);
			this.lastAntError = null;
		}
	}

	protected void notifyConnectionErrorResolved() {
		if (this.lastConError != null) {
			this.lastConError.setTimestamp(new Date());
			this.lastConError.setState(false);
			notifyEvent(this.lastConError);
			this.lastConError = null;
		}
	}

	protected void notifyFirmwareErrorResolved() {
		if (this.lastFwError != null) {
			this.lastFwError.setTimestamp(new Date());
			this.lastFwError.setState(false);
			notifyEvent(this.lastFwError);
			this.lastFwError = null;
		}
	}

	protected void notifyFirmwareWarningResolved() {
		if (this.lastFwWarning != null) {
			this.lastFwWarning.setTimestamp(new Date());
			this.lastFwWarning.setState(false);
			notifyEvent(this.lastFwWarning);
			this.lastFwWarning = null;
		}
	}

	protected void logParameterRangeError(String message) {
		if (!this.parameterRangeErrorOccurred) {
			this.parameterRangeErrorOccurred = true;
			this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, message));
		}
	}

	protected TranspoderIdentifierModeValue getIdentifierMode() {
		if (Tag.isExtended())
			return RF_RConfiguration.TranspoderIdentifierModeValue.EPCandTID;
		else
			return RF_RConfiguration.TranspoderIdentifierModeValue.AutomaticMode;
	}

	protected void validateConnectorProperties(Map<String, String> properties) throws ValidationException {
		for (Entry<String, String> property : properties.entrySet()) {
			switch (property.getKey()) {
			case Property.Connector.ConnectionType:
				break;
			case Property.Connector.Host:
				break;
			case Property.Connector.Port:
				break;
			case Property.Connector.DeviceID:
				break;
			case Property.Connector.Timeout:
				break;
			case RF_RProperties.PropertyName.InventoryAntennas:
				break;
			case RF_RProperties.PropertyName.InventoryAttempts:
				break;
			case RF_RProperties.PropertyName.TagsInField:
				break;
			case RF_RProperties.PropertyName.BlockSize:
				break;
			case RF_RProperties.PropertyName.BlockCount:
				break;
			case RF_RProperties.PropertyName.ReaderErrorCount:
				break;
			case RF_RProperties.PropertyName.IsoErrorCount:
				break;
			case RF_RProperties.PropertyName.AntennaErrorCount:
				break;
			case RF_RProperties.PropertyName.InputDelay:
				break;
			default:
				if (property.getKey().startsWith(Prefix.Connector)) {
					throw new ValidationException("Connector property '" + property.getKey()
							+ "' is not recognized for " + this.devCaps.getModel() + "!");
				}
				break;
			}
		}
	}

	protected RF_RConnection getConnectionObject(Map<String, String> properties) throws ValidationException {
		RF_RConnection connection = null;
		boolean bResult = true;
		String property = "";
		do {
			RF_RConnectionType type;

			String typeString = properties.get(property = Property.Connector.ConnectionType);
			if (typeString == null)
				type = RF_RConnectionType.TCP;
			else {
				try {
					type = RF_RConnectionType.valueOf(typeString);
				} catch (Exception e) {
					bResult = false;
					break;
				}
			}

			if (type == RF_RConnectionType.TCP) {
				String host;

				if ((host = properties.get(property = Property.Connector.Host)) == null) {
					bResult = false;
					break;
				}

				String portString = properties.get(property = Property.Connector.Port);
				int port;
				if (portString == null)
					port = getDefaultPort();
				else {
					try {
						port = Integer.parseInt(portString);
					} catch (NumberFormatException e) {
						bResult = false;
						break;
					}
				}
				connection = RF_RConnection.GetTCPConnection(host, port);
			} else if (type == RF_RConnectionType.USB) {
				try {
					connection = RF_RConnection
							.GetUSBConnection(Integer.parseInt(properties.get(property = Property.Connector.DeviceID)));
				} catch (NumberFormatException e) {
					bResult = false;
					break;
				}
			} else if (type == RF_RConnectionType.COMM) {
				try {
					connection = RF_RConnection
							.GetCOMMConnection(Integer.parseInt(properties.get(property = Property.Connector.Port)));
				} catch (NumberFormatException e) {
					bResult = false;
					break;
				}
			}

			connection.setConnectionProperties(new RF_RProperties());

			try {
				if (properties.containsKey(property = Property.Connector.Timeout))
					connection.setTimeout(Integer.parseInt(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.InventoryAntennas))
					connection.getConnectionProperties().setInventoryAntennas(Byte.parseByte(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.InventoryAttempts))
					connection.getConnectionProperties()
							.setInventoryAttempts(Short.parseShort(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.TagsInField))
					connection.getConnectionProperties().setTagsInField(Short.parseShort(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.BlockSize))
					connection.getConnectionProperties().setBlockSize(Short.parseShort(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.BlockCount))
					connection.getConnectionProperties().setBlockCount(Short.parseShort(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.ReaderErrorCount))
					connection.getConnectionProperties()
							.setReaderErrorCount(Short.parseShort(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.IsoErrorCount))
					connection.getConnectionProperties().setIsoErrorCount(Short.parseShort(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.AntennaErrorCount))
					connection.getConnectionProperties()
							.setAntennaErrorCount(Short.parseShort(properties.get(property)));

				if (properties.containsKey(property = RF_RProperties.PropertyName.InputDelay))
					connection.getConnectionProperties()
							.setInputDelay(Short.parseShort(properties.get(property)));

			} catch (Exception e) {
				bResult = false;
				break;
			}
		} while (false);
		if (bResult)
			return connection;
		else
			throw new ValidationException(
					"Missing or wrong connector property '" + property + "' for " + this.devCaps.getModel() + "!");
	}

	protected int getDefaultPort() {
		return 10001;
	}

	protected Map<Integer, havis.middleware.ale.base.operation.tag.result.Result> validateExecuteOperation(
			TagOperation operation) {
		Map<Integer, havis.middleware.ale.base.operation.tag.result.Result> errorList = new Hashtable<>();
		for (havis.middleware.ale.base.operation.tag.Operation op : operation.getOperations()) {
			switch (op.getType()) {
			case KILL:
				if (op.getData().length > 4)
					errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.KillResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
				break;
			case LOCK:

				LockType lockType = LockType.values()[op.getData()[0]];
				switch (lockType) {
					case LOCK:
					case PERMALOCK:
					case PERMAUNLOCK:
					case UNLOCK:
						break;
					default:
						errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.LockResult(
								havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
						break;
				}
				switch (op.getField().getBank()) {
					case 0:
						if (!(op.getField().getOffset() == 0 && op.getField().getLength() == 32)
								&& !(op.getField().getOffset() == 32 && op.getField().getLength() == 32))
							errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.LockResult(
									havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
						break;
					case 1:
						if (!(op.getField().getOffset() == 0 && op.getField().getLength() == 0))
							errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.LockResult(
									havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
						break;
					case 2:
						if (!(op.getField().getOffset() == 0 && op.getField().getLength() == 0))
							errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.LockResult(
									havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
						break;
					case 3:
						if (!(op.getField().getOffset() == 0 && op.getField().getLength() == 0))
							errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.LockResult(
									havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
						break;
					default:
						errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.LockResult(
								havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
						break;
				}
				break;
			case PASSWORD:
				if (op.getData().length > 4)
					errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.PasswordResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
				break;
			case WRITE:
				if (op.getData().length % 2 != 0 || op.getField().getOffset() % 16 != 0
						|| op.getField().getLength() % 16 != 0 || ((op.getField().getLength() != 0)
								&& (op.getData().length / 2 != op.getField().getLength() / 16))) {
					errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.WriteResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR, 0));
				}
				break;
			
			case CUSTOM:
				if (op.getData() == null || op.getData().length < 2) {
					errorList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.CustomResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.OP_NOT_POSSIBLE_ERROR));
				}
				
			case READ:	
			default:
				break;
			}
		}
		return errorList;
	}

	protected RF_RInventoryOperation getInventoryOperation() {
		RF_RInventoryOperation inventoryOperation = new RF_RInventoryOperation();
		synchronized (this.syncTagObserverList) {

			boolean readComplete = false;
			boolean[] dataBlocks = new boolean[0];

			for (Long id : this.tagObserverList) {
				synchronized (this.tagOperationList) {
					TagOperation operation = this.tagOperationList.get(id);
					if (operation != null) {
						if (operation.getOperations() != null && operation.getOperations().size() > 0) {
							for (havis.middleware.ale.base.operation.tag.Operation op : operation.getOperations()) {
								if (op.getField().getBank() == 0)
									inventoryOperation.setReserved(true);
								else if (op.getField().getBank() == 1)
									inventoryOperation.setEpc(true);
								else if (op.getField().getBank() == 2)
									inventoryOperation.setTid(true);
								else if (op.getField().getBank() == 3) {
									if (!inventoryOperation.isUser()) {
										inventoryOperation.setUser(UserReadMode.ON);
									}

									// Determine the required data blocks
									if (op.getField().getLength() > 0 && !readComplete) {
										int ceiling = Calculator.size(op.getField().getLength() + op.getField().getOffset(), 16);
										if (dataBlocks.length < ceiling)
											dataBlocks = Arrays.copyOf(dataBlocks, ceiling);

										for (int i = op.getField().getOffset() / 16; i < ceiling; i++)
											dataBlocks[i] = true;
									} else {
										// force complete read
										inventoryOperation.setUser(UserReadMode.ON_COMPLETE);
									}
								}
							}
						}
					}
				}
			}
			inventoryOperation.setUserDataBlocks(dataBlocks);
		}
		return inventoryOperation;
	}

	protected boolean executeOperation(FedmIscTagHandler_EPC_Class1_Gen2 executeTag, int inventoryTagsLeft)
			throws ImplementationException {
		boolean executed = false;
		this.syncExecuteTagOperation.lock();
		try {
			if (this.executeTagOperation.getValue() != null) {
				if (this.executeAbort) {
					this.executeTagOperation = new KeyValuePair<>();
					if (this.isConnected && this.tagObserverList.size() == 0)
						this.stopInventory();

					if (executeEvent.availablePermits() == 0)
						executeEvent.release();
					return false;
				}

				if (executeTag != null && this.executeFilterMatch(
						this.executeTagOperation.getValue().getFilter().toArray(new Filter[0]), executeTag)) {
					Map<Integer, havis.middleware.ale.base.operation.tag.result.Result> executeResult = this
							.executeOperationOnTag(executeTag);
					Tag reportTag;
					if (executeTag.getEpcOfUid() == "")
						reportTag = new Tag(new byte[0]);
					else
						reportTag = new Tag(FeHexConvert.hexStringToByteArray(executeTag.getEpcOfUid()));
					if (Tag.isExtended()) {
						if (executeTag.getTidOfUid() == "")
							reportTag.setTid(new byte[0]);
						else
							reportTag.setTid(FeHexConvert.hexStringToByteArray(executeTag.getTidOfUid()));
					}
					byte[] pc = RFCUtils.intToBytes(executeTag.getProtocolControl());
					reportTag.setPc(new byte[] { pc[0], pc[1] });
					byte[] number;
					byte[] status;
					byte[] rssi;

					HashMap<Integer, FedmIscRssiItem> rssiMap = executeTag.getRSSI();
					if (rssiMap != null) {
						int arrSize = 0;
						for (int key : rssiMap.keySet())
							if (key > arrSize)
								arrSize = key;

						number = new byte[arrSize];
						status = new byte[arrSize];
						rssi = new byte[arrSize];

						for (Integer k : rssiMap.keySet()) {
							FedmIscRssiItem item = rssiMap.get(k);
							number[k - 1] = item.antennaNumber;
							status[k - 1] = item.antennaStatus;
							rssi[k - 1] = item.RSSI;
						}

						if (rssiMap.size() > 0)
							reportTag.setSighting(new Sighting(readerConnection.toString(), number[0], rssi[0], reportTag.getFirstTime()));
					}
					reportTag.setResult(executeResult);
					this.sendExecuteReport(reportTag);
					executed = true;
				}

				if (!executed && inventoryTagsLeft == 0) {
					this.inventoryAttempts++;
					if (this.inventoryAttempts == this.readerConnection.getConnectionProperties()
							.getInventoryAttempts())
						this.sendExecuteErrorReport(
								new HashMap<Integer, havis.middleware.ale.base.operation.tag.result.Result>());
				}
			}
		} catch (Exception e) {
			throw new ImplementationException(e);
		} finally {
			this.syncExecuteTagOperation.unlock();
		}
		return executed;
	}

	protected boolean executeFilterMatch(Filter[] filters, FedmIscTagHandler_EPC_Class1_Gen2 tag) {
		if (filters == null || filters.length == 0)
			return true;

		if (filters.length > 0 && filters[0].getBank() == 1 && filters[0].getOffset() == 32) {
			byte[] epcArray;
			if (tag.getEpcOfUid() == "")
				epcArray = new byte[0];
			else
				epcArray = FeHexConvert.hexStringToByteArray(tag.getEpcOfUid());
			if (epcArray.length != filters[0].getMask().length)
				return false;
			for (int i = 0; i < filters[0].getMask().length; i++)
				if (filters[0].getMask()[i] != epcArray[i])
					return false;
		}
		if (Tag.isExtended() && (filters.length > 1 && filters[1].getBank() == 2 && filters[1].getOffset() == 0)) {
			byte[] tidArray;
			if (tag.getTidOfUid() == "")
				tidArray = new byte[0];
			else
				tidArray = FeHexConvert.hexStringToByteArray(tag.getTidOfUid());
			if (tidArray.length != filters[1].getMask().length)
				return false;

			for (int i = 0; i < filters[1].getMask().length; i++)
				if (filters[1].getMask()[i] != tidArray[i])
					return false;
		}
		return true;
	}

	public class InventoryReport {
		Tag tag;
		havis.middleware.ale.base.operation.tag.result.ReadResult[] readResult;

		public InventoryReport(Tag tag, havis.middleware.ale.base.operation.tag.result.ReadResult[] readResult) {
			this.tag = tag;
			this.readResult = readResult;
		}

		/**
		 * @return the tag
		 */
		public Tag getTag() {
			return tag;
		}

		/**
		 * @param tag
		 *            the tag to set
		 */
		public void setTag(Tag tag) {
			this.tag = tag;
		}

		/**
		 * @return the readResult
		 */
		public havis.middleware.ale.base.operation.tag.result.ReadResult[] getReadResult() {
			return readResult;
		}

		/**
		 * @param readResult
		 *            the readResult to set
		 */
		public void setReadResult(havis.middleware.ale.base.operation.tag.result.ReadResult[] readResult) {
			this.readResult = readResult;
		}
	}

	/**
	 * Method to send an inventory report to callback
	 */
	protected void sendInventoryReport(InventoryReport report, Map<Long, TagOperation> operations) {
		for (Entry<Long, TagOperation> entry : operations.entrySet()) {
			Map<Integer, havis.middleware.ale.base.operation.tag.result.Result> opResultList;
			if (entry.getValue().getOperations() != null)
				opResultList = ReaderUtils.toResult(report.readResult, entry.getValue().getOperations());
			else
				opResultList = new HashMap<Integer, havis.middleware.ale.base.operation.tag.result.Result>();

			Tag tag = report.tag.clone();
			tag.setResult(opResultList);
			this.clientCallback.notify(entry.getKey().longValue(), tag);
		}
	}

	/**
	 * Method to send an execute report to callback
	 *
	 * @param executeTag
	 */
	protected void sendExecuteReport(Tag executeTag) {
		this.syncExecuteTagOperation.lock();
		try {
			this.clientCallback.notify(this.executeTagOperation.getKey(), executeTag);
			this.executeTagOperation = new KeyValuePair<>();
			if (this.isConnected && this.tagObserverList.size() == 0)
				this.stopInventory();

			if (executeEvent.availablePermits() == 0)
				executeEvent.release();

		} catch (Exception e) {
			this.clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Sending execute report failed: " + e.getMessage(), e));
		} finally {
			this.syncExecuteTagOperation.unlock();
		}
	}

	/**
	 * Method to send an execute error report to callback
	 *
	 * @param resultList
	 */
	protected void sendExecuteErrorReport(
			Map<Integer, havis.middleware.ale.base.operation.tag.result.Result> resultList) {
		this.syncExecuteTagOperation.lock();
		try {
			for (havis.middleware.ale.base.operation.tag.Operation op : this.executeTagOperation.getValue()
					.getOperations()) {
				if (resultList.containsKey(op.getId()))
					continue;

				switch (op.getType()) {
				case KILL:
					resultList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.KillResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.MISC_ERROR_TOTAL));
					break;
				case LOCK:
					resultList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.LockResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.MISC_ERROR_TOTAL));
					break;
				case PASSWORD:
					resultList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.PasswordResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.MISC_ERROR_TOTAL));
					break;
				case READ:
					resultList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.ReadResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.MISC_ERROR_TOTAL, new byte[0]));
					break;
				case WRITE:
					resultList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.WriteResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.MISC_ERROR_TOTAL, 0));					
				case CUSTOM:
					resultList.put(op.getId(), new havis.middleware.ale.base.operation.tag.result.CustomResult(
							havis.middleware.ale.base.operation.tag.result.ResultState.MISC_ERROR_TOTAL));					
					break;
					
				default:
					break;
				}
			}
			Tag tag = new Tag((byte[]) null);
			tag.setResult(resultList);

			this.clientCallback.notify(this.executeTagOperation.getKey(), tag);
			this.executeTagOperation = new KeyValuePair<>();

			if (this.isConnected && this.tagObserverList.size() == 0)
				this.stopInventory();

			if (executeEvent.availablePermits() == 0)
				executeEvent.release();

		} catch (Exception e) {
			this.clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Sending execute error report failed: " + e.getMessage(), e));
		} finally {
			this.syncExecuteTagOperation.unlock();
		}
	}

	/**
	 * Retrieves Array of Input Port states.
	 */
	protected boolean[] inputPortState;

	/**
	 * Retrieves Array of Output Port states.
	 */
	protected boolean[] outputPortState;

	/**
	 * Retrieves Array of Relay Port states.
	 */
	protected boolean[] relayPortState;

	protected String getEpcOfUid(FedmBrmTableItem tag) {
		String str1 = tag.getStringData(134217730);
		if (str1.length() == 0) {
			return "";
		}
		int i = getProtocolControl(tag);
		int j = ((i & 0xF8) >> 3) * 2;
		if (j == 0) {
			return "";
		}
		String str2 = str1.substring(0, j * 2);
		return str2;
	}

	protected String getTidOfUid(FedmBrmTableItem tag) {
		String str1 = tag.getStringData(134217730);
		if (str1.length() == 0) {
			return "";
		}
		int i = getProtocolControl(tag);
		int j = ((i & 0xF8) >> 3) * 2;
		if (j * 2 >= str1.length()) {
			return "";
		}
		String str2 = str1.substring(j * 2, str1.length());
		return str2;
	}

	protected int getProtocolControl(FedmBrmTableItem tag) {
		return tag.getIntegerData(134217752);
	}

	protected boolean isBarcode(FedmBrmTableItem tag) {
		return tag.transponderType == -62;
	}

	/**
	 * Method to get the initial GPIO State.
	 *
	 * @throws ValidationException
	 * @throws ImplementationException
	 * @throws FedmException
	 * @throws FeReaderDriverException
	 * @throws FePortDriverException
	 */
	protected void getGPIOInitialState() throws ValidationException, ImplementationException, FePortDriverException,
			FeReaderDriverException, FedmException {
		readerLock.lock();
		try {
			FedmIscReaderInfo readerInfo = this.reader.readReaderInfo();

			this.inputPortState = new boolean[readerInfo.noOfInputs];
			this.outputPortState = new boolean[readerInfo.noOfOutputs];
			this.relayPortState = new boolean[readerInfo.noOfRelays];

			int status = 0;
			do {
				if (readerInfo.noOfInputs > 0) {
					status = this.reader.sendProtocol((byte) 0x74);
					if (status != 0)
						break;
				}
				switch (readerInfo.noOfInputs) {
				case 0:
					break;
				case 1:
					this.inputPortState[0] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN1);
					break;
				case 2:
					this.inputPortState[0] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN1);
					this.inputPortState[1] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN2);
					break;
				case 3:
					this.inputPortState[0] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN1);
					this.inputPortState[1] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN2);
					this.inputPortState[2] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN3);
					break;
				case 4:
					this.inputPortState[0] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN1);
					this.inputPortState[1] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN2);
					this.inputPortState[2] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN3);
					this.inputPortState[3] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN4);
					break;
				case 5:
					this.inputPortState[0] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN1);
					this.inputPortState[1] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN2);
					this.inputPortState[2] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN3);
					this.inputPortState[3] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN4);
					this.inputPortState[4] = this.reader.getBooleanData(FedmIscReaderID.FEDM_ISC_TMP_INP_STATE_IN5);
					break;
				default:
					break;
				}

				// Get Configuration
				status = this.reader.readCompleteConfiguration(true);
				if (status != 0)
					break;

				// Get Initial States
				byte data;
				switch (readerInfo.noOfOutputs) {
				case 0:
					break;
				case 1:
					data = this.reader.getConfigParaAsByte(DigitalIO.Output.No1.IdleMode, true);
					this.outputPortState[0] = (data == 0x01 ? true : false);
					break;
				case 2:
					data = this.reader.getConfigParaAsByte(DigitalIO.Output.No1.IdleMode, true);
					this.outputPortState[0] = (data == 0x01 ? true : false);
					data = this.reader.getConfigParaAsByte(DigitalIO.Output.No2.IdleMode, true);
					this.outputPortState[1] = (data == 0x01 ? true : false);
					break;
				default:
					break;
				}

				switch (readerInfo.noOfRelays) {
				case 0:
					break;
				case 1:
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No1.IdleMode, true);
					this.relayPortState[0] = (data == 0x01 ? true : false);
					break;
				case 2:
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No1.IdleMode, true);
					this.relayPortState[0] = (data == 0x01 ? true : false);
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No2.IdleMode, true);
					this.relayPortState[1] = (data == 0x01 ? true : false);
					break;
				case 3:
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No1.IdleMode, true);
					this.relayPortState[0] = (data == 0x01 ? true : false);
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No2.IdleMode, true);
					this.relayPortState[1] = (data == 0x01 ? true : false);
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No3.IdleMode, true);
					this.relayPortState[2] = (data == 0x01 ? true : false);
					break;
				case 4:
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No1.IdleMode, true);
					this.relayPortState[0] = (data == 0x01 ? true : false);
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No2.IdleMode, true);
					this.relayPortState[1] = (data == 0x01 ? true : false);
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No3.IdleMode, true);
					this.relayPortState[2] = (data == 0x01 ? true : false);
					data = this.reader.getConfigParaAsByte(DigitalIO.Relay.No4.IdleMode, true);
					this.relayPortState[3] = (data == 0x01 ? true : false);
					break;
				default:
					break;
				}
			} while (false);
			if (status != 0) {
				if (status > 0)
					throw new ValidationException(
							this.reader.getStatusText((byte) status) + " (" + this.devCaps.getName() + ")!");
				else
					throw new ImplementationException(
							reader.getErrorText(status) + " (" + this.devCaps.getName() + ")!");
			}
		} finally {
			readerLock.unlock();
		}
	}

	private void validateObservation(PortObservation observation) {
		for (Pin pin : observation.getPins()) {
			if (pin.getType() != Type.INPUT)
				this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning,
						"Only input pins could be observed from " + this.devCaps.getModel() + "! "));
			if (pin.getId() < 1 || pin.getId() > this.inputPortState.length)
				this.clientCallback.notify(new Message(Exits.Reader.Controller.Warning, "Only input pins 0 - "
						+ this.inputPortState.length + " could be observed from " + this.devCaps.getModel() + "! "));
		}
	}

	private havis.middleware.ale.base.operation.port.result.ReadResult getPortReadResult(
			havis.middleware.ale.base.operation.port.Operation operation) throws ValidationException {
		switch (operation.getPin().getType()) {
		case INPUT:
			return new havis.middleware.ale.base.operation.port.result.ReadResult(State.SUCCESS,
					(byte) (this.inputPortState[operation.getPin().getId() - 1] ? 0x01 : 0x00));

		case OUTPUT:
			if (operation.getPin().getId() <= this.outputPortState.length) {
				return new havis.middleware.ale.base.operation.port.result.ReadResult(State.SUCCESS,
						(byte) (this.outputPortState[operation.getPin().getId() - 1] ? 0x01 : 0x00));
			} else {
				return new havis.middleware.ale.base.operation.port.result.ReadResult(State.SUCCESS,
						(byte) (this.relayPortState[operation.getPin().getId() - this.outputPortState.length - 1] ? 0x01
								: 0x00));
			}
		default:
			throw new ValidationException("Only port types Input/Output are valid for " + this.devCaps.getModel() + "!" + "("
					+ this.readerConnection + ")!");
		}
	}

	private havis.middleware.ale.base.operation.port.result.WriteResult getPortWriteResult(
			havis.middleware.ale.base.operation.port.Operation operation)
					throws FePortDriverException, FeReaderDriverException, FedmException {
		readerLock.lock();
		try {
			this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_MODE, (byte) 0x01);
			this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_N, (byte) 0x01);
			if (operation.getPin().getId() <= this.outputPortState.length) {
				this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_TYPE_1, (byte) 0x00);
				this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_NR_1, (byte) operation.getPin().getId());
			} else {
				this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_TYPE_1, (byte) 0x04);
				this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_NR_1,
						(byte) operation.getPin().getId() - this.outputPortState.length);
			}

			if (operation.getData() == 0x01)
				this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_MODE_1, (byte) 0x01);
			else
				this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_MODE_1, (byte) 0x02);

			this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_FREQ_1, (byte) 0x00);

			if (operation.getDuration() != null && operation.getDuration() > 0)
				this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_TIME_1,
						(int) (operation.getDuration() / 100.0));
			else
				this.reader.setData(FedmIscReaderID.FEDM_ISC_TMP_0x72_OUT_TIME_1, Integer.MAX_VALUE);

			int status = this.reader.sendProtocol((byte) 0x72);
			if (status != 0)
				return new WriteResult(State.MISC_ERROR_TOTAL);
			else
				return new WriteResult(State.SUCCESS);
		} finally {
			readerLock.unlock();
		}
	}

	/**
	 * Method to send an observation report to callback
	 *
	 * @param port
	 */
	protected void sendObservationReport(final Port port) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					synchronized (syncPortObserverList) {
						for (Long id : portObserverList)
							clientCallback.notify(id, port);
					}
				} catch (Exception e) {
					clientCallback.notify(new Message(Exits.Reader.Controller.Error, "Sending observation report failed: " + e.getMessage(), e));
				}
			}
		}, "portObservationReportThread").start();
	}

	/**
	 * Template method to set the required operating mode in the configuration.
	 *
	 * @return Operating Mode
	 */
	protected abstract RF_RConfiguration.OperatingModeValue getOperatingMode();

	/**
	 * Template method to set the size of the required table.
	 *
	 * @return Status code 0 if table size was successfully set else error or
	 *         status code.
	 * @throws FedmException
	 */
	protected abstract int setReaderTableSize();

	/**
	 * Template method to check reader info.
	 */
	protected abstract void checkReaderInfo();

	/**
	 * Template method to start the inventory using the different reader modes.
	 */
	protected abstract void startInventory() throws ValidationException, ImplementationException;

	/**
	 * Template method to stop the inventory.
	 */
	protected abstract void stopInventory();

	/**
	 * Template method to read a complete bank from a tag
	 *
	 * @param tag
	 *            The tag to read a bank from.
	 * @param bank
	 *            The bank to read.
	 * @param inventoryOperation
	 *            The operation to execute
	 *
	 * @return Read result
	 */
	protected abstract havis.middleware.ale.base.operation.tag.result.ReadResult readBankFromTag(
			FedmIscTagHandler_EPC_Class1_Gen2 tag, int bank, RF_RInventoryOperation inventoryOperation);

	/**
	 * Template method to execute operations on matching tags from the
	 * executeTag list.
	 *
	 * @param executeTag
	 *            List of tags to execute operations on.
	 * @return A list with results for every operation.
	 */
	protected abstract Map<Integer, havis.middleware.ale.base.operation.tag.result.Result> executeOperationOnTag(
			FedmIscTagHandler_EPC_Class1_Gen2 executeTag) throws Exception;

	/**
	 * Template method to start the port observation using the different reader
	 * modes.
	 */
	protected abstract void startPortObservation() throws Exception;

	/**
	 * Template method to stop the port observation.
	 */
	protected abstract void stopPortObservation();

	@Override
	public abstract List<Capabilities> getCapabilities(CapabilityType capType);
	
	@Override
	public List<Configuration> getConfiguration(ConfigurationType arg0, short arg1) {
		if (isDisposed) return null;
		
		List<Configuration> ret = new ArrayList<>();
		
		for (Entry<Short, ConnectType> e : connectTypes.entrySet())
			ret.add(new AntennaConfiguration(e.getKey(), e.getValue()));
		
		return ret;
	}

	@Override
	public void setConfiguration(List<Configuration> configs) {
		if (isDisposed)
			return;
		for (Configuration cfg : configs) {
			if (cfg instanceof AntennaConfiguration) {
				AntennaConfiguration aCfg = (AntennaConfiguration) cfg;
				if (aCfg.getId() == 0) {
					for (Short key : connectTypes.keySet())
						connectTypes.put(key, aCfg.getConnect());
				} else {
					connectTypes.put(aCfg.getId(), aCfg.getConnect());
				}
			}
		}
	}

	protected abstract byte checkAntennas();

	/**
	 * Sets the antenna mask based on the given connect types as controlled
	 * by instances of the {@link AntennaConfiguration} class or the the reader 
	 * property <code>inventoryAntennas</code>.
	 * 
	 * If one of the connect types is set to AUTO the antenna connection states 
	 * are detected automatically by the reader hardware. Otherwise the antenna mask 
	 * is set based only as defined by the {@link ConnectType} of each antenna. 
	 */
	protected void setAntennas() {				
		
		byte antennas = 0;
		
		if (this.connectTypes.containsValue(ConnectType.AUTO)) 
			antennas = checkAntennas();
				
		/* for all 4 antennas */
		for (short antennaIndex = 0; antennaIndex < this.connectTypes.size(); antennaIndex++) {
			
			/* calculate the bit mask for the current antenna: 0001, 0010, 0100, 1000 */
			byte mask = (byte) (1 << antennaIndex); 
			
			/* get the connect type of the current antenna */
			ConnectType ct = connectTypes.get((short)(antennaIndex+1));
			
			/* set the bit of the current antenna by or-ing it with the mask */
			if (ct == ConnectType.TRUE) 
				antennas = (byte)(antennas | mask);
			
			/* unset the bit of the current antenna by and-ing it with the inverted mask */
			else if (ct == ConnectType.FALSE) 
				antennas = (byte)(antennas & ~mask);					
		}
		
		this.antennas = antennas;
	}
	
}
