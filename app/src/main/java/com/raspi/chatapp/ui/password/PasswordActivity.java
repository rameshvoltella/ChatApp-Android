/*
 * Copyright 2016 Niklas Schelten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.raspi.chatapp.ui.password;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;

import com.raspi.chatapp.R;

import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordActivity extends AppCompatActivity implements
        PinFragment.OnFragmentInteractionListener,
        AGBFragment.OnFragmentInteractionListener{

  public static final String PREFERENCES = "com.raspi.chatapp.ui.password" +
          ".PasswordActivity.PREFERENCES";
  public static final String HASH = "com.raspi.chatapp.ui.password" +
          ".PasswordActivity.HASH";
  public static final String SALT = "com.raspi.chatapp.ui.password" +
          ".PasswordActivity.SALT";

  public static final int ASK_PWD_REQUEST = 1;

  public static final int ITERATIONS = 1024;
  public static final int SALT_LENGTH = 32;

  private boolean access = false;

  public static SecretKeyFactory getSecretKeyFactory() throws
          NoSuchAlgorithmException, NullPointerException{
    SecretKeyFactory f;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
      f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1And8bit");
    else
      f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    return f;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_password);
    if (savedInstanceState == null)
      getSupportFragmentManager().beginTransaction().add(R.id
              .fragment_container, AGBFragment.newInstance()).commit();
  }

  private void grantAccess(){
    access = true;
    setResult(Activity.RESULT_OK);
    finish();
  }

  @Override
  protected void onPause(){
    setResult(access ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
    super.onPause();
  }

  private void checkPassword(final char[] pwd){
    //the dialog will be dismissed in the thread
    //and the thread will also display the error message if logging in was
    // not successful.
    ProgressDialog dialog = ProgressDialog.show(this, "", getResources()
                    .getString(R.string.logging_in), true);
    LoginThread loginThread = new LoginThread(dialog, pwd);
    loginThread.start();
  }

  @Override
  public void agbAccepted(){
    getSupportFragmentManager().beginTransaction().replace(R.id
            .fragment_container, PinFragment.newInstance()).commit();

  }

  private class LoginThread extends Thread{

    private ProgressDialog dialog;
    private char[] pwd;

    public LoginThread(ProgressDialog dialog, final char[] pwd){
      this.dialog = dialog;
      this.pwd = pwd;
    }

    @Override
    public void run(){
      try{
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, 0);
        byte[] salt = Base64.decode(preferences.getString(SALT,
                "0123456789ABCDEF0123456789ABCDEF"), Base64.DEFAULT);

        byte[] real_hash = Base64.decode(preferences.getString(HASH,
                "invalid"), Base64.DEFAULT);
        KeySpec spec = new PBEKeySpec(pwd, salt, ITERATIONS, SALT_LENGTH);
        SecretKeyFactory factory = getSecretKeyFactory();
        byte[] gen_hash = factory.generateSecret(spec).getEncoded();

        if (Arrays.equals(real_hash, gen_hash)){
          grantAccess();
          dialog.dismiss();
          return;
        }
        dialog.dismiss();
        runOnUiThread(new Runnable(){
          @Override
          public void run(){
            try{
              findViewById(R.id.password_invalid).setVisibility(View.VISIBLE);
            }catch (Exception e){
              e.printStackTrace();
            }
          }
        });
      }catch (Exception e){
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onPasswordEntered(char[] pwd){
    checkPassword(pwd);
  }
}
