package com.yiyi.span;

import brave.Span;
import brave.propagation.ThreadLocalSpan;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class MqSpan {
    private static final Logger log = LoggerFactory.getLogger(MqSpan.class);

    public Span start(MessageExt messageExt, String methodName, String topic) {
        Span span = null;
        try {
            span = ThreadLocalSpan.CURRENT_TRACER.next();
            span.kind(Span.Kind.CLIENT).name(messageExt.getTopic() + "consume start");
            span.tag("mq.msgId", topic + ":" + messageExt.getMsgId());
            span.remoteServiceName(methodName);
            span.remoteIpAndPort(String.valueOf(messageExt.getBornHost()), 8080);
            span.start();
        } catch (Exception e) {
            log.error("e:", e);
        }
        return span;
    }

    public Span start(List<MessageExt> messageExtList, String methodName, String topic) {
        Span span = null;
        try {
            span = ThreadLocalSpan.CURRENT_TRACER.next();
            MessageExt messageExt = messageExtList.get(0);
            span.kind(Span.Kind.CLIENT).name(messageExt.getTopic() + "consume list start");
            span.tag("mq.msgId", topic + ":" + messageExt.getMsgId());
            span.remoteServiceName(methodName);
            span.remoteIpAndPort(String.valueOf(messageExt.getBornHost()), 8080);
            span.start();
        } catch (Exception e) {
            log.error("e:", e);
        }
        return span;
    }

    public void end(Span span) {
        try {
            Span spanEnd = ThreadLocalSpan.CURRENT_TRACER.remove();
            span.finish();
        } catch (Exception e) {
            log.error("e:", e);
        }
    }

    public void error(Span span, Throwable e) {
        try {
            Span spanEnd = ThreadLocalSpan.CURRENT_TRACER.remove();
            span.error(e);
            span.tag("consume error", "consume error");
            span.finish();
        } catch (Exception ee) {
            log.error("e:", ee);
        }
    }
}