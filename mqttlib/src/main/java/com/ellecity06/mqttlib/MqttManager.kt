package com.ellecity06.mqttlib

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

/**
 * @author zavier
 * @time 2020/8/20 10:56
 * @des 消息队列管理
 */
class MqttManager {

    private lateinit var mConfig: MqttConfig
    private var mqttClient: MqttAndroidClient? = null
    private val mSubscribers = LinkedHashMap<String, MqttSubscriber>()

    /**
     * 初始化Mqtt客户端，建议在MainActivity的onCreate中调用
     */
    fun init(context: Context, config: MqttConfig = MqttConfig()) {
        mConfig = config
        mqttClient = MqttAndroidClient(context, config.getBaseUrl(), config.getClientId())
        mqttClient!!.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    MqttLoger.e("----> mqtt reconnect complete, serverUrl = $serverURI")
                } else {
                    MqttLoger.e("----> mqtt connect complete, serverUrl = $serverURI")
                }
            }

            override fun connectionLost(cause: Throwable?) {
                mSubscribers.entries.forEach {
                    it.value.connectLost?.invoke(cause)
                }
                MqttLoger.e("----> mqtt connect lost, cause = ${cause?.message}")
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                val subscriber = mSubscribers[topic]
                subscriber?.messageArrived?.invoke(topic, String(message.payload), message.qos)
                MqttLoger.e("----> mqtt message arrived, topic = $topic, message = ${String(message.payload)}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                mSubscribers.entries.forEach {
                    it.value.deliveryComplete?.invoke(token.message.toString())
                }
                MqttLoger.e("----> mqtt delivery complete, token = ${token.message}")
            }
        })
    }


    /**
     * 关闭MQTT客户端，建议在MainActivity的onDestroy中调用
     */
    fun close() {
        try {
            mqttClient?.close()
            mqttClient?.disconnect()
            mqttClient?.unregisterResources()
            clear()
            MqttLoger.e("----> mqtt close success.")
        } catch (e: Exception) {
            MqttLoger.e("----> mqtt close failed.")
            e.printStackTrace()
        }
    }


    /**M
     * 连接服务器
     * @param subscriber 表示当前方法的回调，并不会作用到全局
     */
    fun connect(subscriber: (MqttSubscriber.() -> Unit)? = null) {
        if (mqttClient == null) {
            MqttLoger.e("----> mqtt connect failed, please init mqtt first.")
            return
        }
        val callback = MqttSubscriber()
        subscriber?.let { callback.it() }
        try {
            mqttClient?.connect(generateConnectOptions(), null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    callback.connectSuccess?.invoke()
                    mSubscribers.entries.forEach {
                        it.value.connectSuccess?.invoke()
                    }
                    MqttLoger.e("----> mqtt connect success.")
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttClient?.setBufferOpts(disconnectedBufferOptions)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                    callback.connectFailed?.invoke(exception)
                    mSubscribers.entries.forEach {
                        it.value.connectFailed?.invoke(exception)
                    }
                    MqttLoger.e("----> mqtt connect failed, exception = ${exception?.message}")
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    /**
     * 订阅一个话题
     */
    @JvmOverloads
    fun subscribe(
        topic: String = mConfig.getSubscribeTopic(),
        subscriber: (MqttSubscriber.() -> Unit)? = null
    ) {
        if (mqttClient == null) {
            MqttLoger.e("----> mqtt subscribe failed, please init mqtt first.")
            return
        }
        if (isConnected()) {
            performSubscribe(topic, subscriber)
        } else {
            // 如果没有连接，就先去连接
            connect {
                onConnectSuccess { performSubscribe(topic, subscriber) }
            }
        }
    }

    /**
     * 订阅实现
     */
    private fun performSubscribe(topic: String, subscriber: (MqttSubscriber.() -> Unit)? = null) {
        // 判断是否已经订阅
        if (mSubscribers.containsKey(topic)) return
        val callback = MqttSubscriber()
        subscriber?.let { callback.it() }
        mSubscribers[topic] = callback
        try {
            mqttClient?.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    callback.subscriberSuccess?.invoke()
                    MqttLoger.e("----> mqtt subscribe success, topic = $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                    callback.subscriberFailed?.invoke(exception)
                    MqttLoger.e("----> mqtt subscribe failed, exception = ${exception?.message}")
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    /**
     * 退订某一个topic
     */
    @JvmOverloads
    fun unsubscribe(topic: String = mConfig.getSubscribeTopic()) {
        mSubscribers.remove(topic)
        mqttClient?.unsubscribe(topic, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                MqttLoger.e("----> mqtt unsubscribe success, topic = $topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                MqttLoger.e("----> mqtt unsubscribe failed, exception = ${exception?.toString()}")
            }
        })
    }


    /**
     * 发布消息
     */
    @JvmOverloads
    fun publishMessage(topic: String = mConfig.getPublishTopic(), content: String) {
        if (mqttClient == null) {
            MqttLoger.e("----> mqtt publish message failed, please init mqtt first.")
            return
        }
        if (isConnected()) {
            performPublishMessage(topic, content)
        } else {
            connect {
                onConnectSuccess { performPublishMessage(topic, content) }
            }
        }
    }


    private fun performPublishMessage(topic: String, content: String) {
        try {
            val message = MqttMessage()
            message.payload = content.toByteArray()
            mqttClient?.publish(topic, message)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    /**
     * 主动断开连接，不会自动重连
     */
    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断连接是否断开
     */
    fun isConnected(): Boolean {
        try {
            return mqttClient?.isConnected ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getServerUrl(): String? {
        return mConfig.getBaseUrl()
    }

    fun getSubscribers(): LinkedHashMap<String, MqttSubscriber> {
        return mSubscribers
    }

    fun clear() {
        getSubscribers().clear()
    }

    /**
     * 生成默认的连接配置
     */
    private fun generateConnectOptions(): MqttConnectOptions {
        val options = MqttConnectOptions()
        // 设置断开后自动重连
        options.isAutomaticReconnect = true
        //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        options.isCleanSession = false
        //设置超时时间，单位为秒
        options.connectionTimeout = mConfig.getConnectTimeOut()
        //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
        options.keepAliveInterval = mConfig.getKeepAliveInterval()
        options.userName = mConfig.getUserName()
        options.password = mConfig.getPassword().toCharArray()

        return options
    }

    companion object {
        fun getInstance(): MqttManager {
            return Holder.mInstance
        }
    }

    object Holder {
        val mInstance = MqttManager()
    }
}