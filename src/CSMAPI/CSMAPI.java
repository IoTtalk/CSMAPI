package CSMAPI;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CSMAPI {
	static private String log_tag = "CSMAPI";
	static private final String local_log_tag = "CSMAPI";
	static public String ENDPOINT = "http://openmtc.darkgerm.com:9999";
	
	static public void set_logtag (String logtag) {
		CSMAPI.log_tag = logtag;
	}
	
	static public boolean register (String d_id, JSONObject profile) {
		try {
	        String url = ENDPOINT +"/"+ d_id;
			logging("[register] "+ url);
			JSONObject tmp = new JSONObject();
			tmp.put("profile", profile);
			http.response res = http.post(url, tmp);
			if (res.status_code != 200) {
				logging("[register] "+ "Response from "+ url);
				logging("[register] "+ "Response Code: "+ res.status_code);
				logging("[register] "+ res.body);
			}
	        return res.status_code == 200;
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

    static public boolean deregister (String d_id) {
		try {
			String url = ENDPOINT +"/"+ d_id;
			logging("[deregister] "+ url);
			http.response res = http.delete(url);
			if (res.status_code != 200) {
				logging("[deregister] "+ "Response from "+ url);
				logging("[deregister] "+ "Response Code: "+ res.status_code);
				logging("[deregister] "+ res.body);
			}
	        return res.status_code == 200;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    static public boolean push (String d_id, String df_name, JSONObject data) {
    	try {
			//logging(d_id +" pushing to "+ ENDPOINT);
			String url = ENDPOINT +"/"+ d_id + "/" + df_name;
			http.response res = http.put(url, data);
			if (res.status_code != 200) {
				logging("[push] "+ "Response from "+ url);
				logging("[push] "+ "Response Code: "+ res.status_code);
				logging("[push] "+ res.body);
			}
	        return res.status_code == 200;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
    	return false;
    }
    
    static public JSONArray pull (String d_id, String df_name) throws JSONException {
	    try {
			//logging(d_id +" pulling from "+ ENDPOINT);
			String url = ENDPOINT +"/"+ d_id + "/" + df_name;
	        http.response res = http.get(url);
			if (res.status_code != 200) {
				logging("[pull] "+ "Response from "+ url);
				logging("[pull] "+ "Response Code: "+ res.status_code);
				logging("[pull] "+ res.body);
			}
			JSONObject tmp = new JSONObject(res.body);
	        return tmp.getJSONArray("samples");
	        
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static private class http {
    	static public class response {
        	public String body;
        	public int status_code;
        	public response (String body, int status_code) {
                this.body = body;
                this.status_code = status_code;
            }
        }
    	
    	static public response get (String url_str) {
    		return request("GET", url_str, null);
        }
    	
    	static public response post (String url_str, JSONObject post_body) {
    		return request("POST", url_str, post_body.toString());
    	}
    	
    	static public response delete (String url_str) {
    		return request("DELETE", url_str, null);
    	}
    	
    	static public response put (String url_str, JSONObject put_body) {
    		return put(url_str, put_body.toString());
    	}
    	
    	static public response put (String url_str, String post_body) {
    		return request("PUT", url_str, post_body);
    	}
    	
    	static private response request (String method, String url_str, String request_body) {
    		try {
    			URL url = new URL(url_str);
    			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    			connection.setRequestMethod(method);
    			
    			if (method.equals("POST") || method.equals("PUT")) {
	    			connection.setDoOutput(true);	// needed, even if method had been set to POST
	    			connection.setRequestProperty("Content-Type", "application/json");
	    			
	    			OutputStream os = connection.getOutputStream();
				    os.write(request_body.getBytes());
    			}
    			
                int status_code = connection.getResponseCode();
            	InputStream in;
                
                if(status_code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    in = new BufferedInputStream(connection.getErrorStream());
                } else {
                    in = new BufferedInputStream(connection.getInputStream());
                }
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String body = "";
                String line = "";
                while ((line = reader.readLine()) != null) {
                    body += line + "\n";
                }
                connection.disconnect();
                reader.close();
                return new response(body, status_code);
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    			logging("MalformedURLException");
            	return new response("MalformedURLException", 400);
    		} catch (IOException e) {
    			e.printStackTrace();
    			logging("IOException");
            	return new response("IOException", 400);
    		}
    	}
    }
	
	static private void logging (String message) {
		String padding = message.startsWith(" ") || message.startsWith("[") ? "" : " ";
        System.out.printf("[%s][%s]%s%s%n", log_tag, local_log_tag, padding, message);
    }
}