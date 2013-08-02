/**
 * 
 */
package tableauSrvrWebservice.Auth;

/**
 * @author Horváth Attila
 * @created 2013.08.02
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WebServiceClient {
	/**
	 * Fetches The httpresp HttpResponse into a StringBuffer
	 * 
	 * @param httpresp
	 *            HttpResponse
	 * @return StringBuffer with contents of the HttpResponse
	 * @throws IOException
	 */
	public static StringBuffer fetchResponse(HttpResponse httpresp)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(httpresp.getEntity().getContent()));

		StringBuffer strbuffer = new StringBuffer();
		String currentline = "";
		while ((currentline = bufferedReader.readLine()) != null) {
			strbuffer.append(currentline);
		}
		return strbuffer;
	}

	/**
	 * Creates a HttpClient with authenticated connection to the Tableau Server
	 * 
	 * @param serveraddress
	 *            - URL address of the Tableau Server
	 * @param user
	 *            - Username on Tableau Server
	 * @param password
	 *            - Corresponding password to the Username
	 * @return HttpClient with authenticated connection to the Tableau Server
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static HttpClient authenticate(String serveraddress, String user,
			String password) throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException,
			NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		// Initialize apache HttpClient

		HttpClient client = new DefaultHttpClient();

		// Create Http Get request for authentication informations

		String url = serveraddress + "/auth.xml";

		HttpGet request = new HttpGet(url);

		HttpResponse response = client.execute(request);

		StringBuffer result = fetchResponse(response);

		// Parse XML FROM the result
		StringReader reader = new StringReader(result.toString());

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(reader);

		Document doc = db.parse(is);

		// Get Required data for creating the authentication request, such as
		// modulus and exponent of the RSA public key and the authencity_token
		String modulusstr = null;
		String exponentstr = null;
		String authencity_token = null;

		NodeList elements = doc.getElementsByTagName("authinfo");
		for (int i = 0; i < elements.getLength(); i++) {
			NodeList moduluses = ((Element) elements.item(i))
					.getElementsByTagName("modulus");
			for (int k = 0; k < moduluses.getLength(); k++) {
				modulusstr = moduluses.item(k).getTextContent();
			}
			NodeList exponents = ((Element) elements.item(i))
					.getElementsByTagName("exponent");
			for (int k = 0; k < exponents.getLength(); k++) {
				exponentstr = exponents.item(k).getTextContent();
			}
			NodeList authencity_tokens = ((Element) elements.item(i))
					.getElementsByTagName("authenticity_token");
			for (int k = 0; k < exponents.getLength(); k++) {
				authencity_token = authencity_tokens.item(k).getTextContent();
			}
		}

		// Parse the modulus and exponent into a BigInteger and create an RSA
		// public key from it
		BigInteger modulus = new BigInteger(modulusstr, 16);
		BigInteger exponent = new BigInteger(exponentstr, 16);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = new RSAPublicKeySpec(modulus, exponent);
		PublicKey pubkey = keyFactory.generatePublic(pub);

		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubkey);

		// Encrypt the password with the created public key
		byte[] cipherData = cipher.doFinal(password.getBytes());
		String cryptedpass = Hex.encodeHexString(cipherData);

		// Create a post request for the authentication
		HttpPost postrequest = new HttpPost(serveraddress + "/auth/login.xml");

		// Fill in parameters
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("authenticity_token", authencity_token));
		nvps.add(new BasicNameValuePair("crypted", cryptedpass));
		nvps.add(new BasicNameValuePair("username", user));

		// bind parameters to the request
		postrequest.setEntity(new UrlEncodedFormEntity(nvps));

		HttpResponse postResponse = client.execute(postrequest);

		// We clear the entity here so we don't have to shutdown the client
		fetchResponse(postResponse);

		return client;
	}
}
