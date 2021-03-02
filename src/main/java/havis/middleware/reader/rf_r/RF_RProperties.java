package havis.middleware.reader.rf_r;

import havis.middleware.ale.reader.Prefix;

/**
 * Class that represents a set of properties for all RF_R reader connector
 * types.
 */
public class RF_RProperties {
	/**
	 * Static class that hold all property names for RF_R reader connector.
	 */
	public static class PropertyName {
		/**
		 * Describe the antenna ids that are used for inventories.
		 */
		public final static String InventoryAntennas = Prefix.Connector + "Inventory.Antennas";
		/**
		 * Describe the inventory attempty value.
		 */
		public final static String InventoryAttempts = Prefix.Connector + "InventoryAttempts";
		/**
		 * Describe the tags in field value.
		 */
		public final static String TagsInField = Prefix.Connector + "TagsInField";
		/**
		 * Describe the max block size in bytes
		 */
		public final static String BlockSize = Prefix.Connector + "BlockSize";
		/**
		 * Describe the max block count
		 */
		public final static String BlockCount = Prefix.Connector + "BlockCount";
		/**
		 * Describe the amount of inventories without reader error before the
		 * error is logged again.
		 */
		public final static String ReaderErrorCount = Prefix.Connector + "ReaderErrorCount";
		/**
		 * Describe the amount of inventories without iso error before the error
		 * is logged again.
		 */
		public final static String IsoErrorCount = Prefix.Connector + "IsoErrorCount";
		/**
		 * Describe the amount of inventories without antenna error before the
		 * error is logged again.
		 */
		public final static String AntennaErrorCount = Prefix.Connector + "AntennaErrorCount";

		/**
		 * Describe the maximum number of blocks that will be read during an
		 * inventory at once.
		 */
		public final static String MaxNoOfDataBlocksRead = Prefix.Connector + "MaxNoOfDataBlocksRead";

		/**
		 * Describe the delay in milliseconds between poll calls on inputs
		 */
		public final static String InputDelay = Prefix.Connector + "Input.Delay";

	}

	private Byte inventoryAntennas = null;
	private short inventoryAttempts = 3;
	private short tagsInField = 128;
	private short blockCount = 256;
	private short blockSize = 2;
	private short readerErrorCount = 3;
	private short isoErrorCount = 3;
	private short antennaErrorCount = 3;
	private short maxNoOfDataBlocksRead = 128;
	private short inputDelay = 100;

	/**
	 * Gets the antenna ids that are used for an inventory.
	 * 
	 * @return Antennas
	 */
	public Byte getInventoryAntennas() {
		return inventoryAntennas;
	}

	/**
	 * Sets the antenna ids that are used for an inventory.
	 * 
	 * @param inventoryAntennas
	 */
	public void setInventoryAntennas(Byte inventoryAntennas) {
		this.inventoryAntennas = inventoryAntennas;
	}

	/**
	 * Gets the number of inventory attempts to find the tag to execute
	 * operation on. The default value is 3.
	 * 
	 * @return Attempts
	 */
	public short getInventoryAttempts() {
		return inventoryAttempts;
	}

	/**
	 * Sets the number of inventory attempts to find the tag to execute
	 * operation on. The default value is 3.
	 * 
	 * @param inventoryAttempts
	 */
	public void setInventoryAttempts(short inventoryAttempts) {
		this.inventoryAttempts = inventoryAttempts;
	}

	/**
	 * Gets the maximum quantity of of tags in the reader field at the same
	 * time. The default value is 128.
	 * 
	 * @return Max quantity of tags in field
	 */
	public short getTagsInField() {
		return tagsInField;
	}

	/**
	 * Sets the maximum quantity of of tags in the reader field at the same
	 * time. The default value is 128.
	 * 
	 * @param tagsInField
	 */
	public void setTagsInField(short tagsInField) {
		this.tagsInField = tagsInField;
	}

	/**
	 * Gets the maximum block count of tags in the reader field. The default
	 * value is 256.
	 * 
	 * @return Max block count
	 */
	public short getBlockCount() {
		return blockCount;
	}

	/**
	 * Sets the maximum block count of tags in the reader field. The default
	 * value is 256.
	 * 
	 * @param blockCount
	 */
	public void setBlockCount(short blockCount) {
		this.blockCount = blockCount;
	}

	/**
	 * Gets the block size of tags in the reader field. The default value is 2.
	 * 
	 * @return Block size
	 */
	public short getBlockSize() {
		return blockSize;
	}

	/**
	 * Sets the block size of tags in the reader field. The default value is 2.
	 * 
	 * @param blockSize
	 */
	public void setBlockSize(short blockSize) {
		this.blockSize = blockSize;
	}

	/**
	 * Gets the amount of inventories without reader error before the error is
	 * logged again. The default value is 3.
	 * 
	 * @return the readerErrorCount
	 */
	public short getReaderErrorCount() {
		return readerErrorCount;
	}

	/**
	 * Sets the amount of inventories without reader error before the error is
	 * logged again. The default value is 3.
	 * 
	 * @param readerErrorCount
	 */
	public void setReaderErrorCount(short readerErrorCount) {
		this.readerErrorCount = readerErrorCount;
	}

	/**
	 * Gets the amount of inventories without iso error before the error is
	 * logged again. The default value is 3.
	 * 
	 * @return the isoErrorCount
	 */
	public short getIsoErrorCount() {
		return isoErrorCount;
	}

	/**
	 * Sets the amount of inventories without iso error before the error is
	 * logged again. The default value is 3.
	 * 
	 * @param isoErrorCount
	 *            the isoErrorCount to set
	 */
	public void setIsoErrorCount(short isoErrorCount) {
		this.isoErrorCount = isoErrorCount;
	}

	/**
	 * Gets the amount of inventories without antenna error before the error is
	 * logged again. The default value is 3.
	 * 
	 * @return the antennaErrorCount
	 */
	public short getAntennaErrorCount() {
		return antennaErrorCount;
	}

	/**
	 * Sets the amount of inventories without antenna error before the error is
	 * logged again. The default value is 3.
	 * 
	 * @param antennaErrorCount
	 */
	public void setAntennaErrorCount(short antennaErrorCount) {
		this.antennaErrorCount = antennaErrorCount;
	}

	/**
	 * Gets the maximum number of data blocks that will be read during an
	 * inventory at once. The default value is 128.
	 * 
	 * @return Max data blocks to be read
	 */
	public short getMaxNoOfDataBlocksRead() {
		return maxNoOfDataBlocksRead;
	}

	/**
	 * Sets the maximum number of data blocks that will be read during an
	 * inventory at once. The default value is 128.
	 * 
	 * @param maxNoOfDataBlocksRead
	 */
	public void setMaxNoOfDataBlocksRead(short maxNoOfDataBlocksRead) {
		this.maxNoOfDataBlocksRead = maxNoOfDataBlocksRead;
	}

	/**
	 * Gets the delay in milliseconds between poll calls on inputs. The default
	 * value is 100.
	 * 
	 * @return Delay between poll calls
	 */
	public short getInputDelay() {
		return inputDelay;
	}

	/**
	 * Sets the delay in milliseconds between poll calls on inputs. The default
	 * value is 100.
	 * 
	 * @param inputDelay
	 */
	public void setInputDelay(short inputDelay) {
		this.inputDelay = inputDelay;
	}

}
