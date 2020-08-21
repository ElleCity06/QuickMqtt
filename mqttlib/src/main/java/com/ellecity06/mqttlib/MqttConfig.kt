package com.ellecity06.mqttlib

/**
 * @author zavier
 * @time 2020/8/21 10:30
 * @des mqtt连接配置等
 */
class MqttConfig {

    private var baseUrl = "tcp://192.168.1.186:1883"
    private var userName = "admin"
    private var password = "admin"
    private var clientId = "MqttAndroidClient1234567"

    /**
     * 订阅的主题
     */
    private var subscribeTopic: String = "MqttSubscribe"

    /**
     * 推送的主题
     */
    private var publishTopic: String = "MqttPublish"

    /**
     * 连接超时时间 ，单位 s
     */
    private var connectTimeOut: Int = 15

    /**
     * 心跳包间隔 单位 s
     */
    private var keepAliveInterval: Int = 30


    fun setBaseUrl(baseUrl: String): MqttConfig {
        this.baseUrl = baseUrl
        return this
    }

    fun setUserName(userName: String): MqttConfig {
        this.userName = userName
        return this
    }

    fun setPassword(password: String): MqttConfig {
        this.password = password
        return this
    }

    fun setClientId(clientId: String): MqttConfig {
        this.clientId = clientId
        return this
    }

    fun setSubscribeTopic(subscribeTopic: String): MqttConfig {
        this.subscribeTopic = subscribeTopic
        return this
    }

    fun setConnectTimeOut(connectTimeOut: Int): MqttConfig {
        this.connectTimeOut = connectTimeOut
        return this
    }
    fun setKeepAliveInterval(keepAliveInterval: Int): MqttConfig {
        this.keepAliveInterval = keepAliveInterval
        return this
    }
    fun getBaseUrl() = baseUrl

    fun getUserName() = userName

    fun getPassword() = password

    fun getClientId() = clientId

    fun getSubscribeTopic(): String = subscribeTopic

    fun getPublishTopic(): String = publishTopic

    fun getConnectTimeOut() = connectTimeOut
    fun getKeepAliveInterval() = keepAliveInterval
    fun setDebug(isDebug: Boolean) {
        MqttLoger.setDebug(isDebug)
    }
}