package tableauSrvrWebservice.Auth;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import tableauSrvrWebservice.Auth.WebServiceClient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Here you can put your test
     */
    public void testApp()
    {
    	try {
			WebServiceClient.authenticate("http://serverurl","user","password");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (IOException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (SAXException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
			assertFalse(true);
		}
        System.out.println( "Finished!" );
        assertTrue(true);
    }
}
