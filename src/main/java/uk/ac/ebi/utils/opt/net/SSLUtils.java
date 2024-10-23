package uk.ac.ebi.utils.opt.net;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;

/**
 * SSL Utilities.
 * 
 * At the moment contains methods to disable SSL certificate verification in HTTP connections.
 * <b>WARNING</b>: doing this is <b>UNSAFE</b>, you should fix the bad certificate on server side instead.  
 * 
 * Courtesy of <a href =
 * "http://stackoverflow.com/questions/6047996/ignore-self-signed-ssl-cert-using-jersey-client">
 * this</a>.
 *
 * @author brandizi
 *         <dl>
 *         <dt>Date:</dt>
 *         <dd>29 Nov 2016</dd>
 *         </dl>
 *
 */
public final class SSLUtils
{
	/**
	 * Trusts all certificates.
	 *
	 * @author brandizi
	 * <dl><dt>Date:</dt><dd>19 Jan 2017</dd></dl>
	 *
	 */
	public static class FakeX509TrustManager implements X509TrustManager 
	{
		public X509Certificate[] getAcceptedIssuers () {
			return new X509Certificate [ 0 ];
		}
		public void checkClientTrusted ( X509Certificate[] certs, String authType ) {/**/}
		public void checkServerTrusted ( X509Certificate[] certs, String authType )	{/**/}
	}
	
	
	/**
	 * Set the default host name Verifier to an instance of a fake class that trust all hostnames.
	 */
	public static void trustAllHostnames ()
	{
		HttpsURLConnection.setDefaultHostnameVerifier ( new HostnameVerifier() 
		{
			public boolean verify ( String hostname, SSLSession session ) {
				return true;
			}
		});
	}

	/**
	 * Set the default X509 Trust Manager to an instance of a fake class that trust all certificates, even the
	 * self-signed ones.
	 */
	public static void trustAllHttpsCertificates ()
	{
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new FakeX509TrustManager () };

		// Install the all-trusting trust manager
		SSLContext sc;
		try
		{
			sc = SSLContext.getInstance ( "SSL" );
			sc.init ( null, trustAllCerts, new SecureRandom () );
			HttpsURLConnection.setDefaultSSLSocketFactory ( sc.getSocketFactory () );
		}
		catch ( NoSuchAlgorithmException | KeyManagementException ex )
		{
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	}
	
	/**
	 * Gets an {@link HttpClient} that doesn't do any SSL certificate verification.
	 * If user is null, returns a client that doesn't deal with authentication, else sets up a client
	 * that does basic {@link BasicCredentialsProvider HTTP Auth}. 
	 */
	public static HttpClient noCertClient ( String user, String pwd )
	{
		try
		{
			BasicCredentialsProvider credsProvider = null;
			if ( user != null )
			{
				credsProvider = new BasicCredentialsProvider ();
				Credentials credentials = new UsernamePasswordCredentials ( user, pwd.toCharArray () );
				credsProvider.setCredentials ( new AuthScope ( null, -1 ), credentials );
			}
			
			SSLContext sslcontext = 
				SSLContexts
				.custom ()
				.loadTrustMaterial ( 
					null, 
					new TrustStrategy ()
					{
						public boolean isTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
							return true;
						}
					})
				.build();
				
			
			var tlsStrategy = new DefaultClientTlsStrategy ( sslcontext );
			
			var connMgr = PoolingHttpClientConnectionManagerBuilder.create ()
			.setTlsSocketStrategy ( tlsStrategy )
			.build ();
			
			HttpClientBuilder builder = HttpClients
				.custom()
				.setConnectionManager ( connMgr );
			
			if ( credsProvider != null ) builder.setDefaultCredentialsProvider ( credsProvider );
			
		  return builder.build();
		}
		catch ( KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex )
		{
			throw new RuntimeException ( 
				"Internal error while setting up no-cert HTTP connection: " + ex.getMessage (),
				ex 
			);
		}			
	}
}
