package com.example.jacksoket;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Socket收发器 通过Socket发送数据，并使用新线程监听Socket接收到的数据
 *
 * @author jzj1993
 * @since 2015-2-22
 */
public abstract class SocketTransceiver2 implements Runnable {

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
    public SocketTransceiver2(Socket socket) {
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
                        PrintWriter pw = new PrintWriter(os);
                        pw.write(s+"HUSHUAIKANGEND");
                        pw.flush();
                        Log.d("ping", "互ping");
                    }
                }.start();


                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
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
        while (runFlag) {
            try {
                DataInputStream socketReader = new DataInputStream(in);
                int bufferSize = 1024 * 1024;
                byte[] bytes = new byte[bufferSize];
                socketReader.read(bytes);
                String str = new String(bytes);
                str = str.trim();
                String data = str;

                Log.d("SOKET","SOKET："+str);
                this.onReceive(addr, str);
            } catch (IOException e) {
                Log.d("SOKET","SOKET错误："+e);

                // 连接被断开(被动)
                runFlag = false;
            } catch (Exception e) {
                Log.d("SOKET","SOKET错误："+e);

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
}
