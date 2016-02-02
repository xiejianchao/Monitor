package com.huhuo.monitor.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.utils.DateUtil;
import com.huhuo.monitor.utils.Logger;

import java.net.URI;
import java.util.Date;

import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

/**
 * This example demonstrates how to create a websocket connection to a server. Only the most
 * important callbacks are overloaded.
 */
public class WebSocketClient extends org.java_websocket.client.WebSocketClient {

    private static final String TAG = WebSocketClient.class.getSimpleName();
    public static int SOCKET_STATUS = -1;


    private OnReceiveMessageListener listener;
    private OnRestartListener onRestartListener;
    private boolean restart;

    public WebSocketClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public WebSocketClient(URI serverURI) {
        super(serverURI);
    }

    public void setReceiveMessageListener(OnReceiveMessageListener listener) {
        this.listener = listener;
    }


    public void setOnRestartListener(OnRestartListener listener) {
        this.onRestartListener = listener;
//        this.restart = allowRestart;
    }


    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        System.out.println("opened connection");
        Logger.d(TAG, "opened connection " + handshakedata);
        // if you plan to refuse connection based on ip or httpfields overload:
        // onWebsocketHandshakeReceivedAsClient
        final Message msg = Message.obtain();
        msg.what = Constants.OPEN;
        msg.obj = handshakedata;
        handler.sendMessage(msg);
        SOCKET_STATUS = Constants.OPEN;
    }

    @Override
    public void onMessage(final String message) {

        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            Logger.d(TAG,"工作在主线程");
        } else {
            Logger.d(TAG,"工作在子线程");
        }
        final String time = DateUtil.getFormatTime(new Date(), DateUtil.PATTERN_STANDARD);
        Logger.d(TAG, "来自服务器消息，时间：" + time + "，内容：" + message);
        final Message msg = Message.obtain();
        msg.what = Constants.RECEIVE_MESSAGE;
        msg.obj = message;
        handler.sendMessage(msg);
        SOCKET_STATUS = Constants.RECEIVE_MESSAGE;
    }

    @Override
    public void onFragment(Framedata fragment) {
        System.out.println("received fragment: " + new String(fragment.getPayloadData().array()));
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        Logger.d(TAG, "Connection closed by " + (remote ? "remote peer" : "us"));

        final Message msg = Message.obtain();
        Object[] arr = new Object[3];
        arr[0] = code;
        arr[1] = reason;
        arr[2] = remote;

        msg.what = Constants.CLOSE;
        msg.obj = arr;
        handler.sendMessage(msg);

        SOCKET_STATUS = Constants.CLOSE;

        if (onRestartListener != null) {
            final Message message = Message.obtain();
            message.what = Constants.RESTART;
            handler.sendMessage(message);
        }
    }

    @Override
    public void onError(final Exception ex) {
        ex.printStackTrace();
        Logger.e(TAG, "onError", ex);
        // if the error is fatal then onClose will be called additionally

        final Message msg = Message.obtain();
        msg.what = Constants.ERROR;
        msg.obj = ex;
        handler.sendMessage(msg);
        SOCKET_STATUS = Constants.ERROR;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            try {
                int type = msg.what;
                switch (type) {
                    case Constants.OPEN:
                        if (listener != null) {
                            ServerHandshake ser = (ServerHandshake) msg.obj;
                            listener.onOpen(ser);
                        }

                        break;
                    case Constants.RECEIVE_MESSAGE:
                        if (listener != null) {
                            String message = (String) msg.obj;
                            listener.onReceiveMessage(message);
                        }
                        break;
                    case Constants.CLOSE:
                        if (listener != null) {
                            Object[] arr = (Object[]) msg.obj;
                            int code = (int) arr[0];
                            String reason = (String) arr[1];
                            boolean remote = (boolean) arr[2];
                            listener.onClose(code, reason, remote);
                        }
                        break;
                    case Constants.ERROR:
                        if (listener != null) {
                            Exception e = (Exception) msg.obj;
                            listener.onError(e);
                        }
                        break;
                    case Constants.RESTART:
                        if (onRestartListener != null) {
                            onRestartListener.onAllowRestartWebSocket();
                        }
                        break;

                }
//            } catch (Exception e) {
//                Logger.e(TAG, "handleMessage error " + e);
//            }
        }
    };

    public interface OnRestartListener {
        public void onAllowRestartWebSocket();
    }


    public interface OnReceiveMessageListener {
        public void onOpen(ServerHandshake handshakedata);

        public void onReceiveMessage(String message);

        public void onClose(int code, String reason, boolean remote);

        public void onError(Exception ex);
    }
}