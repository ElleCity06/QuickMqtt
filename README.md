## MQTT在Android上的二次封装
#### 添加依赖
* 在project的build.gradle文件中添加maven仓库（mqtt开源库需要）


      repositories {
          maven {
              url "https://repo.eclipse.org/content/repositories/paho-snapshots/"
          }
      }


#### 使用流程


* 连接配置



        val config: MqttConfig = MqttConfig().apply {
            setBaseUrl("tcp://192.168.1.186:1883")
            setClientId("MqttAndroidClient")
            setUserName("admin")
            setPassword("admin")
        }



* 初始化MQTT


     MqttManager.getInstance().init(this,config)



* 连接MQTT服务端

      MqttManager.getInstance().connect {
          onConnectSuccess {
              showTips("服务器连接成功")
          }
          onConnectFailed {
              showTips("服务器连接失败：${it?.message}")
          }
      }


* 订阅一个话题

      MqttManager.getInstance().subscribe(topic) {
          onSubscriberSuccess {
              showTips("订阅成功")
          }
          onSubscriberFailed {
              showTips("订阅失败：${it?.message}")
          }
          onDeliveryComplete {
              showTips("消息推送完毕：$it")
          }
          onConnectionLost {
              showTips("连接已断开")
          }
      }

* 推送一条消息

      MqttManager.getInstance().publishMessage(topic, "Hello Mqtt...")

* 主动断开连接

      MqttManager.getInstance().disconnect()

* 关闭MQTT客户端（一般app退出时调用）

      MqttManager.getInstance().close()

