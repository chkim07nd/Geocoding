package com.example.geoapp;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity {

	GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        final Button button = (Button) findViewById(R.id.find);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
					try {
						RadioButton loc = (RadioButton) findViewById(R.id.loc);
						RadioButton coor = (RadioButton) findViewById(R.id.coor);
						if(loc.isChecked()){
							findLocation(v);
						}
						else if(coor.isChecked()){
							findCoordinates(v);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            }
        });
        
    	MapFragment mapFrag =
				(MapFragment) getFragmentManager().findFragmentById(R.id.map);
		googleMap = mapFrag.getMap();
    }

    public void findLocation(View v) throws IOException{
    	EditText editText = (EditText) findViewById(R.id.location);
    	String locationName = editText.getText().toString();
    	locationName = locationName.replace(' ', '+');
    	double[] latLng = new double[2];
    	
    	String result = getResponseByAddress(locationName);
       	latLng = returnLatlng(result);
       	String flag = returnCountry(result);
       	
       	new DownloadImageTask((ImageView) findViewById(R.id.flag))
        .execute("http://geotree.geonames.org/img/flags18/" + flag + ".png");

		LatLng ll = new LatLng(latLng[0], latLng[1]);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 12);
		googleMap.moveCamera(update);
    	
    }
    
    public void findCoordinates(View v){
    	EditText editText = (EditText) findViewById(R.id.location);
    	String coordinates = editText.getText().toString();
    	double[] latLng = new double[2];
    	String[] latlng = coordinates.split(",");
    	if(latlng.length != 2){
    		Toast.makeText(this, "Please enter correct coordinates.", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	else{
    		try{
    			Double.valueOf(latlng[0]);
    			Double.valueOf(latlng[1]);
    			
    	    	String result = getResponseByCoordinates(latlng[0],latlng[1]);
    	       	latLng = returnLatlng(result);
    	       	String flag = returnCountry(result);
    			
    	       	new DownloadImageTask((ImageView) findViewById(R.id.flag))
    	        .execute("http://geotree.geonames.org/img/flags18/" + flag + ".png");
    	       	
    			LatLng ll = new LatLng(latLng[0], latLng[1]);
    			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 12);
    			googleMap.moveCamera(update);
    		}
    		catch(NumberFormatException e){
    			Toast.makeText(this, "Please enter correct coordinates.", Toast.LENGTH_SHORT).show();
    		}
    		catch(NullPointerException e){
    			Toast.makeText(this, "Please enter correct coordinates.", Toast.LENGTH_SHORT).show();	
    		}
    	}
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public static String getResponseByAddress(String youraddress) {
        String uri = "https://maps.google.com/maps/api/geocode/json?address=" +
                      youraddress + "&sensor=true";
        HttpGet httpGet = new HttpGet(uri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    
    public static String getResponseByCoordinates(String lat, String lng) {
        String uri = "https://maps.google.com/maps/api/geocode/json?latlng=" +
                      lat + "," + lng + "&sensor=true";
        HttpGet httpGet = new HttpGet(uri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    
    public static double[] returnLatlng(String string){
        double[] LatLng = new double[2];
    	JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(string);

            double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lng");

            double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lat");

            Log.d("latitude", "" + lat);
            LatLng[0] = lat;
            Log.d("longitude", "" + lng);
            LatLng[1]= lng;
        } catch (JSONException e) {
            e.printStackTrace();
        }
		return LatLng;
    }
    
    public static String returnCountry(String string){
    	String country = new String();
    	JSONObject jsonObject = new JSONObject();
    	try {
            jsonObject = new JSONObject(string);
            JSONArray results = jsonObject.getJSONArray("results");
            int i = 0;
            JSONObject r = results.getJSONObject(0);
            JSONArray addressComponentsArray = r.getJSONArray("address_components");
            do{
                JSONObject addressComponents = addressComponentsArray.getJSONObject(i);
                JSONArray typesArray = addressComponents.getJSONArray("types");
                String types = typesArray.getString(0);
                
                if(types.equalsIgnoreCase("country")){
                	country = addressComponents.getString("short_name");
                	break;
                }
                i++;
            }while(i<addressComponentsArray.length());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    	return country;
    }
    
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
