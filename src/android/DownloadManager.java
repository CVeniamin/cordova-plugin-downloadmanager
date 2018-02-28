package downloadmanager;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * This class echoes a string called from JavaScript.
 */
public class DownloadManager extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("download")) {
            final JSONObject params = args.getJSONObject(0);
            this.startDownload(params, callbackContext);
            return true;
        }
        return false;
    }

    private void startDownload(final JSONObject options, CallbackContext callbackContext) {
        if (options != null && options.length() > 0) {
            
	    //default filename
            DateFormat df = new SimpleDateFormat("yyMMddHHmmss.SSS");
            String filename = df.format(Calendar.getInstance().getTime());
	    String uri;
			
            if (options.has("uri")) {
                uri = options.getString("uri");
                filename = uri.substring(uri.lastIndexOf("/")+1, uri.length());
            }
           
            try {
                filename = URLDecoder.decode(filename,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                callbackContext.error("Error in converting filename");
            }
			
            android.app.DownloadManager downloadManager = (android.app.DownloadManager) cordova.getActivity().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri Download_Uri = Uri.parse(uri);
            
            android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(Download_Uri);
            
            //Restrict the types of networks over which this download may proceed.
            request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI | android.app.DownloadManager.Request.NETWORK_MOBILE);
			
            //Set whether this download may proceed over a roaming connection.
            request.setAllowedOverRoaming(false);
			
 	    //Set the title of this download, to be displayed in notifications (if enabled).
            if(options.has("title")){
                request.setTitle(options.getString("title"));
            } else {
		request.setTitle(filename);
	    }
			
	    //Set a description of this download, to be displayed in notifications (if enabled)
	    if(options.has("description")){
                request.setDescription(options.getString("description"));   
	    } else {
		request.setDescription("Downloading file");   
	    }

            if(options.has("setPublicDirectory") && options.has("albumName") && options.getBoolean("setPublicDirectory") && options.getString("albumName")) {
		//Set the local destination for the downloaded file to a path within the application's external files directory            
		request.setDestinationInExternalFilesDir(cordova.getActivity().getApplicationContext(), Environment.DIRECTORY_PICTURES, options.getString("albumName"));
	    } else {
		//Set the local destination for the downloaded file to a path within the application's external files directory
		request.setDestinationInExternalFilesDir(cordova.getActivity().getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, filename);   
	    }

            //Set visiblity after download is complete
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            long downloadReference = downloadManager.enqueue(request);
            callbackContext.success(filename);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
