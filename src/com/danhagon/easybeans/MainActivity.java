package com.danhagon.easybeans;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import org.json.JSONException;

import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;

public class MainActivity extends Activity {
	
	private TextView tv1 ;
	private TextView statusTextView ;
	private NfcAdapter mNfcAdapter;
	
	private PendingIntent pendingIntent ;
	private IntentFilter[] intentFiltersArray ;
	private String[][] techListsArray ;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tv1 = (TextView) findViewById(R.id.text1);
        statusTextView = (TextView) findViewById(R.id.status_text) ;
    
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (!mNfcAdapter.isEnabled()) { // FIXME: not checking to see if this device actually has NFC
			statusTextView.setText(R.string.nfc_enabled_status);
		} else {
			statusTextView.setText(R.string.nfc_disabled_status);
		}
		
        handleNFC(getIntent()) ;
		
		// PendingIntent required for Foreground Dispatch
		pendingIntent = PendingIntent.getActivity(
			    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		ndef.addCategory(Intent.CATEGORY_DEFAULT);
		try {
			ndef.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("Problem with MIME type.", e);
		}
		intentFiltersArray = new IntentFilter[] {ndef, };
		
		techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
		
		Intent intent = new Intent(this, PayPalService.class);

	     // live: don't put any environment extra
	     // sandbox: use PaymentActivity.ENVIRONMENT_SANDBOX
	     intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, PaymentActivity.ENVIRONMENT_NO_NETWORK);

	     intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, "AXCRQxAGj3cFR3u8rIlJWS2I5ST7vv0HFVwBX3wjH4vMuEFMQ2NKRiqFe-JC");

	     startService(intent);		
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }    
    
	@Override
	protected void onResume() {
		super.onResume();

		// Setup Foreground Dispatch
		final Intent intent = new Intent(this.getApplicationContext(), this.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray) ;
	}


	@Override
	protected void onPause() {
		super.onPause();		
		// Stop Foreground Dispatch
		mNfcAdapter.disableForegroundDispatch(this) ;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		handleNFC(intent) ;
	}

	
	@Override
	 protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	     if (resultCode == Activity.RESULT_OK) {
	         PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
	         if (confirm != null) {
	             try {
	                 Log.i("paymentExample", confirm.toJSONObject().toString(4));

	                 // TODO: send 'confirm' to your server for verification.
	                 // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
	                 // for more details.

	             } catch (JSONException e) {
	                 Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
	             }
	         }
	     }
	     else if (resultCode == Activity.RESULT_CANCELED) {
	         Log.i("paymentExample", "The user canceled.");
	     }
	     else if (resultCode == PaymentActivity.RESULT_PAYMENT_INVALID) {
	         Log.i("paymentExample", "An invalid payment was submitted. Please see the docs.");
	     }
	 }	

	/**
	 * Receive and handle NFC Intent.
	 */
	protected void handleNFC(Intent intent) {
        // Handle NFC Intent
    	String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
        	
			// FIXME: We ignore the MIME Type
        	
			final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			
			new Thread(new Runnable() {
		        public void run() {
		        	tag.toString() ;
		        	Ndef ndef = Ndef.get(tag);
		        	
		        	NdefMessage ndefMessage = ndef.getCachedNdefMessage();
					NdefRecord[] records = ndefMessage.getRecords();
					byte[] payload = records[0].getPayload() ;
					int lenLangCode = payload[0] & 0x3F ;
					String uriTemp = getString(R.string.no_tag);
					try {
						uriTemp = new String (payload, lenLangCode + 1, payload.length - (lenLangCode + 1),
												((payload[0] & 0x80)==0) ? "UTF-8" : "UTF-16");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					final String uri = "http://nfc" + uriTemp ; // Hack!
					
					// Notify user of choice
		            MainActivity.this.runOnUiThread(new Runnable() {
		            	public void run() {
		                	tv1.setText(uri) ;
		                	if(uri.equals("http://nfc.danhagon.com/red")) {
		                		tv1.setBackgroundColor(Color.RED) ;
		                	}
		                	if(uri.equals("http://nfc.danhagon.com/yellow")) {
		                		tv1.setBackgroundColor(Color.YELLOW) ;
		                	}
		                	if(uri.equals("http://nfc.danhagon.com/blue")) {
		                		tv1.setBackgroundColor(Color.BLUE) ;
		                	}
		                	if(uri.equals("http://nfc.danhagon.com/green")) {
		                		tv1.setBackgroundColor(Color.GREEN) ;
		                	}
		                	if(uri.equals("http://nfc.danhagon.com/white")) {
		                		tv1.setBackgroundColor(Color.WHITE) ;
		                	}
		                	if(uri.equals("http://nfc.danhagon.com/black")) {
		                		tv1.setBackgroundColor(Color.BLACK) ;
		                	}
		            	}
		            }) ;
		            
		            // Authorise payment
		            handlePayment(uri) ;
	            }
		    }).start();		
		}
	}
	
	/**
	 * Handles payment for the capsule using PayPal
	 */
	public void handlePayment(String flavour) {
		 // A coffee capsule costs 2 pounds
	     PayPalPayment payment = new PayPalPayment(new BigDecimal("0.20"), "USD", flavour);

	     Intent intent = new Intent(this, PaymentActivity.class);

	     // comment this line out for live or set to PaymentActivity.ENVIRONMENT_SANDBOX for sandbox
	     intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, PaymentActivity.ENVIRONMENT_NO_NETWORK);

	     // it's important to repeat the clientId here so that the SDK has it if Android restarts your
	     // app midway through the payment UI flow.
	     intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, "AXCRQxAGj3cFR3u8rIlJWS2I5ST7vv0HFVwBX3wjH4vMuEFMQ2NKRiqFe-JC");

	     // Provide a payerId that uniquely identifies a user within the scope of your system,
	     // such as an email address or user ID.
	     intent.putExtra(PaymentActivity.EXTRA_PAYER_ID, "<axiomsofchoice@nfc.danhagon.me.uk>");

	     intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL, "axiomsofchoice-facilitator@gmail.com");
	     intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

	     startActivityForResult(intent, 0);
	 }
}