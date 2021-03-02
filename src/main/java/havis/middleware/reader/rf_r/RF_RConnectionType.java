package havis.middleware.reader.rf_r;

/**
 * Enumeration that provide all supported connection types for RF_RConnection.
 */
public enum RF_RConnectionType {
	/** 
	 * TCP connection type. For connection via network.
	 */
    TCP,
    /**
     * USB connection type. For connection via usb port.
     */
    USB,
    /**
     * COMM connection type. For connection via serial port.
     */
    COMM;
}
