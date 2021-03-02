package havis.middleware.reader.rf_r;
/**
 * Class provides objects to hold RF_R connector connection informations.
 */
public class RF_RConnection {
	private RF_RConnectionType connectionType;
	private String host;
	private int port;
	private int deviceID;
	private int timeout = 5000;
	private RF_RProperties connectionProperties;
	
	
	/**
	 * Gets the RF_R connector connection type.
	 * @return Connection type
	 */
    public RF_RConnectionType getConnectionType() {
		return connectionType;
	}

    /**
     * Sets the RF_R connector connection type.
     * @param connectionType
     */
	public void setConnectionType(RF_RConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	/**
	 * Gets the RF_R reader IP address.
	 * @return Reader IP Address
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the RF_R reader IP address.
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Gets the RF_R reader connection port.
	 * @return Reader port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the RF_R reader connection port.
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the device ID of the reader.
	 * @return Reader ID
	 */
	public int getDeviceID() {
		return deviceID;
	}
/**
 * Sets the device ID of the reader.
 * @param deviceID
 */
	public void setDeviceID(int deviceID) {
		this.deviceID = deviceID;
	}

	/**
	 * Gets the timeout interval after which a non response 
    from the RF_R reader will raise a timeout exception.
    The default value is 5000 milliseconds.
	 * @return Timeout exception time
	 */
	public int getTimeout() {
		return timeout;
	}
/**
 * Sets the timeout interval after which a non response 
    from the RF_R reader will raise a timeout exception.
 * @param timeout
 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Gets an object that holds all connection properties.
	 * @return Connection properties
	 */
	public RF_RProperties getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * Sets an object that holds all connection properties.
	 * @param connectionProperties
	 */
	public void setConnectionProperties(RF_RProperties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}
	
	/**
	 * Returns a new instance of the Havis.Middleware.Reader.RF_RConnection class for connection type TCP.
	 * @param host The host of the reader
	 * @param port The TCP port to use for connection to the reader
	 * @return TCP connection
	 */
    public static RF_RConnection GetTCPConnection(String host, int port)
    {
        RF_RConnection ret = new RF_RConnection();
    	ret.setConnectionType(RF_RConnectionType.TCP);
    	ret.setHost(host);
    	ret.setPort(port);
    	
    	return ret;
    }
    
    /**
     * Returns a new instance of the Havis.Middleware.Reader.RF_RConnection class for connection type USB.
     * @param deviceID The device ID of the reader
     * @return USB connection
     */
    public static RF_RConnection GetUSBConnection(int deviceID)
    {
    	RF_RConnection ret = new RF_RConnection();
    	ret.setConnectionType(RF_RConnectionType.USB);
    	ret.setDeviceID(deviceID);

    	return ret;
    }

    /**
     * Returns a new instance of the Havis.Middleware.Reader.RF_RConnection class for connection type COMM.
     * @param port The comm port to use for connection to the reader
     * @return Serial connection
     */
    public static RF_RConnection GetCOMMConnection(int port)
    {
    	RF_RConnection ret = new RF_RConnection();
    	ret.setConnectionType(RF_RConnectionType.COMM);
    	ret.setPort(port);
    	return ret;
    }

    /**
     * Method that evaluates if this object is equal to obj.
     * @param obj
     * @return true if objects are equal and false otherwise
     */
    @Override
	public boolean equals(Object obj)
    {
        if (obj == null || this.getClass() != obj.getClass()) return false;
        RF_RConnection connection = (RF_RConnection)obj;

        switch (this.connectionType)
        {
            case TCP:
                return (this.connectionType == connection.connectionType) &&
                       (this.host == connection.host) &&
                       (this.port == connection.port);
            case USB:
                return (this.connectionType == connection.connectionType) &&
                       (this.deviceID == connection.deviceID);
            case COMM:
                return (this.connectionType == connection.connectionType) &&
                       (this.port == connection.port);
            default:
                return false;
        }
    }

    /**
     * Method that returns the hashcode of this object.
     * @return The hash code as integer
     */
    @Override
	public int hashCode()
    {
        return super.hashCode();
    }

    /**
     * Return a string value that represents this instance.
     * @return The string that represents this instance
     */
    @Override
	public String toString()
    {
        switch (this.connectionType)
        {
            case TCP:
                return "Type: '" + this.connectionType + "'; Host: '" + this.host + "'; Port: '" + this.port + "'";
            case USB:
                return "Type: '" + this.connectionType + "'; DeviceID: '" + this.deviceID + "'";
            case COMM:
                return "Type: '" + this.connectionType + "'; Port: '" + this.port + "'";
            default:
                return "";
        }
    }
    
    public static byte[] intToBytes(int num) {
		byte[] arr = new byte[4];
		byte b = 0;
		for (int i = 4; i > 0; i--) {
			b = (byte) (num & 0xff);
			num >>= 8;
			arr[i - 1] = b;
		}
		return arr;
	}
    
    public static void main(String[] args) {
    	
    	int num = 0xfefdfcfb;
    	
    	byte[] bytes = intToBytes(num); //new byte[] { (byte)0xfe, (byte)0xfd, (byte)0xfc, (byte)0xfb }; 

    	 
    	
        String val = "";
        for (int i = 0; i < bytes.length; i++)
        {
        	val += (bytes[i] & 0xff) + "";
        	if (i+1 < bytes.length) val += ".";
        }
        
        System.out.println(val);
	}
}
