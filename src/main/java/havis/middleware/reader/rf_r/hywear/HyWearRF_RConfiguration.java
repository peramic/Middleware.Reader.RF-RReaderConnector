package havis.middleware.reader.rf_r.hywear;

import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.reader.Prefix;
import havis.middleware.ale.service.rc.RCConfig;
import havis.middleware.reader.rf_r.RF_RConfiguration;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.feig.FedmIscReader;
import de.feig.ReaderConfig.AccessProtection;
import de.feig.ReaderConfig.AirInterface;
import de.feig.ReaderConfig.Barcode;
import de.feig.ReaderConfig.Device;
import de.feig.ReaderConfig.DigitalIO;
import de.feig.ReaderConfig.HostInterface;
import de.feig.ReaderConfig.OperatingMode;
import de.feig.ReaderConfig.PowerManagement;

/**
 * Class provides objects to hold HyWEAR configuration parameters.
 * 
 */
public class HyWearRF_RConfiguration extends RF_RConfiguration {

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.ReaderConnectors.RF_R500Configuration class.
	 * 
	 * @param reader
	 * @param eeprom
	 */
	public HyWearRF_RConfiguration(FedmIscReader reader, boolean eeprom) {
		super(reader, eeprom);
	}

	@Override
	public RCConfig getReaderConfig() {
		return new RCConfig();
	}

	/**
	 * Validates the configuration and returns a new map with parsed objects.
	 * 
	 * @param properties The properties as strings
	 * @return The properties as parsed objects
	 * @throws ValidationException
	 */
	@Override
	public Map<String, Object> validateConfigurationProperties(Map<String, String> properties) throws ValidationException {
		Map<String, Object> configuration = new HashMap<>();
		for (Entry<String, String> property : properties.entrySet()) {
			try {
				switch (property.getKey()) {
				case Prefix.Reader + AccessProtection.Password:
					configuration.put(AccessProtection.Password, property.getValue());
					break;

				case Prefix.Reader + AirInterface.TimeLimit:
					configuration.put(AirInterface.TimeLimit, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Interface:
					configuration.put(OperatingMode.ScanMode.Interface, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + AirInterface.Antenna.UHF.No1.OutputPower:
					configuration.put(AirInterface.Antenna.UHF.No1.OutputPower, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + AirInterface.Region.UHF.Regulation:
					configuration.put(AirInterface.Region.UHF.Regulation, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataSelector.InputEvents:
					configuration.put(OperatingMode.ScanMode.DataSelector.InputEvents, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Filter.TransponderValidTime:
					configuration.put(OperatingMode.ScanMode.Filter.TransponderValidTime, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataFormat.SeparationChar:
					configuration.put(OperatingMode.ScanMode.DataFormat.SeparationChar, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataFormat.UserSeparationChar:
					configuration.put(OperatingMode.ScanMode.DataFormat.UserSeparationChar, property.getValue());
					break;

				case Prefix.Reader + AirInterface.Antenna.UHF.No1.RSSIFilter:
					configuration.put(AirInterface.Antenna.UHF.No1.RSSIFilter, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataSelector.IDD:
					configuration.put(OperatingMode.ScanMode.DataSelector.IDD, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + PowerManagement.AutoPowerOff:
					configuration.put(PowerManagement.AutoPowerOff, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Trigger.Mode:
					configuration.put(OperatingMode.ScanMode.Trigger.Mode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + AirInterface.ScanTime:
					configuration.put(AirInterface.ScanTime, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataSource.Engine:
					configuration.put(OperatingMode.ScanMode.DataSource.Engine, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Signaler.Buzzer.ActivationSources:
					configuration.put(DigitalIO.Signaler.Buzzer.ActivationSources, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Signaler.Vibrator.ActivationSources:
					configuration.put(DigitalIO.Signaler.Vibrator.ActivationSources, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataFormat.PrefixLength:
					configuration.put(OperatingMode.ScanMode.DataFormat.PrefixLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataFormat.Prefix:
					configuration.put(OperatingMode.ScanMode.DataFormat.Prefix, property.getValue());
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataFormat.SuffixLength:
					configuration.put(OperatingMode.ScanMode.DataFormat.SuffixLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataFormat.Suffix:
					configuration.put(OperatingMode.ScanMode.DataFormat.Suffix, property.getValue());
					break;

				case Prefix.Reader + HostInterface.Bluetooth.DestinationAddress:
					configuration.put(HostInterface.Bluetooth.DestinationAddress, property.getValue());
					break;

				case Prefix.Reader + HostInterface.Bluetooth.PIN:
					configuration.put(HostInterface.Bluetooth.PIN, property.getValue());
					break;

				case Prefix.Reader + HostInterface.WLAN.FrequencyBand:
					configuration.put(HostInterface.WLAN.FrequencyBand, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.WLAN.Security.EAP.Enable_OKC:
					configuration.put(HostInterface.WLAN.Security.EAP.Enable_OKC, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.WLAN.IPv4.Enable_DHCP:
					configuration.put(HostInterface.WLAN.IPv4.Enable_DHCP, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.WLAN.IPv4.IPAddress:
					configuration.put(HostInterface.WLAN.IPv4.IPAddress, InetAddress.getByName(property.getValue()).getAddress());
					break;

				case Prefix.Reader + HostInterface.WLAN.IPv4.PortNumber:
					configuration.put(HostInterface.WLAN.IPv4.PortNumber, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.WLAN.IPv4.SubnetMask:
					configuration.put(HostInterface.WLAN.IPv4.SubnetMask, InetAddress.getByName(property.getValue()).getAddress());
					break;

				case Prefix.Reader + HostInterface.WLAN.IPv4.GatewayAddress:
					configuration.put(HostInterface.WLAN.IPv4.GatewayAddress, InetAddress.getByName(property.getValue()).getAddress());
					break;

				case Prefix.Reader + HostInterface.WLAN.IPv4.DNS1:
					configuration.put(HostInterface.WLAN.IPv4.DNS1, InetAddress.getByName(property.getValue()).getAddress());
					break;

				case Prefix.Reader + HostInterface.WLAN.IPv4.DNS2:
					configuration.put(HostInterface.WLAN.IPv4.DNS2, InetAddress.getByName(property.getValue()).getAddress());
					break;

				case Prefix.Reader + HostInterface.WLAN.Security.ServiceSetIdentifier.SSID:
					configuration.put(HostInterface.WLAN.Security.ServiceSetIdentifier.SSID, property.getValue());
					break;

				case Prefix.Reader + HostInterface.WLAN.Security.WPA2.Key:
					configuration.put(HostInterface.WLAN.Security.WPA2.Key, property.getValue());
					break;

				case Prefix.Reader + HostInterface.WLAN.Security.EAP.Method:
					configuration.put(HostInterface.WLAN.Security.EAP.Method, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.WLAN.Security.EAP.InnerMethod:
					configuration.put(HostInterface.WLAN.Security.EAP.InnerMethod, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.WLAN.Security.EAP.UserID:
					configuration.put(HostInterface.WLAN.Security.EAP.UserID, property.getValue());
					break;

				case Prefix.Reader + HostInterface.WLAN.Security.EAP.UserPassword:
					configuration.put(HostInterface.WLAN.Security.EAP.UserPassword, property.getValue());
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Transmission.Destination.Mode:
					configuration.put(OperatingMode.ScanMode.Transmission.Destination.Mode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Transmission.Destination.IPv4.IPAddress:
					configuration.put(OperatingMode.ScanMode.Transmission.Destination.IPv4.IPAddress, InetAddress.getByName(property.getValue()).getAddress());
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Transmission.Destination.IPv4.Hostname:
					configuration.put(OperatingMode.ScanMode.Transmission.Destination.IPv4.Hostname, property.getValue());
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Transmission.Destination.PortNumber:
					configuration.put(OperatingMode.ScanMode.Transmission.Destination.PortNumber, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Trigger.Enable_UnlockTrigger:
					configuration.put(DigitalIO.Button.Trigger.Enable_UnlockTrigger, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Left.Function:
					configuration.put(DigitalIO.Button.Left.Function, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Right.Function:
					configuration.put(DigitalIO.Button.Right.Function, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Device.Identifier.ScannerID:
					configuration.put(Device.Identifier.ScannerID, property.getValue());
					break;

				case Prefix.Reader + HostInterface.Bluetooth.DeviceName:
					configuration.put(HostInterface.Bluetooth.DeviceName, property.getValue());
					break;

				case Prefix.Reader + DigitalIO.Button.Left.Enable_PowerOff:
					configuration.put(DigitalIO.Button.Left.Enable_PowerOff, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Right.Enable_PowerOff:
					configuration.put(DigitalIO.Button.Right.Enable_PowerOff, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Transmission.Timeout:
					configuration.put(OperatingMode.ScanMode.Transmission.Timeout, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.ScanTime:
					configuration.put(Barcode.ScanTime, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.DataSelector.ScannerID:
					configuration.put(OperatingMode.ScanMode.DataSelector.ScannerID, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Aiming:
					configuration.put(Barcode.Aiming, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Light:
					configuration.put(Barcode.Light, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Sensitivity.Threshold:
					configuration.put(Barcode.Sensitivity.Threshold, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.VideoReverse:
					configuration.put(Barcode.VideoReverse, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_128.Enable:
					configuration.put(Barcode.Type1D.Code_128.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UCC_EAN_128.Enable:
					configuration.put(Barcode.Type1D.UCC_EAN_128.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_8.Enable:
					configuration.put(Barcode.Type1D.EAN_8.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_13.Enable:
					configuration.put(Barcode.Type1D.EAN_13.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_E.Enable:
					configuration.put(Barcode.Type1D.UPC_E.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_A.Enable:
					configuration.put(Barcode.Type1D.UPC_A.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Interleaved_2of5.Enable:
					configuration.put(Barcode.Type1D.Interleaved_2of5.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.ITF_14.Enable:
					configuration.put(Barcode.Type1D.ITF_14.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.ITF_6.Enable:
					configuration.put(Barcode.Type1D.ITF_6.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Matrix_25.Enable:
					configuration.put(Barcode.Type1D.Matrix_25.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_39.Enable:
					configuration.put(Barcode.Type1D.Code_39.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Codabar.Enable:
					configuration.put(Barcode.Type1D.Codabar.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_93.Enable:
					configuration.put(Barcode.Type1D.Code_93.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.AIM_128.Enable:
					configuration.put(Barcode.Type1D.AIM_128.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Industrial_25.Enable:
					configuration.put(Barcode.Type1D.Industrial_25.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Standard_25.Enable:
					configuration.put(Barcode.Type1D.Standard_25.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.GS1_DataBar.Enable:
					configuration.put(Barcode.Type1D.GS1_DataBar.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_128.MinMessageLength:
					configuration.put(Barcode.Type1D.Code_128.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_128.MaxMessageLength:
					configuration.put(Barcode.Type1D.Code_128.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UCC_EAN_128.MinMessageLength:
					configuration.put(Barcode.Type1D.UCC_EAN_128.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UCC_EAN_128.MaxMessageLength:
					configuration.put(Barcode.Type1D.UCC_EAN_128.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.AIM_128.MinMessageLength:
					configuration.put(Barcode.Type1D.AIM_128.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.AIM_128.MaxMessageLength:
					configuration.put(Barcode.Type1D.AIM_128.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Interleaved_2of5.MinMessageLength:
					configuration.put(Barcode.Type1D.Interleaved_2of5.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Interleaved_2of5.MaxMessageLength:
					configuration.put(Barcode.Type1D.Interleaved_2of5.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Matrix_25.MinMessageLength:
					configuration.put(Barcode.Type1D.Matrix_25.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Matrix_25.MaxMessageLength:
					configuration.put(Barcode.Type1D.Matrix_25.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_39.MinMessageLength:
					configuration.put(Barcode.Type1D.Code_39.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_39.MaxMessageLength:
					configuration.put(Barcode.Type1D.Code_39.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Codabar.MinMessageLength:
					configuration.put(Barcode.Type1D.Codabar.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Codabar.MaxMessageLength:
					configuration.put(Barcode.Type1D.Codabar.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_93.MinMessageLength:
					configuration.put(Barcode.Type1D.Code_93.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_93.MaxMessageLength:
					configuration.put(Barcode.Type1D.Code_93.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Industrial_25.MinMessageLength:
					configuration.put(Barcode.Type1D.Industrial_25.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Industrial_25.MaxMessageLength:
					configuration.put(Barcode.Type1D.Industrial_25.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Standard_25.MinMessageLength:
					configuration.put(Barcode.Type1D.Standard_25.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Standard_25.MaxMessageLength:
					configuration.put(Barcode.Type1D.Standard_25.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.PDF417.Enable:
					configuration.put(Barcode.Type2D.PDF417.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.QR_Code.Enable:
					configuration.put(Barcode.Type2D.QR_Code.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.Enable:
					configuration.put(Barcode.Type2D.DataMatrix.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.PDF417.MinMessageLength:
					configuration.put(Barcode.Type2D.PDF417.MinMessageLength, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.PDF417.MaxMessageLength:
					configuration.put(Barcode.Type2D.PDF417.MaxMessageLength, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.QR_Code.MinMessageLength:
					configuration.put(Barcode.Type2D.QR_Code.MinMessageLength, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.QR_Code.MaxMessageLength:
					configuration.put(Barcode.Type2D.QR_Code.MaxMessageLength, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.MinMessageLength:
					configuration.put(Barcode.Type2D.DataMatrix.MinMessageLength, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.MaxMessageLength:
					configuration.put(Barcode.Type2D.DataMatrix.MaxMessageLength, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_8.ExpandToEAN_13:
					configuration.put(Barcode.Type1D.EAN_8.ExpandToEAN_13, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_8.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.EAN_8.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_8.Enable2BitsExpandCharacters:
					configuration.put(Barcode.Type1D.EAN_8.Enable2BitsExpandCharacters, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_8.Enable5BitsExpandCharacters:
					configuration.put(Barcode.Type1D.EAN_8.Enable5BitsExpandCharacters, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_8.AddOnCodeRequired:
					configuration.put(Barcode.Type1D.EAN_8.AddOnCodeRequired, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_13.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.EAN_13.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_13.Enable2BitsAddendaCode:
					configuration.put(Barcode.Type1D.EAN_13.Enable2BitsAddendaCode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_13.Enable5BitsAddendaCode:
					configuration.put(Barcode.Type1D.EAN_13.Enable5BitsAddendaCode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.EAN_13.AddOnCodeRequired:
					configuration.put(Barcode.Type1D.EAN_13.AddOnCodeRequired, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_E.AddOnCodeRequired:
					configuration.put(Barcode.Type1D.UPC_E.AddOnCodeRequired, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_E.ExpandToUPC_A:
					configuration.put(Barcode.Type1D.UPC_E.ExpandToUPC_A, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_E.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.UPC_E.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_E.Enable2BitsAddendaCode:
					configuration.put(Barcode.Type1D.UPC_E.Enable2BitsAddendaCode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_E.Enable5BitsAddendaCode:
					configuration.put(Barcode.Type1D.UPC_E.Enable5BitsAddendaCode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_E.TransmitPrecursor:
					configuration.put(Barcode.Type1D.UPC_E.TransmitPrecursor, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_A.AddOnCodeRequired:
					configuration.put(Barcode.Type1D.UPC_A.AddOnCodeRequired, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_A.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.UPC_A.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_A.Enable2BitsAddendaCode:
					configuration.put(Barcode.Type1D.UPC_A.Enable2BitsAddendaCode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_A.Enable5BitsAddendaCode:
					configuration.put(Barcode.Type1D.UPC_A.Enable5BitsAddendaCode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.UPC_A.TransmitPrecursor:
					configuration.put(Barcode.Type1D.UPC_A.TransmitPrecursor, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Interleaved_2of5.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Interleaved_2of5.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Interleaved_2of5.TransmitCheckDigit_ITF_14:
					configuration.put(Barcode.Type1D.Interleaved_2of5.TransmitCheckDigit_ITF_14, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Interleaved_2of5.TransmitCheckDigit_ITF_6:
					configuration.put(Barcode.Type1D.Interleaved_2of5.TransmitCheckDigit_ITF_6, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Matrix_25.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Matrix_25.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_39.TransmitCode32Prefix:
					configuration.put(Barcode.Type1D.Code_39.TransmitCode32Prefix, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_39.ConvertCode39ToCode32:
					configuration.put(Barcode.Type1D.Code_39.ConvertCode39ToCode32, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_39.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Code_39.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_39.TransmitStartStopCharacters:
					configuration.put(Barcode.Type1D.Code_39.TransmitStartStopCharacters, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_39.ASCIIDecode:
					configuration.put(Barcode.Type1D.Code_39.ASCIIDecode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Codabar.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Codabar.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Codabar.TransmitStartStopCharacters:
					configuration.put(Barcode.Type1D.Codabar.TransmitStartStopCharacters, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.GS1_DataBar.TransmitApplicationID:
					configuration.put(Barcode.Type1D.GS1_DataBar.TransmitApplicationID, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_11.CheckDigitVerification:
					configuration.put(Barcode.Type1D.Code_11.CheckDigitVerification, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_11.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Code_11.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Industrial_25.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Industrial_25.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Standard_25.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Standard_25.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_93.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Code_93.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.PDF417.SingleTwin:
					configuration.put(Barcode.Type2D.PDF417.SingleTwin, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.PDF417.ForwardBackward:
					configuration.put(Barcode.Type2D.PDF417.ForwardBackward, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.PDF417.EnableECIOutput:
					configuration.put(Barcode.Type2D.PDF417.EnableECIOutput, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.PDF417.CharacterEncoding:
					configuration.put(Barcode.Type2D.PDF417.CharacterEncoding, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.QR_Code.SingleTwin:
					configuration.put(Barcode.Type2D.QR_Code.SingleTwin, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.QR_Code.ForwardBackward:
					configuration.put(Barcode.Type2D.QR_Code.ForwardBackward, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.QR_Code.EnableECIOutput:
					configuration.put(Barcode.Type2D.QR_Code.EnableECIOutput, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.QR_Code.CharacterEncoding:
					configuration.put(Barcode.Type2D.QR_Code.CharacterEncoding, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.SingleTwin:
					configuration.put(Barcode.Type2D.DataMatrix.SingleTwin, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.ForwardBackward:
					configuration.put(Barcode.Type2D.DataMatrix.ForwardBackward, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.MirrorImages:
					configuration.put(Barcode.Type2D.DataMatrix.MirrorImages, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.DisableRectangularSymbols:
					configuration.put(Barcode.Type2D.DataMatrix.DisableRectangularSymbols, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.EnableECIOutput:
					configuration.put(Barcode.Type2D.DataMatrix.EnableECIOutput, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.DataMatrix.CharacterEncoding:
					configuration.put(Barcode.Type2D.DataMatrix.CharacterEncoding, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.WLAN.Region:
					configuration.put(HostInterface.WLAN.Region, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Codabar.StartStopCharacters:
					configuration.put(Barcode.Type1D.Codabar.StartStopCharacters, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Codabar.Letter:
					configuration.put(Barcode.Type1D.Codabar.Letter, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Signaler.Buzzer.ActivationTime:
					configuration.put(DigitalIO.Signaler.Buzzer.ActivationTime, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Signaler.Vibrator.ActivationTime:
					configuration.put(DigitalIO.Signaler.Vibrator.ActivationTime, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.ISSN.Enable:
					configuration.put(Barcode.Type1D.ISSN.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.ISBN.Enable:
					configuration.put(Barcode.Type1D.ISBN.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Plessey.Enable:
					configuration.put(Barcode.Type1D.Plessey.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_11.Enable:
					configuration.put(Barcode.Type1D.Code_11.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.MSI_Plessey.Enable:
					configuration.put(Barcode.Type1D.MSI_Plessey.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_11.MinMessageLength:
					configuration.put(Barcode.Type1D.Code_11.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Code_11.MaxMessageLength:
					configuration.put(Barcode.Type1D.Code_11.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Plessey.MinMessageLength:
					configuration.put(Barcode.Type1D.Plessey.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Plessey.MaxMessageLength:
					configuration.put(Barcode.Type1D.Plessey.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.MSI_Plessey.MinMessageLength:
					configuration.put(Barcode.Type1D.MSI_Plessey.MinMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.MSI_Plessey.MaxMessageLength:
					configuration.put(Barcode.Type1D.MSI_Plessey.MaxMessageLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.Chinese_Sensible_Code.Enable:
					configuration.put(Barcode.Type2D.Chinese_Sensible_Code.Enable, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.Chinese_Sensible_Code.MinMessageLength:
					configuration.put(Barcode.Type2D.Chinese_Sensible_Code.MinMessageLength, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.Chinese_Sensible_Code.MaxMessageLength:
					configuration.put(Barcode.Type2D.Chinese_Sensible_Code.MaxMessageLength, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.ISBN.Format:
					configuration.put(Barcode.Type1D.ISBN.Format, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.ISBN.Enable2BitsAddendaCode:
					configuration.put(Barcode.Type1D.ISBN.Enable2BitsAddendaCode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.ISBN.Enable5BitsAddendaCode:
					configuration.put(Barcode.Type1D.ISBN.Enable5BitsAddendaCode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.ISBN.AddOnCodeRequired:
					configuration.put(Barcode.Type1D.ISBN.AddOnCodeRequired, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.Plessey.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.Plessey.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.MSI_Plessey.CheckDigitVerification:
					configuration.put(Barcode.Type1D.MSI_Plessey.CheckDigitVerification, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type1D.MSI_Plessey.TransmitCheckDigit:
					configuration.put(Barcode.Type1D.MSI_Plessey.TransmitCheckDigit, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.Chinese_Sensible_Code.SingleTwin:
					configuration.put(Barcode.Type2D.Chinese_Sensible_Code.SingleTwin, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + Barcode.Type2D.Chinese_Sensible_Code.ForwardBackward:
					configuration.put(Barcode.Type2D.Chinese_Sensible_Code.ForwardBackward, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + PowerManagement.RfOffTime:
					configuration.put(PowerManagement.RfOffTime, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + OperatingMode.ScanMode.Framing:
					configuration.put(OperatingMode.ScanMode.Framing, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.Bluetooth.HID.KeyStroke_Delay:
					configuration.put(HostInterface.Bluetooth.HID.KeyStroke_Delay, Integer.parseInt(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Trigger.Function:
					configuration.put(DigitalIO.Button.Trigger.Function, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Right.TransferString:
					configuration.put(DigitalIO.Button.Right.TransferString, property.getValue());
					break;

				case Prefix.Reader + DigitalIO.Button.Left.TransferString:
					configuration.put(DigitalIO.Button.Left.TransferString, property.getValue());
					break;

				case Prefix.Reader + DigitalIO.Button.Trigger.TransferString:
					configuration.put(DigitalIO.Button.Trigger.TransferString, property.getValue());
					break;

				case Prefix.Reader + DigitalIO.Signaler.LED.Mode:
					configuration.put(DigitalIO.Signaler.LED.Mode, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + HostInterface.Bluetooth.HID.AutoReconnect:
					configuration.put(HostInterface.Bluetooth.HID.AutoReconnect, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Right.TransferStringLength:
					configuration.put(DigitalIO.Button.Right.TransferStringLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Left.TransferStringLength:
					configuration.put(DigitalIO.Button.Left.TransferStringLength, Byte.parseByte(property.getValue()));
					break;

				case Prefix.Reader + DigitalIO.Button.Trigger.TransferStringLength:
					configuration.put(DigitalIO.Button.Trigger.TransferStringLength, Byte.parseByte(property.getValue()));
					break;

				default:
					if (property.getKey().startsWith(Prefix.Reader)) {
						throw new ValidationException("Unkown reader property '" + property + "' for HyWEAR compact reader!");
					}
					break;
				}
			} catch (ValidationException e) {
				throw e;
			} catch (Exception e) {
				throw new ValidationException(property.getKey() + ": " + e.getMessage());
			}
		}
		return configuration;
	}

	@Override
	protected int setMode(byte operatingMode, byte identifierMode) {
		// mode cannot be changed
		return 0;
	}

}
