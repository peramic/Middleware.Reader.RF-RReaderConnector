package havis.middleware.reader.rf_r;

public enum RF_RStatus {
    /**
     * Data has been read or control command has been executed.
     */
    OK(0x00),

    /**
     * No Transponder is located within the detection field of the Reader.
     * The Transponder in the detection field has been switched to mute.
     * Because of the interference Reader is not able to read the Transponder anymore.
     */
    No_Transponder(0x01),

    /**
     * CRC16 data error on received data.
     */
    Data_False(0x02),

    /**
     * Negative plausibility check of the written data.
     */
    Write_Error(0x03),

    /**
     * The required data are outside of the logical or physical Transponder-address area.
     */
    Address_Error(0x04),

    /**
     * This command is not applicable at the Transponder.
     */
    Wrong_Transponder_type(0x05),

    /**
     * Access password is wrong
     */
    Authent_Error(0x08),

    /**
     * The Reader is in full activity. The host should repeat the command later.
     */
    Busy(0x0F),

    /**
     * The EEPROM of the Reader is not able to be written on.
     * Before writing onto the EEPROM a faulty checksum of parameters has been detected.
     */
    
    EEPROM_Failure(0x10),

    /**
     * The value range of the parameters was exceeded.
     */
    Parameter_Range_Error(0x11),

    /**
     * Configuration access without having logged in to the Reader before.
     */
    Login_Request(0x13),

    /**
     * Login attempt with wrong password.
     */
    Login_Error(0x14),

    /**
     * The configuration block is reserved for future use.
     */
    Read_Protect(0x15),

    /**
     * The configuration block is reserved for future use.
     */
    Write_Protect(0x16),
 
    /**
     * The firmware must be activated first using Ha-VIS RFID Config program and the command “Set Firmware Upgrade”.
     */
    Firmware_activation_required(0x17),

    /**
     * Firmwareversion conflict between RFC and FPGA.
     * Conflict between the supported tagdrivers of RFC and FPGA.
     * Readertype is not supported by the FPGA.
     * Mismatch between RFC Firmware and Hardware.
     */
    Wrong_Firmware(0x18),

    /**
     * The Reader does not support the selected function.
     */
    Unknown_Command(0x80),

    /**
     * The selected function has the wrong number of parameters.
     */
    Length_Error(0x81),

    /**
     * A Host command was sent to the Reader in the Buffered Read Mode.
     * A Buffered Read Mode protocol was sent to the Reader in the standard mode.
     * The command with More bit does not correspond with the last command.
     */
    Command_not_available(0x82),
    
    /**
     * Timeout for Transponder communication.
     * The collision handling algorithm was not continued until no collision is detected.
     */ 
    RF_communication_error(0x83),

    /**
     * The antenna configuration isn’t correct.
     * The environment is too noisy.
     * The RF power doesn’t have the configured value.
     * All RF channel are occupied (EU Reader only). 
     */    
    RF_Warning(0x84),

    /**
     * There is no valid data in the Buffered Read Mode.
     * There is no Transponder in the antenna field.
     * The VALID-TIME hasn’t elapsed for Transponders in the antenna field.
     */        
    No_valid_Data(0x92),

    /**
     * A data buffer overflow occurred.
     */
    Data_Buffer_Overflow(0x93),

    /**
     * There are more Transponder data sets requested than the response protocol can transfer at once.
     */
    More_Data(0x94),

    /**
     * A Tag error code was sent from the transponder. The Tag error code is shown in the following byte.
     */
    Tag_Error(0x95),

    /**
     * An array boundary error occurred.
     */
    Array_Boundary_Error(0x98),

    /**
     * RFC, Communication link between ACC and RFC, RF Decoder (FPGA) or Hardware Filter works not properly.
     */
    Hardware_Warning(0xF1),

    /**
     * ACC is initialized partly or completely with default values and Host Mode may be enabled.
     */
    Initialization_Warning(0xF2);
	
	private final int value;
	
	private RF_RStatus(int value) {
		this.value = value;	
	}

	public int getValue() {
		return value;
	}
	
	public static RF_RStatus forValue(int value) {
		switch(value) {
			case 0x00: return OK;
			case 0x01: return No_Transponder;
			case 0x02: return Data_False;
			case 0x03: return Write_Error;
			case 0x04: return Address_Error;
			case 0x05: return Wrong_Transponder_type;
			case 0x08: return Authent_Error;
			case 0x0F: return Busy;
			case 0x10: return EEPROM_Failure;
			case 0x11: return Parameter_Range_Error;
			case 0x13: return Login_Request;
			case 0x14: return Login_Error;
			case 0x15: return Read_Protect;
			case 0x16: return Write_Protect;
			case 0x17: return Firmware_activation_required;
			case 0x18: return Wrong_Firmware;
			case 0x80: return Unknown_Command;
			case 0x81: return Length_Error;
			case 0x82: return Command_not_available;
			case 0x83: return RF_communication_error;
			case 0x84: return RF_Warning;
			case 0x92: return No_valid_Data;
			case 0x93: return Data_Buffer_Overflow;
			case 0x94: return More_Data;
			case 0x95: return Tag_Error;
			case 0x98: return Array_Boundary_Error;
			case (byte) 0x98: return Array_Boundary_Error;
			case 0xF1: return Hardware_Warning;
			case 0xF2: return Initialization_Warning;
			default: throw new IllegalArgumentException("No RF_R_Status exists with value: " + value);
		}
	}
}



