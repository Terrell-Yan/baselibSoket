package com.example.jacksoket;


import android.os.Build;
import android.util.Log;


import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.example.jacksoket.ClientThread.soket_call_back;


public class Soket_Call_back extends TcpClient {


    Back_result soket_call_result;
    List<Back_result> soket_call_result_list;

    public Soket_Call_back() {
        soket_call_result_list = new ArrayList<>();
    }

    @Override
    public void onConnect(SocketTransceiver transceiver) {
        Log.d("Log", "连接成功");


    }

    @Override
    public void onConnectFailed() {
        Log.d("Log", "连接失败");
        for (int i = 0; i < soket_call_result_list.size(); i++) {
            soket_call_result_list.get(i).GetSoketResultfaile("连接失败");
        }
    }

    @Override
    public void onDisconnect(SocketTransceiver transceiver) {
        Log.d("Log", "断开连接");
        for (int i = 0; i < soket_call_result_list.size(); i++) {
            soket_call_result_list.get(i).GetSoketResultfaile("断开连接");
        }
    }

    public static long toUnsignedLong(int x) {
        return ((long) x) & 0xffffffffL;
    }

    public static final String sendHttptop = "";
    int count = 0;

    @Override
    public void onReceive(SocketTransceiver transceiver, String str) {
        try {

                //进行解析
                JSONObject jsonObject1 = new JSONObject(str);
                if (jsonObject1.optString("type").equals("ping")) {
                    try {
                        JSONObject jsout2 = new JSONObject();
                        jsout2.put("type", "base.pong");
                        soket_call_back.getTransceiver().send(jsout2.toString());

                    } catch (Exception e) {

                    }
                }
                if (jsonObject1.optString("type").equals("error")) {
                    for (int i = 0; i < soket_call_result_list.size(); i++) {
                        soket_call_result_list.get(i).GetSoketResultfaile(str);
                    }
                } else if (jsonObject1.optString("type").equals("client_id")) {
                    String data = AESUtilsnew.encrypt(AesUtils.generateKey(), jsonObject1.optString("client_id"));
                    JSONObject jsout = new JSONObject();
                    jsout.put("type", "auth");
                    jsout.put("device", "order");
//                jsout.put("device","device");
                    jsout.put("token", data);
                    soket_call_back.getTransceiver().send(jsout.toString());
                    Log.d("Log", "Soket client_id==" + jsout.toString());


                } else if (jsonObject1.optString("type").equals("auth")) {

                    for (int i = 0; i < soket_call_result_list.size(); i++) {
                        soket_call_result_list.get(i).GetSoketResult_sucess(str);
                    }
                } else {
                    Log.d("Log", "Soket 成功发送数据==" + soket_call_result_list.size());
                    for (int i = 0; i < soket_call_result_list.size(); i++) {

                        soket_call_result_list.get(i).GetSoketResult_sucess(str);
                    }
                }

//            }

        } catch (Exception e) {
            Log.d("Lop", "数据解密decrypt==" + e);

        }


    }

    public void writeUInt32(long uint32) throws IOException {
        writeUInt16((int) (uint32 & 0xffff0000) >> 16);
        writeUInt16((int) uint32 & 0x0000ffff);
    }

    public void writeUInt16(int uint16) throws IOException {
        writeUInt8(uint16 >> 8);
        writeUInt8(uint16);
    }

    int data2;

    public int writeUInt8(int uint8) throws IOException {
        data2 = uint8 & 0xFF;
        return uint8 & 0xFF;
    }


    //四个字节的方法
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];


        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    public void addClientNotify(Back_result soket_call_result) throws Exception {
        this.soket_call_result = soket_call_result;
        soket_call_result_list.add(soket_call_result);
    }

    public static char byteToChar(byte[] b) {
        char c = (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
        return c;
    }
}
