package com.example.jacksoket;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Socket收发器 通过Socket发送数据，并使用新线程监听Socket接收到的数据
 *
 * @author jzj1993
 * @since 2015-2-22
 */
public abstract class SocketTransceiver implements Runnable {
    public static final int PACKET_HEAD_LENGTH = 4;//包头长度
    protected Socket socket;
    protected InetAddress addr;
    protected DataInputStream in;
    protected DataOutputStream out;
    private boolean runFlag;

    /**
     * 实例化
     *
     * @param socket 已经建立连接的socket
     */
    public SocketTransceiver(Socket socket) {
        this.socket = socket;
        this.addr = socket.getInetAddress();
    }

    /**
     * 获取连接到的Socket地址
     *
     * @return InetAddress对象
     */
    public InetAddress getInetAddress() {
        return addr;
    }

    /**
     * 开启Socket收发
     * <p>
     * 如果开启失败，会断开连接并回调{@code onDisconnect()}
     */
    public void start() {
        runFlag = true;
        new Thread(this).start();
    }

    /**
     * 断开连接(主动)
     * <p>
     * 连接断开后，会回调{@code onDisconnect()}
     */
    public void stop() {
        runFlag = false;
        try {
            socket.shutdownInput();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    OutputStream os;

    /**
     * 发送字符串
     *
     * @param s 字符串
     * @return 发送成功返回true
     */
    public boolean send(final String s) {
        if (os != null) {
            try {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            String packTop=pack((int)(s.length()),4, ByteOrder.BIG_ENDIAN,"UTF-8");
                            PrintWriter pw = new PrintWriter(os);
                            pw.write(packTop+s);
                            pw.flush();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }
                }.start();
                return true;
            } catch (Exception e) {
                Log.d("Log", "成功");
                e.printStackTrace();
            }
        }
        return false;
    }
    private String pack(int value, int allocateBytes, ByteOrder byteOrder, String charsetName) throws UnsupportedEncodingException, UnsupportedEncodingException {
        ByteBuffer buf = ByteBuffer.allocate(allocateBytes);
        buf.order(byteOrder);
        byte[] bytes = buf.putInt(value).array();
        String result = new String(bytes, charsetName);
        return result;
    }
    /**
     * 监听Socket接收的数据(新线程中运行)
     */
    @Override
    public void run() {
        try {
            in = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            runFlag = false;
        }
        byte[] bytesAcount= new byte[0];
        while (runFlag) {
            try {
                DataInputStream socketReader = new DataInputStream(in);
                byte[] bytes = new byte[4];
                socketReader.read(bytes);
                int byteArrayInt = byteArrayToInt(bytes);
                Log.d("Log","Soket:数据legth="+byteArrayInt);
                byte[] bytesLeath = new byte[byteArrayInt];
                socketReader.read(bytesLeath);

                String str = new String(bytesLeath);
                str = str.trim();
                this.onReceive(addr, str);
                Log.d("Log","Soket:数据="+str);
            } catch (IOException e) {
                Log.d("Log", "Soket 解析数据错误==" +e);

                // 连接被断开(被动)
                runFlag = false;
            } catch (Exception e) {
                Log.d("Log", "Soket 解析数据错误==" +e);

                runFlag = false;
            }
        }
        // 断开连接
        try {
            in.close();
            out.close();
            socket.close();
            in = null;
            out = null;
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.onDisconnect(addr);
    }
    public static int length(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 3;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }
    //System.arraycopy()方法
    public static byte[] byteMerger(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length+bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }
    public static int byteArrayToInt(byte[] b) {
        byte[] a = new byte[4];
        int i = a.length - 1, j = b.length - 1;
        for (; i >= 0; i--, j--) {//从b的尾部(即int值的低位)开始copy数据
            if (j >= 0)
                a[i] = b[j];
            else
                a[i] = 0;//如果b.length不足4,则将高位补0
        }
        int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        int v1 = (a[1] & 0xff) << 16;
        int v2 = (a[2] & 0xff) << 8;
        int v3 = (a[3] & 0xff);
        return v0 + v1 + v2 + v3;
    }

    public byte[] mergebyte(byte[] a, byte[] b, int begin, int end) {
        byte[] add = new byte[a.length + end - begin];//总的   +
        int i = 0;
        for (i = 0; i < a.length; i++) {
            add[i] = a[i];
        }

        for (int k = begin; k < end; k++, i++) {
            add[i] = b[k];
        }
        return add;
    }

    public byte[] mergebyte2(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length+bt2.length];
        int i=0;
        for(byte bt: bt1){
            bt3[i]=bt;
            i++;
        }
        for(byte bt: bt2){
            bt3[i]=bt;
            i++;
        }
        return bt3;
    }

    public byte[] mergebyte3(byte[] a, byte[] b, int begin, int end) {
        byte[] add = new byte[end-begin];
        int i = 0;


        for (int k = a.length; k < end; k++, i++) {
            add[i] = b[k];
        }
        return add;
    }
    /**
     * 接收到数据
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param addr 连接到的Socket地址
     * @param s    收到的字符串
     */
    public abstract void onReceive(InetAddress addr, String s);

    /**
     * 连接断开
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param addr 连接到的Socket地址
     */
    public abstract void onDisconnect(InetAddress addr);


    public void setAddr22() {
        String  data="{\n" +
                "  \"type\": \"device_list\",\n" +
                "  \"msg\": \"success\",\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"num\": \"USR16351457886234\",\n" +
                "      \"name\": \"测试设备\",\n" +
                "      \"status\": 2\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        data.substring(12);

        this.onReceive(addr, data);
    }
}
