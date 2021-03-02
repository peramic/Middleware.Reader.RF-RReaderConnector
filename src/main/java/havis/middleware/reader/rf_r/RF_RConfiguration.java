package havis.middleware.reader.rf_r;

import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.service.rc.RCConfig;
import havis.middleware.ale.service.rc.RCProperty;

import java.util.Map;
import java.util.Map.Entry;

import de.feig.FePortDriverException;
import de.feig.FeReaderDriverException;
import de.feig.FedmException;
import de.feig.FedmIscReader;

/**
 * Class provides objects to hold RF_R configuration parameters.
 */
public abstract class RF_RConfiguration {
	/**
	 * Enumeration that provide all supported operations modes for RF_R readers.
	 */
	public enum OperatingModeValue {
		/**
		 * The host mode value.
		 */
		HostMode((byte) 0x00),
		/**
		 * The scan mode value.
		 */
		ScanMode((byte) 0x01),
		/**
		 * The buffered read mode value.
		 */
		BufferedReadMode((byte) 0x80),
		/**
		 * The notification mode value.
		 */
		NotificationMode((byte) 0xC0);

		private final byte value;

		private OperatingModeValue(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}
	}

	/**
	 * Enumeration that provide all supported transponder identifier modes for RF_R
	 * readers.
	 */
	public enum TranspoderIdentifierModeValue {
		/**
		 * The automatic mode value.
		 */
		AutomaticMode,

		/**
		 * The IDD is UID mode value.
		 */
		IDDisUID,

		/**
		 * The EPC and TID mode value.
		 */
		EPCandTID;
	}

	public interface ReaderCall {
		int call() throws ImplementationException;
	}

	private FedmIscReader reader;

	/**
	 * Gets the reader.
	 * 
	 * @return reader object
	 */
	public FedmIscReader getReader() {
		return reader;
	}

	/**
	 * Sets the reader.
	 * 
	 * @param reader
	 */
	public void setReader(FedmIscReader reader) {
		this.reader = reader;
	}

	/**
	 * TODO
	 */
	protected boolean eeprom;

	/**
	 * Initializes a new instance of the {@link RF_RConfiguration} class.
	 * 
	 * @param reader
	 * @param eeprom
	 */
	public RF_RConfiguration(FedmIscReader reader, boolean eeprom) {
		this.reader = reader;
		this.eeprom = eeprom;
	}

	/**
	 * Method to get string parameter from reader configuration.
	 * 
	 * @param location The location of the parameter.
	 * @param name     The name of the parameter.
	 * @return The requested parameter.
	 */
	protected RCProperty getString(String location, String name) {
		RCProperty property = new RCProperty();
		property.setName(name);
		addString(location, property);
		return property;
	}

	/**
	 * Method to add string parameter to an property.
	 * 
	 * @param location The location of the prameter.
	 * @param property The property to add.
	 */
	protected void addString(String location, RCProperty property) {
		String data = this.reader.getConfigParaAsString(location, eeprom);
		property.getValue().add(data);
	}

	/**
	 * Method to get unsigned integer parameter from reader configuration.
	 * 
	 * @param location The location of the parameter.
	 * @param name     The name of the parameter.
	 * @return The requested parameter.
	 */
	protected RCProperty getUInt(String location, String name) {
		RCProperty property = new RCProperty();
		property.setName(name);
		addUInt(location, property);
		return property;
	}

	/**
	 * Method to add unsigned integer parameter to an property.
	 * 
	 * @param location The location of the prameter.
	 * @param property The property to add.
	 */
	protected void addUInt(String location, RCProperty property) {
		int data = reader.getConfigParaAsInteger(location, eeprom);
		property.getValue().add(data + "");
	}

	/**
	 * Method to get ip parameter from reader configuration.
	 * 
	 * @param location The location of the parameter.
	 * @param name     The name of the parameter.
	 * @return The requested parameter.
	 */
	protected RCProperty getIP(String location, String name) {
		RCProperty property = new RCProperty();
		property.setName(name);
		addIP(location, property);
		return property;
	}

	/**
	 * Method to add IP parameter to an property.
	 * 
	 * @param location The location of the prameter.
	 * @param property The property to add.
	 */
	protected void addIP(String location, RCProperty property) {
		int para = reader.getConfigParaAsInteger(location, eeprom);
		byte[] bytes = intToBytes(para);

		String val = "";
		for (int i = 0; i < bytes.length; i++) {
			val += (bytes[i] & 0xff) + "";
			if (i + 1 < bytes.length)
				val += ".";
		}

		property.getValue().add(val);
	}

	public byte[] intToBytes(int num) {
		byte[] arr = new byte[4];
		byte b = 0;
		for (int i = 4; i > 0; i--) {
			b = (byte) (num & 0xff);
			num >>= 8;
			arr[i - 1] = b;
		}
		return arr;
	}

	/**
	 * Method to get byte parameter from reader configuration.
	 * 
	 * @param location The location of the parameter.
	 * @param name     The name of the parameter.
	 * @return The requested parameter.
	 */
	protected RCProperty getByte(String location, String name) {
		RCProperty property = new RCProperty();
		property.setName(name);
		addByte(location, property);
		return property;
	}

	/**
	 * Method to add byte parameter to an property.
	 * 
	 * @param location The location of the prameter.
	 * @param property The property to add.
	 */
	protected void addByte(String location, RCProperty property) {
		byte data = reader.getConfigParaAsByte(location, eeprom);
		property.getValue().add(data + "");
	}

	/**
	 * Returns the reader configuration.
	 * 
	 * @return The reader configuration
	 */
	public abstract RCConfig getReaderConfig();

	/**
	 * Validates the configuration and returns a new dictionary with parsed objects.
	 * 
	 * @param properties The properties as strings
	 * @return The properties as parsed objects
	 */
	public abstract Map<String, Object> validateConfigurationProperties(Map<String, String> properties) throws ValidationException;

	/**
	 * Applies the complete configuration to reader and resets the RF controller
	 * 
	 * @param configuration  The configuration with parsed parameter values
	 * @param operatingMode  The operation mode
	 * @param identifierMode the identifier mode
	 * @throws ValidationException
	 * @throws ImplementationException
	 */
	public void applyCompleteReaderConfig(Map<String, Object> configuration, byte operatingMode, byte identifierMode, ReaderCall resetReaderCall)
			throws ValidationException, ImplementationException {
		int status = 0;
		do {
			// Get Configuration
			try {
				status = this.reader.readCompleteConfiguration(this.eeprom);
			} catch (FePortDriverException | FeReaderDriverException | FedmException e) {
				throw new ImplementationException(e);
			}

			if (status != 0)
				break;

			// Set Configuration Parameter
			setConfigParams(configuration);

			status = setMode(operatingMode, identifierMode);

			// Apply Configuration
			try {
				status = this.reader.applyConfiguration(this.eeprom);
			} catch (FePortDriverException | FeReaderDriverException | FedmException e) {
				throw new ImplementationException(e);
			}



			if (status > 1)
				break;
			if (resetReaderCall != null) {
				status = resetReaderCall.call();
			}

			if (status > 1)
				break;

			int retries = 0;
			do {
				status = RF_RStatus.Busy.getValue();
				try {
					if (++retries > 100) {
						throw new ImplementationException("Never leave busy state");
					}
					Thread.sleep(100);
					status = this.reader.sendProtocol((byte) 0x66);
				} catch (FePortDriverException e) {
				} catch (FeReaderDriverException | FedmException | InterruptedException e) {
					throw new ImplementationException(e);
				}
			} while (status == RF_RStatus.Busy.getValue());
		} while (false);

		if (status != 0) {
			if (status > 0)
				throw new ValidationException(reader.getStatusText((byte) status));
			else
				throw new ImplementationException(reader.getErrorText(status));
		}
	}

	protected int setMode(byte operatingMode, byte identifierMode) {
		int status = 0;
		status = this.reader.setConfigPara(de.feig.ReaderConfig.OperatingMode.Mode, operatingMode, this.eeprom);
		if (status == 0)
			status = this.reader.setConfigPara(de.feig.ReaderConfig.Transponder.Miscellaneous.IdentifierInterpretationMode, identifierMode, this.eeprom);
		return status;
	}

	/**
	 * Applies the configuration without resetting the RF controller (see
	 * {@link RF_RConfiguration#applyCompleteReaderConfig(Map, byte, byte)})
	 * 
	 * @param configuration The configuration with parsed parameter values
	 * @throws ValidationException
	 * @throws ImplementationException
	 */
	public void applyReaderConfig(Map<String, Object> configuration) throws ValidationException, ImplementationException {
		setConfigParams(configuration);
		try {
			this.reader.applyConfiguration(this.eeprom);
		} catch (FePortDriverException | FeReaderDriverException | FedmException e) {
			throw new ImplementationException(e);
		}

	}

	private void setConfigParams(Map<String, Object> configuration) {
		for (Entry<String, Object> configParam : configuration.entrySet()) {
			if (configParam.getValue() instanceof Boolean)
				reader.setConfigPara(configParam.getKey(), ((Boolean) configParam.getValue()).booleanValue(), this.eeprom);
			else if (configParam.getValue() instanceof Byte)
				reader.setConfigPara(configParam.getKey(), ((Byte) configParam.getValue()).byteValue(), this.eeprom);
			else if (configParam.getValue() instanceof byte[])
				reader.setConfigPara(configParam.getKey(), (byte[]) configParam.getValue(), this.eeprom);
			else if (configParam.getValue() instanceof Long)
				reader.setConfigPara(configParam.getKey(), ((Long) configParam.getValue()).longValue(), this.eeprom);
			else if (configParam.getValue() instanceof String)
				setStringParameter(configParam.getKey(), (String) configParam.getValue());
			else if (configParam.getValue() instanceof Integer)
				reader.setConfigPara(configParam.getKey(), ((Integer) configParam.getValue()).intValue(), this.eeprom);
		}
	}

	public int setStringParameter(String key, String value) {
		byte[] currentValue = reader.getConfigParaAsByteArray(key, this.eeprom);
		byte[] data = new byte[currentValue.length];
		byte[] newValue = value.getBytes();
		System.arraycopy(newValue, 0, data, 0, Math.min(data.length, newValue.length));
		return reader.setConfigPara(key, data, this.eeprom);
	}

	public String getStringParameter(String key) {
		byte[] currentValue = reader.getConfigParaAsByteArray(key, this.eeprom);
		return new String(currentValue);
	}
}
