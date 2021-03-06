package com.example.khk.lknmessenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;

/**
 * Created by KHK on 2015-12-06.
 */
public class MessageActivity extends Activity {
    private String Roomname;
    private int ID;
    private Button Send;
    private EditText editText;
    private ScrollView scrollView;
    private ListView listView;
    private String message="";
    private String sendMsg,recvMsg;
    private MssReq mssReq;
    private MssAck mssAck;
    private Packet packet;
    private ArrayAdapter<String> arrAdapter ;
    private ArrayList<String> list;
    private Intent intent;
    boolean trd =true;
    Thread thread;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messagewindow);


        intent = getIntent();
        Roomname = intent.getExtras().getString("Roomname");
        ID = intent.getExtras().getInt("RoomId");
        Log.e("RoomId", String.valueOf(ID));
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        list = new ArrayList<String>() ;
        arrAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.mylist,list);
        listView = (ListView) findViewById(R.id.Message);
        listView.setAdapter(arrAdapter);

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        Send = (Button) findViewById(R.id.Send);
        editText = (EditText) findViewById(R.id.EditText);


        Send.setOnClickListener(OnClickListener);

        mssReq = new MssReq();
        mssAck = new MssAck();




                    /*for(Iterator<String> it = list.iterator() ; it.hasNext() ; )
                    {
                        it.remove();
                        arrAdapter.notifyDataSetChanged();
                    }*/

    }



    private View.OnClickListener OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            message = editText.getText().toString();
                mssReq.setMessage(message);
                sendMsg = PacketCodec.encodeMssReq(mssReq);
                Log.e("sendMsg", sendMsg);

                try {
                    Log.e("2sendMsg", "Link Start!!"); //Toast.makeText(getBaseContext(),"Link Start!!",Toast.LENGTH_SHORT).show();
                    SocketManager.sendMsg(sendMsg);
                    Log.e("3sendMsg", sendMsg);

                    recvMsg = SocketManager.receiveMsg();
                    Log.e("recvMsg", recvMsg);
                    packet = PacketCodec.decodeHeader(recvMsg);
                } catch (IOException e) {
                    e.printStackTrace();

                }

                mssAck = PacketCodec.decodeMssAck(packet.getData());

                //  analyzing the ACK sent from server
                if (mssAck.getAnswer() == Packet.FAIL) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MessageActivity.this);
                    dialog.setMessage("Please Input Correct Sentence");
                    Log.e("Please","Please Input Correct Sentence");
                    dialog.setPositiveButton("ok", null);
                    Log.e("ok","ok");
                    dialog.show();
                }                   //  if the ACK means login fail, create the alert dialog

                else if (mssAck.getAnswer() == Packet.SUCCESS) {
                    if(!message.equals("")) {
                        arrAdapter.add(message);
                        message = "";
                        editText.setText("");
                        String[] arg;
                        MssAndArrtimeAndUser[] mau;
                        if (mssAck.getListnum() > 0) {
                            arg = PacketCodec.nextDecode(mssAck.getListnum(), mssAck.getlist());
                            Log.e("arg[0]", arg[0]);
                            mau = PacketCodec.nextTinydecode(arg.length, arg);
                            for (int i = 0; i < mau.length; i++) {
                                list.add(mau[i].getMss());
                                arrAdapter.notifyDataSetChanged();
                            }

                        } else {
                            Toast.makeText(getBaseContext(), "Send!", Toast.LENGTH_SHORT).show();
                            Log.e("Sever", "Send!");
                        }
                    }

                }


        }
    };
}
