package info.androidhive.locationapi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
public class start extends Activity{
	
	private Button SignUpButton,SignInButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
     
		SignUpButton = (Button) findViewById(R.id.SignUpButton);
		SignInButton = (Button) findViewById(R.id.SignInButton);
		
		
		SignUpButton.setOnClickListener(new View.OnClickListener(){
			//@Override
			public void onClick(View v){
				Intent intent = new Intent(start.this,Register.class);
				startActivity(intent);
				finish();
					
				}
			});
		SignInButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Intent i = new Intent(start.this,Login.class);
				startActivity(i);
				finish();
			}
		});
		}
	}

