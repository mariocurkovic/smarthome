package com.mariocurkovic.smarthome.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariocurkovic.smarthome.tuya.open.sdk.consumer.MessageVO;
import com.mariocurkovic.smarthome.tuya.open.sdk.model.TuyaMessageModel;
import com.mariocurkovic.smarthome.tuya.open.sdk.mq.MqConfigs;
import com.mariocurkovic.smarthome.tuya.open.sdk.mq.MqConstants;
import com.mariocurkovic.smarthome.tuya.open.sdk.mq.MqConsumer;
import com.mariocurkovic.smarthome.tuya.open.sdk.util.decrypt.AESBaseDecryptor;
import org.apache.pulsar.client.api.MessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TuyaConsumerUtil {

    private static final Logger logger = LoggerFactory.getLogger(TuyaConsumerUtil.class);


    /**
     * starts Tuya pulsar message consumer
     */
    public static void init() throws Exception {
        MqConsumer mqConsumer = MqConsumer.build().serviceUrl(MqConfigs.EU_SERVER_URL).accessId(PropertiesUtil.getTuyaAccessId()).accessKey(PropertiesUtil.getTuyaAccessSecret())
                .messageListener(message -> {
                    MessageId msgId = message.getMessageId();
                    String encryptModel = message.getProperty(MqConstants.ENCRYPT_MODEL);
                    long publishTime = message.getPublishTime();
                    String payload = new String(message.getData());
                    logger.debug("###TUYA_PULSAR_MSG => start process message, messageId={}, publishTime={}, encryptModel={}, payload={}",
                            msgId, publishTime, encryptModel, payload);
                    payloadHandler(payload, encryptModel);
                    logger.debug("###TUYA_PULSAR_MSG => finish process message, messageId={}, publishTime={}, encryptModel={}",
                            msgId, publishTime, encryptModel);
                });
        mqConsumer.start();
    }

    /**
     * This method is used to process your message business
     */
    private static void payloadHandler(String payload, String encryptModel) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            MessageVO messageVO = objectMapper.readValue(payload, MessageVO.class);
            String dataJsonStr = AESBaseDecryptor.decrypt(messageVO.getData(), PropertiesUtil.getTuyaAccessSecret().substring(8, 24), encryptModel);
            logger.info("Received Tuya message: " + dataJsonStr);
            TuyaMessageModel resultModel = objectMapper.readValue(dataJsonStr, TuyaMessageModel.class);
            Boolean isSwitchOn = resultModel.getStatus().get(0).getValue();
            if (isSwitchOn) {
                GpioUtil.turnOn(PropertiesUtil.getRelayPosition());
            } else {
                GpioUtil.turnOff(PropertiesUtil.getRelayPosition());
            }
        } catch (Exception e) {
            logger.error("payload=" + payload + "; your business processing exception, please check and handle. e=", e);
        }
    }

}