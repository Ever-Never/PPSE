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
	private byte[] DGI9102;
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
				short length = 0;
				//FCI
				buf[0] = 0x6F;
				buf[2] = (byte)0x84;
				buf[3] = JCSystem.getAID().getBytes(buf,(short)4);
				length = (byte)(buf[3]+4);
				if(isEnd)
				{
					Util.arrayCopyNonAtomic(DGI9102,(byte)0,buf,(short)length,(short)DGI9102.length);
					length=(short)(length+DGI9102.length);
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
			if( buf[ISO7816.OFFSET_P1]!=4 || buf[ISO7816.OFFSET_P2]!=0 )
				ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
			else if ( buf[ISO7816.OFFSET_LC]<5 || buf[ISO7816.OFFSET_LC]>16 ) 
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			else				
				ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
			break;
		case (byte) INS_STORE_DATA:
			if( (buf[ISO7816.OFFSET_P1]&0xFF)!=0x80 || buf[ISO7816.OFFSET_P2]!=0 )
				ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
		
			if (isEnd || secureChannel.getSecurityLevel()==SecureChannel.NO_SECURITY_LEVEL) 
				ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
			apdu.setIncomingAndReceive();
			secureChannel.unwrap(buf, (short)0, (short)(buf[ISO7816.OFFSET_LC]+5));
			if (Util.getShort(buf,ISO7816.OFFSET_CDATA) != (short)0x9102) 
				ISOException.throwIt(ISO7816.SW_WRONG_DATA);
			else {
				DGI9102 = new byte[buf[ISO7816.OFFSET_LC]];
				Util.arrayCopyNonAtomic(buf, (short)8, DGI9102, (short)0, buf[ISO7816.OFFSET_LC]);
				isEnd = true;
			}
			break;
		case (byte) INS_INIT_UPDATE:
		case (byte) INS_EXT_AUTH:
			if (isEnd) 
				ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		
			apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, secureChannel.processSecurity(apdu));;
			break;
			
		/*	
		case (byte) 0xB0: //DEBUG COMMANDS
			Util.arrayCopyNonAtomic(DGI9102, (short)0, buf, (short)0, (short)DGI9102.length);
			apdu.setOutgoingAndSend((short)0, (short)DGI9102.length);
			break;
		*/
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
		
	}
	
}