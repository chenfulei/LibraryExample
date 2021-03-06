package com.library.callback;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.library.FLAjaxMain;
import com.library.constants.FLConstants;
import com.library.utils.Debug;

import org.apache.http.HttpRequest;

/**
 * AQuery internal use only. Handle account, account manager related tasks.
 * 
 */

public class FLGoogleHandle extends FLAccountHandle implements DialogInterface.OnClickListener, OnCancelListener{

	private AccountManager am;
	private Account acc;
	private String type;
	private Activity act;
	private String email;
	private Account[] accs;
	private String token;
	
	public FLGoogleHandle(Activity act, String type, String email){
	
		if(FLConstants.ACTIVE_ACCOUNT.equals(email)){
			email = getActiveAccount(act);
		}
		
		this.act = act;
		this.type = type.substring(2);
		this.email = email;
		this.am = AccountManager.get(act);
		
	}
	
	@Override
	protected void auth(){
		
		if(email == null){
			accountDialog();
		}else{
	        Account[] accounts = am.getAccountsByType("com.google");
	        for(int i = 0; i < accounts.length; i++) {
	        	Account account = accounts[i];
	            if(email.equals(account.name)) {
	            	auth(account);
	            	return;
	            }
	        }
		}
	}
	
	
	public boolean reauth(FLAbstractAjaxCallback<?, ?> cb){
		
		am.invalidateAuthToken(acc.type, token);
		
		try {
			token = am.blockingGetAuthToken(acc, type, true);
			Debug.Log("re token", token);
		} catch (Exception e) {
			Debug.Log(e);
			token = null;
		} 
		
		return token != null;
		
	}
	
	public String getType(){
		return type;
	}
	
	private void accountDialog() {
	    
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        //builder.setTitle("Select a Google account");
        accs = am.getAccountsByType("com.google");
        int size = accs.length;
        
        if(size == 1){
        	auth(accs[0]);
        }else{
        
	        String[] names = new String[size];
	        for(int i = 0; i < size; i++) {
	        	names[i] = accs[i].name;
	        }
	        builder.setItems(names, this);
	        builder.setOnCancelListener(this);
	        
	        AlertDialog dialog = builder.create();//.show();
			FLAjaxMain ajaxMain = new FLAjaxMain(act);
			ajaxMain.show(dialog);
        }
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		Account acc = accs[which];
		Debug.Log("acc", acc.name);
		
		setActiveAccount(act, acc.name);		
		auth(acc);
	}
	
	public static void setActiveAccount(Context context, String account){
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(FLConstants.ACTIVE_ACCOUNT, account).commit();
	}

	public static String getActiveAccount(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getString(FLConstants.ACTIVE_ACCOUNT, null);
	}
	
	private void auth(Account account){
		
		this.acc = account;
		
		Task task = new Task();
		task.execute();
	}
	
	private class Task extends AsyncTask<String, String, Bundle>{
	
		@Override
		protected Bundle doInBackground(String... params) {
	
			Bundle bundle = null;
			
			try {
			    AccountManagerFuture<Bundle> future = am.getAuthToken(acc, type, null, act, null, null);
				bundle = future.getResult();
			} catch (OperationCanceledException e) {
			} catch (AuthenticatorException e) {
				Debug.Log(e);
			} catch (Exception e) {
				Debug.Log(e);
			}
			
			
			return bundle;
		}
		
		
		@Override
		protected void onPostExecute(Bundle bundle) {
			
			if(bundle != null && bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
	          	token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
	          	//AQUtility.debug("stored auth", token);        	
	          	success(act);
			}else{
	        	failure(act, FLAjaxStatus.AUTH_ERROR, "rejected");
	        }
			
		}
	
		
	
	}
	

	@Override
	public void onCancel(DialogInterface dialog) {		
		failure(act, FLAjaxStatus.AUTH_ERROR, "cancel");
	}
	
	@Override
	public boolean expired(FLAbstractAjaxCallback<?, ?> cb, FLAjaxStatus status) {
		int code = status.getCode();
		return code == 401 || code == 403;
	}
	
	@Override
	public void applyToken(FLAbstractAjaxCallback<?, ?> cb, HttpRequest request) {
		
		//AQUtility.debug("apply token", token);
		
		request.addHeader("Authorization", "GoogleLogin auth=" + token);
	}

	@Override
	public String getCacheUrl(String url){
		return url + "#" + token;
	}


	@Override
	public boolean authenticated() {
		return token != null;
	}

	
	
}
