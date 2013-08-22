package com.basetentwo.smstohttp;

import java.io.IOException;

import android.os.AsyncTask;
import android.os.Handler;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;


/**
* Base class for objects that connect to a RabbitMQ Broker
*/
public class IConnectToRabbitMQ {
  public String mServer;
  public String mExchange;
  public String mUsername;
  public String mPassword;
  public boolean connected = false;
  public int mPort;

  protected Channel mModel = null;
  protected Connection  mConnection;

  protected boolean Running ;

  protected  String MyExchangeType ;
  
  //The Queue name for this consumer
  public String mQueue;
  public QueueingConsumer MySubscription;

  //last message to post back
  public byte[] mLastMessage;

  /**
   *
   * @param server The server address
   * @param exchange The named exchange
   * @param exchangeType The exchange type name
   */
  public IConnectToRabbitMQ(String server, String exchange, String exchangeType, String username, String password, int port)
  {
      mServer = server;
      mExchange = exchange;
      mUsername = username;
      mPassword = password;
      mPort = port;
      MyExchangeType = exchangeType;
  }

  public void Dispose()
  {
      Running = false;

        try {
            if (mConnection!=null)
                mConnection.close();
            if (mModel != null)
                mModel.abort();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

  }
  

  
  // An interface to be implemented by an object that is interested in messages(listener)
  public interface OnReceiveMessageHandler{
      public void onReceiveMessage(byte[] message);
  };

  //A reference to the listener, we can only have one at a time(for now)
  private OnReceiveMessageHandler mOnReceiveMessageHandler;

  /**
   *
   * Set the callback for received messages
   * @param handler The callback
   */
  public void setOnReceiveMessageHandler(OnReceiveMessageHandler handler){
      mOnReceiveMessageHandler = handler;
  };

  public Handler mMessageHandler = new Handler();
  public Handler mConsumeHandler = new Handler();

  // Create runnable for posting back to main thread
  final Runnable mReturnMessage = new Runnable() {
      public void run() {
          mOnReceiveMessageHandler.onReceiveMessage(mLastMessage);
      }
  };

  final Runnable mConsumeRunner = new Runnable() {
      public void run() {
          Consume();
      }
  };


  /**
   * Add a binding between this consumers Queue and the Exchange with routingKey
   * @param routingKey the binding key eg GOOG
   */
  public void AddBinding(String routingKey)
  {

  }

  /**
   * Remove binding between this consumers Queue and the Exchange with routingKey
   * @param routingKey the binding key eg GOOG
   */
  public void RemoveBinding(String routingKey)
  {
      try {
          mModel.queueUnbind(mQueue, mExchange, routingKey);
      } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
  }

  private void Consume()
  {
      Thread thread = new Thread()
      {

           @Override
              public void run() {
               while(Running){
                  QueueingConsumer.Delivery delivery;
                  try {
                      delivery = MySubscription.nextDelivery();
                      mLastMessage = delivery.getBody();
                      mMessageHandler.post(mReturnMessage);
                      try {
                          mModel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  } catch (InterruptedException ie) {
                      ie.printStackTrace();
                  }
               }
           }
      };
      thread.start();

  }

  public void dispose(){
      Running = false;
  }
  
  public void connectToRabbitMQ(){
	  new ConnectTask().execute(this);
  }


}

final class ConnectTask extends AsyncTask<IConnectToRabbitMQ, Integer, Boolean> {
	IConnectToRabbitMQ rabbit = null;
    protected Boolean doInBackground(IConnectToRabbitMQ ... rabbits) {
    	this.rabbit = rabbits[0];
    	boolean status = connectToRabbitMQ();
    	return status;
    }
    
    /**
     * Connect to the broker and create the exchange
     * @return success
     */
    public boolean connectToRabbitMQ()
    {
        if(rabbit.mModel!= null && rabbit.mModel.isOpen() )//already declared
            return true;
        try
        {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(rabbit.mServer);
            connectionFactory.setUsername(rabbit.mUsername);
            connectionFactory.setPassword(rabbit.mPassword);
            connectionFactory.setPort(rabbit.mPort);
            
//            connectionFactory.useSslProtocol();
            rabbit.mConnection = connectionFactory.newConnection();
            rabbit.mModel = rabbit.mConnection.createChannel();
            rabbit.mModel.exchangeDeclare(rabbit.mExchange, rabbit.MyExchangeType, false);
            
            
            try {
            	rabbit.mQueue = rabbit.mModel.queueDeclare().getQueue();
                rabbit.mModel.queueBind(rabbit.mQueue, rabbit.mExchange, "sms_send");
                
            	rabbit.MySubscription = new QueueingConsumer(rabbit.mModel);
            	rabbit.mModel.basicConsume(rabbit.mQueue, false, rabbit.MySubscription);
             } catch (IOException e) {
                 e.printStackTrace();
                 return false;
             }
              if (rabbit.MyExchangeType == "fanout"){
            	  rabbit.AddBinding("");//fanout has default binding
              }
              rabbit.Running = true;
              rabbit.mConsumeHandler.post(rabbit.mConsumeRunner);
  
            return true;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    protected void onPostExecute(Boolean result) {
        rabbit.connected = result;
    }
}
