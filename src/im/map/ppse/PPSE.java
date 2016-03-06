/**
 * 
 */
package im.map.ppse;

import org.globalplatform.GPSystem;
import org.globalplatform.SecureChannel;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

/**
 * @author APDU
 *
 */
public class PPSE extends Applet {
	private final static byte INS_INIT_UPDATE        = (byte) 0x50;
    private final static byte INS_EXT_AUTH           = (byte) 0x82;
    private final static byte INS_SELECT      		 = (byte) 0xA4;
    private final static byte INS_STORE_DATA         = (byte) 0xE2;
	private SecureChannel secureChannel;
	private boolean isEnd;
	private byte[] DGI9103;
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new im.map.ppse.PPSE().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}
	public PPSE(){
		isEnd = false;
	}
	
	public boolean select() { 
		secureChannel = GPSystem.getSecureChannel();  
		return true;    
   }   

    public void deselect() {
    	secureChannel.resetSecurity();
    }
    
   
	public void process(APDU apdu) {
		byte[] buf = apdu.getBuffer();
		
		if (selectingApplet()) {
			{           
				return;
			}
			
		}

		
		switch (buf[ISO7816.OFFSET_INS]) {
		case (byte) INS_SELECT:
			break;
		case (byte) INS_STORE_DATA:
			break;
		case (byte) INS_INIT_UPDATE:
		case (byte) INS_EXT_AUTH:
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
		
	}
	
}