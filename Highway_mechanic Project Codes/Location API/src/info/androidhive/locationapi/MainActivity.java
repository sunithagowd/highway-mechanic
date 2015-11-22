package info.androidhive.locationapi;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.os.Message;
import android.os.Handler;
//import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;



public class MainActivity extends Activity  implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
   // public class ConnectionResult;
	private Location mLastLocation;

	// Google client to interact with Google API
	private GoogleApiClient mGoogleApiClient;

	// boolean flag to toggle periodic location updates
	private boolean mRequestingLocationUpdates = false;

	private LocationRequest mLocationRequest;

	// Location updates intervals in sec
	private static int UPDATE_INTERVAL = 10000; // 10 sec
	private static int FATEST_INTERVAL = 5000; // 5 sec
	private static int DISPLACEMENT = 10; // 10 meters

	// UI elements
	private TextView lblLocation;
	private Button btnShowLocation, btnStartLocationUpdates,NextButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_googlemap);

		lblLocation = (TextView) findViewById(R.id.lblLocation);
		btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
		btnStartLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);
		NextButton = (Button) findViewById(R.id.NextButton);
		
		// First we need to check availability of play services
		if (checkPlayServices()) {

			// Building the GoogleApi client
			buildGoogleApiClient();

			createLocationRequest();
		}
		// Show location button click listener
		btnShowLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				displayLocation();
			}
		});
		// Toggling the periodic location updates
		btnStartLocationUpdates.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				togglePeriodicLocationUpdates();
			}
		});
		NextButton.setOnClickListener(new View.OnClickListener(){
			//@Override
			public void onClick(View v){
					GoToNextProblemsPage();
				}
			});
		}
		
	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkPlayServices();

		// Resuming the periodic location updates
		if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	/**
	 * Method to display the location on UI
	 * */
	private void displayLocation() {

		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (mLastLocation != null) {
			double latitude = mLastLocation.getLatitude();
			double longitude = mLastLocation.getLongitude();
			getAddress(latitude,longitude,new GeocoderHandler());
			lblLocation.setText(latitude + ", " + longitude);

		} else {

			lblLocation
					.setText("(Couldn't get the location. Make sure location is enabled on the device)");
		}
	}

	/**
	 * Method to toggle periodic location updates
	 * */
	 private class GeocoderHandler extends Handler {
	        @Override
	        public void handleMessage(Message message) {
	            String locationAddress;
	            switch (message.what) {
	                case 1:
	                    Bundle bundle = message.getData();
	                    locationAddress = bundle.getString("address");
	                    break;
	                default:
	                    locationAddress = null;
	            }
	            lblLocation.setText(locationAddress);
	        }
	    }
	private void togglePeriodicLocationUpdates() {
		if (!mRequestingLocationUpdates) {
			// Changing the button text
			btnStartLocationUpdates
					.setText(getString(R.string.btn_stop_location_updates));

			mRequestingLocationUpdates = true;

			// Starting the location updates
			startLocationUpdates();

			Log.d(TAG, "Periodic location updates started!");

		} else {
			// Changing the button text
			btnStartLocationUpdates
					.setText(getString(R.string.btn_start_location_updates));

			mRequestingLocationUpdates = false;

			// Stopping the location updates
			stopLocationUpdates();

			Log.d(TAG, "Periodic location updates stopped!");
		}
	}
	private void GoToNextProblemsPage(){
		Intent intent = new Intent(MainActivity.this ,ListViewCheckboxesActivity.class);
		startActivity(intent);
		finish();
		
	}
	private void getAddress(double lati,double longi, Handler handler){
		Geocoder geocoder = new Geocoder(this,Locale.getDefault());
		 String result = null;
         try {
             List<Address> addressList = geocoder.getFromLocation(
                     lati, longi, 1);
             if (addressList != null && addressList.size() > 0) {
                 Address address = addressList.get(0);
                 StringBuilder sb = new StringBuilder();
                 for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                     sb.append(address.getAddressLine(i)).append("\n");
                 }
                 sb.append(address.getLocality()).append("\n");
                 sb.append(address.getPostalCode()).append("\n");
                 sb.append(address.getCountryName());
                 result = sb.toString();
             }
         } catch (IOException e) {
             Log.e(TAG, "Unable connect to Geocoder", e);
         } finally {
             Message message = Message.obtain();
             message.setTarget(handler);
             if (result != null) {
                 message.what = 1;
                 Bundle bundle = new Bundle();
                 result = "Latitude: " + lati + " Longitude: " + longi +
                         "\n\nAddress:\n" + result;
                 bundle.putString("address", result);
                 message.setData(bundle);
             } else {
                 message.what = 1;
                 Bundle bundle = new Bundle();
                 result = "Latitude: " + lati + " Longitude: " + longi +
                         "\n Unable to get address for this lat-long.";
                 bundle.putString("address", result);
                 message.setData(bundle);
             }
             message.sendToTarget();
         }
     }
		

/** private void GoToNextProblemsPage(){
		Intent intent = new Intent("info.androidhive.locationapi.ListViewCheckboxesActivity");
		startActivity(intent);
		finish();
		
	}**/

	/**
	 * Creating google api client object
	 * */
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	/**
	 * Creating location request object
	 * */
	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FATEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
	}

	/**
	 * Method to verify google play services on the device
	 * */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"This device is not supported.", Toast.LENGTH_LONG)
						.show();
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Starting the location updates
	 * */
	protected void startLocationUpdates() {

		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);

	}

	/**
	 * Stopping location updates
	 */
	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
	}

	/**
	 * Google api callback methods
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle arg0) {

		// Once connected with google api, get the location
		displayLocation();

		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		// Assign the new location
		mLastLocation = location;

		Toast.makeText(getApplicationContext(), "Location changed!",
				Toast.LENGTH_SHORT).show();

		// Displaying the new location on UI
		displayLocation();
	}
	
}
