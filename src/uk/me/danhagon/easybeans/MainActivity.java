package uk.me.danhagon.easybeans;

import java.io.UnsupportedEncodingException;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
		// Stop Foreground Dispatch
		mNfcAdapter.disableForegroundDispatch(this) ;
		super.onPause();		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		handleNFC(intent) ;
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
					final String uri = uriTemp ;
		            MainActivity.this.runOnUiThread(new Runnable() {
		            	public void run() {
		                	tv1.setText(uri) ;
		            	}
		            }) ;
	            }
		    }).start();		
		}
	}
    
}