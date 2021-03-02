package havis.middleware.reader.rf_r500;

import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.reader.Prefix;
import havis.middleware.ale.service.rc.RCConfig;
import havis.middleware.reader.rf_r.RF_RConfiguration;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.feig.FeHexConvert;
import de.feig.FedmIscReader;
import de.feig.ReaderConfig.AirInterface;
import de.feig.ReaderConfig.Clock;
import de.feig.ReaderConfig.DigitalIO;
import de.feig.ReaderConfig.HostInterface;
import de.feig.ReaderConfig.OperatingMode;
import de.feig.ReaderConfig.SystemTools;
import de.feig.ReaderConfig.Transponder;

/**
 * Class provides objects to hold RF_R500 configuration parameters.
 * 
 */
public class RF_R500Configuration extends RF_RConfiguration {

	/**
	 * Initializes a new instance of the
	 * Havis.Middleware.ReaderConnectors.RF_R500Configuration class.
	 * 
	 * @param reader
	 * @param eeprom
	 */
	public RF_R500Configuration(FedmIscReader reader, boolean eeprom) {
		super(reader, eeprom);
	}

	@Override
	public RCConfig getReaderConfig() {
		return new RCConfig();
	}

	/**
	 * Validates the configuration and returns a new map with parsed objects.
	 * 
	 * @param properties
	 *            The properties as strings
	 * @return The properties as parsed objects
	 * @throws ValidationException
	 */
	@Override
	public Map<String, Object> validateConfigurationProperties(
			Map<String, String> properties) throws ValidationException {
		Map<String, Object> configuration = new HashMap<>();
		for (Entry<String, String> property : properties.entrySet()) {
			try {
				switch (property.getKey()) {
				/* HostInteface */

				/* HostInterface.Interfaces */

				case Prefix.Reader + HostInterface.Interfaces:
					if (property.getValue().contains("LAN"))
						configuration.put(HostInterface.Enable_LAN, 1);
					else
						configuration.put(HostInterface.Enable_LAN, 0);

					if (property.getValue().contains("USB"))
						configuration.put(HostInterface.Enable_USB, 1);
					else
						configuration.put(HostInterface.Enable_USB, 0);

					if (property.getValue().contains("RS232"))
						configuration.put(HostInterface.Enable_RS232, 1);
					else
						configuration.put(HostInterface.Enable_RS232, 0);

					if (property.getValue().contains("RS4xx"))
						configuration.put(HostInterface.Enable_RS4xx, 1);
					else
						configuration.put(HostInterface.Enable_RS4xx, 0);

					if (property.getValue().contains("WLAN"))
						configuration.put(HostInterface.Enable_WLAN, 1);
					else
						configuration.put(HostInterface.Enable_WLAN, 0);

					if (property.getValue().contains("Discovery"))
						configuration.put(HostInterface.Enable_Discovery, 1);
					else
						configuration.put(HostInterface.Enable_Discovery, 0);
					break;

				/* HostInterface.Serial */

				case Prefix.Reader + HostInterface.Serial.BusAddress:
					configuration.put(HostInterface.Serial.BusAddress,
							Byte.parseByte(property.getValue()));
					break;
				case Prefix.Reader + HostInterface.Serial.Baudrate:
					configuration.put(HostInterface.Serial.Baudrate,
							Byte.parseByte(property.getValue()));
					break;
				case Prefix.Reader + HostInterface.Serial.Parity:
					configuration.put(HostInterface.Serial.Parity,
							Byte.parseByte(property.getValue()));
					break;
				case Prefix.Reader + HostInterface.Serial.Databits:
					configuration.put(HostInterface.Serial.Databits,
							Byte.parseByte(property.getValue()));
					break;
				case Prefix.Reader + HostInterface.Serial.Stopbits:
					configuration.put(HostInterface.Serial.Stopbits,
							Byte.parseByte(property.getValue()));
					break;

				/* HostInterface.Serial.RS4xx */

				case Prefix.Reader
						+ HostInterface.Serial.RS4xx.Enable_TerminationResistors:
					configuration
							.put(HostInterface.Serial.RS4xx.Enable_TerminationResistors,
									Byte.parseByte(property.getValue()));
					break;

				/* HostInterface.LAN */

				/* HostInterface.LAN.Keepalive */

				case Prefix.Reader + HostInterface.LAN.Keepalive.Enable:
					configuration.put(HostInterface.LAN.Keepalive.Enable,
							Byte.parseByte(property.getValue()));
					break;
				case Prefix.Reader
						+ HostInterface.LAN.Keepalive.RetransmissionCount:
					configuration.put(
							HostInterface.LAN.Keepalive.RetransmissionCount,
							Byte.parseByte(property.getValue()));
					break;
				case Prefix.Reader + HostInterface.LAN.Keepalive.IdleTime:
					configuration.put(HostInterface.LAN.Keepalive.IdleTime,
							Integer.parseInt(property.getValue()));
					break;
				case Prefix.Reader + HostInterface.LAN.Keepalive.IntervalTime:
					configuration.put(HostInterface.LAN.Keepalive.IntervalTime,
							Integer.parseInt(property.getValue()));
					break;

				/* HostInterface.LAN.LocalHost */

				case Prefix.Reader + HostInterface.LAN.LocalHost.PortNumber:
					configuration.put(HostInterface.LAN.LocalHost.PortNumber,
							Integer.parseInt(property.getValue()));

					break;

				/* HostInterface.LAN.IPv4 */

				case Prefix.Reader + HostInterface.LAN.IPv4.Enable_IPv4:
					configuration.put(HostInterface.LAN.IPv4.Enable_IPv4,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.LAN.IPv4.IPAddress:
					// IP must use Hex String.
					String sIP = FeHexConvert.byteArrayToHexString(InetAddress
							.getByName(property.getValue()).getAddress());
					configuration.put(HostInterface.LAN.IPv4.IPAddress, sIP);
					break;
				case Prefix.Reader + HostInterface.LAN.IPv4.PortNumber:
					configuration.put(HostInterface.LAN.IPv4.PortNumber,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.LAN.IPv4.SubnetMask:
					// SubnetMask must use Hex String.
					String sSubnetMask = FeHexConvert
							.byteArrayToHexString(InetAddress.getByName(
									property.getValue()).getAddress());
					configuration.put(HostInterface.LAN.IPv4.SubnetMask,
							sSubnetMask);
					break;
				case Prefix.Reader + HostInterface.LAN.IPv4.GatewayAddress:
					configuration.put(HostInterface.LAN.IPv4.GatewayAddress,
							InetAddress.getByName(property.getValue())
									.getAddress());

					break;
				case Prefix.Reader + HostInterface.LAN.IPv4.Enable_DHCP:
					configuration.put(HostInterface.LAN.IPv4.Enable_DHCP,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.LAN.IPv4.TCP_UserTimeout:
					configuration.put(HostInterface.LAN.IPv4.TCP_UserTimeout,
							Integer.parseInt(property.getValue()));

					break;

				/* HostInterface.WLAN */

				case Prefix.Reader + HostInterface.WLAN.NetworkType:
					configuration.put(HostInterface.WLAN.NetworkType,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.AdHocChannel:
					configuration.put(HostInterface.WLAN.AdHocChannel,
							Byte.parseByte(property.getValue()));

					break;

				/* HostInterface.WLAN.Keepalive */

				case Prefix.Reader + HostInterface.WLAN.Keepalive.Enable:
					configuration.put(HostInterface.WLAN.Keepalive.Enable,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ HostInterface.WLAN.Keepalive.RetransmissionCount:
					configuration.put(
							HostInterface.WLAN.Keepalive.RetransmissionCount,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.Keepalive.IdleTime:
					configuration.put(HostInterface.WLAN.Keepalive.IdleTime,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.Keepalive.IntervalTime:
					configuration.put(
							HostInterface.WLAN.Keepalive.IntervalTime,
							Integer.parseInt(property.getValue()));

					break;

				/* HostInterface.WLAN.Security */

				case Prefix.Reader
						+ HostInterface.WLAN.Security.AuthenticationType:
					configuration.put(
							HostInterface.WLAN.Security.AuthenticationType,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.Security.EncryptionType:
					configuration.put(
							HostInterface.WLAN.Security.EncryptionType,
							Byte.parseByte(property.getValue()));

					break;

				/* HostInterface.WLAN.Security.ServiceSetIdentifier */

				case Prefix.Reader
						+ HostInterface.WLAN.Security.ServiceSetIdentifier.Length:
					configuration
							.put(HostInterface.WLAN.Security.ServiceSetIdentifier.Length,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ HostInterface.WLAN.Security.ServiceSetIdentifier.SSID:
					configuration
							.put(HostInterface.WLAN.Security.ServiceSetIdentifier.SSID,
									property.getValue());// Byte.parseByte(property.getValue()));

					break;

				/* HostInterface.WLAN.Security.WEP */

				case Prefix.Reader + HostInterface.WLAN.Security.WEP.KeyLength:
					configuration.put(
							HostInterface.WLAN.Security.WEP.KeyLength,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.Security.WEP.Key:
					configuration.put(HostInterface.WLAN.Security.WEP.Key,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;

				/* HostInterface.WLAN.Security.WPA */

				case Prefix.Reader + HostInterface.WLAN.Security.WPA.KeyLength:
					configuration.put(
							HostInterface.WLAN.Security.WPA.KeyLength,
							Byte.parseByte(property.getValue()));

					break;

				case Prefix.Reader + HostInterface.WLAN.Security.WPA.Key:
					configuration.put(HostInterface.WLAN.Security.WPA.Key,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;

				/* HostInterface.WLAN.Security.WPA2 */

				case Prefix.Reader + HostInterface.WLAN.Security.WPA2.KeyLength:
					configuration.put(
							HostInterface.WLAN.Security.WPA2.KeyLength,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.Security.WPA2.Key:
					configuration.put(HostInterface.WLAN.Security.WPA2.Key,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;

				/* HostInterface.WLAN.IPv4 */
				case Prefix.Reader + HostInterface.WLAN.IPv4.Enable_IPv4:
					configuration.put(HostInterface.WLAN.IPv4.Enable_IPv4,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.IPv4.IPAddress:
					// IP must use Hex String.
					sIP = FeHexConvert.byteArrayToHexString(InetAddress
							.getByName(property.getValue()).getAddress());
					configuration.put(HostInterface.WLAN.IPv4.IPAddress, sIP);
					break;
				case Prefix.Reader + HostInterface.WLAN.IPv4.PortNumber:
					configuration.put(HostInterface.WLAN.IPv4.PortNumber,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.IPv4.SubnetMask:
					// SubnetMask must use Hex String.
					sSubnetMask = FeHexConvert.byteArrayToHexString(InetAddress
							.getByName(property.getValue()).getAddress());
					configuration.put(HostInterface.WLAN.IPv4.SubnetMask,
							sSubnetMask);
					break;
				case Prefix.Reader + HostInterface.WLAN.IPv4.GatewayAddress:
					configuration.put(HostInterface.WLAN.IPv4.GatewayAddress,
							InetAddress.getByName(property.getValue())
									.getAddress());

					break;
				case Prefix.Reader + HostInterface.WLAN.IPv4.Enable_DHCP:
					configuration.put(HostInterface.WLAN.IPv4.Enable_DHCP,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + HostInterface.WLAN.IPv4.TCP_UserTimeout:
					configuration.put(HostInterface.WLAN.IPv4.TCP_UserTimeout,
							Integer.parseInt(property.getValue()));

					break;

				/* HastInterface.DataClock */

				case Prefix.Reader + HostInterface.DataClock.Format:
					configuration.put(HostInterface.DataClock.Format,
							Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode */
				case Prefix.Reader + OperatingMode.Mode:
					configuration.put(OperatingMode.Mode,
							Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.BufferedReadMode */
				/* OpertingMode.BufferedreadMode.DataSelector */
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.UID:
					configuration.put(
							OperatingMode.BufferedReadMode.DataSelector.UID,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.Data:
					configuration.put(
							OperatingMode.BufferedReadMode.DataSelector.Data,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.AntennaNo:
					configuration
							.put(OperatingMode.BufferedReadMode.DataSelector.AntennaNo,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.Time:
					configuration.put(
							OperatingMode.BufferedReadMode.DataSelector.Time,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.Date:
					configuration.put(
							OperatingMode.BufferedReadMode.DataSelector.Date,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.InputEvents:
					configuration
							.put(OperatingMode.BufferedReadMode.DataSelector.InputEvents,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.RSSI:
					configuration.put(
							OperatingMode.BufferedReadMode.DataSelector.RSSI,
							Byte.parseByte(property.getValue()));

					break;
				/* OperatingMode.BufferedReadMode.DataSelector.Mode */
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.Mode.Enable_AntennaPool:
					configuration
							.put(OperatingMode.BufferedReadMode.DataSelector.Mode.Enable_AntennaPool,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSelector.Mode.ReadCompleteBank:
					configuration
							.put(OperatingMode.BufferedReadMode.DataSelector.Mode.ReadCompleteBank,
									Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.BufferedReadMode.DataSource */
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSource.BankNo:
					configuration.put(
							OperatingMode.BufferedReadMode.DataSource.BankNo,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSource.FirstDataBlock:
					configuration
							.put(OperatingMode.BufferedReadMode.DataSource.FirstDataBlock,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSource.NoOfDataBlocks:
					configuration
							.put(OperatingMode.BufferedReadMode.DataSource.NoOfDataBlocks,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.DataSource.ByteOrderOfData:
					configuration
							.put(OperatingMode.BufferedReadMode.DataSource.ByteOrderOfData,
									Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.BufferedReadMode.Filter */
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Filter.TransponderValidTime:
					configuration
							.put(OperatingMode.BufferedReadMode.Filter.TransponderValidTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Filter.Enable_Input1Event:
					configuration
							.put(OperatingMode.BufferedReadMode.Filter.Enable_Input1Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Filter.Enable_Input2Event:
					configuration
							.put(OperatingMode.BufferedReadMode.Filter.Enable_Input2Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Filter.Enable_Input3Event:
					configuration
							.put(OperatingMode.BufferedReadMode.Filter.Enable_Input3Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Filter.Enable_Input4Event:
					configuration
							.put(OperatingMode.BufferedReadMode.Filter.Enable_Input4Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Filter.Enable_Input5Event:
					configuration
							.put(OperatingMode.BufferedReadMode.Filter.Enable_Input5Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Filter.Enable_TriggerEvent:
					configuration
							.put(OperatingMode.BufferedReadMode.Filter.Enable_TriggerEvent,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Filter.Enable_TimeoutEvent:
					configuration
							.put(OperatingMode.BufferedReadMode.Filter.Enable_TimeoutEvent,
									Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.BufferedReadMode.Trigger */
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Enable:
					configuration.put(
							OperatingMode.BufferedReadMode.Trigger.Enable,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Condition:
					configuration.put(
							OperatingMode.BufferedReadMode.Trigger.Condition,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Enable_UnlimitTransponderValidTime:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Enable_UnlimitTransponderValidTime,
									Byte.parseByte(property.getValue()));

					break;
				/*
				 * OperatingMode.BufferedReadMode.Trigger.Source.Input.No1, No2,
				 * No3, No4, No5
				 */
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No1.TriggerUse:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No1.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No1.HoldTime:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No1.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No2.TriggerUse:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No2.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No2.HoldTime:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No2.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No3.TriggerUse:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No3.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No3.HoldTime:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No3.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No4.TriggerUse:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No4.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No4.HoldTime:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No4.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No5.TriggerUse:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No5.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.BufferedReadMode.Trigger.Source.Input.No5.HoldTime:
					configuration
							.put(OperatingMode.BufferedReadMode.Trigger.Source.Input.No5.HoldTime,
									Integer.parseInt(property.getValue()));

					break;

				/* OperatingMode.NotificationMode */

				/* OperatingMode.NotificationMode.DataSelector */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.UID:
					configuration.put(
							OperatingMode.NotificationMode.DataSelector.UID,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.Data:
					configuration.put(
							OperatingMode.NotificationMode.DataSelector.Data,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.AntennaNo:
					configuration
							.put(OperatingMode.NotificationMode.DataSelector.AntennaNo,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.Time:
					configuration.put(
							OperatingMode.NotificationMode.DataSelector.Time,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.Date:
					configuration.put(
							OperatingMode.NotificationMode.DataSelector.Date,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.InputEvents:
					configuration
							.put(OperatingMode.NotificationMode.DataSelector.InputEvents,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.RSSI:
					configuration.put(
							OperatingMode.NotificationMode.DataSelector.RSSI,
							Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.Notification.DataSelector.Mode */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.Mode.Enable_AntennaPool:
					configuration
							.put(OperatingMode.NotificationMode.DataSelector.Mode.Enable_AntennaPool,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSelector.Mode.ReadCompleteBank:
					configuration
							.put(OperatingMode.NotificationMode.DataSelector.Mode.ReadCompleteBank,
									Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.NotificationMode.DataSource */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSource.BankNo:
					configuration.put(
							OperatingMode.NotificationMode.DataSource.BankNo,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSource.FirstDataBlock:
					configuration
							.put(OperatingMode.NotificationMode.DataSource.FirstDataBlock,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSource.NoOfDataBlocks:
					configuration
							.put(OperatingMode.NotificationMode.DataSource.NoOfDataBlocks,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.DataSource.ByteOrderOfData:
					configuration
							.put(OperatingMode.NotificationMode.DataSource.ByteOrderOfData,
									Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.NotificationMode.Filter */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Filter.TransponderValidTime:
					configuration
							.put(OperatingMode.NotificationMode.Filter.TransponderValidTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Filter.Enable_Input1Event:
					configuration
							.put(OperatingMode.NotificationMode.Filter.Enable_Input1Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Filter.Enable_Input2Event:
					configuration
							.put(OperatingMode.NotificationMode.Filter.Enable_Input2Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Filter.Enable_Input3Event:
					configuration
							.put(OperatingMode.NotificationMode.Filter.Enable_Input3Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Filter.Enable_Input4Event:
					configuration
							.put(OperatingMode.NotificationMode.Filter.Enable_Input4Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Filter.Enable_Input5Event:
					configuration
							.put(OperatingMode.NotificationMode.Filter.Enable_Input5Event,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Filter.Enable_TriggerEvent:
					configuration
							.put(OperatingMode.NotificationMode.Filter.Enable_TriggerEvent,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Filter.Enable_TimeoutEvent:
					configuration
							.put(OperatingMode.NotificationMode.Filter.Enable_TimeoutEvent,
									Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.NotificationMode.Trigger */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Enable:
					configuration.put(
							OperatingMode.NotificationMode.Trigger.Enable,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Condition:
					configuration.put(
							OperatingMode.NotificationMode.Trigger.Condition,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Enable_UnlimitTransponderValidTime:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Enable_UnlimitTransponderValidTime,
									Byte.parseByte(property.getValue()));

					break;
				/*
				 * OperatingMode.NotificationMode.Trigger.Source.Input.No1, No2,
				 * No3, No4, No5
				 */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No1.TriggerUse:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No1.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No1.HoldTime:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No1.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No2.TriggerUse:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No2.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No2.HoldTime:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No2.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No3.TriggerUse:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No3.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No3.HoldTime:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No3.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No4.TriggerUse:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No4.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No4.HoldTime:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No4.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No5.TriggerUse:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No5.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Trigger.Source.Input.No5.HoldTime:
					configuration
							.put(OperatingMode.NotificationMode.Trigger.Source.Input.No5.HoldTime,
									Integer.parseInt(property.getValue()));

					break;

				/* OperatingMode.NotificationMode.Transmission */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.NotifyTrigger:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.NotifyTrigger,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.TimeTriggeredTime:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.TimeTriggeredTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.DataSetsLimit:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.DataSetsLimit,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.Enable_Acknowledge:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.Enable_Acknowledge,
									Byte.parseByte(property.getValue()));

					break;
				/* OperatingMode.NotificationMode.Transmission.Destination */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.Destination.PortNumber:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.Destination.PortNumber,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.Destination.ConnectionHoldTime:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.Destination.ConnectionHoldTime,
									Integer.parseInt(property.getValue()));

					break;
				/*
				 * OperatingMode.NotificationMode.Transmission.Destination.IPv4
				 */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.Destination.IPv4.IPAddress:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.Destination.IPv4.IPAddress,
									InetAddress.getByName(property.getValue())
											.getAddress());
					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.Destination.IPv4.TCP_UserTimeout:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.Destination.IPv4.TCP_UserTimeout,
									Integer.parseInt(property.getValue()));

					break;

				/* OperatingMode.NotificationMode.Transmission.KeepAlive */
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.KeepAlive.Enable:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.KeepAlive.Enable,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.NotificationMode.Transmission.KeepAlive.IntervalTime:
					configuration
							.put(OperatingMode.NotificationMode.Transmission.KeepAlive.IntervalTime,
									Integer.parseInt(property.getValue()));

					break;

				/* OperatingMode.ScanMode */

				case Prefix.Reader + OperatingMode.ScanMode.Interface:
					configuration.put(OperatingMode.ScanMode.Interface,
							Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.ScanMode.DataSelector */

				case Prefix.Reader + OperatingMode.ScanMode.DataSelector.UID:
					configuration.put(OperatingMode.ScanMode.DataSelector.UID,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + OperatingMode.ScanMode.DataSelector.Data:
					configuration.put(OperatingMode.ScanMode.DataSelector.Data,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSelector.AntennaNo:
					configuration.put(
							OperatingMode.ScanMode.DataSelector.AntennaNo,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + OperatingMode.ScanMode.DataSelector.Time:
					configuration.put(OperatingMode.ScanMode.DataSelector.Time,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + OperatingMode.ScanMode.DataSelector.Date:
					configuration.put(OperatingMode.ScanMode.DataSelector.Date,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSelector.InputEvents:
					configuration.put(
							OperatingMode.ScanMode.DataSelector.InputEvents,
							Byte.parseByte(property.getValue()));

					break;
				/* OperatingMode.ScanMode.DataSelector.Mode */
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSelector.Mode.Enable_AntennaPool:
					configuration
							.put(OperatingMode.ScanMode.DataSelector.Mode.Enable_AntennaPool,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSelector.Mode.ReadCompleteBank:
					configuration
							.put(OperatingMode.ScanMode.DataSelector.Mode.ReadCompleteBank,
									Byte.parseByte(property.getValue()));

					break;

				/* OperatingMode.ScanMode.DataSource */
				case Prefix.Reader + OperatingMode.ScanMode.DataSource.BankNo:
					configuration.put(OperatingMode.ScanMode.DataSource.BankNo,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSource.FirstDataBlock:
					configuration.put(
							OperatingMode.ScanMode.DataSource.FirstDataBlock,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSource.NoOfDataBlocks:
					configuration.put(
							OperatingMode.ScanMode.DataSource.NoOfDataBlocks,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSource.ByteOrderOfData:
					configuration.put(
							OperatingMode.ScanMode.DataSource.ByteOrderOfData,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSource.FirstByte:
					configuration.put(
							OperatingMode.ScanMode.DataSource.FirstByte,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataSource.NoOfBytes:
					configuration.put(
							OperatingMode.ScanMode.DataSource.NoOfBytes,
							Integer.parseInt(property.getValue()));

					break;

				/* OperatingMode.ScanMode.DataFormat */
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.BusAddressPrefix:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.BusAddressPrefix,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + OperatingMode.ScanMode.DataFormat.Format:
					configuration.put(OperatingMode.ScanMode.DataFormat.Format,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.NoOfUserHeaderChars:
					configuration
							.put(OperatingMode.ScanMode.DataFormat.NoOfUserHeaderChars,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.UserHeaderChar1:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.UserHeaderChar1,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.UserHeaderChar2:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.UserHeaderChar2,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.UserHeaderChar3:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.UserHeaderChar3,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.UserHeaderChar4:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.UserHeaderChar4,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.SeparationChar:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.SeparationChar,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.UserSeparationChar:
					configuration
							.put(OperatingMode.ScanMode.DataFormat.UserSeparationChar,
									property.getValue());// Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + OperatingMode.ScanMode.DataFormat.EndChar:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.EndChar,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.NoOfUserEndChars:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.NoOfUserEndChars,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.UserEndChar1:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.UserEndChar1,
							property.getValue());// Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.UserEndChar2:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.UserEndChar2,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.DataFormat.UserEndChar3:
					configuration.put(
							OperatingMode.ScanMode.DataFormat.UserEndChar3,
							property.getValue());// Integer.parseInt(property.getValue()));

					break;

				/* OperatingMode.ScanMode.Filter */
				case Prefix.Reader
						+ OperatingMode.ScanMode.Filter.TransponderValidTime:
					configuration.put(
							OperatingMode.ScanMode.Filter.TransponderValidTime,
							Integer.parseInt(property.getValue()));

					break;

				/* OperatingMode.ScanMode.Trigger */
				case Prefix.Reader + OperatingMode.ScanMode.Trigger.Enable:
					configuration.put(OperatingMode.ScanMode.Trigger.Enable,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + OperatingMode.ScanMode.Trigger.Condition:
					configuration.put(OperatingMode.ScanMode.Trigger.Condition,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Enable_UnlimitTransponderValidTime:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Enable_UnlimitTransponderValidTime,
									Byte.parseByte(property.getValue()));

					break;

				/*
				 * OperatingMode.ScanMode.Trigger.Source.Input.No1,No2,No3,No4,
				 * No5
				 */
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No1.TriggerUse:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No1.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No1.HoldTime:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No1.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No2.TriggerUse:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No2.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No2.HoldTime:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No2.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No3.TriggerUse:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No3.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No3.HoldTime:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No3.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No4.TriggerUse:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No4.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No4.HoldTime:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No4.HoldTime,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No5.TriggerUse:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No5.TriggerUse,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.ScanMode.Trigger.Source.Input.No5.HoldTime:
					configuration
							.put(OperatingMode.ScanMode.Trigger.Source.Input.No5.HoldTime,
									Integer.parseInt(property.getValue()));

					break;

				/* OperatingMode.Miscellaneous */

				case Prefix.Reader
						+ OperatingMode.Miscellaneous.TransponderIdentification.Source:
					configuration
							.put(OperatingMode.Miscellaneous.TransponderIdentification.Source,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.Miscellaneous.TransponderIdentification.DataBlockNo:
					configuration
							.put(OperatingMode.Miscellaneous.TransponderIdentification.DataBlockNo,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ OperatingMode.Miscellaneous.TransponderIdentification.NoOfDataBlocks:
					configuration
							.put(OperatingMode.Miscellaneous.TransponderIdentification.NoOfDataBlocks,
									Integer.parseInt(property.getValue()));

					break;

				/* AirInterface */

				case Prefix.Reader + AirInterface.TimeLimit:
					configuration.put(AirInterface.TimeLimit,
							Integer.parseInt(property.getValue()));

					break;

				/* AirInterface.Antenna */

				/* AirInterface.Antenna.UHF */
				// / * AirInterface.Antenna.UHF.No1, No2,No3,No4 */
				case Prefix.Reader + AirInterface.Antenna.UHF.No1.OutputPower:
					configuration.put(AirInterface.Antenna.UHF.No1.OutputPower,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + AirInterface.Antenna.UHF.No2.OutputPower:
					configuration.put(AirInterface.Antenna.UHF.No2.OutputPower,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + AirInterface.Antenna.UHF.No3.OutputPower:
					configuration.put(AirInterface.Antenna.UHF.No3.OutputPower,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + AirInterface.Antenna.UHF.No4.OutputPower:
					configuration.put(AirInterface.Antenna.UHF.No4.OutputPower,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ AirInterface.Antenna.UHF.Miscellaneous.Enable_DCPower:
					int value = 0;
					if (property.getValue().contains("AO1"))
						value = value + 1;
					if (property.getValue().contains("AO2"))
						value = value + 2;
					if (property.getValue().contains("AO3"))
						value = value + 4;
					if (property.getValue().contains("AO4"))
						value = value + 8;
					configuration
							.put(AirInterface.Antenna.UHF.Miscellaneous.Enable_DCPower,
									value);

					break;

				/* AirInterface.Region */

				/* AirInterface.Region.UHF */
				case Prefix.Reader + AirInterface.Region.UHF.Regulation:
					configuration.put(AirInterface.Region.UHF.Regulation,
							Integer.parseInt(property.getValue()));

					break;

				/* AirInterface.Region.UHF.EU */

				/* AirInterface.Region.UHF.EU.Channel */

				/* AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan */

				/*
				 * AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.
				 * PreferredChannels
				 */

				case Prefix.Reader
						+ AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.NoOfChannels:
					configuration
							.put(AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.NoOfChannels,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.ChannelNo1:
					configuration
							.put(AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.ChannelNo1,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.ChannelNo2:
					configuration
							.put(AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.ChannelNo2,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.ChannelNo3:
					configuration
							.put(AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.ChannelNo3,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.ChannelNo4:
					configuration
							.put(AirInterface.Region.UHF.EU.Channel.EN302208_4_ChannelPlan.PreferredChannels.ChannelNo4,
									Byte.parseByte(property.getValue()));

					break;

				/* AirInterface.Region.UHF.FCC */

				/* AirInterface.Region.UHF.FCC.Channel */

				case Prefix.Reader
						+ AirInterface.Region.UHF.FCC.Channel.UpperChannel:
					configuration.put(
							AirInterface.Region.UHF.FCC.Channel.UpperChannel,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ AirInterface.Region.UHF.FCC.Channel.LowerChannel:
					configuration.put(
							AirInterface.Region.UHF.FCC.Channel.LowerChannel,
							Byte.parseByte(property.getValue()));

					break;

				/* AirInterface.Multiplexer */

				case Prefix.Reader + AirInterface.Multiplexer.Enable:
					configuration.put(AirInterface.Multiplexer.Enable,
							Byte.parseByte(property.getValue()));

					break;

				/* AirInterface.Multiplexer.UHF.Internal */
				case Prefix.Reader
						+ AirInterface.Multiplexer.UHF.Internal.AntennaSelectionMode:
					configuration
							.put(AirInterface.Multiplexer.UHF.Internal.AntennaSelectionMode,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ AirInterface.Multiplexer.UHF.Internal.NoOfAntennas:
					configuration.put(
							AirInterface.Multiplexer.UHF.Internal.NoOfAntennas,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ AirInterface.Multiplexer.UHF.Internal.SelectedAntennas:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					configuration
							.put(AirInterface.Multiplexer.UHF.Internal.SelectedAntennas,
									value);

					break;

				/* AirInterface.Multiplexer.UHF.External */

				/* AirInterface.Multiplexer.UHF.External.Output */

				case Prefix.Reader
						+ AirInterface.Multiplexer.UHF.External.Output.No1.SelectedAntennas:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					if (property.getValue().contains("No5"))
						value = value + 16;
					if (property.getValue().contains("No6"))
						value = value + 32;
					if (property.getValue().contains("No7"))
						value = value + 64;
					if (property.getValue().contains("No8"))
						value = value + 128;
					configuration
							.put(AirInterface.Multiplexer.UHF.External.Output.No1.SelectedAntennas,
									value);

					break;
				case Prefix.Reader
						+ AirInterface.Multiplexer.UHF.External.Output.No2.SelectedAntennas:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					if (property.getValue().contains("No5"))
						value = value + 16;
					if (property.getValue().contains("No6"))
						value = value + 32;
					if (property.getValue().contains("No7"))
						value = value + 64;
					if (property.getValue().contains("No8"))
						value = value + 128;
					configuration
							.put(AirInterface.Multiplexer.UHF.External.Output.No2.SelectedAntennas,
									value);

					break;
				case Prefix.Reader
						+ AirInterface.Multiplexer.UHF.External.Output.No3.SelectedAntennas:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					if (property.getValue().contains("No5"))
						value = value + 16;
					if (property.getValue().contains("No6"))
						value = value + 32;
					if (property.getValue().contains("No7"))
						value = value + 64;
					if (property.getValue().contains("No8"))
						value = value + 128;
					configuration
							.put(AirInterface.Multiplexer.UHF.External.Output.No3.SelectedAntennas,
									value);

					break;
				case Prefix.Reader
						+ AirInterface.Multiplexer.UHF.External.Output.No4.SelectedAntennas:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					if (property.getValue().contains("No5"))
						value = value + 16;
					if (property.getValue().contains("No6"))
						value = value + 32;
					if (property.getValue().contains("No7"))
						value = value + 64;
					if (property.getValue().contains("No8"))
						value = value + 128;
					configuration
							.put(AirInterface.Multiplexer.UHF.External.Output.No4.SelectedAntennas,
									value);

					break;

				/* Transponder */

				/* Transponder.Driver */

				/* Transponder.Driver.UHF */
				case Prefix.Reader + Transponder.Driver.UHF.EPC_Class1Gen2:
					configuration.put(Transponder.Driver.UHF.EPC_Class1Gen2,
							Byte.parseByte(property.getValue()));

					break;

				/* Transponder.Anticollison */
				case Prefix.Reader + Transponder.Anticollision.Enable:
					configuration.put(Transponder.Anticollision.Enable,
							Byte.parseByte(property.getValue()));

					break;

				/* Transponder.PersistenceReset */

				case Prefix.Reader + Transponder.PersistenceReset.Mode:
					configuration.put(Transponder.PersistenceReset.Mode,
							Byte.parseByte(property.getValue()));
					break;

				/* Transponder.PersistenceReset.Antenna */

				case Prefix.Reader
						+ Transponder.PersistenceReset.Antenna.No1.PersistenceResetTime:
					configuration
							.put(Transponder.PersistenceReset.Antenna.No1.PersistenceResetTime,
									Integer.parseInt(property.getValue()));
					break;
				case Prefix.Reader
						+ Transponder.PersistenceReset.Antenna.No2.PersistenceResetTime:
					configuration
							.put(Transponder.PersistenceReset.Antenna.No2.PersistenceResetTime,
									Integer.parseInt(property.getValue()));
					break;
				case Prefix.Reader
						+ Transponder.PersistenceReset.Antenna.No3.PersistenceResetTime:
					configuration
							.put(Transponder.PersistenceReset.Antenna.No3.PersistenceResetTime,
									Integer.parseInt(property.getValue()));
					break;
				case Prefix.Reader
						+ Transponder.PersistenceReset.Antenna.No4.PersistenceResetTime:
					configuration
							.put(Transponder.PersistenceReset.Antenna.No4.PersistenceResetTime,
									Integer.parseInt(property.getValue()));
					break;

				/* Transponder.UHF */

				/* Transponder.UHF.EPC_Class1Gen2 */

				/* Transponder.UHF.EPC_Class1Gen2.Anticollision */

				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.Anticollision.Session:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.Anticollision.Session,
									Byte.parseByte(property.getValue()));

					break;

				/* Transponder.UHF.EPC_Class1Gen2.SelectionMask */

				/* Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1 */
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1.BankNo:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1.BankNo,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1.MaskLength:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1.MaskLength,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1.FirstBit:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1.FirstBit,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1.Mask:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No1.Mask,
									property.getValue());// Integer.parseInt(property.getValue()));

					break;

				/* Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2 */
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.BankNo:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.BankNo,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.MaskLength:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.MaskLength,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.FirstBit:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.FirstBit,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.Mask:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.Mask,
									property.getValue());// Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.Negation:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.Negation,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.Logic:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No2.Logic,
									Byte.parseByte(property.getValue()));

					break;

				/* Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3 */
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.BankNo:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.BankNo,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.MaskLength:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.MaskLength,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.FirstBit:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.FirstBit,
									Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.Mask:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.Mask,
									property.getValue());// Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.Negation:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.Negation,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader
						+ Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.Logic:
					configuration
							.put(Transponder.UHF.EPC_Class1Gen2.SelectionMask.No3.Logic,
									Byte.parseByte(property.getValue()));

					break;

				/* Transponder.Miscellaneous */
				case Prefix.Reader
						+ Transponder.Miscellaneous.IdentifierInterpretationMode:
					configuration
							.put(Transponder.Miscellaneous.IdentifierInterpretationMode,
									Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + Transponder.Miscellaneous.TIDLength:
					configuration.put(Transponder.Miscellaneous.TIDLength,
							Byte.parseByte(property.getValue()));

					break;

				/* DigitalIO */

				/* DitgitalIO.Input.No1, No2, No3, No4, No5 */
				case Prefix.Reader + DigitalIO.Input.No1.Mode:
					configuration.put(DigitalIO.Input.No1.Mode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Input.No2.Mode:
					configuration.put(DigitalIO.Input.No2.Mode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Input.No3.Mode:
					configuration.put(DigitalIO.Input.No3.Mode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Input.No4.Mode:
					configuration.put(DigitalIO.Input.No4.Mode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Input.No5.Mode:
					configuration.put(DigitalIO.Input.No5.Mode,
							Integer.parseInt(property.getValue()));

					break;

				/* DigitalIO.Output */
				/* DigitalIO.Output.No1 */
				case Prefix.Reader + DigitalIO.Output.No1.IdleMode:
					configuration.put(DigitalIO.Output.No1.IdleMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Output.No1.IdleFlashMode:
					configuration.put(DigitalIO.Output.No1.IdleFlashMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Output.No1.SettlingTime:
					configuration.put(DigitalIO.Output.No1.SettlingTime,
							Integer.parseInt(property.getValue()));

					break;
				/* DigitalIO.Output.No1.ReadEventActivation */
				case Prefix.Reader
						+ DigitalIO.Output.No1.ReadEventActivation.AntennaNo:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					configuration.put(
							DigitalIO.Output.No1.ReadEventActivation.AntennaNo,
							value);

					break;

				/* DigitalIO.Output.No2 */
				case Prefix.Reader + DigitalIO.Output.No2.IdleMode:
					configuration.put(DigitalIO.Output.No2.IdleMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Output.No2.IdleFlashMode:
					configuration.put(DigitalIO.Output.No2.IdleFlashMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Output.No2.SettlingTime:
					configuration.put(DigitalIO.Output.No2.SettlingTime,
							Integer.parseInt(property.getValue()));

					break;
				/* DigitalIO.Output.No2.ReadEventActivation */
				case Prefix.Reader
						+ DigitalIO.Output.No2.ReadEventActivation.AntennaNo:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					configuration.put(
							DigitalIO.Output.No2.ReadEventActivation.AntennaNo,
							value);

					break;

				/* DigitalIO.Relay */

				/* DigitalIO.Relay.No1 */
				case Prefix.Reader + DigitalIO.Relay.No1.IdleMode:
					configuration.put(DigitalIO.Relay.No1.IdleMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Relay.No1.IdleFlashMode:
					configuration.put(DigitalIO.Relay.No1.IdleFlashMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Relay.No1.SettlingTime:
					configuration.put(DigitalIO.Relay.No1.SettlingTime,
							Integer.parseInt(property.getValue()));

					break;

				/* DigitalIO.Relay.No1.ReadEventActivation */
				case Prefix.Reader
						+ DigitalIO.Relay.No1.ReadEventActivation.AntennaNo:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					configuration.put(
							DigitalIO.Relay.No1.ReadEventActivation.AntennaNo,
							value);

					break;

				/* DigitalIO.Relay.No2 */
				case Prefix.Reader + DigitalIO.Relay.No2.IdleMode:
					configuration.put(DigitalIO.Relay.No2.IdleMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Relay.No2.IdleFlashMode:
					configuration.put(DigitalIO.Relay.No2.IdleFlashMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Relay.No2.SettlingTime:
					configuration.put(DigitalIO.Relay.No2.SettlingTime,
							Integer.parseInt(property.getValue()));

					break;

				/* DigitalIO.Relay.No2.ReadEventActivation */
				case Prefix.Reader
						+ DigitalIO.Relay.No2.ReadEventActivation.AntennaNo:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					configuration.put(
							DigitalIO.Relay.No2.ReadEventActivation.AntennaNo,
							value);

					break;

				/* DigitalIO.Relay.No3 */
				case Prefix.Reader + DigitalIO.Relay.No3.IdleMode:
					configuration.put(DigitalIO.Relay.No3.IdleMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Relay.No3.IdleFlashMode:
					configuration.put(DigitalIO.Relay.No3.IdleFlashMode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + DigitalIO.Relay.No3.SettlingTime:
					configuration.put(DigitalIO.Relay.No3.SettlingTime,
							Integer.parseInt(property.getValue()));

					break;

				/* DigitalIO.Relay.No3.ReadEventActivation */
				case Prefix.Reader
						+ DigitalIO.Relay.No3.ReadEventActivation.AntennaNo:
					value = 0;
					if (property.getValue().contains("No1"))
						value = value + 1;
					if (property.getValue().contains("No2"))
						value = value + 2;
					if (property.getValue().contains("No3"))
						value = value + 4;
					if (property.getValue().contains("No4"))
						value = value + 8;
					configuration.put(
							DigitalIO.Relay.No3.ReadEventActivation.AntennaNo,
							value);

					break;

				/* Digital.Signaler.LED.Yellow */
				case Prefix.Reader
						+ DigitalIO.Signaler.LED.Yellow.InputEventActivation:
					value = 0;
					if (property.getValue().contains("I1"))
						value = value + 1;
					if (property.getValue().contains("I2"))
						value = value + 2;
					if (property.getValue().contains("I3"))
						value = value + 4;
					if (property.getValue().contains("I4"))
						value = value + 8;
					if (property.getValue().contains("I5"))
						value = value + 16;
					configuration.put(
							DigitalIO.Signaler.LED.Yellow.InputEventActivation,
							value);

					break;
				case Prefix.Reader
						+ DigitalIO.Signaler.LED.Yellow.OutputEventActivation:
					value = 0;
					if (property.getValue().contains("O1"))
						value = value + 1;
					if (property.getValue().contains("O2"))
						value = value + 2;
					if (property.getValue().contains("R1"))
						value = value + 4;
					if (property.getValue().contains("R2"))
						value = value + 8;
					if (property.getValue().contains("R3"))
						value = value + 16;
					configuration
							.put(DigitalIO.Signaler.LED.Yellow.OutputEventActivation,
									value);

					break;

				/* Clock */

				/* Clock.NetworkTimeProtocol */

				case Prefix.Reader + Clock.NetworkTimeProtocol.Mode:
					configuration.put(Clock.NetworkTimeProtocol.Mode,
							Integer.parseInt(property.getValue()));

					break;
				case Prefix.Reader + Clock.NetworkTimeProtocol.TriggerTime:
					configuration.put(Clock.NetworkTimeProtocol.TriggerTime,
							Integer.parseInt(property.getValue()));

					break;
				/* Clock.NetworkTimeProtocol.IPv4 */
				case Prefix.Reader + Clock.NetworkTimeProtocol.IPv4.IPAddress:
					configuration.put(Clock.NetworkTimeProtocol.IPv4.IPAddress,
							InetAddress.getByName(property.getValue())
									.getAddress());

					break;

				/* SystemTools */

				/* SystemTools.Linux */
				case Prefix.Reader + SystemTools.Linux.Telnet:
					configuration.put(SystemTools.Linux.Telnet,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + SystemTools.Linux.ssh:
					configuration.put(SystemTools.Linux.ssh,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + SystemTools.Linux.WebServer:
					configuration.put(SystemTools.Linux.WebServer,
							Byte.parseByte(property.getValue()));

					break;
				case Prefix.Reader + SystemTools.Linux.FTPServer:
					configuration.put(SystemTools.Linux.FTPServer,
							Byte.parseByte(property.getValue()));

					break;

				default:
					if (property.getKey().startsWith(Prefix.Reader)) {
						throw new ValidationException(
								"Unkown reader property '" + property
										+ "' for RF-R500 reader!");
					}
					break;
				}
			} catch (ValidationException e) {
				throw e;
			} catch (Exception e) {
				throw new ValidationException(property.getKey() + ": "
						+ e.getMessage());
			}
		}
		return configuration;
	}

}
