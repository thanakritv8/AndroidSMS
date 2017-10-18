package com.example.a1hz.sms;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Connection con;
    String un,pass,db,ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip = "10.40.1.5";
        db = "SMS";
        un = "sa";
        pass = "sa";
        // Start timer
        Timer myTimer;
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            public void run() {
                timerTick();
            }
        }, 0, 3000); // 3 sec 1 times
        // End start timer
    }

    private void timerTick() {
        this.runOnUiThread(doTask);
    }   // Run timer

    private Runnable doTask = new Runnable() {
        public void run() {
            TextView result = (TextView) findViewById(R.id.lbMain);
            try {
                con = connectionclass(un, pass, db, ip);

                if (con == null)
                {
                    result.setText("Check Your Internet Access!");
                }
                else
                {
                    String query = "SELECT * FROM [SMS].[dbo].[smsMain] WHERE smsStatus = 0";
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        Statement stmtUp = con.createStatement();
                        stmtUp.executeUpdate("UPDATE [SMS].[dbo].[smsMain] SET smsStatus = 1, smsDateSend = GETDATE() WHERE smsSEQ = " + rs.getString("smsSEQ"));
                        SmsManager smsManager = SmsManager.getDefault();
                        String strPhoneNo = rs.getString("smsPhone");
                        String strMessage = rs.getString("smsMessage") + "[MIT]" + rs.getString("smsID");
                        if (strMessage.length() > 140)
                        {
                            double smsnum = strMessage.length() / 140.0;
                            double num = Math.ceil(smsnum) - 1;
                            for (int i = 0; i <= num; i++)
                            {
                                int dataend = (i + 1) * 139;
                                if (dataend > strMessage.length())
                                {
                                    dataend = strMessage.length();
                                }
                                String msg = strMessage.substring(i * 139, dataend);
                                smsManager.sendTextMessage(strPhoneNo, null, msg, null, null);
                            }
                        }
                        else
                        {
                            smsManager.sendTextMessage(strPhoneNo, null, strMessage, null, null);
                        }
                        result.setText(strPhoneNo + " - " + strMessage);
                    }
                    con.close();
                }
            } catch (Exception e) {
                result.setText(e.toString());
            }
        }
    };

    @SuppressLint("NewApi")
    public Connection connectionclass(String user, String password, String database, String server)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL = null;
        //TextView result = (TextView) findViewById(R.id.lbMain);
        try
        {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + server + ";databaseName=" + database + ";user=" + user + ";password=" + password + ";";
            connection = DriverManager.getConnection(ConnectionURL);
            //result.setText("OK");
        }
        catch (SQLException se)
        {
            //result.setText("error here 1 " + ConnectionURL + se.getMessage());
            //Log.e("error here 1 : ", se.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            //result.setText("error here 2 " + ConnectionURL + e.getMessage());
            //Log.e("error here 2 : ", e.getMessage());
        }
        catch (Exception e)
        {
            //result.setText("error here 3 " + ConnectionURL + e.getMessage());
            //Log.e("error here 3 : ", e.getMessage());
        }
        return connection;
    }

}
