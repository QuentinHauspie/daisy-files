import daisy.*;

import java.util.Random;

/**
 * The DaisyUserThread provides a thread that random chooses an operation
 * (read, write, create, or delete) and performs it.  The number of operations the
 * thread will perform before quitting is configurable through the constructor.
 *
 * @author Todd Wallentine tcw AT cis ksu edu
 * @version $Revision: 1.1 $ - $Date: 2004/12/15 20:47:45 $
 * 
 * Modified by Charles Pecheur, UC Louvain
 */
public class DaisyUserThread extends Thread {

	private int iterations;
	
	public static final int MAX_OPERATIONS = 6;
	public static final int READ_OPERATION = 0;
	public static final int WRITE_OPERATION = 1;
	public static final int CREATE_OPERATION = 2;
	public static final int DELETE_OPERATION = 3;
	public static final int SET_ATTR_OPERATION = 4;
	public static final int GET_ATTR_OPERATION = 5;
	public static final int RANDOM_OPERATION = -1;
	
	public static final int MAX_FILE_SIZE = 9;
	
	private Random random;
	
	private byte[][] filenames;
	
	private FileHandle root;
	
	private int opCode;
	
	public DaisyUserThread(int iterations, byte[][] filenames, FileHandle root) {
		this(RANDOM_OPERATION, iterations, filenames, root);
	}
	
	public DaisyUserThread(int opCode, int iterations, byte[][] filenames, FileHandle root) {
		this.iterations = iterations;
		this.filenames = filenames;
		this.root = root;
		if((opCode >= 0) && (opCode < MAX_OPERATIONS)) {
			this.opCode = opCode;
		} else {
			this.opCode = RANDOM_OPERATION;
		}
		random = new Random();
	}
	
	public void run() {
		FileHandle fh = new FileHandle();
		Attribute a = null;
		int status;
		int offset;
		byte[] contents;
		
		for(int i = 0; i < iterations; i++) {
			
			int operation;
			if(opCode == RANDOM_OPERATION) {
				operation = random.nextInt(MAX_OPERATIONS);
			} else {
				operation = opCode;
			}
			int fileID = random.nextInt(filenames.length);
			int lookupStatus = DaisyDir.lookup(root, filenames[fileID], fh);
			assert (lookupStatus == Daisy.DAISY_ERR_OK) || (lookupStatus == Daisy.DAISY_ERR_NOENT);
			
			System.out.printf("Doing op %d on file %d, inode %d, status=%d\n", 
					operation, fileID, fh.inodenum, lookupStatus);
			switch(operation) {
				case READ_OPERATION:
					System.out.println("Reading...");
					int size = 1;
					contents = new byte[size];
					offset = 0;
					status = DaisyDir.read(fh, offset, size, contents);
					if(lookupStatus != Daisy.DAISY_ERR_OK) {
						assert status == Daisy.DAISY_ERR_BADHANDLE;
				    } else {
					    assert status == Daisy.DAISY_ERR_OK;
					}
					break;
					
				case WRITE_OPERATION:
					System.out.println("Writing...");
					contents = DaisyTest.stringToBytes("randomData");
					offset = 0;
					status = DaisyDir.write(fh, offset, contents.length, contents);
					if(lookupStatus == Daisy.DAISY_ERR_NOENT) {
						assert status == Daisy.DAISY_ERR_BADHANDLE;
					} else {
						assert status == Daisy.DAISY_ERR_OK;
					}
					break;
					
				case CREATE_OPERATION:
					System.out.println("Creating...");
					status = DaisyDir.creat(root, filenames[fileID], fh);
					if(lookupStatus == Daisy.DAISY_ERR_NOENT) {
						assert status == Daisy.DAISY_ERR_OK;
					} else {
						assert status == Daisy.DAISY_ERR_EXIST;
					}
					break;
					
				case DELETE_OPERATION:
					System.out.println("Deleting...");
					status = DaisyDir.unlink(root, filenames[fileID]);
					if(lookupStatus == Daisy.DAISY_ERR_NOENT) {
						assert status == Daisy.DAISY_ERR_NOENT;
					} else {
						assert status == Daisy.DAISY_ERR_OK;
					}
					break;
					
				case GET_ATTR_OPERATION:
					System.out.println("Getting attribute...");
					a = new Attribute();
					status = DaisyDir.get_attr(fh, a);
					if(lookupStatus == Daisy.DAISY_ERR_NOENT) {
						assert status == Daisy.DAISY_ERR_NOENT;
					} else {
						assert status == Daisy.DAISY_ERR_OK;
					}
					break;
					
				case SET_ATTR_OPERATION:
					System.out.println("Setting attribute...");
					a = new Attribute();
					a.size = 0;
					status = DaisyDir.set_attr(fh, a);
					if(lookupStatus == Daisy.DAISY_ERR_NOENT) {
						assert status == Daisy.DAISY_ERR_NOENT;
					} else {
						assert status == Daisy.DAISY_ERR_OK;
					}
					break;
					
				default:
					assert false;
					break;
			}
		}
	}
}
