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
 *  https://github.com/APDU/PPSE
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
				short length=0;
				//FCI
				buf[0]=0x6F;
				buf[2]=(byte)0x84;
				buf[3]=JCSystem.getAID().getBytes(buf,(short)4);
				length=(byte)(buf[3]+4);
				if(DGI9103 != null)
				{
					Util.arrayCopyNonAtomic(DGI9103,(byte)0,buf,(short)length,(short)DGI9103.length);
					length=(short)(length+DGI9103.length);
				}
				else
				{
					buf[length] = (byte)0xA5;
					buf[(short)(length+1)] = 0x00;
					length +=2;
				}
				buf[1]=(byte)(length-2);

				apdu.setOutgoingAndSend((short)0,(short)length);
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
			if (isEnd) 
				ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		
			apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, secureChannel.processSecurity(apdu));;
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
		
	}
	
}