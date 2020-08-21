package com.ellecity06.quickmqtt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ellecity06.mqttlib.MqttManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MqttManager.getInstance().init(this)
        showTips("服务器地址：${MqttManager.getInstance().getServerUrl()}")
        btn_connect.setOnClickListener {
            showTips("正在连接...")
            MqttManager.getInstance().connect {
                onConnectSuccess {
                    showTips("连接成功了")
                }
                onConnectFailed {
                    showTips("连接失败了,异常信息为： ${it?.message}")
                }
            }
        }
        btn_subscribe.setOnClickListener {
            showTips("正在订阅...")
            MqttManager.getInstance().subscribe{
                onSubscriberSuccess {
                    showTips("订阅成功了")
                }
                onSubscriberFailed {
                    showTips("订阅失败了，异常信息为： ${it?.message}")
                }
                onDeliveryComplete {
                    showTips("消息推送完成，消息为：$it")
                }
                onConnectionLost {
                    showTips("连接已断开")
                }
                onMessageArrived { _, message, _ ->
                    showTips("收到消息： $message")
                }
            }
        }
        btn_publish.setOnClickListener {
            showTips("消息正在推送...")
            MqttManager.getInstance().publishMessage(content = "Hello Android")
        }
        btn_close.setOnClickListener {
            showTips("正在断开中...")
            MqttManager.getInstance().disconnect()
        }
    }

    private fun showTips(msg: String) {
        tv_tip.text = msg
    }

    override fun onDestroy() {
        super.onDestroy()
        MqttManager.getInstance().close()
    }
}